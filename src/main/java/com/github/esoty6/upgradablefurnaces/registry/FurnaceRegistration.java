package com.github.esoty6.upgradablefurnaces.registry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import com.github.esoty6.upgradablefurnaces.block.FurnaceListener;
import com.github.esoty6.upgradablefurnaces.block.UpgradableFurnace;
import com.github.esoty6.upgradablefurnaces.config.FurnaceConfig;
import com.github.esoty6.upgradablefurnaces.constants.Key;
import com.github.esoty6.upgradablefurnaces.constants.NamespacedKeys;
import com.github.esoty6.upgradablefurnaces.constants.Upgrade;
import net.md_5.bungee.api.ChatColor;

public class FurnaceRegistration {

  private static final Set<Material> MATERIALS =
      Set.of(Material.FURNACE, Material.BLAST_FURNACE, Material.SMOKER);
  protected final Plugin plugin;
  private final Listener listener;
  private FurnaceConfig config;

  public FurnaceRegistration(Plugin plugin, UpgradableFurnaceManager manager) {
    this.plugin = plugin;
    listener = new FurnaceListener(manager);
    plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    getConfig();
  }

  public FurnaceConfig getConfig() {
    if (config == null) {
      config = loadFullConfig(plugin.getConfig());
    }

    return config;
  }

  protected final FurnaceConfig loadFullConfig(FileConfiguration fileConfiguration) {
    String path = "furnaceUpgrades";
    ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(path);

    if (configurationSection == null) {
      configurationSection = fileConfiguration.createSection(path);
    }

    return new FurnaceConfig(configurationSection);
  }

  public UpgradableFurnace newBlock(Block block, ItemStack itemStack,
      ConfigurationSection storage) {
    return new UpgradableFurnace(this, block, itemStack, storage);
  }

