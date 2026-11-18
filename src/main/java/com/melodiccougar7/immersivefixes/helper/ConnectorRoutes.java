package com.melodiccougar7.immersivefixes.helper;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ConnectorRoutes {

    List<AbstractConnection> immersivefixes$outputsByPriority(BlockPos node, World world);

    List<AbstractConnection> immersivefixes$shockTargets(BlockPos node, World world);
}
