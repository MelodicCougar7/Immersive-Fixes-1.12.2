package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import javax.annotation.Nonnull;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityConnectorLV.class, remap = false)
public abstract class TileEntityConnectorLVClientMixin extends TileEntityImmersiveConnectable {

    @Shadow private AxisAlignedBB renderAABB;

    @Unique private BlockPos immersivefixes$renderAABBPos;

    @Shadow abstract int getRenderRadiusIncrease();

    /**
     * @author tgstyle
     * @reason Builds the render box once per position instead of a new one on every call.
     */
    @Overwrite
    @Nonnull
    public AxisAlignedBB getRenderBoundingBox() {
        if(renderAABB==null||!pos.equals(immersivefixes$renderAABBPos)) {
            int inc = getRenderRadiusIncrease();
            renderAABB = new AxisAlignedBB(pos.getX()-inc, pos.getY()-inc, pos.getZ()-inc, pos.getX()+inc+1, pos.getY()+inc+1, pos.getZ()+inc+1);
            immersivefixes$renderAABBPos = pos;
        }
        return renderAABB;
    }
}
