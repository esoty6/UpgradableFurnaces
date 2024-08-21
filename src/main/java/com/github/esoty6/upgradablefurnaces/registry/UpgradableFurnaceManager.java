package com.github.esoty6.upgradablefurnaces.registry;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.github.esoty6.upgradablefurnaces.block.UpgradableBlock;
import com.github.esoty6.upgradablefurnaces.block.UpgradableFurnace;
import com.github.esoty6.upgradablefurnaces.constants.Upgrade;
import com.github.esoty6.upgradablefurnaces.util.Cache;
import com.github.esoty6.upgradablefurnaces.util.Region;
import com.github.esoty6.upgradablefurnaces.util.RegionStorage;
import com.github.jikoo.planarwrappers.collections.BlockMap;
import com.github.jikoo.planarwrappers.util.Coords;

public class UpgradableFurnaceManager {

  private final Logger logger;
  private final Registry registry;
  private final BlockMap<UpgradableFurnace> furnaceMap;
  private final Cache<Region, RegionStorageData> saveFileCache;

  public UpgradableFurnaceManager(Plugin plugin) {
    this(new Registry(plugin.getLogger()), new Cache.CacheBuilder<>(),
        plugin.getConfig().getInt("autosave", 5), plugin.getDataFolder().toPath().resolve("data"),
        plugin.getLogger());
  }

  UpgradableFurnaceManager(Registry registry,
      Cache.CacheBuilder<Region, RegionStorageData> cacheBuilder, int autoSave, Path dataDir,
      Logger logger) {
    this.furnaceMap = new BlockMap<>();
    this.logger = logger;
    this.registry = registry;
    saveFileCache = cacheBuilder.withRetention(Math.max(autoSave * 60_000L, 60_000L))
        .withInUseCheck(new RegionInUseCheck(logger))
        .withLoadFunction(new RegionLoadFunction(this, dataDir, logger)).build();

  }

  public Registry getRegistry() {
    return registry;
  }

  public UpgradableFurnace getBlock(final Block block) {
    UpgradableFurnace upgradableBlock = furnaceMap.get(block);

    return upgradableBlock;
  }

  public UpgradableFurnace createBlock(final Block block, final ItemStack itemStack,
      final Block blockAgainst, final Player player) {
    if (isInvalidBlock(itemStack)) {
      return null;
    }

    final UpgradableFurnace upgradableBlock = newBlock(block, itemStack, blockAgainst, player);

    if (upgradableBlock == null) {
      return null;
    }

    furnaceMap.put(block, upgradableBlock);

    return upgradableBlock;
  }

  private boolean isInvalidBlock(ItemStack itemStack) {
    return itemStack == null || itemStack.getType().isAir() || !itemStack.getType().isBlock();
  }

  private UpgradableFurnace newBlock(Block block, ItemStack itemStack, Block blockAgainst,
      Player player) {
    var registration = registry.get(itemStack.getType());

    var isUpgradeBlock = registry.getUpgradeBlock(itemStack.getType());

    if (isUpgradeBlock) {
      if (registry.get(blockAgainst.getType()) == null) {
        return null;
      }

      UpgradableFurnace furnace = getBlock(blockAgainst);

      if (furnace == null) {
        furnace = registration.newBlock(blockAgainst, itemStack, getBlockStorage(blockAgainst));
      }

      registration = registry.get(furnace.getBlock().getType());
      Map<Upgrade, Double> ar =
          registry.getBlock(block.getType()).getConfig().getUpgrades(block.getType());

      return registration.upgradeFurnace(furnace, ar, block, player);
    }

    if (registration == null) {
      return null;
    }

    return registration.newBlock(block, itemStack, getBlockStorage(block));
  }

  private ConfigurationSection getBlockStorage(Block block) {
    var chunkStorage = this.getChunkStorage(block);
    var blockPath = getBlockPath(block);

    if (chunkStorage.isConfigurationSection(blockPath)) {
      return Objects.requireNonNull(chunkStorage.getConfigurationSection(blockPath));
    }

    return chunkStorage.createSection(blockPath);
  }

  private ConfigurationSection getChunkStorage(Block block) {
    var storagePair = saveFileCache.get(new Region(block));
    var regionStorage = Objects.requireNonNull(storagePair).getStorage();
    var chunkPath = getChunkPath(block);

    if (regionStorage.isConfigurationSection(chunkPath)) {
      return Objects.requireNonNull(regionStorage.getConfigurationSection(chunkPath));
    }

    return regionStorage.createSection(chunkPath);
  }

  private UpgradableFurnace loadUpgradableBlock(final Block block,
      final ConfigurationSection storage) {

    ItemStack itemStack = storage.getItemStack("itemstack");

    if (isInvalidBlock(itemStack)) {
      return null;
    }

    UpgradableFurnace upgradableBlock = newBlock(block, itemStack, null, null);

    if (upgradableBlock == null || !upgradableBlock.isCorrectBlockType()) {
      return null;
    }

    return upgradableBlock;
  }

