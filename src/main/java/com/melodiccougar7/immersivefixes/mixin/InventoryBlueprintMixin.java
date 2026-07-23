package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.common.gui.InventoryBlueprint;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = InventoryBlueprint.class, remap = false)
public class InventoryBlueprintMixin {

    @ModifyExpressionValue(
            method = "updateOutputs(Lnet/minecraft/inventory/IInventory;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Math;min(II)I"
            ),
            require = 1
    )
    private int stackSensitiveCraftable(int craftable, @Local(name = "out") ItemStack out) {
        return Math.min(out.getCount() * craftable, out.getMaxStackSize() - out.getMaxStackSize() % out.getCount());
    }
}
