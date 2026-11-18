package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.models.smart.ConnModelReal;
import blusunrize.immersiveengineering.client.models.smart.ConnModelReal.ExtBlockstateAdapter;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import java.util.Set;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = ClientProxy.class, remap = false)
public abstract class ClientProxyMixin {

    /**
     * @author tgstyle
     * @reason Invalidates with the same Pair of chunk offset and adapter that ConnModelReal caches under, instead of a bare adapter that never matches a key.
     */
    @Overwrite
    public void removeStateFromConnectionModelCache(IExtendedBlockState state) {
        Object[] additional = null;
        if(state.getUnlistedProperties().containsKey(IEProperties.TILEENTITY_PASSTHROUGH)) {
            TileEntity te = state.getValue(IEProperties.TILEENTITY_PASSTHROUGH);
            if(te instanceof IEBlockInterfaces.ICacheData) { additional = ((IEBlockInterfaces.ICacheData)te).getCacheData(); }
        }
        int x = 0, z = 0;
        if(state.getUnlistedProperties().containsKey(IEProperties.CONNECTIONS)) {
            Set<?> conns = state.getValue(IEProperties.CONNECTIONS);
            if(conns!=null&&!conns.isEmpty()) {
                BlockPos start = ((Connection)conns.iterator().next()).start;
                x = (start.getX()%16+16)%16;
                z = (start.getZ()%16+16)%16;
            }
        }
        ConnModelReal.cache.invalidate(new ImmutablePair<>((byte)((x<<4)|z), new ExtBlockstateAdapter(state, null, ExtBlockstateAdapter.ONLY_OBJ_CALLBACK, additional)));
    }
}
