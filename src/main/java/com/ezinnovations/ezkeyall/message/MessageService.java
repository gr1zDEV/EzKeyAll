package com.ezinnovations.ezkeyall.message;

import com.ezinnovations.ezkeyall.config.ConfigManager;
import com.ezinnovations.ezkeyall.reward.RewardDefinition;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MessageService {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final String CLICK_TOKEN = "[click-command-message]";

    private final ConfigManager configManager;
    private final LegacyComponentSerializer legacySerializer;

    public MessageService(ConfigManager configManager) {
        this.configManager = configManager;
        this.legacySerializer = LegacyComponentSerializer.builder()
                .character('&')
                .hexColors()
                .useUnusualXRepeatedCharacterHexFormat()
                .build();
    }

    public void sendRewardFeedback(Player player, RewardDefinition reward) {
        List<String> messageTypes = configManager.getMessageTypes();
        if (messageTypes == null || messageTypes.isEmpty()) {
            playRewardSound(player);
            return;
        }

        for (String rawType : messageTypes) {
            if (rawType == null) {
                continue;
            }

            switch (rawType.toUpperCase(Locale.ROOT)) {
                case "ACTIONBAR" -> sendActionbar(player, reward);
                case "CHAT" -> sendChat(player, reward);
                case "TITLE" -> sendTitle(player, reward);
                default -> {
                }
            }
        }

        playRewardSound(player);
    }

    private void sendActionbar(Player player, RewardDefinition reward) {
        List<String> lines = configManager.getActionbarLines();
        if (lines.isEmpty()) {
            return;
        }

        String joined = lines.stream()
                .map(line -> replaceRewardPlaceholders(line, reward))
                .reduce((a, b) -> a + " | " + b)
                .orElse("");

        if (!joined.isEmpty()) {
            player.sendActionBar(parse(joined));
        }
    }

    private void sendChat(Player player, RewardDefinition reward) {
        List<String> lines = configManager.getChatLines();
        if (lines.isEmpty()) {
            return;
        }

        String clickCommand = configManager.getClickCommand();
        String clickTextRaw = configManager.getClickCommandMessage();
        Component clickText = parse(clickTextRaw);
        boolean hasClick = clickCommand != null && !clickCommand.isBlank();

        for (String line : lines) {
            String parsedLine = replaceRewardPlaceholders(line, reward);
            if (parsedLine.contains(CLICK_TOKEN)) {
                String[] pieces = parsedLine.split(Pattern.quote(CLICK_TOKEN), -1);
                TextComponent.Builder builder = Component.text();
                if (!pieces[0].isEmpty()) {
                    builder.append(parse(pieces[0]));
                }

                Component clickable = clickText;
                if (hasClick) {
                    clickable = clickable.clickEvent(ClickEvent.runCommand(clickCommand));
                }
                builder.append(clickable);

                if (pieces.length > 1 && !pieces[1].isEmpty()) {
                    builder.append(parse(pieces[1]));
                }

                player.sendMessage(builder.build());
            } else {
                player.sendMessage(parse(parsedLine));
            }
        }
    }

    private void sendTitle(Player player, RewardDefinition reward) {
        String title = replaceRewardPlaceholders(configManager.getTitleMessage(), reward);
        String subtitle = replaceRewardPlaceholders(configManager.getSubtitleMessage(), reward);

        Title.Times times = Title.Times.times(
                Duration.ofMillis(configManager.getTitleFadeIn() * 50L),
                Duration.ofMillis(configManager.getTitleStay() * 50L),
                Duration.ofMillis(configManager.getTitleFadeOut() * 50L)
        );

        player.showTitle(Title.title(parse(title), parse(subtitle), times));
    }

    private void playRewardSound(Player player) {
        Sound sound = configManager.getRewardSound();
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    private String replaceRewardPlaceholders(String input, RewardDefinition reward) {
        if (input == null) {
            return "";
        }
        String withClick = input.replace(CLICK_TOKEN, configManager.getClickCommandMessage());
        return withClick.replace("{key-name}", reward.keyName());
    }

    public Component parse(String input) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        String converted = convertHex(input);
        return legacySerializer.deserialize(converted);
    }

    private String convertHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            char[] chars = matcher.group(1).toCharArray();
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : chars) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(buffer, replacement.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
