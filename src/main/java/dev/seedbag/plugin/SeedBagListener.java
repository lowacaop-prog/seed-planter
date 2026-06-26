package dev.seedbag.plugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
public class SeedBagListener implements Listener {
    private final SeedBagPlugin plugin;
    private final Set<UUID> openGUI = new HashSet<>();
    private final Map<UUID, Material> selectedSeed = new HashMap<>();
    private final Set<UUID> interactCooldown = new HashSet<>();

    // Map seed items to their plantable block
    private static final Map<Material, Material> SEED_TO_CROP = new HashMap<>();
    static {
        SEED_TO_CROP.put(Material.WHEAT_SEEDS, Material.WHEAT);
        SEED_TO_CROP.put(Material.CARROT, Material.CARROTS);
        SEED_TO_CROP.put(Material.POTATO, Material.POTATOES);
        SEED_TO_CROP.put(Material.BEETROOT_SEEDS, Material.BEETROOTS);
        SEED_TO_CROP.put(Material.NETHER_WART, Material.NETHER_WART);
        SEED_TO_CROP.put(Material.MELON_SEEDS, Material.MELON_STEM);
        SEED_TO_CROP.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM);
    }

    // Required soil for each crop
    private static final Map<Material, Material> CROP_TO_SOIL = new HashMap<>();
    static {
        CROP_TO_SOIL.put(Material.WHEAT, Material.FARMLAND);
        CROP_TO_SOIL.put(Material.CARROTS, Material.FARMLAND);
        CROP_TO_SOIL.put(Material.POTATOES, Material.FARMLAND);
        CROP_TO_SOIL.put(Material.BEETROOTS, Material.FARMLAND);
        CROP_TO_SOIL.put(Material.MELON_STEM, Material.FARMLAND);
        CROP_TO_SOIL.put(Material.PUMPKIN_STEM, Material.FARMLAND);
        CROP_TO_SOIL.put(Material.NETHER_WART, Material.SOUL_SAND);
    }

    public SeedBagListener(SeedBagPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        boolean hasDiamond = false;
        int uniqueSeedCount = 0;
        int totalItems = 0;
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            totalItems++;
            if (item.getType() == Material.DIAMOND_HOE) { hasDiamond = true; continue; }
            if (SeedBagItems.isUniqueSeed(item)) uniqueSeedCount++;
        }
        // Check for infinite water bucket (3 water buckets + iron block with 32 amount)
        int waterBuckets = 0;
        boolean hasIronBlock32 = false;
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (item.getType() == Material.WATER_BUCKET) waterBuckets++;
            if (item.getType() == Material.IRON_BLOCK && item.getAmount() >= 32) hasIronBlock32 = true;
        }
        if (waterBuckets == 3 && hasIronBlock32) {
            event.getInventory().setResult(SeedBagItems.createInfiniteWaterBucket());
            return;
        }
        if (hasDiamond && uniqueSeedCount == 8) {
            event.getInventory().setResult(SeedBagItems.createFiveHoe());
            return;
        }
        if (uniqueSeedCount == 9 && !hasDiamond) {
            event.getInventory().setResult(SeedBagItems.createSeedBag());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack result = event.getRecipe().getResult();

        // Unique Seed — require exactly 64 of each ingredient, consume manually
        if (SeedBagItems.isUniqueSeed(result)) {
            ItemStack[] matrix = event.getInventory().getMatrix();
            // Check all slots have 64
            for (ItemStack item : matrix) {
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getAmount() < 64) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You need 64 of each ingredient!");
                    return;
                }
            }
            // Cancel default crafting and manually consume 64 of each
            event.setCancelled(true);
            for (int i = 0; i < matrix.length; i++) {
                ItemStack item = matrix[i];
                if (item == null || item.getType() == Material.AIR) continue;
                item.setAmount(item.getAmount() - 64);
                event.getInventory().setItem(i, item.getAmount() <= 0 ? null : item);
            }
            // Give result
            player.getInventory().addItem(SeedBagItems.createUniqueSeed());
            player.sendMessage(ChatColor.GOLD + "✔ You crafted a Unique Seed!");
        }

        // Infinite Water Bucket — require 3 water buckets + 32 iron blocks
        if (SeedBagItems.isInfiniteWaterBucket(result)) {
            ItemStack[] matrix = event.getInventory().getMatrix();
            boolean hasEnoughIron = false;
            for (ItemStack item : matrix) {
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getType() == Material.IRON_BLOCK && item.getAmount() >= 32) { hasEnoughIron = true; break; }
            }
            if (!hasEnoughIron) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You need 32 Iron Blocks in the center!");
                return;
            }
            event.setCancelled(true);
            for (int i = 0; i < matrix.length; i++) {
                ItemStack item = matrix[i];
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getType() == Material.IRON_BLOCK) {
                    item.setAmount(item.getAmount() - 32);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                event.getInventory().setItem(i, item.getAmount() <= 0 ? null : item);
            }
            player.getInventory().addItem(SeedBagItems.createInfiniteWaterBucket());
            player.sendMessage(ChatColor.AQUA + "✔ You crafted an Infinite Water Bucket!");
        }

        // 5x5 Hoe — require 8 Unique Seeds around a diamond, consume manually
        if (SeedBagItems.isFiveHoe(result)) {
            ItemStack[] matrix = event.getInventory().getMatrix();
            boolean hasDiamond = false;
            for (ItemStack item : matrix) {
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.getType() == Material.DIAMOND_HOE) { hasDiamond = true; continue; }
                if (!SeedBagItems.isUniqueSeed(item)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You need 8 Unique Seeds and 1 Diamond!");
                    return;
                }
            }
            if (!hasDiamond) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You need a Diamond in the center!");
                return;
            }
            // Cancel and manually consume
            event.setCancelled(true);
            for (int i = 0; i < matrix.length; i++) {
                ItemStack item = matrix[i];
                if (item == null || item.getType() == Material.AIR) continue;
                item.setAmount(item.getAmount() - 1);
                event.getInventory().setItem(i, item.getAmount() <= 0 ? null : item);
            }
            player.getInventory().addItem(SeedBagItems.createFiveHoe());
            player.sendMessage(ChatColor.AQUA + "✔ You crafted a 5x5 Hoe!");
        }

        // Seed Bag — require 9 Unique Seeds
        if (SeedBagItems.isSeedBag(result)) {
            for (ItemStack item : event.getInventory().getMatrix()) {
                if (item == null || item.getType() == Material.AIR) continue;
                if (!SeedBagItems.isUniqueSeed(item)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "You need 9 Unique Seeds to craft the Seed Bag!");
                    return;
                }
            }
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SeedBagItems.isSeedBag(item)) return;
        if (interactCooldown.contains(player.getUniqueId())) return;
        interactCooldown.add(player.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> interactCooldown.remove(player.getUniqueId()), 2L);
        event.setCancelled(true);
        openGUI.add(player.getUniqueId());
        player.openInventory(buildSeedPickerGUI(player));
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SeedBagItems.isSeedBag(item)) return;
        if (interactCooldown.contains(player.getUniqueId())) return;
        interactCooldown.add(player.getUniqueId());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> interactCooldown.remove(player.getUniqueId()), 2L);
        event.setCancelled(true);
        Material lastSeed = selectedSeed.get(player.getUniqueId());
        if (lastSeed == null) {
            player.sendMessage(ChatColor.RED + "Left-click to select a seed first!");
            return;
        }
        plantSeeds(player, lastSeed);
    }

    private org.bukkit.inventory.Inventory buildSeedPickerGUI(Player player) {
        org.bukkit.inventory.Inventory inv = Bukkit.createInventory(null, 27, "§2Select a Seed");
        int slot = 0;
        for (Material seed : SEED_TO_CROP.keySet()) {
            ItemStack display = new ItemStack(seed);
            ItemMeta meta = display.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + formatName(seed.name()));
            meta.setLore(Arrays.asList(
                ChatColor.GRAY + "Click to plant 5x5",
                ChatColor.GRAY + "Consumes seeds from your inventory"
            ));
            display.setItemMeta(meta);
            inv.setItem(slot++, display);
        }
        // Filler
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = slot; i < 27; i++) inv.setItem(i, filler);
        return inv;
    }

    @EventHandler
    public void onGUIClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!openGUI.contains(player.getUniqueId())) return;
        if (!event.getView().getTitle().equals("§2Select a Seed")) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        Material seed = clicked.getType();
        if (!SEED_TO_CROP.containsKey(seed)) return;
        selectedSeed.put(player.getUniqueId(), seed);
        player.closeInventory();
        player.sendMessage(ChatColor.GREEN + "✔ Selected " + formatName(seed.name()) + "! Right-click with the Seed Bag to plant.");
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        openGUI.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onWaterBucketUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SeedBagItems.isInfiniteWaterBucket(item)) return;
        if (event.getClickedBlock() == null) return;
        event.setCancelled(true);
        // Place water on the block face clicked
        Block target = event.getClickedBlock().getRelative(event.getBlockFace());
        if (target.getType() == Material.AIR || target.getType().isAir()) {
            target.setType(Material.WATER);
            player.getWorld().playSound(target.getLocation(), Sound.ITEM_BUCKET_EMPTY, 1f, 1f);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onHoeUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!SeedBagItems.isFiveHoe(item)) return;
        if (event.getClickedBlock() == null) return;
        event.setCancelled(true);

        Block center = event.getClickedBlock();
        int tilled = 0;
        int radius = 2;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block block = center.getRelative(x, 0, z);
                if (block.getType() == Material.DIRT || block.getType() == Material.GRASS_BLOCK ||
                    block.getType() == Material.DIRT_PATH || block.getType() == Material.COARSE_DIRT) {
                    Block above = block.getRelative(BlockFace.UP);
                    if (above.getType() == Material.AIR) {
                        block.setType(Material.FARMLAND);
                        tilled++;
                    }
                }
            }
        }
        if (tilled > 0) {
            player.sendMessage(ChatColor.AQUA + "✔ Tilled " + tilled + " blocks!");
            player.getWorld().playSound(center.getLocation(), Sound.ITEM_HOE_TILL, 1f, 1f);
            player.getWorld().spawnParticle(Particle.BLOCK, center.getLocation().add(0.5, 1, 0.5),
                20, 2, 0, 2, 0, Material.DIRT.createBlockData());
        }
    }

    private void plantSeeds(Player player, Material seed) {
        Material crop = SEED_TO_CROP.get(seed);
        Material requiredSoil = CROP_TO_SOIL.get(crop);
        if (requiredSoil == null) return;

        // Find the block the player is looking at
        Block targetBlock = player.getTargetBlockExact(10);
        if (targetBlock == null) {
            player.sendMessage(ChatColor.RED + "Look at a farming area to plant!");
            return;
        }

        int planted = 0;
        int radius = 2; // 5x5
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block soil = targetBlock.getRelative(x, 0, z);
                Block above = soil.getRelative(BlockFace.UP);
                if (soil.getType() != requiredSoil) continue;
                if (above.getType() != Material.AIR) continue;
                // Check player has enough seeds
                if (!hasSeed(player, seed)) break;
                // Plant
                above.setType(crop);
                if (above.getBlockData() instanceof Ageable ageable) {
                    ageable.setAge(0);
                    above.setBlockData(ageable);
                }
                consumeSeed(player, seed);
                planted++;
                // Particle effect
                above.getWorld().spawnParticle(Particle.HAPPY_VILLAGER,
                    above.getLocation().add(0.5, 0.5, 0.5), 3, 0.3, 0.3, 0.3, 0);
            }
        }
        if (planted > 0) {
            player.sendMessage(ChatColor.GREEN + "✔ Planted " + planted + " " + formatName(seed.name()) + "!");
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_CROP_PLANT, 1f, 1f);
        } else {
            player.sendMessage(ChatColor.RED + "No valid soil found or not enough seeds!");
        }
    }

    private boolean hasSeed(Player player, Material seed) {
        return player.getInventory().containsAtLeast(new ItemStack(seed), 1);
    }

    private void consumeSeed(Player player, Material seed) {
        player.getInventory().removeItem(new ItemStack(seed, 1));
    }

    private String formatName(String name) {
        String[] words = name.toLowerCase().replace("_", " ").split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
        return sb.toString().trim();
    }
}
