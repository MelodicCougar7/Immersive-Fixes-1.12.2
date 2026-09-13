package com.melodiccougar7.immersivefixes.helper;

import com.melodiccougar7.immersivefixes.Tags;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.ItemEarmuffs;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Tags.MOD_ID) public final class EarmuffHandler {

    public static final float MIN_VOLUME = 0.001f;
    private static final Map<SoundCategory, Float> MULTIPLIERS = defaultMultipliers();
    private static String[] blacklistSource = new String[0];
    private static Set<String> blacklist = Collections.emptySet();

    private EarmuffHandler() {}

    @SubscribeEvent public static void onClientTick(ClientTickEvent event) {
        if (event.phase != Phase.START) { return; }
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        if (player == null) { return; }
        refreshBlacklist();
        ItemStack earmuffs = findEarmuffs(player);
        for (SoundCategory category : SoundCategory.values()) {
            float multiplier = 1;
            if (!earmuffs.isEmpty() && ItemEarmuffs.affectedSoundCategories.contains(category.getName()) && !ItemNBTHelper.getBoolean(earmuffs, "IE:Earmuffs:Cat_" + category.getName())) { multiplier = ItemEarmuffs.getVolumeMod(earmuffs); }
            if (MULTIPLIERS.get(category) != multiplier) {
                MULTIPLIERS.put(category, multiplier);
                mc.getSoundHandler().setSoundLevel(category, mc.gameSettings.getSoundLevel(category));
            }
        }
    }

    public static float apply(float volume, ISound sound) {
        if (volume <= 0 || sound == null) { return volume; }
        float multiplier = MULTIPLIERS.get(sound.getCategory());
        if (multiplier >= 1 || blacklist.contains(sound.getSoundLocation().toString().toLowerCase(Locale.ROOT))) { return volume; }
        return Math.max(volume * multiplier, MIN_VOLUME);
    }

    private static ItemStack findEarmuffs(EntityPlayer player) {
        ItemStack head = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (head.isEmpty()) { return ItemStack.EMPTY; }
        ItemStack earmuffs = ItemNBTHelper.hasKey(head, Lib.NBT_Earmuffs) ? ItemNBTHelper.getItemStack(head, Lib.NBT_Earmuffs) : head;
        if (earmuffs.isEmpty() || !IEContent.itemEarmuffs.equals(earmuffs.getItem())) { return ItemStack.EMPTY; }
        return earmuffs;
    }

    private static void refreshBlacklist() {
        String[] current = IEConfig.Tools.earDefenders_SoundBlacklist;
        if (current == blacklistSource) { return; }
        blacklistSource = current;
        Set<String> names = new HashSet<>();
        for (String name : current) { if (name != null) { names.add(name.toLowerCase(Locale.ROOT)); } }
        blacklist = names;
    }

    private static Map<SoundCategory, Float> defaultMultipliers() {
        Map<SoundCategory, Float> result = new EnumMap<>(SoundCategory.class);
        for (SoundCategory category : SoundCategory.values()) { result.put(category, 1f); }
        return result;
    }
}
