package com.melodiccougar7.immersivefixes.mixin;

import com.melodiccougar7.immersivefixes.helper.ChunkDirty;

import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnaceAdvanced;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = {TileEntityBelljar.class, TileEntityBlastFurnaceAdvanced.class}, remap = false)
public abstract class InsertStackDirtyMixin extends TileEntityIEBase {

    @Redirect(method = "update()V", at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/common/util/Utils;insertStackIntoInventory(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;", remap = false), remap = true, require = 1)
    private ItemStack dirtyOnInsert(TileEntity inventory, ItemStack stack, EnumFacing side) {
        int count = stack.getCount();
        ItemStack remainder = Utils.insertStackIntoInventory(inventory, stack, side);
        if(remainder.getCount()!=count) { ChunkDirty.mark(this); }
        return remainder;
    }
}