  public ItemStack destroyBlock(final Block block) {
    UpgradableFurnace upgradableBlock = furnaceMap.remove(block);

    if (upgradableBlock == null) {
      return null;
    }

    var saveData = this.saveFileCache.get(new Region(block));

    if (saveData == null) {
      return null;
    }

    var chunkPath = getChunkPath(block);

    ItemStack itemStack = upgradableBlock.getItemStack();

    if (!saveData.getStorage().isConfigurationSection(chunkPath)) {
      saveData.getStorage().set(chunkPath, null);
      saveData.setDirty();

      if (!upgradableBlock.isCorrectType(block.getType())) {
        return null;
      }

      return itemStack;
    }

    var chunkSection = saveData.getStorage().getConfigurationSection(chunkPath);
    var blockPath = getBlockPath(block.getX(), block.getY(), block.getZ());

    if (chunkSection != null) {
      chunkSection.set(blockPath, null);

      if (chunkSection.getKeys(false).isEmpty()) {
        saveData.getStorage().set(chunkPath, null);
      }
    }

    saveData.setDirty();

    if (!upgradableBlock.isCorrectType(block.getType())) {
      return null;
    }

    return itemStack;
  }

  public void loadChunkBlocks(final Chunk chunk) {

    RegionStorageData saveData = saveFileCache.get(new Region(chunk), false);

    if (saveData == null) {
      return;
    }

    String path = getChunkPath(chunk);
    ConfigurationSection chunkStorage = saveData.getStorage().getConfigurationSection(path);

    if (chunkStorage == null) {
      return;
    }

    for (String xyz : chunkStorage.getKeys(false)) {
      if (!chunkStorage.isConfigurationSection(xyz)) {
        chunkStorage.set(path, null);
        saveData.setDirty();
        logger.warning(
            () -> String.format("Invalid ConfigurationSection %s: %s", xyz, chunkStorage.get(xyz)));
        continue;
      }

      String itemPath = xyz + ".itemstack";
      String[] split = xyz.split("_");
      Block block;

      if (split.length != 3) {
        chunkStorage.set(xyz, null);
        saveData.setDirty();
        logger.warning(() -> String.format("Unparseable coordinates in %s: %s representing %s",
            chunk.getWorld().getName(), xyz, chunkStorage.getItemStack(itemPath)));
        continue;
      }

      try {
        block = chunk.getWorld().getBlockAt(Integer.parseInt(split[0]), Integer.parseInt(split[1]),
            Integer.parseInt(split[2]));
      } catch (NumberFormatException e) {
        chunkStorage.set(xyz, null);
        saveData.setDirty();
        logger.warning(() -> String.format("Unparseable coordinates in %s: %s representing %s",
            chunk.getWorld().getName(), xyz, chunkStorage.getItemStack(itemPath)));
        continue;
      }

      var upgradableBlock = this.loadUpgradableBlock(block,
          Objects.requireNonNull(chunkStorage.getConfigurationSection(xyz)));

      if (upgradableBlock == null) {
        chunkStorage.set(xyz, null);
        saveData.setDirty();
        logger.warning(
            () -> String.format("Removed invalid save in %s at %s: %s", chunk.getWorld().getName(),
                block.getLocation().toVector(), chunkStorage.getItemStack(itemPath)));
        continue;
      }

      furnaceMap.put(block, upgradableBlock);
    }
  }

  public void unloadChunkBlocks(final Chunk chunk) {
    this.furnaceMap.remove(chunk);
  }

  public void expireCache() {
    saveFileCache.expireAll();
  }

  static String getChunkPath(Block block) {
    return getChunkPath(Coords.blockToChunk(block.getX()), Coords.blockToChunk(block.getZ()));
  }

  private static String getChunkPath(Chunk chunk) {
    return getChunkPath(chunk.getX(), chunk.getZ());
  }

  private static String getChunkPath(int chunkX, int chunkZ) {
    return chunkX + "_" + chunkZ;
  }

  static String getBlockPath(Block block) {
    return getBlockPath(block.getX(), block.getY(), block.getZ());
  }

  static String getBlockPath(int x, int y, int z) {
    return x + "_" + y + "_" + z;
  }

  class RegionStorageData {

    private final RegionStorage storage;
    private boolean dirty = false;

    RegionStorageData(RegionStorage storage) {
      this.storage = storage;
    }

    public RegionStorage getStorage() {
      return storage;
    }

    boolean isDirty() {
      if (dirty) {
        return true;
      }
      final String worldName = storage.getRegion().worldName();
      dirty = storage.getRegion().anyChunkMatch((chunkX, chunkZ) -> furnaceMap
          .get(worldName, chunkX, chunkZ).stream().anyMatch(UpgradableBlock::isDirty));
      return dirty;
    }

    public void setDirty() {
      this.dirty = true;
    }

    void clean() {
      this.dirty = false;
      final String worldName = storage.getRegion().worldName();
      this.storage.getRegion()
          .forEachChunk((chunkX, chunkZ) -> furnaceMap.get(worldName, chunkX, chunkZ)
              .forEach(upgradableBlock -> upgradableBlock.setDirty(false)));
    }
  }
}
