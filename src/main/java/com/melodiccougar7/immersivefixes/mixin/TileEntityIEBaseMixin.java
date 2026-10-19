package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;
import com.melodiccougar7.immersivefixes.helper.ChunkDirty.IChunkDirtyTile;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = TileEntityIEBase.class, remap = false)
public abstract class TileEntityIEBaseMixin extends TileEntity implements IChunkDirtyTile {

    @Unique private long immersivefixes$dirtyTick = -1;

    @Override public long immersivefixes$getDirtyTick() { return immersivefixes$dirtyTick; }

    @Override public void immersivefixes$setDirtyTick(long tick) { immersivefixes$dirtyTick = tick; }

    @Override public void postEnergyTransferUpdate(int energy, boolean simulate) {
        if(energy!=0&&!simulate) { ChunkDirty.mark(this); }
    }
}
