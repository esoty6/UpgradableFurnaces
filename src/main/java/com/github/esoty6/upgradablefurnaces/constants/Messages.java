package com.github.esoty6.upgradablefurnaces.constants;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public final class Messages {
        public static final BaseComponent SUFFIX =
                        new ComponentBuilder("upgrade is at maximum level!")
                                        .color(net.md_5.bungee.api.ChatColor.RED).build();

        public static BaseComponent MAX_SPEED_LEVEL = new ComponentBuilder("Efficiency ")
                        .color(ChatColor.DARK_AQUA).append(SUFFIX)
                        .color(net.md_5.bungee.api.ChatColor.RED).build();

        public static BaseComponent MAX_FORTUNE_LEVEL =
                        new ComponentBuilder("Fortune ").color(ChatColor.DARK_AQUA).append(SUFFIX)
                                        .color(net.md_5.bungee.api.ChatColor.RED).build();

        public static BaseComponent MAX_FUEL_EFFICIENCY_LEVEL =
                        new ComponentBuilder("Fuel efficiency ").color(ChatColor.DARK_AQUA)
                                        .append(SUFFIX).color(net.md_5.bungee.api.ChatColor.RED)
                                        .build();

        public static BaseComponent BLOCK_NOT_UPGRADABLE =
                        new ComponentBuilder("Block cannot be upgraded!")
                                        .color(net.md_5.bungee.api.ChatColor.RED).build();
}
