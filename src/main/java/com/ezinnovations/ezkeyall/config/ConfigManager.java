package com.ezinnovations.ezkeyall.config;

import com.ezinnovations.ezkeyall.reward.RewardDefinition;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ConfigManager {
    private final JavaPlugin plugin;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    public int getTimerSeconds() {
        return Math.max(1, getConfig().getInt("timer", 3600));
    }

    public boolean isServerWide() {
        return getConfig().getBoolean("server-wide", true);
    }

    public List<String> getMessageTypes() {
        return getConfig().getStringList("messages");
    }

    public Sound getRewardSound() {
        String raw = getConfig().getString("sound-on-reward", "");
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            return Sound.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public String getClickCommand() {
        return getConfig().getString("click-command", "");
    }

    public String getClickCommandMessage() {
        return getConfig().getString("click-command-message", "");
    }

    public List<String> getActionbarLines() {
        return getConfig().getStringList("actionbar-settings.message");
    }

    public List<String> getChatLines() {
        return getConfig().getStringList("chat-settings.message");
    }

    public int getTitleFadeIn() {
        return getConfig().getInt("title-settings.fade-in", 10);
    }

    public int getTitleStay() {
        return getConfig().getInt("title-settings.stay", 40);
    }

    public int getTitleFadeOut() {
        return getConfig().getInt("title-settings.fade-out", 10);
    }

    public String getTitleMessage() {
        return getConfig().getString("title-settings.title-message", "");
    }

    public String getSubtitleMessage() {
        return getConfig().getString("title-settings.subtitle-message", "");
    }

    public List<RewardDefinition> getRewards() {
        ConfigurationSection section = getConfig().getConfigurationSection("rewards");
        if (section == null) {
            return Collections.emptyList();
        }

        List<RewardDefinition> rewards = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            if (rewardSection == null) {
                continue;
            }

            String command = rewardSection.getString("command", "");
            double chance = rewardSection.getDouble("chance", 0.0D);
            String keyName = rewardSection.getString("key-name", "Unknown Key");
            rewards.add(new RewardDefinition(key, command, chance, keyName));
        }
        return rewards;
    }
}
