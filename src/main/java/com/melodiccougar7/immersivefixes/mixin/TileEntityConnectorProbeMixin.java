package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorProbe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorRedstone;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityConnectorProbe.class, remap = false)
public abstract class TileEntityConnectorProbeMixin extends TileEntityConnectorRedstone {

    @Shadow private int redstoneChannelSending;

    @Shadow private int lastOutput;

    @Shadow private int getComparatorSignal() { throw new AssertionError(); }

    @Inject(method = "readCustomNBT(Lnet/minecraft/nbt/NBTTagCompound;Z)V", at = @At("TAIL"), require = 1)
    private void maskSendingChannel(NBTTagCompound nbt, boolean descPacket, CallbackInfo ci) {
        redstoneChannelSending &= 15;
    }

    @Inject(method = "update()V", at = @At("HEAD"), remap = true, require = 1)
    private void pollOncePerEightTicks(CallbackInfo ci) {
        if(world.isRemote||((world.getTotalWorldTime()+(pos.getX()^pos.getZ()))&7)!=0) { return; }
        int out = getComparatorSignal();
        if(out!=lastOutput) {
            lastOutput = out;
            rsDirty = true;
        }
    }

    @Redirect(method = "update()V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isRemote:Z", opcode = Opcodes.GETFIELD), remap = true, require = 1)
    private boolean skipStockPoll(World world) {
        return true;
    }
}
