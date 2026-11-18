package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.client.models.smart.ConnModelReal;
import com.google.common.cache.CacheBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ConnModelReal.class, remap = false)
public abstract class ConnModelRealMixin {

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/google/common/cache/CacheBuilder;maximumSize(J)Lcom/google/common/cache/CacheBuilder;"), require = 1)
    private static CacheBuilder<Object, Object> oneLargeSegment(CacheBuilder<Object, Object> builder, long size) {
        return builder.maximumSize(1000).concurrencyLevel(1);
    }
}
