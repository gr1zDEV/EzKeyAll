package com.ezinnovations.ezkeyall.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DataStore {
    private final JavaPlugin plugin;
    private final File dataFile;
    private YamlConfiguration dataConfig;

    public DataStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        load();
    }

    public void load() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Failed to create plugin data folder.");
        }

        if (!dataFile.exists()) {
            try {
                if (!dataFile.createNewFile()) {
                    plugin.getLogger().warning("Failed to create data.yml");
                }
            } catch (IOException exception) {
                plugin.getLogger().severe("Error creating data.yml: " + exception.getMessage());
            }
        }

        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public int getServerTimer(int fallback) {
        return Math.max(0, dataConfig.getInt("serverTimer", fallback));
    }

    public void setServerTimer(int value) {
        dataConfig.set("serverTimer", Math.max(0, value));
    }

    public Map<UUID, Integer> getPlayerTimers() {
        Map<UUID, Integer> map = new HashMap<>();
        if (!dataConfig.isConfigurationSection("players")) {
            return map;
        }

        for (String uuidRaw : dataConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidRaw);
                int timer = Math.max(0, dataConfig.getInt("players." + uuidRaw + ".timerRemaining", 0));
                map.put(uuid, timer);
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().warning("Skipping invalid UUID in data.yml: " + uuidRaw);
            }
        }
        return map;
    }

    public void setPlayerTimers(Map<UUID, Integer> timers) {
        dataConfig.set("players", null);
        for (Map.Entry<UUID, Integer> entry : timers.entrySet()) {
            dataConfig.set("players." + entry.getKey() + ".timerRemaining", Math.max(0, entry.getValue()));
        }
    }

    public void save() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException exception) {
            plugin.getLogger().severe("Error saving data.yml: " + exception.getMessage());
        }
    }
}
