package com.github.esoty6.upgradablefurnaces.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.github.esoty6.upgradablefurnaces.config.data.UpgradeBlockMapping;
import com.github.esoty6.upgradablefurnaces.constants.Upgrade;

public class UpgradeBlockConfig {

  private final ConfigurationSection section;
  private final Map<Upgrade, UpgradeBlockMapping> upgradeBlocks = new HashMap<>();
  private static Set<Material> materialSet = new HashSet<>();

  public UpgradeBlockConfig(ConfigurationSection configurationSection) {
    section = configurationSection;
    upgradeBlocks.put(Upgrade.EFFICIENCY, new UpgradeBlockMapping(section, Upgrade.EFFICIENCY.getPath()));
    upgradeBlocks.put(Upgrade.FORTUNE, new UpgradeBlockMapping(section, Upgrade.FORTUNE.getPath()));
    upgradeBlocks.put(Upgrade.FUEL_EFFICIENCY, new UpgradeBlockMapping(section, Upgrade.FUEL_EFFICIENCY.getPath()));

    for (UpgradeBlockMapping a : upgradeBlocks.values()) {
      materialSet.addAll(a.keySet());
    }

  }

  public ConfigurationSection getSection() {
    return section;
  }

  public Set<Material> getBlocks() {
    return materialSet;
  }

  public Map<Upgrade, Double> getUpgrades(Material material) {
    Map<Upgrade, Double> upgrades = new HashMap<>();
    for (Entry<Upgrade, UpgradeBlockMapping> entry : upgradeBlocks.entrySet()) {
      upgrades.put(entry.getKey(), entry.getValue().get(material));
    }
    return upgrades;
  }

}
