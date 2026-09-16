package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Connection.class, remap = false)
public abstract class ConnectionMixin {

    @Shadow public BlockPos start;
    @Shadow public BlockPos end;
    @Shadow public WireType cableType;

    /**
     * @author tgstyle
     * @reason The same value as Objects.hash without the varargs array it allocates on every lookup.
     */
    @Overwrite
    public int hashCode() {
        int result = 31+(start==null?0: start.hashCode());
        result = 31*result+(end==null?0: end.hashCode());
        return 31*result+(cableType==null?0: cableType.hashCode());
    }
}
