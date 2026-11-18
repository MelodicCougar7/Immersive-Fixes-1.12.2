package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ConnectorOutputMemo;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.common.blocks.metal.BlockConnector;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockConnector.class, remap = false)
public abstract class BlockConnectorMixin {

    @Inject(method = "neighborChanged(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V", at = @At("TAIL"), remap = true, require = 1)
    private void refreshRoutesOnOutputChange(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, CallbackInfo ci) {
        if(world.isRemote) { return; }
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof ConnectorOutputMemo&&((ConnectorOutputMemo)te).immersivefixes$outputChanged()) {
            ImmersiveNetHandler.INSTANCE.resetCachedIndirectConnections(world, pos);
        }
    }
}
