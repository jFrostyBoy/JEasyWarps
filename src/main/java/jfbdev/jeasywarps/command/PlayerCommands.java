package jfbdev.jeasywarps.command;

import jfbdev.jeasywarps.JEasyWarps;
import jfbdev.jeasywarps.data.WarpData;
import jfbdev.jeasywarps.gui.MainGUI;
import jfbdev.jeasywarps.manager.WarpManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerCommands implements CommandExecutor, TabCompleter {

    private final JEasyWarps plugin;
    private final WarpManager manager;
    private final MainGUI mainGUI;

    public PlayerCommands(JEasyWarps plugin, WarpManager manager, MainGUI mainGUI) {
        this.plugin = plugin;
        this.manager = manager;
        this.mainGUI = mainGUI;

        Objects.requireNonNull(plugin.getCommand("warp")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("warps")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("wset")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("wdel")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("wrename")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("wsetlore")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("wdellore")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("wicon")).setExecutor(this);

        Objects.requireNonNull(plugin.getCommand("warp")).setTabCompleter(this);
        Objects.requireNonNull(plugin.getCommand("wdel")).setTabCompleter(this);
        Objects.requireNonNull(plugin.getCommand("wrename")).setTabCompleter(this);
        Objects.requireNonNull(plugin.getCommand("wsetlore")).setTabCompleter(this);
        Objects.requireNonNull(plugin.getCommand("wdellore")).setTabCompleter(this);
        Objects.requireNonNull(plugin.getCommand("wicon")).setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cТолько игроки!");
            return true;
        }

        if (!p.hasPermission("jeasywarps.player")) {
            p.sendMessage(manager.color(plugin.getConfig().getString("messages.no-permission", "&cНет прав!")));
            return true;
        }

        String name = cmd.getName().toLowerCase();

        switch (name) {
            case "warps" -> mainGUI.open(p);

            case "warp" -> {
                if (args.length == 0) return false;
                String input = String.join(" ", args);
                WarpData data = manager.findWarp(input);
                if (data == null) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-not-found", "&cВарп не найден"))
                            .replace("%warp%", input));
                    return true;
                }
                p.teleport(data.location);
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.teleport-success", "&aТелепортация на варп &e%warp%&a!"))
                        .replace("%warp%", manager.color(manager.getDisplay(data, input))));
            }

            case "wset" -> {
                if (args.length == 0) return false;
                String raw = String.join(" ", args);
                String clean = manager.stripColors(raw).toLowerCase(Locale.ROOT);
                if (clean.isEmpty()) {
                    p.sendMessage("§cИмя не может быть пустым!");
                    return true;
                }
                if (manager.getWarps().keySet().stream().anyMatch(k -> manager.stripColors(k).toLowerCase(Locale.ROOT).equals(clean))) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-exists", "&cВарп существует"))
                            .replace("%warp%", raw));
                    return true;
                }
                int limit = plugin.getConfig().getInt("warp-limits.default", 3);
                long owned = manager.getWarps().values().stream().filter(d -> d.owner.equals(p.getUniqueId())).count();
                if (owned >= limit) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-limit-reached", "&cЛимит достигнут"))
                            .replace("%limit%", String.valueOf(limit)));
                    return true;
                }
                WarpData data = new WarpData();
                data.location = p.getLocation();
                data.owner = p.getUniqueId();
                data.displayName = raw;
                manager.getWarps().put(clean, data);
                manager.saveWarps();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-created", "&aВарп создан"))
                        .replace("%warp%", manager.color(raw)));
            }

            case "wdel" -> {
                if (args.length == 0) return false;
                String input = String.join(" ", args);
                WarpData data = manager.findWarp(input);
                if (data == null || !data.owner.equals(p.getUniqueId())) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.not-your-warp", "&cНе ваш варп")));
                    return true;
                }
                String key = manager.getKey(data);
                manager.getWarps().remove(key);
                manager.saveWarps();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-deleted", "&cВарп удалён"))
                        .replace("%warp%", manager.color(manager.getDisplay(data, input))));
            }

            case "wrename" -> {
                if (args.length < 2) return false;

                WarpData data = null;
                String newRaw = "";
                String oldDisplay = "";

                for (int i = args.length - 1; i >= 1; i--) {
                    String possibleOldName = String.join(" ", Arrays.copyOfRange(args, 0, i));
                    WarpData possibleData = manager.findWarp(possibleOldName);
                    if (possibleData != null && possibleData.owner.equals(p.getUniqueId())) {
                        data = possibleData;
                        oldDisplay = manager.getDisplay(data, possibleOldName);
                        newRaw = String.join(" ", Arrays.copyOfRange(args, i, args.length));
                        break;
                    }
                }

                if (data == null) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.not-your-warp", "&cНе ваш варп или варп не найден")));
                    return true;
                }

                String newClean = manager.stripColors(newRaw).toLowerCase(Locale.ROOT);
                if (newClean.isEmpty()) {
                    p.sendMessage("§cНовое имя не может быть пустым!");
                    return true;
                }

                if (manager.getWarps().keySet().stream().anyMatch(k -> manager.stripColors(k).toLowerCase(Locale.ROOT).equals(newClean))) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-exists", "&cВарп существует"))
                            .replace("%warp%", newRaw));
                    return true;
                }

                String oldKey = manager.getKey(data);
                manager.getWarps().remove(oldKey);
                data.displayName = newRaw;
                manager.getWarps().put(newClean, data);
                manager.saveWarps();

                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-renamed", "&aВарп переименован: &e%old% &7→ &e%new%"))
                        .replace("%old%", manager.color(oldDisplay))
                        .replace("%new%", manager.color(newRaw)));
            }

            case "wsetlore" -> {
                if (args.length < 2) return false;
                WarpData data = null;
                String lore = "";

                for (int i = args.length - 1; i >= 1; i--) {
                    String possibleName = String.join(" ", Arrays.copyOfRange(args, 0, i));
                    WarpData possibleData = manager.findWarp(possibleName);
                    if (possibleData != null && possibleData.owner.equals(p.getUniqueId())) {
                        data = possibleData;
                        lore = String.join(" ", Arrays.copyOfRange(args, i, args.length));
                        break;
                    }
                }

                if (data == null) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.not-your-warp", "&cНе ваш варп или варп не найден")));
                    return true;
                }

                int max = plugin.getConfig().getInt("lore-max-length", 100);
                if (lore.length() > max) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.lore-too-long", "&cСлишком длинное описание"))
                            .replace("%max%", String.valueOf(max)));
                    return true;
                }

                data.lore = lore;
                manager.saveWarps();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-lore-set", "&aОписание установлено")));
            }

            case "wdellore" -> {
                if (args.length == 0) return false;
                String input = String.join(" ", args);
                WarpData data = manager.findWarp(input);
                if (data == null || !data.owner.equals(p.getUniqueId())) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.not-your-warp", "&cНе ваш варп")));
                    return true;
                }
                data.lore = "";
                manager.saveWarps();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-lore-deleted", "&aОписание удалено")));
            }

            case "wicon" -> {
                if (args.length < 2) return false;

                String materialInput = args[args.length - 1].toUpperCase();
                String warpInput = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));

                Material mat = Material.matchMaterial(materialInput);
                if (mat == null || !mat.isItem()) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.invalid-material", "&cМатериал &e%material% &cне найден или не является предметом!"))
                            .replace("%material%", args[args.length - 1]));
                    return true;
                }

                WarpData data = manager.findWarp(warpInput);
                if (data == null || !data.owner.equals(p.getUniqueId())) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.not-your-warp", "&cНе ваш варп")));
                    return true;
                }

                data.iconMaterial = materialInput;
                manager.saveWarps();
                String prettyName = mat.getKey().getKey().replace("_", " ").toLowerCase();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-icon-set", "&aИконка варпа изменена на &e%material%"))
                        .replace("%material%", prettyName));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, String[] args) {
        if (!(sender instanceof Player p)) return List.of();
        List<String> list = new ArrayList<>();
        String name = cmd.getName().toLowerCase();

        List<String> owned = manager.getWarps().entrySet().stream()
                .filter(e -> e.getValue().owner.equals(p.getUniqueId()))
                .map(Map.Entry::getKey)
                .toList();

        List<String> all = new ArrayList<>(manager.getWarps().keySet());

        if (args.length == 1 && List.of("wdel", "wrename", "wsetlore", "wdellore", "wicon").contains(name)) {
            String lower = args[0].toLowerCase(Locale.ROOT);
            list.addAll(owned.stream().filter(k -> k.toLowerCase(Locale.ROOT).startsWith(lower)).sorted().toList());
        } else if (args.length == 1 && name.equals("warp")) {
            String lower = args[0].toLowerCase(Locale.ROOT);
            list.addAll(all.stream().filter(k -> k.toLowerCase(Locale.ROOT).startsWith(lower)).sorted().toList());
        } else if (name.equals("wicon") && args.length > 1) {
            String partial = args[args.length - 1].toUpperCase(Locale.ROOT);
            list.addAll(Arrays.stream(Material.values())
                    .filter(Material::isItem)
                    .map(Enum::name)
                    .filter(m -> m.startsWith(partial))
                    .sorted()
                    .toList());
        }

        return list;
    }
}