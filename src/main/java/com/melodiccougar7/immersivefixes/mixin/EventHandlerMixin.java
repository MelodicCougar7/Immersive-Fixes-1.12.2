package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.energy.wires.IICProxy;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IEntityProof;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRazorWire;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.Utils;
import com.melodiccougar7.immersivefixes.helper.ConnectionRevalidation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Pabilo8 (pabilo@iiteam.net)
 * @since 23.07.2026
 */
@Mixin(value = EventHandler.class, remap = false)
public class EventHandlerMixin
{
	/**
	 * @author Pabilo8
	 * @reason Razor wire accepts any wire cutter through Utils.isWirecutter, not only IE's own tool item
	 */
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

	@Inject(method = "onWorldTick(Lnet/minecraftforge/fml/common/gameevent/TickEvent$WorldTickEvent;)V", at = @At("HEAD"), require = 1)
	private void validateWithoutLoadingChunks(WorldTickEvent event, CallbackInfo ci)
	{
		if(event.phase!=TickEvent.Phase.START||!EventHandler.validateConnsNextTick||FMLCommonHandler.instance().getEffectiveSide()!=Side.SERVER)
			return;
		EventHandler.validateConnsNextTick = false;
		if(!IEConfig.validateConnections)
			return;
		ConnectionRevalidation.reset();
		ImmersiveNetHandler handler = ImmersiveNetHandler.INSTANCE;
		int invalidConnectionsDropped = 0;
		for(int dim : handler.getRelevantDimensions())
		{
			World world = DimensionManager.getWorld(dim);
			if(world==null)
			{
				for(BlockPos node : handler.directConnections.get(dim).keySet())
					ConnectionRevalidation.defer(dim, node);
				continue;
			}
			for(Connection con : handler.getAllConnections(world))
			{
				if(ConnectionRevalidation.endInvalid(world, con.start)||ConnectionRevalidation.endInvalid(world, con.end))
				{
					handler.removeConnection(world, con);
					invalidConnectionsDropped++;
				}
			}
			IELogger.info("removed "+invalidConnectionsDropped+" invalid connections from world");
		}
		int invalidProxies = 0;
		List<DimensionBlockPos> toRemove = new ArrayList<>();
		for(Map.Entry<DimensionBlockPos, IICProxy> e : handler.proxies.entrySet())
		{
			DimensionBlockPos p = e.getKey();
			World w = DimensionManager.getWorld(p.dimension);
			if(w==null||!w.isBlockLoaded(p))
				ConnectionRevalidation.defer(p.dimension, new BlockPos(p));
			else if(!(w.getTileEntity(p) instanceof IImmersiveConnectable))
			{
				invalidProxies++;
				toRemove.add(p);
			}
		}
		for(DimensionBlockPos p : toRemove)
			handler.setProxy(p, null);
		if(invalidProxies > 0)
			IELogger.info("Removed "+invalidProxies+" invalid connector proxies (used to transfer power through unloaded chunks)");
	}
}
