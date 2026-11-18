package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ConnectorRoutes;
import com.melodiccougar7.immersivefixes.helper.ShockDemand;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IICProxy;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import com.google.common.collect.MapMaker;
import gnu.trove.map.TIntObjectMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ImmersiveNetHandler.class, remap = false)
public abstract class ImmersiveNetHandlerMixin implements ConnectorRoutes {

    @Shadow public TIntObjectMap<Map<BlockPos, Set<AbstractConnection>>> indirectConnections;
    @Shadow public TIntObjectMap<Map<BlockPos, Set<AbstractConnection>>> indirectConnectionsIgnoreOut;
    @Shadow public Map<Integer, ConcurrentHashMap<BlockPos, Set<Connection>>> directConnections;
    @Shadow public Map<Integer, HashMap<Connection, Integer>> transferPerTick;
    @Shadow public abstract Set<Connection> getConnections(World world, BlockPos node);
    @Shadow public abstract void resetCachedIndirectConnections(World w, BlockPos start);
    @Shadow public abstract void addConnection(int world, BlockPos node, Connection con);
    @Shadow public IntHashMap<Map<BlockPos, ImmersiveNetHandler.BlockWireInfo>> blockWireMap;
    @Shadow public Map<DimensionBlockPos, IICProxy> proxies;

    @Unique private static final Map<Integer, Set<BlockPos>> immersivefixes$alreadyReset = new ConcurrentHashMap<>();
    @Unique private static final Map<Integer, Map<BlockPos, Set<BlockPos>>> immersivefixes$wireDataAdded = new ConcurrentHashMap<>();
    @Unique private static final Map<Integer, Map<BlockPos, Map<BlockPos, Set<BlockPos>>>> immersivefixes$wirePositions = new ConcurrentHashMap<>();
    @Unique private static final Map<Set<AbstractConnection>, List<AbstractConnection>> immersivefixes$outputLists = new MapMaker().weakKeys().makeMap();
    @Unique private static final Map<Set<AbstractConnection>, List<AbstractConnection>> immersivefixes$shockLists = new MapMaker().weakKeys().makeMap();

    /**
     * @author tgstyle
     * @reason The same search without the original's quadratic list, queue and backtracking scans.
     */
    @Overwrite
    public Set<AbstractConnection> getIndirectEnergyConnections(BlockPos node, World world, boolean ignoreIsEnergyOutput) {
        int dimension = world.provider.getDimension();
        TIntObjectMap<Map<BlockPos, Set<AbstractConnection>>> store = ignoreIsEnergyOutput?indirectConnectionsIgnoreOut: indirectConnections;
        Map<BlockPos, Set<AbstractConnection>> cached = store.get(dimension);
        if(cached!=null) {
            Set<AbstractConnection> hit = cached.get(node);
            if(hit!=null) { return hit; }
        }

        Set<AbstractConnection> closedList = Collections.newSetFromMap(new ConcurrentHashMap<>());
        List<AbstractConnection> outputs = immersivefixes$search(node, world, ignoreIsEnergyOutput, closedList);

        if(FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER) {
            Map<BlockPos, Set<AbstractConnection>> conns = store.get(dimension);
            if(conns==null) {
                conns = new ConcurrentHashMap<>();
                store.put(dimension, conns);
            }
            Set<AbstractConnection> existing = conns.putIfAbsent(node, closedList);
            if(existing!=null) {
                existing.addAll(closedList);
                immersivefixes$outputLists.remove(existing);
                immersivefixes$shockLists.remove(existing);
            }
            else if(ignoreIsEnergyOutput) { immersivefixes$outputLists.put(closedList, outputs); }
            if(!immersivefixes$alreadyReset.isEmpty()) { immersivefixes$alreadyReset.clear(); }
        }
        return closedList;
    }

    @Override
    public List<AbstractConnection> immersivefixes$outputsByPriority(BlockPos node, World world) {
        Set<AbstractConnection> routes = getIndirectEnergyConnections(node, world, true);
        List<AbstractConnection> outputs = immersivefixes$outputLists.get(routes);
        if(outputs==null) {
            outputs = immersivefixes$search(node, world, true, Collections.newSetFromMap(new ConcurrentHashMap<>()));
            immersivefixes$outputLists.put(routes, outputs);
        }
        return outputs;
    }

