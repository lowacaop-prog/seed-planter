package dev.seedbag.plugin;
import org.bukkit.plugin.java.JavaPlugin;
public class SeedBagPlugin extends JavaPlugin {
    private static SeedBagPlugin instance;
    @Override
    public void onEnable() {
        instance = this;
        getServer().getPluginManager().registerEvents(new SeedBagListener(this), this);
        new SeedBagRecipe().register();
        getLogger().info("SeedBag enabled!");
    }
    @Override
    public void onDisable() { getLogger().info("SeedBag disabled."); }
    public static SeedBagPlugin getInstance() { return instance; }
}
