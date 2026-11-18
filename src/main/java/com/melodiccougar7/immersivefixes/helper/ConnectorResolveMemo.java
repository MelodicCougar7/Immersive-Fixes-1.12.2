package com.melodiccougar7.immersivefixes.helper;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber public final class ConnectorResolveMemo {

    private static final Int2ObjectMap<Long2ObjectMap<Memo>> MEMOS = new Int2ObjectOpenHashMap<>();

    private ConnectorResolveMemo() {}

    public static IImmersiveConnectable resolve(BlockPos pos, World world) {
        TileEntity hit = remembered(pos, world);
        if(hit instanceof IImmersiveConnectable) { return (IImmersiveConnectable)hit; }
        IImmersiveConnectable iic = ApiUtils.toIIC(pos, world);
        if(iic instanceof TileEntity) { remember(pos, world, (TileEntity)iic); }
        return iic;
    }

    public static TileEntity tileAt(BlockPos pos, World world) {
        TileEntity hit = remembered(pos, world);
        if(hit!=null) { return hit; }
        TileEntity tile = Utils.getExistingTileEntity(world, pos);
        if(tile!=null) { remember(pos, world, tile); }
        return tile;
    }

    private static TileEntity remembered(BlockPos pos, World world) {
        Long2ObjectMap<Memo> chunks = MEMOS.get(world.provider.getDimension());
        Memo memo = chunks==null?null: chunks.get(ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4));
        if(memo==null) { return null; }
        long key = pos.toLong();
        TileEntity hit = memo.tiles.get(key);
        if(hit==null) { return null; }
        if(hit.isInvalid()) {
            memo.tiles.remove(key);
            return null;
        }
        memo.chunk.unloadQueued = false;
        return hit;
    }

    private static void remember(BlockPos pos, World world, TileEntity tile) {
        int dimension = world.provider.getDimension();
        Long2ObjectMap<Memo> chunks = MEMOS.get(dimension);
        if(chunks==null) {
            chunks = new Long2ObjectOpenHashMap<>();
            MEMOS.put(dimension, chunks);
        }
        long key = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        Memo memo = chunks.get(key);
        if(memo==null) {
            memo = new Memo(world.getChunk(pos));
            chunks.put(key, memo);
        }
        memo.tiles.put(pos.toLong(), tile);
    }

    @SubscribeEvent public static void onChunkUnload(ChunkEvent.Unload event) {
        if(event.getWorld().isRemote) { return; }
        Long2ObjectMap<Memo> chunks = MEMOS.get(event.getWorld().provider.getDimension());
        if(chunks!=null) { chunks.remove(ChunkPos.asLong(event.getChunk().x, event.getChunk().z)); }
    }

    @SubscribeEvent public static void onWorldUnload(WorldEvent.Unload event) {
        if(event.getWorld().isRemote) { return; }
        MEMOS.remove(event.getWorld().provider.getDimension());
    }

    private static final class Memo {
        private final Chunk chunk;
        private final Long2ObjectMap<TileEntity> tiles = new Long2ObjectOpenHashMap<>();

        private Memo(Chunk chunk) { this.chunk = chunk; }
    }
}
