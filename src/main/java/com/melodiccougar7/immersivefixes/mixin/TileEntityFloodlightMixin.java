package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.FakeLightRemovalQueue;

import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFloodlight;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityFloodlight.class, remap = false)
public abstract class TileEntityFloodlightMixin extends TileEntity implements ISpawnInterdiction {

    @Shadow public List<BlockPos> fakeLights;

    @Inject(method = "invalidate()V", at = @At("HEAD"), require = 1)
    private void clearFakeLightsOnInvalidate(CallbackInfo ci) {
        if(world==null||world.isRemote) { return; }
        FakeLightRemovalQueue.enqueueAll(world, fakeLights);
        fakeLights.clear();
    }

    @ModifyConstant(method = "readCustomNBT(Lnet/minecraft/nbt/NBTTagCompound;Z)V", constant = @Constant(stringValue = "energy"), require = 1)
    private String fixEnergyStorageNbtKey(String key) { return "energyStorage"; }

    @Redirect(method = "hammerUseSide(Lnet/minecraft/util/EnumFacing;Lnet/minecraft/entity/player/EntityPlayer;FFF)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/util/EnumFacing;UP:Lnet/minecraft/util/EnumFacing;", ordinal = 1, opcode = Opcodes.GETSTATIC, remap = true), require = 1)
    private EnumFacing fixHammerUseSideSouthCheck() { return EnumFacing.SOUTH; }

    @Inject(method = "<init>()V", at = @At("TAIL"), require = 1)
    private void registerSpawnInterdiction(CallbackInfo ci) {
        if(IEConfig.Machines.floodlight_spawnPrevent) { synchronized(EventHandler.interdictionTiles) { if(!EventHandler.interdictionTiles.contains(this)) { EventHandler.interdictionTiles.add(this); } } }
    }
}
