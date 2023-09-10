package me.alex4386.plugin.kamikaze;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {
    public static JavaPlugin plugin;
    public KamikazeListener kamikazeListener = new KamikazeListener();

    @Override
    public void onEnable() {
        // Plugin startup logic
        Main.plugin = this;
        this.getLogger().info("KAMIKAZE Activated!");

        this.getServer().getPluginManager().registerEvents(kamikazeListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        this.getLogger().info("KAMIKAZE Deactivated!");

    }
}
