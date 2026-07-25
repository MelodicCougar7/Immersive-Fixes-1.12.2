package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

/**
 * @author Pabilo8
 * @since 24.07.2026
 */
@Mixin(value = BlockMetalDecoration2.class, remap = false)
public abstract class BlockMetalDecoration2Mixin
{
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Nullable
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		return meta==BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta()?"razor_wire": null;
	}
}
