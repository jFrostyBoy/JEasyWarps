package jfbdev.jeasywarps.command;

import jfbdev.jeasywarps.JEasyWarps;
import jfbdev.jeasywarps.data.WarpData;
import jfbdev.jeasywarps.manager.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class AdminCommands implements CommandExecutor {

    private final JEasyWarps plugin;
    private final WarpManager manager;

    public AdminCommands(JEasyWarps plugin, WarpManager manager) {
        this.plugin = plugin;
        this.manager = manager;

        Objects.requireNonNull(plugin.getCommand("jewreload")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("jewdelwarp")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("jewrelore")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("jewdellore")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!sender.hasPermission("jeasywarps.admin")) {
            sender.sendMessage("§cНет прав!");
            return true;
        }

        String name = cmd.getName().toLowerCase();

        switch (name) {
            case "jewreload" -> {
                plugin.reloadConfig();
                manager.loadWarps();
                sender.sendMessage("§aПлагин перезагружен.");
            }
            case "jewdelwarp" -> {
                if (args.length == 0) return false;
                String input = String.join(" ", args);
                WarpData data = manager.findWarp(input);
                if (data == null) {
                    sender.sendMessage("§cВарп не найден.");
                    return true;
                }
                String key = manager.getKey(data);
                manager.getWarps().remove(key);
                manager.saveWarps();
                sender.sendMessage("§aВарп удалён админом.");
            }
            case "jewrelore" -> {
                if (args.length < 2) return false;
                String warpInput = args[0];
                String lore = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                WarpData data = manager.findWarp(warpInput);
                if (data == null) {
                    sender.sendMessage("§cВарп не найден.");
                    return true;
                }
                data.lore = lore;
                manager.saveWarps();
                sender.sendMessage("§aОписание варпа изменено админом.");
            }
            case "jewdellore" -> {
                if (args.length == 0) return false;
                String input = String.join(" ", args);
                WarpData data = manager.findWarp(input);
                if (data == null) {
                    sender.sendMessage("§cВарп не найден.");
                    return true;
                }
                data.lore = "";
                manager.saveWarps();
                sender.sendMessage("§aОписание варпа удалено админом.");
            }
        }
        return true;
    }
}