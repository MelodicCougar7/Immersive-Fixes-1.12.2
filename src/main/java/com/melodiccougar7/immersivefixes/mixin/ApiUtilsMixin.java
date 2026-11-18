package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ApiUtils.class, remap = false)
public abstract class ApiUtilsMixin {

    @Redirect(method = "doCoilUse(Lblusunrize/immersiveengineering/api/energy/wires/IWireCoil;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getTileEntity(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/tileentity/TileEntity;", ordinal = 2, remap = true), require = 1)
    private static TileEntity loadedLinkTileOnly(World world, BlockPos pos) {
        return world.isBlockLoaded(pos)?world.getTileEntity(pos): null;
    }

    @Redirect(method = "lambda$raytraceWires$3(Ljava/util/Map;Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;Lnet/minecraft/util/math/Vec3d;Lcom/google/common/util/concurrent/AtomicDouble;Ljava/util/concurrent/atomic/AtomicReference;Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(DDD)D", remap = true), require = 1)
    private static double clampSegmentCoefficient(double num, double min, double max) {
        return MathHelper.clamp(min, num, max);
    }
}
