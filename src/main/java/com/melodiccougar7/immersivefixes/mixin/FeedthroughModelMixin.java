package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.energy.wires.WireApi;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "blusunrize.immersiveengineering.client.models.smart.FeedthroughModel$SpecificFeedthroughModel", remap = false)
public abstract class FeedthroughModelMixin {

    @Inject(method = "getConnQuads(Lnet/minecraft/util/EnumFacing;Lnet/minecraft/util/EnumFacing;Lblusunrize/immersiveengineering/api/energy/wires/WireType;Lblusunrize/immersiveengineering/common/util/chickenbones/Matrix4;)Ljava/util/List;", at = @At("HEAD"), cancellable = true, require = 1)
    private void emptyWithoutInfo(EnumFacing facing, EnumFacing side, WireType type, Matrix4 mat, CallbackInfoReturnable<List<BakedQuad>> cir) {
        if(WireApi.INFOS.get(type)==null) { cir.setReturnValue(Collections.emptyList()); }
    }
}
