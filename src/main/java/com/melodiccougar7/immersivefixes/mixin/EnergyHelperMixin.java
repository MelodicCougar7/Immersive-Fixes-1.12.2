package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.ImmersiveFixes;

import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EnergyHelper.class, remap = false)
public abstract class EnergyHelperMixin {

    @Unique private static final Set<String> immersivefixes$reportedReceivers = ConcurrentHashMap.newKeySet();

    @Redirect(method = "insertFlux(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;IZ)I", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/immersiveflux/IFluxReceiver;receiveEnergy(Lnet/minecraft/util/EnumFacing;IZ)I"), require = 1)
    @SuppressWarnings({"ConstantValue", "ConstantConditions"}) private static int guardNullFluxStorage(IFluxReceiver receiver, EnumFacing facing, int energy, boolean simulate) {
        if(receiver instanceof IIEInternalFluxHandler&&((IIEInternalFluxHandler)receiver).getFluxStorage()==null) {
            if(immersivefixes$reportedReceivers.add(receiver.getClass().getName())) { ImmersiveFixes.LOGGER.warn("{} at {} returned a null flux storage; skipping energy insertion instead of crashing", receiver.getClass().getName(), ((TileEntity)receiver).getPos()); }
            return 0;
        }
        return receiver.receiveEnergy(facing, energy, simulate);
    }
}
