package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.RedstoneNetState;

import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorProbe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorRedstone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityConnectorRedstone.class, remap = false)
public abstract class TileEntityConnectorRedstoneMixin extends TileEntityImmersiveConnectable {

    @Shadow public int redstoneChannel;

    @Shadow public EnumFacing facing;

    @Shadow public boolean rsDirty;

    @Shadow protected RedstoneWireNetwork wireNetwork;

    @Unique private int immersivefixes$lastEmitted = Integer.MIN_VALUE;

    @Unique private int immersivefixes$lastLocal = Integer.MIN_VALUE;

    @Shadow public abstract boolean isRSOutput();

    @Shadow protected abstract int getLocalRS();

    @Inject(method = "readCustomNBT(Lnet/minecraft/nbt/NBTTagCompound;Z)V", at = @At("TAIL"), require = 1)
    private void maskChannel(NBTTagCompound nbt, boolean descPacket, CallbackInfo ci) {
        redstoneChannel &= 15;
    }

    @ModifyArg(method = "onChange()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityConnectorRedstone;markBlockForUpdate(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)V"), index = 1, require = 1)
    private IBlockState attachedBlockState(IBlockState stateHere) {
        return world.getBlockState(pos.offset(facing));
    }

    @Redirect(method = "update()V", at = @At(value = "FIELD", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityConnectorRedstone;refreshWireNetwork:Z", opcode = Opcodes.GETFIELD, remap = false), remap = true, require = 1)
    private boolean rebuildOnlyStaleNet(TileEntityConnectorRedstone self) {
        return ((RedstoneNetState)wireNetwork).immersivefixes$built();
    }

    @Redirect(method = "update()V", at = @At(value = "FIELD", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityConnectorRedstone;rsDirty:Z", opcode = Opcodes.GETFIELD, remap = false), remap = true, require = 1)
    private boolean recomputeOnlyChangedInput(TileEntityConnectorRedstone self) {
        if(!rsDirty) { return false; }
        if(self instanceof TileEntityConnectorProbe) { return true; }
        if(getLocalRS()!=immersivefixes$lastLocal) { return true; }
        rsDirty = false;
        return false;
    }

    @Redirect(method = "updateInput([B)V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/common/blocks/metal/TileEntityConnectorRedstone;getLocalRS()I"), require = 1)
    private int recordLocal(TileEntityConnectorRedstone self) {
        immersivefixes$lastLocal = getLocalRS();
        return immersivefixes$lastLocal;
    }

    @Inject(method = "onChange()V", at = @At("HEAD"), cancellable = true, require = 1)
    private void skipUnchangedOutput(CallbackInfo ci) {
        int emitted = isRSOutput()?wireNetwork.getPowerOutput(redstoneChannel): -1;
        if(emitted==immersivefixes$lastEmitted) { ci.cancel(); }
        immersivefixes$lastEmitted = emitted;
    }

    @Redirect(method = "removeCable(Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;)V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/wires/redstone/RedstoneWireNetwork;removeFromNetwork(Lblusunrize/immersiveengineering/api/energy/wires/redstone/IRedstoneConnector;)V"), require = 1)
    private void deferSplit(RedstoneWireNetwork net, IRedstoneConnector removedConnector) {
        ((RedstoneNetState)net).immersivefixes$markStale();
    }

    @Inject(method = "onConnectivityUpdate(Lnet/minecraft/util/math/BlockPos;I)V", at = @At("TAIL"), require = 1)
    private void markNetStale(CallbackInfo ci) {
        ((RedstoneNetState)wireNetwork).immersivefixes$markStale();
    }

    @Inject(method = "update()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/wires/redstone/RedstoneWireNetwork;removeFromNetwork(Lblusunrize/immersiveengineering/api/energy/wires/redstone/IRedstoneConnector;)V", shift = At.Shift.AFTER, remap = false), remap = true, require = 1)
    private void buildOwnNetIfLeftOut(CallbackInfo ci) {
        if(!((RedstoneNetState)wireNetwork).immersivefixes$built()) { RedstoneWireNetwork.updateConnectors(pos, world, wireNetwork); }
    }
}
