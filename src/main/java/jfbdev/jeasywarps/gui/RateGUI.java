package jfbdev.jeasywarps.gui;

import jfbdev.jeasywarps.JEasyWarps;
import jfbdev.jeasywarps.manager.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RateGUI {

    private final JEasyWarps plugin;
    private final WarpManager manager;

    public RateGUI(JEasyWarps plugin, WarpManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player p, String warpKey) {
        var data = manager.getWarps().get(warpKey);
        String display = manager.getDisplay(data, warpKey);

        String title = manager.color(plugin.getConfig().getString("gui.rate.title", "&6Оценить варп: &e%warp%").replace("%warp%", display));
        Inventory inv = Bukkit.createInventory(null, plugin.getConfig().getInt("gui.rate.size", 27), title);

        ItemStack filler = createItem("gui.rate.filler");
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(plugin.getConfig().getInt("gui.rate.back-item.slot", 22), createItem("gui.rate.back-item"));

        for (int i = 1; i <= 5; i++) {
            String path = "gui.rate.stars." + i;
            inv.setItem(plugin.getConfig().getInt(path + ".slot", 9 + (i - 1) * 2), createItem(path));
        }

        p.openInventory(inv);
    }

    private ItemStack createItem(String path) {
        String matStr = plugin.getConfig().getString(path + ".material", "BLACK_STAINED_GLASS_PANE");
        org.bukkit.Material mat = org.bukkit.Material.matchMaterial(matStr.toUpperCase());
        if (mat == null) mat = org.bukkit.Material.BLACK_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(mat);
        if (plugin.getConfig().contains(path + ".amount")) item.setAmount(plugin.getConfig().getInt(path + ".amount", 1));

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (plugin.getConfig().contains(path + ".name")) meta.setDisplayName(manager.color(plugin.getConfig().getString(path + ".name")));
            if (plugin.getConfig().contains(path + ".lore")) {
                meta.setLore(plugin.getConfig().getStringList(path + ".lore").stream().map(manager::color).toList());
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}