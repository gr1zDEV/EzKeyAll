package com.ezinnovations.ezkeyall.command;

import com.ezinnovations.ezkeyall.config.ConfigManager;
import com.ezinnovations.ezkeyall.time.CompactTimeFormatter;
import com.ezinnovations.ezkeyall.timer.TimerService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EzKeyAllCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN_PERMISSION = "ezkeyall.admin";

    private final ConfigManager configManager;
    private final TimerService timerService;

    public EzKeyAllCommand(ConfigManager configManager, TimerService timerService) {
        this.configManager = configManager;
        this.timerService = timerService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eUsage: /" + label + " <reload|reset|time>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                configManager.reload();
                timerService.reload();
                sender.sendMessage("§aEzKeyAll config reloaded. Running timer state preserved from data.yml.");
                return true;
            }
            case "reset" -> {
                timerService.resetTimers();
                sender.sendMessage("§aEzKeyAll timer(s) reset to configured timer value.");
                return true;
            }
            case "time" -> {
                if (configManager.isServerWide()) {
                    sender.sendMessage("§aServer-wide timer remaining: §f" + CompactTimeFormatter.format(timerService.getServerRemaining()));
                } else {
                    if (sender instanceof Player player) {
                        int remaining = timerService.getRemainingFor(player);
                        sender.sendMessage("§aYour timer remaining: §f" + CompactTimeFormatter.format(remaining));
                    } else {
                        sender.sendMessage("§ePer-player mode is active. Run as a player to view personal remaining time.");
                    }
                }
                return true;
            }
            default -> {
                sender.sendMessage("§eUsage: /" + label + " <reload|reset|time>");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            return List.of();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            for (String value : List.of("reload", "reset", "time")) {
                if (value.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    suggestions.add(value);
                }
            }
            return suggestions;
        }
        return List.of();
    }

}
