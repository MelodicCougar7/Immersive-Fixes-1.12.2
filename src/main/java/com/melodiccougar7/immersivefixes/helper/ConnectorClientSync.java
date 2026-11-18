package com.melodiccougar7.immersivefixes.helper;

import net.minecraft.client.Minecraft;

public final class ConnectorClientSync {

    private ConnectorClientSync() {}

    public static boolean sharesServerNet() { return Minecraft.getMinecraft().isSingleplayer(); }
}
