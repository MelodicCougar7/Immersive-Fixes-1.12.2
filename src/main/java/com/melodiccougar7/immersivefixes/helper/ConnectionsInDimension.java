package com.melodiccougar7.immersivefixes.helper;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import com.google.common.collect.Iterators;
import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.math.BlockPos;

public final class ConnectionsInDimension extends AbstractCollection<Connection> {

    private final Map<BlockPos, Set<Connection>> byNode;

    public ConnectionsInDimension(Map<BlockPos, Set<Connection>> byNode) { this.byNode = byNode; }

    @Override
    public Iterator<Connection> iterator() { return Iterators.concat(Iterators.transform(byNode.values().iterator(), Set::iterator)); }

    @Override
    public int size() {
        int size = 0;
        for(Set<Connection> conns : byNode.values()) { size += conns.size(); }
        return size;
    }
}
