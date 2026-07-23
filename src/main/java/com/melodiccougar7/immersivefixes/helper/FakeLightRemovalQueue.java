package com.melodiccougar7.immersivefixes.helper;

import blusunrize.immersiveengineering.common.blocks.BlockFakeLight.TileEntityFakeLight;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod.EventBusSubscriber public class FakeLightRemovalQueue {

    private static final int MAX_PER_TICK = 16;
    private static final Deque<Entry> QUEUE = new ArrayDeque<>();

    public static void enqueueAll(World world, Iterable<BlockPos> positions) {
        synchronized(QUEUE) { for(BlockPos pos : positions) { QUEUE.add(new Entry(world, pos)); } }
    }

    @SubscribeEvent public static void onServerTick(ServerTickEvent event) {
        if(event.phase!=Phase.END) { return; }
        int removed = 0;
        while(removed < MAX_PER_TICK) {
            Entry entry;
            synchronized(QUEUE) { entry = QUEUE.poll(); }
            if(entry==null) { break; }
            if(entry.world.getTileEntity(entry.pos) instanceof TileEntityFakeLight) { entry.world.setBlockToAir(entry.pos); }
            removed++;
        }
    }

    @SubscribeEvent public static void onWorldUnload(WorldEvent.Unload event) {
        synchronized(QUEUE) { QUEUE.removeIf(entry -> entry.world==event.getWorld()); }
    }

    private static final class Entry {
        private final World world;
        private final BlockPos pos;
        private Entry(World world, BlockPos pos) { this.world = world; this.pos = pos; }
    }
}
