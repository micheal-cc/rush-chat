package com.rushlabs.rushchat.listener;

import com.rushlabs.rushchat.RushChat;
import com.rushlabs.rushchat.util.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class ChatListener implements Listener {

    private final RushChat plugin;

    public ChatListener(RushChat plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (plugin.isChatLocked() && !player.hasPermission("rushchat.admin")) {
            event.setCancelled(true);
            player.sendMessage(ColorUtil.parse(plugin.getConfigManager().getLockedMessage()));
            return;
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> {

            String rawFormat = plugin.getConfigManager().getChatFormat();
            List<String> rawHover = plugin.getConfigManager().getHoverMessage();
            String rawAction = plugin.getConfigManager().getClickAction();

            Component rootComponent;
            Component suffixComponent = Component.empty();
            String lastColors = "";

            if (rawFormat.contains("{message}")) {
                int index = rawFormat.indexOf("{message}");
                String part1 = rawFormat.substring(0, index);
                String part2 = rawFormat.substring(index + 9);

                String parsedPart1 = PlaceholderAPI.setPlaceholders(source, part1);
                String parsedPart2 = PlaceholderAPI.setPlaceholders(source, part2);

                // Extract the active tags/colors from the end of the prefix
                lastColors = ColorUtil.getLastColors(parsedPart1);

                if (!lastColors.isEmpty()) {
                    parsedPart1 = parsedPart1.substring(0, parsedPart1.length() - lastColors.length());
                }

                rootComponent = ColorUtil.parse(parsedPart1);
                suffixComponent = ColorUtil.parse(parsedPart2);
            } else {
                String parsedFormat = PlaceholderAPI.setPlaceholders(source, rawFormat);
                rootComponent = ColorUtil.parse(parsedFormat);
            }

            if (!rawHover.isEmpty()) {
                StringBuilder hoverText = new StringBuilder();
                for (int i = 0; i < rawHover.size(); i++) {
                    hoverText.append(rawHover.get(i));
                    if (i < rawHover.size() - 1) hoverText.append("\n");
                }
                String parsedHover = PlaceholderAPI.setPlaceholders(source, hoverText.toString());
                rootComponent = rootComponent.hoverEvent(HoverEvent.showText(ColorUtil.parse(parsedHover)));
            }

            if (rawAction != null && !rawAction.isEmpty()) {
                String parsedAction = PlaceholderAPI.setPlaceholders(source, rawAction);
                String[] parts = parsedAction.split(":", 2);
                if (parts.length == 2) {
                    try {
                        ClickEvent.Action actionType = ClickEvent.Action.valueOf(parts[0].toUpperCase());
                        rootComponent = rootComponent.clickEvent(ClickEvent.clickEvent(actionType, parts[1]));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            String messageText = LegacyComponentSerializer.legacyAmpersand().serialize(message);

            // Prepend the extracted tags (like <gradient>) to the message text
            Component parsedMessage = ColorUtil.parse(lastColors + messageText);

            return rootComponent.append(parsedMessage).append(suffixComponent);
        });
    }
}