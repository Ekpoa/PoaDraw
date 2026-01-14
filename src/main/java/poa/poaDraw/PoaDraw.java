package poa.poaDraw;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import poa.poaDraw.commands.DrawCMD;
import poa.poaDraw.commands.GlobeCMD;
import poa.poaDraw.events.Click;
import poa.poaDraw.events.Leave;

public final class PoaDraw extends JavaPlugin {

    public static PoaDraw INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;

        getCommand("draw").setExecutor(new DrawCMD());
        getCommand("globe").setExecutor(new GlobeCMD());

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new Click(), this);
        pm.registerEvents(new Leave(), this);
    }

    @Override
    public void onDisable() {
    }
}
