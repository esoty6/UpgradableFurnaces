package com.github.esoty6.upgradablefurnaces.registry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Material;

public final class Registry {
  private final Logger logger;
  private final Map<Material, FurnaceRegistration> map;
  private final Map<Material, UpgradeBlocksRegistration> blocksMap;
  private final Set<Material> set;

  Registry(Logger logger) {
    this.logger = logger;
    map = new HashMap<>();
    blocksMap = new HashMap<>();
    set = new HashSet<>();
  }

  public void register(FurnaceRegistration registration) {
    registration.getMaterials().forEach(material -> {
      this.map.put(material, registration);
    });

    logger.info("Registered upgradable blocks");
  }

  public void register(UpgradeBlocksRegistration registration) {
    registration.getMaterials().forEach(material -> {
      this.blocksMap.put(material, registration);
      this.set.add(material);
    });

    logger.info("Registered upgrade blocks");
  }

  public FurnaceRegistration get(Material material) {
    return map.get(material);
  }

  public UpgradeBlocksRegistration getBlock(Material material) {
    return blocksMap.get(material);
  }

  public boolean getUpgradeBlock(Material material) {
    return set.contains(material);
  }

  public void reload() {
    map.values().stream().distinct().forEach(FurnaceRegistration::reload);
  }

}
