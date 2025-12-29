package jfbdev.jeasywarps;

import jfbdev.jeasywarps.command.AdminCommands;
import jfbdev.jeasywarps.command.PlayerCommands;
import jfbdev.jeasywarps.gui.DeleteConfirmGUI;
import jfbdev.jeasywarps.gui.MainGUI;
import jfbdev.jeasywarps.gui.RateGUI;
import jfbdev.jeasywarps.manager.WarpManager;
import jfbdev.jeasywarps.listener.InventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class JEasyWarps extends JavaPlugin {

    private WarpManager warpManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        warpManager = new WarpManager(this);
        warpManager.loadWarps();

        MainGUI mainGUI = new MainGUI(this, warpManager);
        RateGUI rateGUI = new RateGUI(this, warpManager);
        DeleteConfirmGUI deleteConfirmGUI = new DeleteConfirmGUI(this, warpManager);

        getServer().getPluginManager().registerEvents(new InventoryListener(this, warpManager, mainGUI, rateGUI, deleteConfirmGUI), this);

        new PlayerCommands(this, warpManager, mainGUI);
        new AdminCommands(this, warpManager);
    }

    @Override
    public void onDisable() {
        warpManager.saveWarps();
    }
}