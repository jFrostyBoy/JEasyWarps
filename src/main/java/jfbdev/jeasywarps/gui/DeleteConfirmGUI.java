package jfbdev.jeasywarps.gui;

import jfbdev.jeasywarps.JEasyWarps;
import jfbdev.jeasywarps.manager.WarpManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DeleteConfirmGUI {

    private final JEasyWarps plugin;
    private final WarpManager manager;

    public DeleteConfirmGUI(JEasyWarps plugin, WarpManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void open(Player p, String warpKey) {
        var data = manager.getWarps().get(warpKey);
        String display = manager.getDisplay(data, warpKey);

        String title = manager.color(plugin.getConfig().getString("gui.delete-confirm.title", "&cУдалить варп: &e%warp%").replace("%warp%", display));
        Inventory inv = Bukkit.createInventory(null, plugin.getConfig().getInt("gui.delete-confirm.size", 27), title);

        ItemStack filler = createItem("gui.delete-confirm.filler", display);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(plugin.getConfig().getInt("gui.delete-confirm.confirm.slot"), createItem("gui.delete-confirm.confirm", display));
        inv.setItem(plugin.getConfig().getInt("gui.delete-confirm.cancel.slot"), createItem("gui.delete-confirm.cancel", display));

        p.openInventory(inv);
    }

    private ItemStack createItem(String path, String warpDisplay) {
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
                String name = plugin.getConfig().getString(path + ".name");
                meta.setDisplayName(manager.color(name));
            }

            if (plugin.getConfig().contains(path + ".lore")) {
                List<String> lore = new ArrayList<>();
                for (String line : plugin.getConfig().getStringList(path + ".lore")) {
                    lore.add(manager.color(line.replace("%warp%", warpDisplay)));
                }
                meta.setLore(lore);
            }

            item.setItemMeta(meta);
        }
        return item;
    }
}
