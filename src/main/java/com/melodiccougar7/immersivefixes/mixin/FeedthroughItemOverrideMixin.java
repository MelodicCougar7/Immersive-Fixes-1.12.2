package com.melodiccougar7.immersivefixes.mixin;

import com.google.common.cache.Cache;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "blusunrize.immersiveengineering.client.models.smart.FeedthroughModel$FeedthroughItemOverride", remap = false)
public abstract class FeedthroughItemOverrideMixin {

    @Unique private static final NBTTagCompound NO_TAG_IT = new NBTTagCompound();

    @Redirect(method = "handleItemState(Lnet/minecraft/client/renderer/block/model/IBakedModel;Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/client/renderer/block/model/IBakedModel;", at = @At(value = "INVOKE", target = "Lcom/google/common/cache/Cache;get(Ljava/lang/Object;Ljava/util/concurrent/Callable;)Ljava/lang/Object;"), require = 1)
    private Object keyOnTag(Cache<Object, Object> cache, Object stack, Callable<?> loader) throws ExecutionException {
        NBTTagCompound tag = ((ItemStack)stack).getTagCompound();
        NBTTagCompound key = tag==null?NO_TAG_IT: tag;
        Object model = cache.getIfPresent(key);
        return model!=null?model: cache.get(key.copy(), loader);
    }
}
