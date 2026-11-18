package com.melodiccougar7.immersivefixes.helper;

import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod.EventBusSubscriber public final class ConnectionRevalidation {

    private static final Map<Integer, Map<Long, Set<BlockPos>>> PENDING = new HashMap<>();
    private static final Map<Integer, Set<BlockPos>> DUE = new HashMap<>();

    private ConnectionRevalidation() { }

    public static void reset() {
        PENDING.clear();
        DUE.clear();
    }

    public static boolean endInvalid(World world, BlockPos end) {
        if(world.isBlockLoaded(end)) { return !(world.getTileEntity(end) instanceof IImmersiveConnectable); }
        defer(world.provider.getDimension(), end);
        return false;
    }

    public static void defer(int dim, BlockPos pos) {
        PENDING.computeIfAbsent(dim, d -> new HashMap<>()).computeIfAbsent(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4), c -> new HashSet<>()).add(pos);
    }

    @SubscribeEvent public static void onChunkLoad(ChunkEvent.Load event) {
        if(!IEConfig.validateConnections||event.getWorld().isRemote) { return; }
        int dim = event.getWorld().provider.getDimension();
        Map<Long, Set<BlockPos>> chunks = PENDING.get(dim);
        if(chunks==null) { return; }
        Set<BlockPos> ends = chunks.remove(ChunkPos.asLong(event.getChunk().x, event.getChunk().z));
        if(ends!=null) { DUE.computeIfAbsent(dim, d -> new HashSet<>()).addAll(ends); }
    }

    @SubscribeEvent public static void onServerTick(ServerTickEvent event) {
        if(!IEConfig.validateConnections||event.phase!=Phase.END||DUE.isEmpty()) { return; }
        Map<Integer, Set<BlockPos>> due = new HashMap<>(DUE);
        DUE.clear();
        ImmersiveNetHandler handler = ImmersiveNetHandler.INSTANCE;
        int connections = 0;
        int proxies = 0;
        for(Map.Entry<Integer, Set<BlockPos>> entry : due.entrySet()) {
            int dim = entry.getKey();
            World world = DimensionManager.getWorld(dim);
            for(BlockPos pos : entry.getValue()) {
                if(world==null||!world.isBlockLoaded(pos)) { defer(dim, pos); continue; }
                if(world.getTileEntity(pos) instanceof IImmersiveConnectable) { continue; }
                Set<Connection> conns = handler.getConnections(world, pos);
                if(conns!=null) {
                    for(Connection con : new ArrayList<>(conns)) {
                        handler.removeConnection(world, con);
                        connections++;
                    }
                }
                DimensionBlockPos proxy = new DimensionBlockPos(pos, dim);
                if(handler.proxies.containsKey(proxy)) {
                    handler.setProxy(proxy, null);
                    proxies++;
                }
            }
        }
        if(connections > 0||proxies > 0) { IELogger.info("Removed "+connections+" invalid connections and "+proxies+" invalid connector proxies in newly loaded chunks"); }
    }

    @SubscribeEvent public static void onWorldUnload(WorldEvent.Unload event) {
        if(IEConfig.validateConnections&&!event.getWorld().isRemote&&event.getWorld().provider.getDimension()==0) { reset(); }
    }
}
