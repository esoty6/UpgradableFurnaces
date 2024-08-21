package com.github.esoty6.upgradablefurnaces.config.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class UpgradeBlockMapping extends HashMap<Material, Double> {
  private Map<Material, Double> blocksMap = new HashMap<>();

  public UpgradeBlockMapping(ConfigurationSection configurationSection, String path) {
    Map<String, Object> configBlocks = configurationSection.getConfigurationSection(path).getValues(true);

    configBlocks.forEach((o1, o2) -> {
      if (o1 instanceof String name && o2 instanceof Number value) {
        this.blocksMap.put(Material.getMaterial(name), Double.parseDouble(value.toString()));

      }
    });
  }

  public Set<Material> keySet() {
    return blocksMap.keySet();
  }

  public Double get(Material key) {
    return blocksMap.get(key);
  }

  @Override
  public String toString() {
    return blocksMap.toString();
  }

}
