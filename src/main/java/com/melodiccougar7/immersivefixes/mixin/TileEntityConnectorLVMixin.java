package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.TileEntityImmersiveConnectable;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntityConnectorLV.class, remap = false)
public abstract class TileEntityConnectorLVMixin extends TileEntityImmersiveConnectable {

    @Unique private final Map<BlockPos, TileEntity> immersivefixes$resolved = new HashMap<>();
    @Unique private long immersivefixes$resolvedTick = -1;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true, remap = true, require = 1)
    private void stopRelayTicking(CallbackInfo ci) {
        if(world.isRemote||!isRelay()) { return; }
        EventHandler.REMOVE_FROM_TICKING.add(this);
        ci.cancel();
    }

    @Redirect(method = {"transferEnergy(IZI)I", "notifyAvailableEnergy(ILjava/util/Set;)V"}, at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/api/ApiUtils;toIIC(Ljava/lang/Object;Lnet/minecraft/world/World;)Lblusunrize/immersiveengineering/api/energy/wires/IImmersiveConnectable;"), require = 5)
    private IImmersiveConnectable resolveOncePerTick(Object object, World world) {
        if(!(object instanceof BlockPos)) { return ApiUtils.toIIC(object, world); }
        long tick = world.getTotalWorldTime();
        if(immersivefixes$resolvedTick!=tick) {
            immersivefixes$resolved.clear();
            immersivefixes$resolvedTick = tick;
        }
        BlockPos pos = (BlockPos)object;
        TileEntity hit = immersivefixes$resolved.get(pos);
        if(hit!=null&&!hit.isInvalid()) { return (IImmersiveConnectable)hit; }
        IImmersiveConnectable iic = ApiUtils.toIIC(object, world);
        if(iic instanceof TileEntity) { immersivefixes$resolved.put(pos, (TileEntity)iic); }
        else { immersivefixes$resolved.remove(pos); }
        return iic;
    }
}
