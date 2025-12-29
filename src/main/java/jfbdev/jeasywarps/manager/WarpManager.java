package jfbdev.jeasywarps.manager;

import jfbdev.jeasywarps.JEasyWarps;
import jfbdev.jeasywarps.data.WarpData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class WarpManager {

    private final JEasyWarps plugin;
    private final File warpsFile;
    private final YamlConfiguration warpsData;
    private final Map<String, WarpData> warps = new HashMap<>();
    private final DecimalFormat df = new DecimalFormat("#.#");

    public WarpManager(JEasyWarps plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");

        if (!plugin.getDataFolder().exists()) {
            if (plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().info("Папка плагина создана: " + plugin.getDataFolder().getPath());
            }
        }

        if (!warpsFile.exists()) {
            try {
                if (warpsFile.createNewFile()) {
                    plugin.getLogger().info("Файл warps.yml успешно создан.");
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать файл warps.yml: " + e.getMessage());
                plugin.getLogger().warning("Причина ошибки:");
                e.getStackTrace();
            }
        }
        this.warpsData = YamlConfiguration.loadConfiguration(warpsFile);
    }

    public void loadWarps() {
        warps.clear();
        ConfigurationSection sec = warpsData.getConfigurationSection("warps");
        if (sec == null) return;

        for (String key : sec.getKeys(false)) {
            ConfigurationSection w = sec.getConfigurationSection(key);
            if (w == null) continue;

            WarpData data = new WarpData();
            String worldName = w.getString("world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) continue;

            data.location = new Location(Bukkit.getWorld(worldName),
                    w.getDouble("x"), w.getDouble("y"), w.getDouble("z"),
                    (float) w.getDouble("yaw"), (float) w.getDouble("pitch"));
            data.owner = UUID.fromString(Objects.requireNonNull(w.getString("owner")));
            data.lore = w.getString("lore", "");
            data.displayName = w.getString("display-name", key);
            data.iconMaterial = w.getString("icon-material");

            ConfigurationSection ratingsSec = w.getConfigurationSection("ratings");
            if (ratingsSec != null) {
                for (String uuid : ratingsSec.getKeys(false)) {
                    data.ratings.put(UUID.fromString(uuid), ratingsSec.getInt(uuid));
                }
            }
            warps.put(key, data);
        }
    }

    public void saveWarps() {
        warpsData.set("warps", null);
        for (Map.Entry<String, WarpData> e : warps.entrySet()) {
            String key = e.getKey();
            WarpData d = e.getValue();
            String path = "warps." + key + ".";
            warpsData.set(path + "world", d.location.getWorld().getName());
            warpsData.set(path + "x", d.location.getX());
            warpsData.set(path + "y", d.location.getY());
            warpsData.set(path + "z", d.location.getZ());
            warpsData.set(path + "yaw", d.location.getYaw());
            warpsData.set(path + "pitch", d.location.getPitch());
            warpsData.set(path + "owner", d.owner.toString());
            warpsData.set(path + "lore", d.lore);
            warpsData.set(path + "display-name", d.displayName);
            warpsData.set(path + "icon-material", d.iconMaterial);

            ConfigurationSection ratingsSec = warpsData.createSection(path + "ratings");
            for (Map.Entry<UUID, Integer> r : d.ratings.entrySet()) {
                ratingsSec.set(r.getKey().toString(), r.getValue());
            }
        }
        try {
            warpsData.save(warpsFile);
        } catch (IOException ex) {
            plugin.getLogger().severe("Не удалось сохранить warps.yml!");
        }
    }

    public String stripColors(String s) {
        return ChatColor.stripColor(color(s)).trim();
    }

    public String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public WarpData findWarp(String input) {
        String clean = stripColors(input);
        if (clean.isEmpty()) return null;
        return warps.entrySet().stream()
                .filter(e -> stripColors(e.getKey()).equalsIgnoreCase(clean))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public String getKey(WarpData data) {
        return warps.entrySet().stream()
                .filter(e -> e.getValue() == data)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public String getDisplay(WarpData data, String fallback) {
        return data.displayName != null ? data.displayName : fallback;
    }

    public String getRatingStars(WarpData data) {
        if (data.ratings.isEmpty()) return "&7☆☆☆☆☆ &7(0)";
        double avg = data.ratings.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        int full = (int) avg;
        boolean half = (avg - full) >= 0.5;
        int empty = 5 - full - (half ? 1 : 0);
        return "★".repeat(full) + (half ? "✭" : "") + "☆".repeat(empty) + " &7(" + df.format(avg) + ")";
    }

    public Map<String, WarpData> getWarps() {
        return warps;
    }
}