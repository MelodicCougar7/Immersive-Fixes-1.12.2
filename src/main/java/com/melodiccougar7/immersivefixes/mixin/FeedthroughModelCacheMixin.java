package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.smart.FeedthroughModel;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough;
import com.google.common.cache.Cache;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = FeedthroughModel.class, remap = false)
public abstract class FeedthroughModelCacheMixin {

    @Redirect(method = "getQuads(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/EnumFacing;J)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lcom/google/common/cache/Cache;get(Ljava/lang/Object;Ljava/util/concurrent/Callable;)Ljava/lang/Object;", remap = false), remap = true, require = 1)
    private Object keyOnTint(Cache<Object, Object> cache, Object key, Callable<?> loader, IBlockState state, EnumFacing side, long rand) throws ExecutionException {
        int tint = -1;
        if(state instanceof IExtendedBlockState) {
            TileEntity te = ((IExtendedBlockState)state).getValue(IEProperties.TILEENTITY_PASSTHROUGH);
            if(te instanceof TileEntityFeedthrough&&((TileEntityFeedthrough)te).offset==0) {
                tint = Minecraft.getMinecraft().getBlockColors().colorMultiplier(((TileEntityFeedthrough)te).stateForMiddle, te.getWorld(), te.getPos(), 0);
            }
        }
        return cache.get(Pair.of(key, tint), loader);
    }
}
