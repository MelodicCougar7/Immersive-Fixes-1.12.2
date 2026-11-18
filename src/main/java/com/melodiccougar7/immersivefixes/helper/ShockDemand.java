package com.melodiccougar7.immersivefixes.helper;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;

@Mod.EventBusSubscriber public final class ShockDemand {

    private static final Map<Integer, Demand> DEMANDS = new HashMap<>();

    private ShockDemand() {}

    public static void note(World world, Set<Triple<Connection, Vec3d, Vec3d>> wires) {
        Demand demand = null;
        for(Triple<Connection, Vec3d, Vec3d> wire : wires) {
            Connection con = wire.getLeft();
            if(!con.cableType.canCauseDamage()) { continue; }
            if(demand==null) {
                demand = DEMANDS.computeIfAbsent(world.provider.getDimension(), dimension -> new Demand());
                demand.last = world.getTotalWorldTime();
            }
            demand.nodes.put(con.start, demand.last);
            demand.nodes.put(con.end, demand.last);
        }
    }

    public static boolean idle(World world) {
        Demand demand = DEMANDS.get(world.provider.getDimension());
        return demand==null||world.getTotalWorldTime()-demand.last > 1;
    }

    public static boolean unwatched(World world, BlockPos node) {
        Demand demand = DEMANDS.get(world.provider.getDimension());
        Long last = demand==null?null: demand.nodes.get(node);
        return last==null||world.getTotalWorldTime()-last > 1;
    }

    @SubscribeEvent public static void onWorldUnload(WorldEvent.Unload event) {
        if(event.getWorld().isRemote) { return; }
        DEMANDS.remove(event.getWorld().provider.getDimension());
    }

    private static final class Demand {
        private final Map<BlockPos, Long> nodes = new HashMap<>();
        private long last;
    }
}