    @Override
    public List<AbstractConnection> immersivefixes$shockTargets(BlockPos node, World world) {
        Set<AbstractConnection> routes = getIndirectEnergyConnections(node, world, true);
        List<AbstractConnection> targets = immersivefixes$shockLists.get(routes);
        if(targets==null) {
            List<AbstractConnection> found = new ArrayList<>();
            for(AbstractConnection con : routes) {
                if(con.cableType!=null&&immersivefixes$shockingWireAt(world, con.end)) { found.add(con); }
            }
            targets = found.isEmpty()?Collections.emptyList(): Collections.unmodifiableList(found);
            immersivefixes$shockLists.put(routes, targets);
        }
        return targets;
    }

    @Unique private boolean immersivefixes$shockingWireAt(World world, BlockPos node) {
        Set<Connection> wires = getConnections(world, node);
        if(wires==null) { return false; }
        for(Connection wire : wires) {
            if(wire.cableType!=null&&wire.cableType.canCauseDamage()) { return true; }
        }
        return false;
    }

    @Unique private List<AbstractConnection> immersivefixes$search(BlockPos node, World world, boolean ignoreIsEnergyOutput, Set<AbstractConnection> closedList) {
        PriorityQueue<Pair<IImmersiveConnectable, Float>> queue = new PriorityQueue<>(Comparator.comparingDouble(Pair::getRight));
        Map<IImmersiveConnectable, Pair<IImmersiveConnectable, Float>> queued = new IdentityHashMap<>();
        Set<BlockPos> checked = new HashSet<>();
        Map<BlockPos, Connection> backtracker = new HashMap<>();

        checked.add(node);
        Set<Connection> conL = getConnections(world, node);
        if(conL!=null) {
            for(Connection con : conL) {
                IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
                if(end!=null) {
                    immersivefixes$offer(queue, queued, end, con.getBaseLoss());
                    backtracker.put(con.end, con);
                }
            }
        }

        final int closedListMax = 1200;
        int outputsFound = 0;
        Set<AbstractConnection> outputs = new TreeSet<>();
        List<Connection> connectionParts = new ArrayList<>();
        while(outputsFound < closedListMax&&!queue.isEmpty()) {
            Pair<IImmersiveConnectable, Float> pair = queue.poll();
            IImmersiveConnectable next = pair.getLeft();
            if(queued.get(next)==pair) { queued.remove(next); }
            float loss = pair.getRight();
            BlockPos nextPos = ApiUtils.toBlockPos(next);
            if(checked.contains(nextPos)) { continue; }
            boolean isOutput = next.isEnergyOutput();
            boolean listed = (ignoreIsEnergyOutput||isOutput)&&closedList.size() < closedListMax;
            if(listed||isOutput) {
                WireType minimumType = null;
                int distance = 0;
                connectionParts.clear();
                Connection step = backtracker.get(nextPos);
                while(step!=null) {
                    connectionParts.add(step);
                    distance += step.length;
                    if(minimumType==null||step.cableType.getTransferRate() < minimumType.getTransferRate()) { minimumType = step.cableType; }
                    step = backtracker.get(step.start);
                }
                Collections.reverse(connectionParts);
                AbstractConnection route = new AbstractConnection(node, nextPos, minimumType, distance, isOutput, connectionParts.toArray(new Connection[0]));
                if(listed) { closedList.add(route); }
                if(isOutput) {
                    outputsFound++;
                    if(minimumType!=null) { outputs.add(route); }
                }
            }
            Set<Connection> conLN = getConnections(world, nextPos);
            if(conLN!=null) {
                for(Connection con : conLN) {
                    if(next.allowEnergyToPass(con)) {
                        IImmersiveConnectable end = ApiUtils.toIIC(con.end, world);
                        if(end!=null&&!checked.contains(con.end)&&immersivefixes$offer(queue, queued, end, con.getBaseLoss()+loss)) { backtracker.put(con.end, con); }
                    }
                }
            }
            checked.add(nextPos);
        }
        return Collections.unmodifiableList(new ArrayList<>(outputs));
    }

