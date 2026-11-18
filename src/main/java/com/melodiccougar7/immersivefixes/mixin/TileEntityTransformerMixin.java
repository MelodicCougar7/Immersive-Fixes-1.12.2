package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityTransformer.class, remap = false)
public abstract class TileEntityTransformerMixin extends TileEntityImmersiveConnectable {

    @Inject(method = "removeCable(Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;)V", at = @At("TAIL"), require = 1)
    private void dirtyAfterRemoveCable(Connection connection, CallbackInfo ci) {
        if(world!=null) { ChunkDirty.mark(this); }
    }
}
