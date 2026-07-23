package com.melodiccougar7.immersivefixes.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(value = blusunrize.immersiveengineering.common.blocks.BlockFakeLight.class, remap = false)
public abstract class BlockFakeLight extends Block {

    protected BlockFakeLight() { super(Material.AIR); }

    @Override public boolean isReplaceable(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) { return true; }
}
