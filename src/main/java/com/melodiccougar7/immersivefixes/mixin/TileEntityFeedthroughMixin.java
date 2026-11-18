package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFeedthrough;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityFeedthrough.class, remap = false)
public abstract class TileEntityFeedthroughMixin extends TileEntityImmersiveConnectable {

    @Shadow public BlockPos connPositive;
    @Shadow public boolean hasNegative;

    @Inject(method = "removeCable(Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void recomputeFromRemaining(Connection connection, CallbackInfo ci) {
        if(connection==null) {
            connPositive = null;
            hasNegative = false;
        } else {
            Set<Connection> remaining = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
            boolean positiveKept = false;
            boolean negativeKept = false;
            if(remaining!=null) {
                for(Connection c : remaining) {
                    BlockPos other = pos.equals(c.start)?c.end: c.start;
                    if(other.equals(connPositive)) { positiveKept = true; }
                    else { negativeKept = true; }
                }
            }
            if(!positiveKept) { connPositive = null; }
            hasNegative = negativeKept;
        }
        if(world!=null) { ChunkDirty.mark(this); }
        ci.cancel();
    }

    @Inject(method = "readCustomNBT(Lnet/minecraft/nbt/NBTTagCompound;Z)V", at = @At("TAIL"), require = 1)
    private void clearStalePositive(NBTTagCompound nbt, boolean descPacket, CallbackInfo ci) {
        if(!nbt.hasKey("posConnX")) { connPositive = null; }
    }
}
