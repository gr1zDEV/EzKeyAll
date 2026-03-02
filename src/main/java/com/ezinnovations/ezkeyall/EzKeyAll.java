package com.ezinnovations.ezkeyall;

import com.ezinnovations.ezkeyall.command.EzKeyAllCommand;
import com.ezinnovations.ezkeyall.config.ConfigManager;
import com.ezinnovations.ezkeyall.data.DataStore;
import com.ezinnovations.ezkeyall.message.MessageService;
import com.ezinnovations.ezkeyall.reward.RewardManager;
import com.ezinnovations.ezkeyall.timer.TimerService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzKeyAll extends JavaPlugin {
    private ConfigManager configManager;
    private DataStore dataStore;
    private RewardManager rewardManager;
    private MessageService messageService;
    private TimerService timerService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configManager = new ConfigManager(this);
        this.dataStore = new DataStore(this);
        this.rewardManager = new RewardManager(this, configManager);
        this.messageService = new MessageService(configManager);
        this.timerService = new TimerService(this, configManager, dataStore, rewardManager, messageService);

        this.timerService.start();
        registerCommand();
    }

    @Override
    public void onDisable() {
        if (timerService != null) {
            timerService.shutdown();
        }
        if (dataStore != null) {
            dataStore.save();
        }
    }

    private void registerCommand() {
        PluginCommand command = getCommand("ezkeyall");
        if (command == null) {
            getLogger().severe("Could not register /ezkeyall command from plugin.yml");
            return;
        }

        EzKeyAllCommand handler = new EzKeyAllCommand(configManager, timerService);
        command.setExecutor(handler);
        command.setTabCompleter(handler);
    }
}