  public UpgradableFurnace upgradeFurnace(UpgradableFurnace furnace, Map<Upgrade, Double> ar,
      Block block, Player player) {

    ItemStack newItemStack = new ItemStack(furnace.getBlock().getType());
    ItemMeta newItemMeta = newItemStack.getItemMeta();

    furnace.getItemStack().getItemMeta().getPersistentDataContainer()
        .copyTo(newItemMeta.getPersistentDataContainer(), true);


    for (Entry<Upgrade, Double> a : ar.entrySet()) {
      if (!furnace.getConfig().isUpgradeEnabled(a.getKey())) {
        plugin.getServer().getLogger().info(a.getKey().getName() + " not enabled");
        continue;
      }

      if (a.getValue() == null) {
        continue;
      }


      Boolean hasMeta = newItemMeta.getPersistentDataContainer().has(
          NamespacedKeys.getData(a.getKey(), Key.CURRENT_LEVEL_KEY), PersistentDataType.INTEGER);

      if (!hasMeta) {
        saveData(newItemMeta, a.getKey(), Key.NEXT_LEVEL_KEY, PersistentDataType.DOUBLE,
            furnace.getConfig().getBaseNextLevel());
        saveData(newItemMeta, a.getKey(), Key.LEVEL_PROGRESS_KEY, PersistentDataType.DOUBLE, 0d);
        saveData(newItemMeta, a.getKey(), Key.CURRENT_LEVEL_KEY, PersistentDataType.INTEGER, 0);
      }

      Integer currentLevel =
          getStoredData(newItemMeta, a.getKey(), Key.CURRENT_LEVEL_KEY, PersistentDataType.INTEGER);

      if (furnace.getConfig().getMaxLevel(a.getKey()) <= currentLevel) {
        continue;
      }

      Double levelProgress =
          getStoredData(newItemMeta, a.getKey(), Key.LEVEL_PROGRESS_KEY, PersistentDataType.DOUBLE)
              + a.getValue();

      Double nextLevel =
          getStoredData(newItemMeta, a.getKey(), Key.NEXT_LEVEL_KEY, PersistentDataType.DOUBLE);

      levelProgress =
          BigDecimal.valueOf(levelProgress).setScale(2, RoundingMode.HALF_UP).doubleValue();
      nextLevel = BigDecimal.valueOf(nextLevel).setScale(2, RoundingMode.HALF_UP).doubleValue();

      while (levelProgress >= nextLevel) {
        levelProgress = BigDecimal.valueOf(levelProgress - nextLevel)
            .setScale(2, RoundingMode.HALF_UP).doubleValue();
        nextLevel =
            BigDecimal.valueOf(nextLevel * furnace.getConfig().getLevelMultiplier(a.getKey()))
                .setScale(2, RoundingMode.HALF_UP).doubleValue();

        currentLevel += 1;
        if (furnace.getConfig().getMaxLevel(a.getKey()) <= currentLevel) {
          furnace.maxLevelAcquired(a.getKey(), player);
          break;
        }
      }

      saveData(newItemMeta, a.getKey(), Key.NEXT_LEVEL_KEY, PersistentDataType.DOUBLE, nextLevel);
      saveData(newItemMeta, a.getKey(), Key.LEVEL_PROGRESS_KEY, PersistentDataType.DOUBLE,
          levelProgress);
      saveData(newItemMeta, a.getKey(), Key.CURRENT_LEVEL_KEY, PersistentDataType.INTEGER,
          currentLevel);

      if (furnace.getConfig().getMaxLevel(a.getKey()) <= currentLevel) {
        saveData(newItemMeta, a.getKey(), Key.NEXT_LEVEL_KEY, PersistentDataType.DOUBLE, 0d);
        saveData(newItemMeta, a.getKey(), Key.CURRENT_LEVEL_KEY, PersistentDataType.INTEGER,
            furnace.getConfig().getMaxLevel(a.getKey()));
      }

      block.setType(Material.AIR);
    }

    ArrayList<String> furnaceItemLore = new ArrayList<String>();
    StringBuilder itemLore = new StringBuilder();

    for (Upgrade upgrade : Upgrade.values()) {
      Integer currentLevel =
          getStoredData(newItemMeta, upgrade, Key.CURRENT_LEVEL_KEY, PersistentDataType.INTEGER);
      Double nextLevel =
          getStoredData(newItemMeta, upgrade, Key.NEXT_LEVEL_KEY, PersistentDataType.DOUBLE);
      Double levelProgress =
          getStoredData(newItemMeta, upgrade, Key.LEVEL_PROGRESS_KEY, PersistentDataType.DOUBLE);

      if (currentLevel == null) {
        continue;
      }

      itemLore = new StringBuilder();
      itemLore.append(ChatColor.DARK_AQUA);
      itemLore.append(upgrade.getName() + ": " + currentLevel + " | ");

      if (currentLevel == getConfig().getMaxLevel(upgrade)) {
        itemLore.append(ChatColor.RED);
        itemLore.append(ChatColor.BOLD);
        itemLore.append("MAX");
      } else {
        itemLore.append(ChatColor.YELLOW);
        itemLore.append("Next level: ");
        itemLore.append(levelProgress);
        itemLore.append(" / ");
        itemLore.append(nextLevel);
      }

      furnaceItemLore.add(itemLore.toString());
    }

    newItemMeta.setLore(furnaceItemLore);
    newItemStack.setItemMeta(newItemMeta);
    furnace.setItemStack(newItemStack);

    return furnace;
  }

  public Collection<Material> getMaterials() {
    return MATERIALS;
  }

  protected void reload() {
    config = null;
  }

  public <T> T getStoredData(ItemMeta itemMeta, Upgrade upgrade, Key key,
      PersistentDataType<T, T> pdt) {
    return itemMeta.getPersistentDataContainer().get(NamespacedKeys.getData(upgrade, key), pdt);
  }

  public <T> void saveData(ItemMeta itemMeta, Upgrade upgrade, Key key,
      PersistentDataType<T, T> pdt, T value) {
    itemMeta.getPersistentDataContainer().set(NamespacedKeys.getData(upgrade, key), pdt, value);
  }
}
