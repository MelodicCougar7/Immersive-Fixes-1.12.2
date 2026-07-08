package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.common.gui.InventoryBlueprint;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.melodiccougar7.immersivefixes.ModClass;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryBlueprint.class, remap = false)
public class WorkbenchFixes {

    @ModifyExpressionValue(
        method = "updateOutputs(Lnet/minecraft/inventory/IInventory;)V",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;min(II)I"
            ),
        require = 1
    )
    private int stackSensitiveCraftable(int craftable, @Local(name = "out") ItemStack out) {
        ModClass.LOGGER.info("test2");
        ModClass.LOGGER.info(out.getMaxStackSize());
        return Math.min(out.getCount() * craftable, out.getMaxStackSize() - out.getMaxStackSize() % out.getCount());
    }

    // test mixin that did not work
    @Inject(method = "updateOutputs(Lnet/minecraft/inventory/IInventory;)V", at = @At(
            value = "HEAD"
    ))
    private void test(IInventory inputInventory, CallbackInfo ci) {
        ModClass.LOGGER.info("test1");
    }

}
