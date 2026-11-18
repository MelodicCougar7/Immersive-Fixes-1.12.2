package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBreakerSwitch;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityBreakerSwitch.class, remap = false)
public abstract class TileEntityBreakerSwitchMixin extends TileEntityImmersiveConnectable {

    @Shadow public int wires;

    @Shadow protected abstract void onConnectionChange();

    @Inject(method = "removeCable(Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void countRemainingWires(Connection connection, CallbackInfo ci) {
        if(connection==null||connection.cableType==null) { wires = 0; }
        else {
            Set<Connection> remaining = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
            wires = remaining==null?0: remaining.size();
        }
        if(wires==0) { limitType = null; }
        onConnectionChange();
        if(world!=null) { ChunkDirty.mark(this); }
        ci.cancel();
    }
}
