package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ConnectionsInDimension;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.common.IESaveData;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = IESaveData.class, remap = false)
public abstract class IESaveDataMixin {

    @Redirect(method = "writeToNBT", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler;getAllConnections(I)Ljava/util/Collection;", remap = false), remap = true, require = 1)
    private Collection<Connection> iterateConnectionsInPlace(ImmersiveNetHandler handler, int dimensionId) {
        Map<BlockPos, Set<Connection>> byNode = handler.directConnections.get(dimensionId);
        return byNode==null?Collections.emptySet(): new ConnectionsInDimension(byNode);
    }
}
