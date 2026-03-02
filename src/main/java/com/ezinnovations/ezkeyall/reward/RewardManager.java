package com.ezinnovations.ezkeyall.reward;

import com.ezinnovations.ezkeyall.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class RewardManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public RewardManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public RewardDefinition selectWeightedReward() {
        List<RewardDefinition> valid = new ArrayList<>();
        double totalWeight = 0.0D;

        for (RewardDefinition reward : configManager.getRewards()) {
            if (reward.chance() <= 0.0D || reward.command().isBlank()) {
                continue;
            }
            valid.add(reward);
            totalWeight += reward.chance();
        }

        if (valid.isEmpty() || totalWeight <= 0.0D) {
            return null;
        }

        double random = ThreadLocalRandom.current().nextDouble(totalWeight);
        double running = 0.0D;
        for (RewardDefinition reward : valid) {
            running += reward.chance();
            if (random <= running) {
                return reward;
            }
        }

        return valid.getLast();
    }

    public void executeReward(Player player, RewardDefinition reward) {
        String command = reward.command().replace("%player%", player.getName()).trim();
        if (command.isEmpty()) {
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public void reload() {
        plugin.getLogger().info("Reward definitions reloaded from config.yml");
    }
}
