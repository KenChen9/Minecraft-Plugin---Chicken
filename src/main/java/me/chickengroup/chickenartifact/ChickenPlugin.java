package me.chickengroup.chickenartifact;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ChickenPlugin extends JavaPlugin {

    private final List<String> allCommands = new ArrayList<>();
    private final Map<UUID, UUID> teleportRequests = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        registerCommand("hello", 0, (player, args) -> {
            player.sendMessage(Component.text("Hello world!", NamedTextColor.GREEN));
        });

        registerCommand("commands", 0, (player, args) -> {
            final List<String> allCommandsWithSlash = allCommands.stream().map(cmd -> "/" + cmd).toList();
            player.sendMessage(Component.text("Commands:\n" + String.join("\n", allCommandsWithSlash), NamedTextColor.AQUA));
        });

        registerCommand("tpa", 1, (player, args) -> {
            final Player target = getServer().getPlayerExact(args[0]);
            if (target == null) {
                player.sendMessage(Component.text("Player not found.", NamedTextColor.RED));
                return;
            }
            if (target.equals(player)) {
                player.sendMessage(Component.text("You cannot teleport to yourself.", NamedTextColor.RED));
                return;
            }
            teleportRequests.put(target.getUniqueId(), player.getUniqueId());
            player.sendMessage(Component.text("Teleport request sent to " + target.getName() + ". Please wait for a response.", NamedTextColor.GREEN));
            target.sendMessage(Component.text(player.getName() + " wants to teleport to you. Type /tpaccept to accept or /tpdeny to deny.", NamedTextColor.YELLOW));
        });

        registerCommand("tpaccept", 0, (player, args) -> {
            final UUID requesterUUID = teleportRequests.get(player.getUniqueId());
            if (requesterUUID == null) {
                player.sendMessage(Component.text("No teleport requests found.", NamedTextColor.RED));
                return;
            }
            final Player requester = getServer().getPlayer(requesterUUID);
            teleportRequests.remove(player.getUniqueId());
            if (requester == null) {
                player.sendMessage(Component.text("The player who requested to teleport is no longer online.", NamedTextColor.RED));
                return;
            }
            requester.sendMessage(Component.text("Your teleport request to " + player.getName() + " has been accepted.", NamedTextColor.GREEN));
            player.sendMessage(Component.text("You have accepted the teleport request from " + requester.getName() + ".", NamedTextColor.GREEN));
            requester.teleport(player);
        });

        registerCommand("tpdeny", 0, (player, args) -> {
            final UUID requesterUUID = teleportRequests.get(player.getUniqueId());
            if (requesterUUID == null) {
                player.sendMessage(Component.text("No teleport requests found.", NamedTextColor.RED));
                return;
            }
            final Player requester = getServer().getPlayer(requesterUUID);
            teleportRequests.remove(player.getUniqueId());
            if (requester == null) {
                player.sendMessage(Component.text("The player who requested to teleport is no longer online.", NamedTextColor.RED));
                return;
            }
            requester.sendMessage(Component.text("Your teleport request to " + player.getName() + " has been denied.", NamedTextColor.RED));
            player.sendMessage(Component.text("You have denied the teleport request from " + requester.getName() + ".", NamedTextColor.GREEN));
        });
    }

    private void registerCommand(@NotNull final String name, final int numArgs, @NotNull final BiConsumer<Player, String[]> commandCallback) {
        allCommands.add(name);
        final PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            getLogger().warning("Command " + name + " not found in plugin.yml");
            return;
        }
        pluginCommand.setExecutor((sender, command, alias, args) -> {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                return false;
            }
            if (!command.getName().equalsIgnoreCase(name) || args.length != numArgs) {
                sender.sendMessage(Component.text("Usage: /" + name + " <arguments>", NamedTextColor.YELLOW));
                return true;
            }
            commandCallback.accept((Player) sender, args);
            return true;
        });
        pluginCommand.setTabCompleter(((sender, command, alias, args) -> {
            if (command.getName().equalsIgnoreCase("tpa") && args.length == 1) {
                return getServer().getOnlinePlayers().stream().map(Player::getName).toList();
            }
            return Collections.emptyList();
        }));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @FunctionalInterface
    interface BiConsumer<T, U> {
        void accept(T t, U u);
    }
}