    /**
     * @author tgstyle
     * @reason One map read instead of three, and no empty dimension map created by a plain lookup.
     */
    @Overwrite @Nullable
    public synchronized Set<Connection> getConnections(int world, BlockPos node) {
        ConcurrentHashMap<BlockPos, Set<Connection>> map = directConnections.get(world);
        return map==null?null: map.get(node);
    }

    /**
     * @author tgstyle
     * @reason One map call instead of three on a path walked twice per wire segment per tick.
     */
    @Overwrite
    public HashMap<Connection, Integer> getTransferedRates(int dimension) { return transferPerTick.computeIfAbsent(dimension, d -> new HashMap<>()); }

    /**
     * @author tgstyle
     * @reason Both directions go in before one connectivity flood, instead of a flood after each.
     */
    @Overwrite
    public Connection addAndGetConnection(World world, BlockPos node, BlockPos connection, int distance, WireType cableType) {
        Connection conn = new Connection(node, connection, cableType, distance);
        int dim = world.provider.getDimension();
        addConnection(dim, node, conn);
        addConnection(dim, connection, new Connection(connection, node, cableType, distance));
        resetCachedIndirectConnections(world, node);
        if(world.isBlockLoaded(node)) { world.addBlockEvent(node, world.getBlockState(node).getBlock(), -1, 0); }
        if(world.isBlockLoaded(connection)) { world.addBlockEvent(connection, world.getBlockState(connection).getBlock(), -1, 0); }
        return conn;
    }

    /**
     * @author tgstyle
     * @reason One connectivity flood for the whole hub, sent while every wire still exists, instead of one per wire.
     */
    @Overwrite
    public void clearAllConnectionsFor(BlockPos node, World world, @Nullable IImmersiveConnectable iic, boolean doDrops) {
        if(iic==null) { iic = ApiUtils.toIIC(node, world); }
        if(iic!=null) { iic.removeCable(null); }
        int dim = world.provider.getDimension();
        Set<Connection> conns = directConnections.computeIfAbsent(dim, d -> new ConcurrentHashMap<>()).get(node);
        if(conns!=null) {
            if(!conns.isEmpty()) { resetCachedIndirectConnections(world, node); }
            for(Connection con : conns) {
                immersivefixes$detach(world, con, iic!=null?iic.getConnectionOffset(con): Vec3d.ZERO, ApiUtils.getVecForIICAt(world, con.end, con));
                double dx = node.getX()+.5+Math.signum(con.end.getX()-con.start.getX());
                double dy = node.getY()+.5+Math.signum(con.end.getY()-con.start.getY());
                double dz = node.getZ()+.5+Math.signum(con.end.getZ()-con.start.getZ());
                if(doDrops&&world.getGameRules().getBoolean("doTileDrops")) { world.spawnEntity(new EntityItem(world, dx, dy, dz, con.cableType.getWireCoil(con))); }
            }
        }
        IESaveData.setDirty(dim);
    }

