package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author Pabilo8
 * @since 24.07.2026
 */
@Mixin(value = BlockMetalDecoration2.class, remap = false)
public abstract class BlockMetalDecoration2Mixin implements IIEMetaBlock
{
	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	//null means default mapping, so no @Nullable despite IE's nonnull-by-default package
	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		return meta==BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta()?"razor_wire": null;
	}
}
