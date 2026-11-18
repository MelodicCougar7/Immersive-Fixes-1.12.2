package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.util.Utils;
import java.util.Objects;
import java.util.Set;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Utils.class, remap = false)
public abstract class UtilsMixin {

    @Unique
    private static boolean immersivefixes$catenaryDiffers(Connection inA, Connection inB) {
        return !(Math.abs(inA.catA-inB.catA) < 1e-5&&Math.abs(inA.catOffsetX-inB.catOffsetX) < 1e-5&&Math.abs(inA.catOffsetY-inB.catOffsetY) < 1e-5);
    }

    /**
     * @author tgstyle
     * @reason Compares the two wire sets without building a HashMap of one of them on every model cache key compare.
     */
    @Overwrite
    private static boolean areRenderConnectionsEqual(Set<Connection> setA, Set<Connection> setB) {
        if(setA==setB) { return true; }
        if(setA==null||setB==null) { return false; }
        for(Connection inB : setB) {
            int hash = Objects.hashCode(inB);
            boolean matched = false;
            for(Connection c : setA) {
                if(Objects.equals(inB, c)&&Objects.hashCode(c)==hash) {
                    if(immersivefixes$catenaryDiffers(c, inB)) { return false; }
                    matched = true;
                    break;
                }
            }
            if(!matched) { return false; }
        }
        return setA.size()==setB.size();
    }
}
