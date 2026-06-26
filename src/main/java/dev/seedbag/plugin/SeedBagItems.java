package dev.seedbag.plugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;
public class SeedBagItems {
    public static final NamespacedKey UNIQUE_SEED_KEY = new NamespacedKey(SeedBagPlugin.getInstance(), "unique_seed");
    public static final NamespacedKey SEED_BAG_KEY = new NamespacedKey(SeedBagPlugin.getInstance(), "seed_bag");

    public static ItemStack createUniqueSeed() {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Unique Seed");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "A magical seed imbued with nature's power.",
            ChatColor.GRAY + "Used to craft a Seed Bag."
        ));
        meta.getPersistentDataContainer().set(UNIQUE_SEED_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack createSeedBag() {
        ItemStack item = new ItemStack(Material.BUNDLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Seed Bag");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click to select a seed and plant",
            ChatColor.GRAY + "a 5x5 area instantly!",
            "",
            ChatColor.GREEN + "Infinite uses"
        ));
        meta.getPersistentDataContainer().set(SEED_BAG_KEY, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isUniqueSeed(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(UNIQUE_SEED_KEY, PersistentDataType.BYTE);
    }

    public static final NamespacedKey HOE_KEY = new NamespacedKey(SeedBagPlugin.getInstance(), "five_hoe");

    public static ItemStack createFiveHoe() {
        ItemStack item = new ItemStack(Material.DIAMOND_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "5x5 Hoe");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click to till a 5x5 area.",
            "",
            ChatColor.AQUA + "Infinite uses"
        ));
        meta.getPersistentDataContainer().set(HOE_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static final NamespacedKey WATER_KEY = new NamespacedKey(SeedBagPlugin.getInstance(), "infinite_water");

    public static ItemStack createInfiniteWaterBucket() {
        ItemStack item = new ItemStack(Material.WATER_BUCKET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Infinite Water Bucket");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Right-click to place water infinitely.",
            ChatColor.GRAY + "Never runs out!",
            "",
            ChatColor.AQUA + "Infinite uses"
        ));
        meta.getPersistentDataContainer().set(WATER_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.setUnbreakable(true);
        meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isInfiniteWaterBucket(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(WATER_KEY, PersistentDataType.BYTE);
    }

    public static boolean isFiveHoe(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(HOE_KEY, PersistentDataType.BYTE);
    }

    public static boolean isSeedBag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(SEED_BAG_KEY, PersistentDataType.BYTE);
    }
}
