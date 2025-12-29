package jfbdev.jeasywarps.gui;

import jfbdev.jeasywarps.JEasyWarps;
import jfbdev.jeasywarps.data.WarpData;
import jfbdev.jeasywarps.manager.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MainGUI {

    private final JEasyWarps plugin;
    private final WarpManager manager;
    public final Map<UUID, Integer> playerPage = new HashMap<>();

    public MainGUI(JEasyWarps plugin, WarpManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player p) {
        open(p, playerPage.getOrDefault(p.getUniqueId(), 0));
    }

    public void open(Player p, int page) {
        playerPage.put(p.getUniqueId(), page);

        int size = plugin.getConfig().getInt("gui.main.size", 54);
        int itemsPerPage = plugin.getConfig().getInt("gui.main.items-per-page", 28);
        String title = manager.color(plugin.getConfig().getString("gui.main.title", "&6✦ Варпы ✦"));
        Inventory inv = Bukkit.createInventory(null, size, title);

        ItemStack filler = createItem("gui.main.filler");
        for (int i = 0; i < size; i++) {
            inv.setItem(i, filler);
        }

        List<Map.Entry<String, WarpData>> warpList = new ArrayList<>(manager.getWarps().entrySet());
        int totalWarps = warpList.size();
        int totalPages = (int) Math.ceil((double) totalWarps / itemsPerPage);
        int currentPage = Math.max(0, Math.min(page, totalPages - 1));

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalWarps);

        if (totalWarps == 0) {
            ItemStack noWarpsItem = createItem("gui.main.no-warps");
            inv.setItem(plugin.getConfig().getInt("gui.main.no-warps.slot", 22), noWarpsItem);
        } else {
            int slot = plugin.getConfig().getInt("gui.main.warps-start-slot", 10);
            for (int i = startIndex; i < endIndex; i++) {
                if (slot >= size - 9) break;

                var entry = warpList.get(i);
                String key = entry.getKey();
                WarpData data = entry.getValue();
                String display = manager.getDisplay(data, key);

                OfflinePlayer owner = Bukkit.getOfflinePlayer(data.owner);
                String ownerName = owner.getName() != null ? owner.getName() : "Неизвестно";

                List<String> baseLore = plugin.getConfig().getStringList("gui.main.warp-item.lore");
                List<String> finalLore = new ArrayList<>();
                for (String line : baseLore) {
                    if (line.contains("%lore_lines%")) {
                        String raw = data.lore.isEmpty()
                                ? plugin.getConfig().getString("gui.main.no-description", "&7Нет описания")
                                : data.lore;
                        finalLore.addAll(wrapText(raw));
                    } else {
                        finalLore.add(manager.color(line
                                .replace("%owner%", ownerName)
                                .replace("%world%", data.location.getWorld().getName())
                                .replace("%rating%", manager.getRatingStars(data))));
                    }
                }

                if (data.owner.equals(p.getUniqueId())) {
                    List<String> ownerLore = plugin.getConfig().getStringList("gui.main.warp-item.owner-only-lore");
                    for (String ownerLine : ownerLore) {
                        finalLore.add(manager.color(ownerLine));
                    }
                }

                ItemStack item;
                if (data.iconMaterial != null) {
                    Material mat = Material.matchMaterial(data.iconMaterial.toUpperCase());
                    if (mat != null && mat.isItem()) {
                        item = new ItemStack(mat);
                    } else {
                        item = createItem("gui.main.warp-item");
                    }
                } else {
                    item = createItem("gui.main.warp-item");
                }

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(manager.color(plugin.getConfig().getString("gui.main.warp-item.name", "&a%warp%").replace("%warp%", display)));
                    if (!finalLore.isEmpty()) {
                        meta.setLore(finalLore);
                    }

                    List<String> flagNames = plugin.getConfig().getStringList("gui.main.warp-item.hide-flags");
                    for (String flagName : flagNames) {
                        try {
                            ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                            meta.addItemFlags(flag);
                        } catch (IllegalArgumentException ignored) {}
                    }

                    item.setItemMeta(meta);
                }

                inv.setItem(slot, item);
                slot++;
                if ((slot + 1) % 9 == 8) slot += 2;
            }
        }

        String prevPath = currentPage > 0 ? "gui.main.pagination.previous-page" : "gui.main.pagination.previous-page.no-previous";
        String nextPath = currentPage < totalPages - 1 ? "gui.main.pagination.next-page" : "gui.main.pagination.next-page.no-next";

        inv.setItem(plugin.getConfig().getInt(prevPath + ".slot", 48), createItem(prevPath));
        inv.setItem(plugin.getConfig().getInt(nextPath + ".slot", 50), createItem(nextPath));

        ItemStack pageInfo = createItem("gui.main.pagination.page-info");
        ItemMeta infoMeta = pageInfo.getItemMeta();
        if (infoMeta != null) {
            if (infoMeta.hasDisplayName()) {
                String name = infoMeta.getDisplayName()
                        .replace("%page%", String.valueOf(currentPage + 1))
                        .replace("%total%", String.valueOf(Math.max(1, totalPages)))
                        .replace("%warps%", String.valueOf(totalWarps));
                infoMeta.setDisplayName(manager.color(name));
            }

            if (infoMeta.hasLore()) {
                List<String> lore = new ArrayList<>();
                for (String line : infoMeta.getLore()) {
                    lore.add(manager.color(line.replace("%warps%", String.valueOf(totalWarps))));
                }
                infoMeta.setLore(lore);
            }

            pageInfo.setItemMeta(infoMeta);
        }
        inv.setItem(plugin.getConfig().getInt("gui.main.pagination.page-info.slot", 49), pageInfo);

        p.openInventory(inv);
    }

    private ItemStack createItem(String path) {
        String matStr = plugin.getConfig().getString(path + ".material", "BLACK_STAINED_GLASS_PANE");
        Material mat = Material.matchMaterial(matStr.toUpperCase());
        if (mat == null) mat = Material.BLACK_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(mat);
        if (plugin.getConfig().contains(path + ".amount")) {
            item.setAmount(plugin.getConfig().getInt(path + ".amount", 1));
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (plugin.getConfig().contains(path + ".name")) {
                meta.setDisplayName(manager.color(plugin.getConfig().getString(path + ".name")));
            }
            if (plugin.getConfig().contains(path + ".lore")) {
                meta.setLore(plugin.getConfig().getStringList(path + ".lore").stream()
                        .map(manager::color)
                        .toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private List<String> wrapText(String rawLore) {
        List<String> lines = new ArrayList<>();

        if (rawLore == null || rawLore.isEmpty() || ChatColor.stripColor(manager.color(rawLore)).trim().isEmpty()) {
            lines.add(manager.color(plugin.getConfig().getString("gui.main.no-description", "&7Нет описания")));
            return lines;
        }

        String colored = manager.color(rawLore);
        StringBuilder currentLine = new StringBuilder();
        String lastColors = "";

        String[] words = colored.split(" ");
        for (String word : words) {
            if (word.isEmpty()) continue;

            String testLine = currentLine + (currentLine.isEmpty() ? "" : " ") + word;
            if (ChatColor.stripColor(testLine).length() > 35) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(lastColors).append(word);
                } else {
                    lines.add(word);
                    currentLine = new StringBuilder();
                }
            } else {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            }

            lastColors = ChatColor.getLastColors(currentLine.toString());
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
