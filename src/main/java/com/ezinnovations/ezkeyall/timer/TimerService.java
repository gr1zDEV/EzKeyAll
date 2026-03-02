package com.ezinnovations.ezkeyall.timer;

import com.ezinnovations.ezkeyall.config.ConfigManager;
import com.ezinnovations.ezkeyall.data.DataStore;
import com.ezinnovations.ezkeyall.message.MessageService;
import com.ezinnovations.ezkeyall.reward.RewardDefinition;
import com.ezinnovations.ezkeyall.reward.RewardManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class TimerService {
    private static final String BYPASS_PERMISSION = "ezkeyall.bypass";

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final DataStore dataStore;
    private final RewardManager rewardManager;
    private final MessageService messageService;

    private final Map<UUID, Integer> playerTimers = new HashMap<>();
    private int serverTimer;

    private ScheduledTask tickTask;
    private ScheduledTask saveTask;

    public TimerService(JavaPlugin plugin,
                        ConfigManager configManager,
                        DataStore dataStore,
                        RewardManager rewardManager,
                        MessageService messageService) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.dataStore = dataStore;
        this.rewardManager = rewardManager;
        this.messageService = messageService;
    }

    public void start() {
        loadState();

        this.tickTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> tick(), 20L, 20L);
        this.saveTask = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> saveState(), 20L * 60L, 20L * 60L);
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
        saveState();
    }

    public void reload() {
        saveState();
        loadState();
    }

    public void resetTimers() {
        int base = configManager.getTimerSeconds();
        serverTimer = base;
        playerTimers.clear();
        saveState();
    }

    public int getServerRemaining() {
        return serverTimer;
    }

    public int getRemainingFor(Player player) {
        if (player == null) {
            return configManager.getTimerSeconds();
        }
        if (configManager.isServerWide()) {
            return serverTimer;
        }
        return playerTimers.getOrDefault(player.getUniqueId(), configManager.getTimerSeconds());
    }

    private void tick() {
        if (configManager.isServerWide()) {
            tickServerWide();
        } else {
            tickPerPlayer();
        }
    }

    private void tickServerWide() {
        if (serverTimer > 0) {
            serverTimer--;
        }

        if (serverTimer > 0) {
            return;
        }

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission(BYPASS_PERMISSION)) {
                continue;
            }
            rewardPlayer(player);
        }

        serverTimer = configManager.getTimerSeconds();
    }

    private void tickPerPlayer() {
        int base = configManager.getTimerSeconds();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.hasPermission(BYPASS_PERMISSION)) {
                continue;
            }

            UUID uuid = player.getUniqueId();
            int remaining = playerTimers.getOrDefault(uuid, base);
            remaining = Math.max(0, remaining - 1);

            if (remaining <= 0) {
                rewardPlayer(player);
                remaining = base;
            }

            playerTimers.put(uuid, remaining);
        }
    }

    private void rewardPlayer(Player player) {
        RewardDefinition reward = rewardManager.selectWeightedReward();
        if (reward == null) {
            return;
        }

        rewardManager.executeReward(player, reward);
        messageService.sendRewardFeedback(player, reward);
    }

    private void loadState() {
        int fallbackTimer = configManager.getTimerSeconds();
        this.serverTimer = dataStore.getServerTimer(fallbackTimer);
        this.playerTimers.clear();
        this.playerTimers.putAll(dataStore.getPlayerTimers());
    }

    private void saveState() {
        dataStore.setServerTimer(serverTimer);
        dataStore.setPlayerTimers(playerTimers);
        dataStore.save();
    }
}
