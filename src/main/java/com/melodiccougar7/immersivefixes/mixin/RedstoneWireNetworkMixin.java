package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.RedstoneNetState;

import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@Mixin(value = RedstoneWireNetwork.class, remap = false)
public abstract class RedstoneWireNetworkMixin implements RedstoneNetState {

    @Shadow public List<WeakReference<IRedstoneConnector>> connectors;

    @Unique private boolean immersivefixes$isBuilt;

    @Unique private List<IRedstoneConnector> immersivefixes$staleMembers;

    @Inject(method = "updateConnectors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/api/energy/wires/redstone/RedstoneWireNetwork;)V", at = @At("HEAD"), require = 1)
    private static void markBuilt(BlockPos start, World world, RedstoneWireNetwork network, CallbackInfo ci) {
        ((RedstoneNetState)network).immersivefixes$beginBuild();
    }

    @Inject(method = "updateConnectors(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lblusunrize/immersiveengineering/api/energy/wires/redstone/RedstoneWireNetwork;)V", at = @At("TAIL"), require = 1)
    private static void splitLeftOut(BlockPos start, World world, RedstoneWireNetwork network, CallbackInfo ci) {
        ((RedstoneNetState)network).immersivefixes$splitLeftOut();
    }

    @Override
    public boolean immersivefixes$built() {
        return immersivefixes$isBuilt;
    }

    @Override
    public void immersivefixes$markStale() {
        immersivefixes$isBuilt = false;
    }

    @Override
    public void immersivefixes$beginBuild() {
        if(!immersivefixes$isBuilt&&!connectors.isEmpty()) {
            immersivefixes$staleMembers = new ArrayList<>();
            for(WeakReference<IRedstoneConnector> ref : connectors) {
                IRedstoneConnector member = ref.get();
                if(member!=null) { immersivefixes$staleMembers.add(member); }
            }
        }
        immersivefixes$isBuilt = true;
    }

    @Override
    public void immersivefixes$splitLeftOut() {
        List<IRedstoneConnector> members = immersivefixes$staleMembers;
        immersivefixes$staleMembers = null;
        if(members==null) { return; }
        Set<IRedstoneConnector> reached = Collections.newSetFromMap(new IdentityHashMap<>());
        for(WeakReference<IRedstoneConnector> ref : connectors) { reached.add(ref.get()); }
        RedstoneWireNetwork leftOut = new RedstoneWireNetwork();
        RedstoneWireNetwork self = (RedstoneWireNetwork)(Object)this;
        for(IRedstoneConnector member : members) {
            if(member.getNetwork()==self&&!reached.contains(member)) { leftOut.add(member); }
        }
        if(!leftOut.connectors.isEmpty()) { leftOut.removeFromNetwork(null); }
    }
}
