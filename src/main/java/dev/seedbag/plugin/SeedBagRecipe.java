package dev.seedbag.plugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
public class SeedBagRecipe {
    public void register() {
        // Unique Seed — shapeless, 1 of each (64 enforced in CraftItemEvent)
        NamespacedKey uniqueSeedKey = new NamespacedKey(SeedBagPlugin.getInstance(), "unique_seed_recipe");
        ShapelessRecipe uniqueSeedRecipe = new ShapelessRecipe(uniqueSeedKey, SeedBagItems.createUniqueSeed());
        uniqueSeedRecipe.addIngredient(Material.HAY_BLOCK);
        uniqueSeedRecipe.addIngredient(Material.POTATO);
        uniqueSeedRecipe.addIngredient(Material.GOLDEN_CARROT);
        uniqueSeedRecipe.addIngredient(Material.MELON);
        uniqueSeedRecipe.addIngredient(Material.PUMPKIN);
        uniqueSeedRecipe.addIngredient(Material.BEETROOT);
        uniqueSeedRecipe.addIngredient(Material.NETHER_WART_BLOCK);
        uniqueSeedRecipe.addIngredient(Material.SUGAR_CANE);
        uniqueSeedRecipe.addIngredient(Material.CACTUS);
        SeedBagPlugin.getInstance().getServer().addRecipe(uniqueSeedRecipe);

        // Seed Bag — 9x Unique Seed (enforced in CraftItemEvent)
        NamespacedKey seedBagKey = new NamespacedKey(SeedBagPlugin.getInstance(), "seed_bag_recipe");
        ShapelessRecipe seedBagRecipe = new ShapelessRecipe(seedBagKey, SeedBagItems.createSeedBag());
        for (int i = 0; i < 9; i++) seedBagRecipe.addIngredient(Material.WHEAT_SEEDS);
        SeedBagPlugin.getInstance().getServer().addRecipe(seedBagRecipe);

        // 5x5 Hoe — Diamond center, 8 Unique Seeds around it (enforced via CraftItemEvent)
        NamespacedKey hoeKey = new NamespacedKey(SeedBagPlugin.getInstance(), "five_hoe_recipe");
        ShapedRecipe hoeRecipe = new ShapedRecipe(hoeKey, SeedBagItems.createFiveHoe());
        hoeRecipe.shape("SSS", "SDS", "SSS");
        hoeRecipe.setIngredient('S', org.bukkit.Material.WHEAT_SEEDS);
        hoeRecipe.setIngredient('D', org.bukkit.Material.DIAMOND_HOE);
        SeedBagPlugin.getInstance().getServer().addRecipe(hoeRecipe);

        // Infinite Water Bucket
        NamespacedKey waterKey = new NamespacedKey(SeedBagPlugin.getInstance(), "infinite_water_recipe");
        ShapedRecipe waterRecipe = new ShapedRecipe(waterKey, SeedBagItems.createInfiniteWaterBucket());
        waterRecipe.shape("   ", "WIW", " W ");
        waterRecipe.setIngredient('W', org.bukkit.Material.WATER_BUCKET);
        waterRecipe.setIngredient('I', org.bukkit.Material.IRON_BLOCK);
        SeedBagPlugin.getInstance().getServer().addRecipe(waterRecipe);
    }
}
