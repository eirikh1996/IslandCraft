package com.github.hoqhuuep.islandcraft.bukkit;

import org.bukkit.configuration.file.FileConfiguration;

public class Language {
    final FileConfiguration fileConfiguration;

    public Language(final FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public String get(final String id, final Object... args) {
        String format = fileConfiguration.getString(id);
        if (format == null) {
            return null;
        }
        return String.format(format, args);
    }
}