    @Redirect(method = "clearAllConnectionsFor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/api/TargetingInfo;)Z", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler;removeConnection(Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;)V"), require = 1)
    private void removeWithoutFlood(ImmersiveNetHandler handler, World world, Connection con) {
        immersivefixes$detach(world, con, ApiUtils.getVecForIICAt(world, con.start, con), ApiUtils.getVecForIICAt(world, con.end, con));
    }

    /**
     * @author tgstyle
     * @reason Looks up the block's wire record before the entity checks, so the common no-wire case reads nothing else.
     */
    @Overwrite
    public static void handleEntityCollision(BlockPos p, Entity e) {
        if(e.world.isRemote||!IEConfig.enableWireDamage||!(e instanceof EntityLivingBase)) { return; }
        Map<BlockPos, ImmersiveNetHandler.BlockWireInfo> mapForDim = ImmersiveNetHandler.INSTANCE.blockWireMap.lookup(e.dimension);
        ImmersiveNetHandler.BlockWireInfo info = mapForDim==null?null: mapForDim.get(p);
        if(info==null||e.isEntityInvulnerable(IEDamageSources.wireShock)) { return; }
        if(e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.disableDamage) { return; }
        ShockDemand.note(e.world, info.in);
        ShockDemand.note(e.world, info.near);
        handleMapForDamage(info.in, (EntityLivingBase)e, p);
        handleMapForDamage(info.near, (EntityLivingBase)e, p);
    }

    @Shadow private static void handleMapForDamage(Set<Triple<Connection, Vec3d, Vec3d>> in, EntityLivingBase e, BlockPos here) { }

    @Redirect(method = "onTEValidated", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler;resetCachedIndirectConnections(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"), require = 1)
    private void coalesceValidationResets(ImmersiveNetHandler handler, World w, BlockPos start) {
        if(w.isRemote||FMLCommonHandler.instance().getEffectiveSide()!=Side.SERVER) { handler.resetCachedIndirectConnections(w, start); }
        else if(!immersivefixes$routesUnchanged(w, start)) { immersivefixes$resetOnce(w, start); }
    }

    @Unique private boolean immersivefixes$routesUnchanged(World world, BlockPos pos) {
        IICProxy proxy = proxies.get(new DimensionBlockPos(pos, world));
        if(proxy==null) { return false; }
        TileEntity te = world.getTileEntity(pos);
        if(!(te instanceof IImmersiveConnectable)) { return false; }
        IImmersiveConnectable iic = (IImmersiveConnectable)te;
        if(iic.isEnergyOutput()!=proxy.isEnergyOutput()) { return false; }
        Set<Connection> conns = getConnections(world, pos);
        if(conns!=null) {
            for(Connection c : conns) {
                if(iic.allowEnergyToPass(c)!=proxy.allowEnergyToPass(c)) { return false; }
            }
        }
        return true;
    }

    @Inject(method = "resetCachedIndirectConnections(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("HEAD"), require = 1)
    private void dropResetRecord(World w, BlockPos start, CallbackInfo ci) {
        if(!w.isRemote) { immersivefixes$alreadyReset.clear(); }
    }

    @Inject(method = "clearAllConnections(I)V", at = @At("HEAD"), require = 1)
    private void dropResetRecordOnClear(int world, CallbackInfo ci) {
        immersivefixes$alreadyReset.clear();
        immersivefixes$wireDataAdded.remove(world);
        immersivefixes$wirePositions.remove(world);
        indirectConnections.remove(world);
        indirectConnectionsIgnoreOut.remove(world);
        transferPerTick.remove(world);
    }

    @Inject(method = "clearAllConnectionsFor(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/api/TargetingInfo;)Z", at = @At("HEAD"), cancellable = true, require = 1)
    private void skipMissingNodeOrConnections(BlockPos node, World world, TargetingInfo target, CallbackInfoReturnable<Boolean> cir) {
        if(ApiUtils.toIIC(node, world)==null||getConnections(world, node)==null) { cir.setReturnValue(false); }
    }

    @Inject(method = "addBlockData", at = @At("HEAD"), cancellable = true, require = 1)
    private void skipRepeatedBlockData(World world, Connection con, CallbackInfo ci) {
        if(world.isRemote||!world.isBlockLoaded(con.end)) { return; }
        Map<BlockPos, Set<BlockPos>> traced = immersivefixes$wireDataAdded.computeIfAbsent(world.provider.getDimension(), d -> new ConcurrentHashMap<>());
        BlockPos low = con.start.compareTo(con.end) <= 0?con.start: con.end;
        BlockPos high = low==con.start?con.end: con.start;
        if(!traced.computeIfAbsent(low, p -> ConcurrentHashMap.newKeySet()).add(high)) { ci.cancel(); }
    }

    @Redirect(method = "addBlockData(Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;)V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/ApiUtils;raytraceAlongCatenary(Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;Lnet/minecraft/world/World;Ljava/util/function/Predicate;Ljava/util/function/Consumer;)Z"), require = 1)
    private boolean indexTracedBlocks(Connection conn, World w, Predicate<Triple<BlockPos, Vec3d, Vec3d>> shouldStop, Consumer<Triple<BlockPos, Vec3d, Vec3d>> close) {
        if(w.isRemote) { return ApiUtils.raytraceAlongCatenary(conn, w, shouldStop, close); }
        Set<BlockPos> positions = ConcurrentHashMap.newKeySet();
        boolean stopped = ApiUtils.raytraceAlongCatenary(conn, w, p -> {
            positions.add(p.getLeft());
            return shouldStop.test(p);
        }, p -> {
            positions.add(p.getLeft());
            close.accept(p);
        });
        BlockPos low = conn.start.compareTo(conn.end) <= 0?conn.start: conn.end;
        BlockPos high = low==conn.start?conn.end: conn.start;
        immersivefixes$wirePositions.computeIfAbsent(w.provider.getDimension(), d -> new ConcurrentHashMap<>()).computeIfAbsent(low, p -> new ConcurrentHashMap<>()).put(high, positions);
        return stopped;
    }

    @Inject(method = "removeConnectionAndDrop(Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void skipDropOfGoneWire(Connection conn, World world, @Nullable BlockPos dropPos, CallbackInfo ci) {
        if(conn==null||world==null) {
            ci.cancel();
            return;
        }
        Set<Connection> conns = getConnections(world, conn.start);
        if(conns!=null&&conns.stream().anyMatch(conn::hasSameConnectors)) { return; }
        Map<BlockPos, ImmersiveNetHandler.BlockWireInfo> mapForDim = blockWireMap.lookup(world.provider.getDimension());
        if(mapForDim!=null) {
            for(BlockPos pos : mapForDim.keySet()) { immersivefixes$forgetWireAt(mapForDim, pos, conn); }
        }
        ci.cancel();
    }

    /**
     * @author tgstyle
     * @reason IE drops a block's whole wire record as soon as either of its two sets empties, taking every other wire's entry at that block with it.
     */
    @Overwrite
    public void removeConnection(World world, Connection con, Vec3d vecStart, Vec3d vecEnd) {
        if(con==null||world==null) { return; }
        resetCachedIndirectConnections(world, con.start);
        immersivefixes$detach(world, con, vecStart, vecEnd);
    }

    @Unique private void immersivefixes$detach(World world, Connection con, Vec3d vecStart, Vec3d vecEnd) {
        int dim = world.provider.getDimension();
        Map<BlockPos, Set<Connection>> connsInDim = directConnections.computeIfAbsent(dim, d -> new ConcurrentHashMap<>());
        Set<Connection> reverseConns = connsInDim.get(con.end);
        Set<Connection> forwardConns = connsInDim.get(con.start);
        Optional<Connection> back = reverseConns==null?Optional.empty(): reverseConns.stream().filter(con::hasSameConnectors).findAny();
        if(reverseConns!=null) { reverseConns.removeIf(con::hasSameConnectors); }
        if(forwardConns!=null) { forwardConns.removeIf(con::hasSameConnectors); }
        Map<BlockPos, ImmersiveNetHandler.BlockWireInfo> mapForDim = blockWireMap.lookup(dim);
        Set<BlockPos> indexed = world.isRemote?null: immersivefixes$takeTracedPositions(dim, con);
        if(indexed!=null) {
            for(BlockPos pos : indexed) { immersivefixes$forgetWireAt(mapForDim, pos, con); }
        } else {
            ApiUtils.raytraceAlongCatenaryRelative(con, p -> {
                immersivefixes$forgetWireAt(mapForDim, p.getLeft(), con);
                return false;
            }, p -> immersivefixes$forgetWireAt(mapForDim, p.getLeft(), con), vecStart, vecEnd);
        }
        if(!world.isRemote) { immersivefixes$forgetTracedWire(dim, con); }

        IImmersiveConnectable iic = ApiUtils.toIIC(con.end, world);
        if(iic!=null) {
            iic.removeCable(con);
            back.ifPresent(iic::removeCable);
        }
        iic = ApiUtils.toIIC(con.start, world);
        if(iic!=null) {
            iic.removeCable(con);
            back.ifPresent(iic::removeCable);
        }

        if(world.isBlockLoaded(con.start)) { world.addBlockEvent(con.start, world.getBlockState(con.start).getBlock(), -1, 0); }
        if(world.isBlockLoaded(con.end)) { world.addBlockEvent(con.end, world.getBlockState(con.end).getBlock(), -1, 0); }

        IESaveData.setDirty(dim);
    }

    @Unique private static void immersivefixes$forgetWireAt(Map<BlockPos, ImmersiveNetHandler.BlockWireInfo> mapForDim, BlockPos pos, Connection con) {
        if(mapForDim==null) { return; }
        ImmersiveNetHandler.BlockWireInfo info = mapForDim.get(pos);
        if(info==null) { return; }
        info.in.removeIf(t -> t.getLeft().hasSameConnectors(con));
        info.near.removeIf(t -> t.getLeft().hasSameConnectors(con));
        if(info.in.isEmpty()&&info.near.isEmpty()) { mapForDim.remove(pos); }
    }

    @Unique private static Set<BlockPos> immersivefixes$takeTracedPositions(int dimension, Connection con) {
        Map<BlockPos, Map<BlockPos, Set<BlockPos>>> byLow = immersivefixes$wirePositions.get(dimension);
        if(byLow==null) { return null; }
        BlockPos low = con.start.compareTo(con.end) <= 0?con.start: con.end;
        Map<BlockPos, Set<BlockPos>> peers = byLow.get(low);
        return peers==null?null: peers.remove(low==con.start?con.end: con.start);
    }

    @Unique private static void immersivefixes$forgetTracedWire(int dimension, Connection con) {
        Map<BlockPos, Set<BlockPos>> traced = immersivefixes$wireDataAdded.get(dimension);
        if(traced==null) { return; }
        BlockPos low = con.start.compareTo(con.end) <= 0?con.start: con.end;
        Set<BlockPos> peers = traced.get(low);
        if(peers!=null) { peers.remove(low==con.start?con.end: con.start); }
    }

    @Unique private void immersivefixes$resetOnce(World world, BlockPos start) {
        int dimension = world.provider.getDimension();
        Map<BlockPos, Set<Connection>> connsForDim = directConnections.get(dimension);
        if(connsForDim==null) { return; }
        Set<BlockPos> done = immersivefixes$alreadyReset.computeIfAbsent(dimension, d -> ConcurrentHashMap.newKeySet());
        if(done.contains(start)) { return; }
        Set<BlockPos> open = new HashSet<>();
        open.add(start);
        Set<BlockPos> closed = new HashSet<>();
        while(!open.isEmpty()) {
            Iterator<BlockPos> it = open.iterator();
            BlockPos next = it.next();
            it.remove();
            closed.add(next);
            IImmersiveConnectable iic = ApiUtils.toIIC(next, world);
            if(iic!=null) {
                iic.onConnectivityUpdate(next, dimension);
                done.add(next);
            }
            Set<Connection> connsAtBlock = connsForDim.get(next);
            if(connsAtBlock!=null) {
                for(Connection c : connsAtBlock) {
                    if(!closed.contains(c.end)) { open.add(c.end); }
                }
            }
        }
    }

    @Unique private static boolean immersivefixes$offer(PriorityQueue<Pair<IImmersiveConnectable, Float>> queue, Map<IImmersiveConnectable, Pair<IImmersiveConnectable, Float>> queued, IImmersiveConnectable end, float loss) {
        Pair<IImmersiveConnectable, Float> existing = queued.get(end);
        if(existing!=null&&existing.getRight() <= loss) { return false; }
        if(existing!=null) { queue.remove(existing); }
        Pair<IImmersiveConnectable, Float> added = new ImmutablePair<>(end, loss);
        queue.add(added);
        queued.put(end, added);
        return true;
    }
}
