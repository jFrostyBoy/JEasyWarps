package jfbdev.jeasywarps.data;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarpData {
    public Location location;
    public UUID owner;
    public String lore = "";
    public String displayName;
    public String iconMaterial;
    public final Map<UUID, Integer> ratings = new HashMap<>();
}