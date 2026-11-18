package com.melodiccougar7.immersivefixes.mixin;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Connection.class, remap = false)
public abstract class ConnectionMixin {

    @Shadow public BlockPos start;
    @Shadow public BlockPos end;
    @Shadow public Vec3d[] catenaryVertices;
    @Shadow public boolean vertical;

    @Unique private Object[] immersivefixes$catKey;

    /**
     * @author tgstyle
     * @reason Hashes only start and end, which is all equals compares once the transfer rate and length match, and skips the varargs array Objects.hash allocates on every lookup.
     */
    @Overwrite
    public int hashCode() {
        int result = 31+(start==null?0: start.hashCode());
        return 31*result+(end==null?0: end.hashCode());
    }

    @Inject(method = "readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lblusunrize/immersiveengineering/api/energy/wires/ImmersiveNetHandler$Connection;", at = @At("HEAD"), cancellable = true, require = 1)
    private static void rejectShortPositions(NBTTagCompound tag, CallbackInfoReturnable<Connection> cir) {
        if(tag!=null&&(tag.getIntArray("start").length < 3||tag.getIntArray("end").length < 3)) { cir.setReturnValue(null); }
    }

    /**
     * @author tgstyle
     * @reason Publishes the vertex array only after vertical and the catenary fields are set, and rebuilds it when either end point has moved.
     */
    @Overwrite
    public Vec3d[] getSubVertices(Vec3d vecStart, Vec3d vecEnd) {
        Vec3d[] cached = catenaryVertices;
        Object[] key = immersivefixes$catKey;
        if(cached!=null&&key!=null&&key[0]==cached&&vecStart.equals(key[1])&&vecEnd.equals(key[2])) { return cached; }
        Vec3d[] built = ApiUtils.getConnectionCatenary((Connection)(Object)this, vecStart, vecEnd);
        vertical = vecStart.x==vecEnd.x&&vecStart.z==vecEnd.z;
        catenaryVertices = built;
        immersivefixes$catKey = new Object[]{built, vecStart, vecEnd};
        return built;
    }
}
