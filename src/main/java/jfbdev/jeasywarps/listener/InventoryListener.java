package jfbdev.jeasywarps.listener;

import jfbdev.jeasywarps.JEasyWarps;
import jfbdev.jeasywarps.data.WarpData;
import jfbdev.jeasywarps.gui.DeleteConfirmGUI;
import jfbdev.jeasywarps.gui.MainGUI;
import jfbdev.jeasywarps.gui.RateGUI;
import jfbdev.jeasywarps.manager.WarpManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final JEasyWarps plugin;
    private final WarpManager manager;
    private final MainGUI mainGUI;
    private final RateGUI rateGUI;
    private final DeleteConfirmGUI deleteConfirmGUI;
    private final Map<UUID, String> pendingRate = new HashMap<>();
    private final Map<UUID, String> pendingDelete = new HashMap<>();

    public InventoryListener(JEasyWarps plugin, WarpManager manager, MainGUI mainGUI, RateGUI rateGUI, DeleteConfirmGUI deleteConfirmGUI) {
        this.plugin = plugin;
        this.manager = manager;
        this.mainGUI = mainGUI;
        this.rateGUI = rateGUI;
        this.deleteConfirmGUI = deleteConfirmGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        String mainTitle = manager.color(plugin.getConfig().getString("gui.main.title", "&6✦ Варпы ✦"));
        if (title.equals(mainTitle)) {
            e.setCancelled(true);
            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
            var meta = e.getCurrentItem().getItemMeta();
            if (meta == null || !meta.hasDisplayName()) return;

            String displayClean = manager.stripColors(meta.getDisplayName());
            WarpData data = manager.getWarps().entrySet().stream()
                    .filter(entry -> manager.stripColors(manager.getDisplay(entry.getValue(), entry.getKey())).equalsIgnoreCase(displayClean))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (data == null) return;

            String key = manager.getKey(data);

            if (e.getClick().isRightClick()) {
                pendingRate.put(p.getUniqueId(), key);
                rateGUI.open(p, key);
            } else if (e.isShiftClick() && data.owner.equals(p.getUniqueId())) {
                pendingDelete.put(p.getUniqueId(), key);
                deleteConfirmGUI.open(p, key);
            } else {
                p.teleport(data.location);
                p.closeInventory();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.teleport-success", "&aТелепортация на варп &e%warp%&a!"))
                        .replace("%warp%", manager.color(manager.getDisplay(data, key))));
            }
            return;
        }

        String deleteTitlePattern = plugin.getConfig().getString("gui.delete-confirm.title", "&cУдалить варп: &e%warp%");
        String deletePrefix = manager.color(deleteTitlePattern.replace("%warp%", ""));

        if (title.startsWith(deletePrefix)) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            String warpKey = pendingDelete.remove(p.getUniqueId());
            if (warpKey == null) {
                p.closeInventory();
                return;
            }

            WarpData data = manager.getWarps().get(warpKey);
            if (data == null) {
                p.closeInventory();
                return;
            }

            Material confirmMat = Material.matchMaterial(plugin.getConfig().getString("gui.delete-confirm.confirm.material", "LIME_CONCRETE").toUpperCase());
            Material cancelMat = Material.matchMaterial(plugin.getConfig().getString("gui.delete-confirm.cancel.material", "RED_CONCRETE").toUpperCase());

            if (e.getCurrentItem().getType() == confirmMat) {
                manager.getWarps().remove(warpKey);
                manager.saveWarps();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-deleted", "&cВарп &e%warp% &cудалён."))
                        .replace("%warp%", manager.color(manager.getDisplay(data, warpKey))));
                p.closeInventory();
                mainGUI.open(p);
            } else if (e.getCurrentItem().getType() == cancelMat) {
                p.closeInventory();
                mainGUI.open(p);
            }
            return;
        }

        String rateTitlePattern = plugin.getConfig().getString("gui.rate.title", "&6Оценить варп: &e%warp%");
        String ratePrefix = manager.color(rateTitlePattern.replace("%warp%", ""));

        if (title.startsWith(ratePrefix)) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

            Material backMat = Material.matchMaterial(plugin.getConfig().getString("gui.rate.back-item.material", "ARROW").toUpperCase());
            if (backMat != null && e.getCurrentItem().getType() == backMat) {
                mainGUI.open(p);
                return;
            }

            if (e.getCurrentItem().getType() == Material.matchMaterial(plugin.getConfig().getString("gui.rate.stars.1.material", "GOLD_NUGGET").toUpperCase())
                    && e.getCurrentItem().getAmount() >= 1 && e.getCurrentItem().getAmount() <= 5) {
                int stars = e.getCurrentItem().getAmount();

                String warpKey = pendingRate.remove(p.getUniqueId());
                if (warpKey == null) {
                    mainGUI.open(p);
                    return;
                }

                WarpData data = manager.getWarps().get(warpKey);
                if (data == null) {
                    mainGUI.open(p);
                    return;
                }

                if (data.owner.equals(p.getUniqueId())) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.cannot-rate-own", "&cВы не можете оценивать свой варп!")));
                    p.closeInventory();
                    mainGUI.open(p);
                    return;
                }

                if (data.ratings.containsKey(p.getUniqueId())) {
                    p.sendMessage(manager.color(plugin.getConfig().getString("messages.already-rated", "&cВы уже оценили этот варп.")));
                    p.closeInventory();
                    mainGUI.open(p);
                    return;
                }

                data.ratings.put(p.getUniqueId(), stars);
                manager.saveWarps();
                p.sendMessage(manager.color(plugin.getConfig().getString("messages.warp-rated", "&aВы поставили &e%stars%★ &aварпу &e%warp%"))
                        .replace("%warp%", manager.color(manager.getDisplay(data, warpKey)))
                        .replace("%stars%", String.valueOf(stars)));
                p.closeInventory();
                mainGUI.open(p);
            }
        }
    }
}