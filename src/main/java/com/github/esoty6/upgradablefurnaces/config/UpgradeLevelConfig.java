package com.github.esoty6.upgradablefurnaces.config;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;

import com.github.esoty6.upgradablefurnaces.constants.Upgrade;

public abstract class UpgradeLevelConfig {
  protected final ConfigurationSection section;

  private Map<Upgrade, Integer> maxLevels = new HashMap<>();
  TreeSet<Integer> efficiencyTreeSet;
  TreeSet<Integer> fortuneTreeSet;
  TreeSet<Integer> fuelEfficiencyTreeSet;

  protected UpgradeLevelConfig(ConfigurationSection configurationSection, String key) {
    section = configurationSection.getConfigurationSection(key);

    efficiencyTreeSet = new TreeSet<>(section.getConfigurationSection(Upgrade.EFFICIENCY.getPath()).getKeys(false)
        .stream().map(entry -> Integer.parseInt(entry)).collect(Collectors.toSet()));

    fortuneTreeSet = new TreeSet<>(section.getConfigurationSection(Upgrade.FORTUNE.getPath()).getKeys(false)
        .stream().map(entry -> Integer.parseInt(entry)).collect(Collectors.toSet()));

    fuelEfficiencyTreeSet = new TreeSet<>(section.getConfigurationSection(Upgrade.FUEL_EFFICIENCY.getPath())
        .getKeys(false).stream().map(entry -> Integer.parseInt(entry)).collect(Collectors.toSet()));

    maxLevels.put(Upgrade.EFFICIENCY, efficiencyTreeSet.last());
    maxLevels.put(Upgrade.FORTUNE, fortuneTreeSet.last());
    maxLevels.put(Upgrade.FUEL_EFFICIENCY, fuelEfficiencyTreeSet.last());
  }

  public Map<String, Double> getLevelModifiers(int currentLevel, Upgrade upgrade) {
    Map<String, Double> levelModifiers = new HashMap<>();
    TreeSet<Integer> upgradeSet = switch (upgrade) {
      case EFFICIENCY:
        yield efficiencyTreeSet;
      case FORTUNE:
        yield fortuneTreeSet;
      case FUEL_EFFICIENCY:
        yield fuelEfficiencyTreeSet;
      default:
        yield null;
    };

    Double modifierValue = section
        .getDouble(upgrade.getPath() + "." + getModifierLevel(currentLevel, upgradeSet) + ".modifier");
    Double fuelPenaltyValue = section
        .getDouble(upgrade.getPath() + "." + getModifierLevel(currentLevel, upgradeSet) + ".fuelpenalty");

    levelModifiers.put("modifier", modifierValue);

    if (fuelPenaltyValue instanceof Double) {
      levelModifiers.put("fuelPenalty", fuelPenaltyValue);
    }

    return levelModifiers;
  }

  private int getModifierLevel(int currentLevel, TreeSet<Integer> treeSet) {
    return treeSet.contains(currentLevel) ? currentLevel : treeSet.lower(currentLevel);
  }

  public ConfigurationSection getSection() {
    return section;
  }

  public int getMaxUpgradeLevel(Upgrade upgrade) {
    return maxLevels.get(upgrade);
  }

}
