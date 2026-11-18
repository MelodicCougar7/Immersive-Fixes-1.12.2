package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ConnectorClientSync;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.util.IELogger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityImmersiveConnectable.class, remap = false)
public abstract class TileEntityImmersiveConnectableMixin extends TileEntity {

    @Unique private Set<Connection> immersivefixes$sentConnections;

    @Inject(method = "getBaseDamage(Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;)F", at = @At("RETURN"), cancellable = true, require = 1)
    private void shockOtherWireTypes(Connection c, CallbackInfoReturnable<Float> cir) {
        WireType type = c.cableType;
        if(cir.getReturnValueF()!=0F||type==null||!type.canCauseDamage()) { return; }
        int rate = type.getTransferRate();
        if(rate<=0) { return; }
        float tier = rate<=WireType.COPPER.getTransferRate()?2F: rate<=WireType.ELECTRUM.getTransferRate()?5F: 15F;
        cir.setReturnValue(8*tier/rate);
    }

    /**
     * @author tgstyle
     * @reason Swaps in the received wire set only when it differs, with one model cache reset, instead of clearing the live set and resetting once per wire.
     */
    @Overwrite
    private void loadConnsFromNBT(NBTTagCompound nbt) {
        if(world==null||!world.isRemote||nbt==null||ConnectorClientSync.sharesServerNet()) { return; }
        NBTTagList connectionList = nbt.getTagList("connectionList", 10);
        Set<Connection> received = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for(int i = 0; i < connectionList.tagCount(); i++) {
            Connection con = Connection.readFromNBT(connectionList.getCompoundTagAt(i));
            if(con!=null) { received.add(con); }
            else { IELogger.error("CLIENT read connection as null from {}", nbt); }
        }
        ConcurrentHashMap<BlockPos, Set<Connection>> connsInDim = ImmersiveNetHandler.INSTANCE.directConnections.computeIfAbsent(world.provider.getDimension(), d -> new ConcurrentHashMap<>());
        if(immersivefixes$sameWires(connsInDim.get(pos), received)) { return; }
        connsInDim.put(pos, received);
        ImmersiveNetHandler.INSTANCE.resetCachedIndirectConnections(world, pos);
        immersivefixes$redrawFarEnds(received);
    }

    @Inject(method = "validate()V", at = @At("RETURN"), remap = true, require = 1)
    private void redrawFarEndsOnLoad(CallbackInfo ci) {
        if(!world.isRemote) { return; }
        Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
        if(conns!=null) { immersivefixes$redrawFarEnds(conns); }
    }

    @Inject(method = "getUpdatePacket()Lnet/minecraft/network/play/server/SPacketUpdateTileEntity;", at = @At("RETURN"), remap = true, require = 1)
    private void rememberSentConnections(CallbackInfoReturnable<SPacketUpdateTileEntity> cir) {
        if(world==null||world.isRemote) { return; }
        Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
        immersivefixes$sentConnections = conns==null?Collections.emptySet(): new HashSet<>(conns);
    }

    @Inject(method = "receiveClientEvent(II)Z", at = @At("HEAD"), cancellable = true, remap = true, require = 1)
    private void skipRepeatedListSync(int id, int arg, CallbackInfoReturnable<Boolean> cir) {
        if(id!=-1||immersivefixes$sentConnections==null) { return; }
        if(immersivefixes$sameWires(ImmersiveNetHandler.INSTANCE.getConnections(world, pos), immersivefixes$sentConnections)) { cir.setReturnValue(true); }
    }

    @Unique private void immersivefixes$redrawFarEnds(Set<Connection> conns) {
        for(Connection con : conns) {
            if(world.isBlockLoaded(con.end)) { world.markBlockRangeForRenderUpdate(con.end, con.end); }
        }
    }

    @Unique private static boolean immersivefixes$sameWires(@Nullable Set<Connection> current, Set<Connection> received) {
        if(current==null) { return received.isEmpty(); }
        if(current.size()!=received.size()) { return false; }
        for(Connection con : received) {
            boolean found = false;
            for(Connection old : current) {
                if(old.equals(con)&&old.cableType==con.cableType) { found = true; break; }
            }
            if(!found) { return false; }
        }
        return true;
    }
}
