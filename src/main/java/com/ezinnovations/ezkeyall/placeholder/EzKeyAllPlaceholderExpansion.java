package com.ezinnovations.ezkeyall.placeholder;

import com.ezinnovations.ezkeyall.EzKeyAll;
import com.ezinnovations.ezkeyall.time.CompactTimeFormatter;
import com.ezinnovations.ezkeyall.timer.TimerService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class EzKeyAllPlaceholderExpansion extends PlaceholderExpansion {
    private final EzKeyAll plugin;
    private final TimerService timerService;

    public EzKeyAllPlaceholderExpansion(EzKeyAll plugin, TimerService timerService) {
        this.plugin = plugin;
        this.timerService = timerService;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ezkeyall";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (!params.equalsIgnoreCase("timer")) {
            return null;
        }

        if (player != null && player.isOnline() && player.getPlayer() != null) {
            return CompactTimeFormatter.format(timerService.getRemainingFor(player.getPlayer()));
        }

        return CompactTimeFormatter.format(timerService.getServerRemaining());
    }

}
