package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRazorWire;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 23.07.2026
 */
@Mixin(value = EventHandler.class, remap = false)
public class EventHandlerMixin
{
	@Overwrite
	@SubscribeEvent
	public void digSpeedEvent(BreakSpeed event)
	{
		ItemStack current = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);

		//Stop the combustion drill from working underwater
		if(!current.isEmpty()&&current.getItem().equals(IEContent.itemDrill)&&current.getItemDamage()==0&&event.getEntityPlayer().isInsideOfMaterial(Material.WATER))
			if(((ItemDrill)IEContent.itemDrill).getUpgrades(current).getBoolean("waterproof"))
				event.setNewSpeed(event.getOriginalSpeed()*5);
			else
				event.setCanceled(true);

		//Patch wire cutters behavior on razor wire
		if(event.getState().getBlock()==IEContent.blockMetalDecoration2&&IEContent.blockMetalDecoration2.getMetaFromState(event.getState())==BlockTypes_MetalDecoration2.RAZOR_WIRE.getMeta())
			if(!Utils.isWirecutter(current))
			{
				event.setCanceled(true);
				TileEntityRazorWire.applyDamage(event.getEntityLiving());
			}

		//Do not allow breaking of entity-proof blocks
		TileEntity te = event.getEntityPlayer().getEntityWorld().getTileEntity(event.getPos());
		if(te instanceof IEntityProof&&!((IEntityProof)te).canEntityDestroy(event.getEntityPlayer()))
			event.setCanceled(true);

	}
}
