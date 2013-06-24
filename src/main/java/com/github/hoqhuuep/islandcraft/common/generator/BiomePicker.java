package com.github.hoqhuuep.islandcraft.common.generator;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.github.hoqhuuep.islandcraft.bukkit.IslandCraftPlugin;
import com.github.hoqhuuep.islandcraft.common.api.ICConfig;
import com.github.hoqhuuep.islandcraft.common.type.ICBiome;

// TODO Remove dependency on Bukkit here

public class BiomePicker {
    private static ICBiome[] biomes;

    public static ICBiome pick(final long seed) {
        if (biomes == null) {
            // Hacks to get configuration from IslandCraft
            final Plugin plugin = Bukkit.getPluginManager().getPlugin("IslandCraft");
            if (plugin == null || !(plugin instanceof IslandCraftPlugin)) {
                throw new Error("Could not find IslandCraft plugin");
            }
            final IslandCraftPlugin islandCraft = (IslandCraftPlugin) plugin;
            final ICConfig config = islandCraft.getICConfig();
            biomes = config.getIslandBiomes();
        }
        return biomes[new Random(seed).nextInt(biomes.length)];
    }
}