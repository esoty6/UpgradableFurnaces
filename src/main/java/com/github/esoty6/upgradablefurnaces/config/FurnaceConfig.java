package com.github.esoty6.upgradablefurnaces.config;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import com.github.esoty6.upgradablefurnaces.config.data.UpgradeLevelMapping;
import com.github.esoty6.upgradablefurnaces.config.data.UpgradeMapping;
import com.github.esoty6.upgradablefurnaces.constants.Upgrade;

public class FurnaceConfig {

  private final ConfigurationSection section;
  private final Map<Upgrade, UpgradeMapping> furnaceMap = new HashMap<>();
  private final Map<Upgrade, UpgradeLevelMapping> upgradeLevels = new HashMap<>();
  private final String modifierCalculationMethod;
  private final Double globalLevelMultiplier;
  private final Double globalBaseNextLevel;
  private final Boolean globalLevelMultiplierEnabled;
  private UpgradeLevelConfig levelConfig;

  public FurnaceConfig(ConfigurationSection configurationSection) {
    section = configurationSection;
    globalLevelMultiplierEnabled = section.getBoolean("globalLevelMultiplierEnabled");
    globalLevelMultiplier = section.getDouble("globalLevelMultiplier");
    modifierCalculationMethod = section.getString("modifierCalculationMethod");
    globalBaseNextLevel = section.getDouble("globalBaseNextLevel");

    furnaceMap.put(Upgrade.EFFICIENCY, new UpgradeMapping(section, Upgrade.EFFICIENCY.getPath()));
    furnaceMap.put(Upgrade.FORTUNE, new UpgradeMapping(section, Upgrade.FORTUNE.getPath()));
    furnaceMap.put(Upgrade.FUEL_EFFICIENCY, new UpgradeMapping(section, Upgrade.FUEL_EFFICIENCY.getPath()));

    if (isStepMethodEnabled()) {
      levelConfig = new UpgradeLevelMapping(section, "upgradeLevels");
    }
  }

  public Map<Upgrade, UpgradeMapping> getFurnaces() {
    return furnaceMap;
  }

  public ConfigurationSection getSection() {
    return section;
  }

  public String getModifierCalculationMethod() {
    return modifierCalculationMethod;
  }

  public Double getBaseNextLevel() {
    return globalBaseNextLevel;
  }

  public Double getLevelMultiplier(Upgrade key) {
    if (globalLevelMultiplierEnabled) {
      return globalLevelMultiplier;
    }

    return furnaceMap.get(key).getLevelUpMultiplier();
  }

  public boolean isStepMethodEnabled() {
    return modifierCalculationMethod.equals("step");
  }

  public int getMaxLevel(Upgrade upgrade) {
    if (isStepMethodEnabled()) {
      return levelConfig.getMaxUpgradeLevel(upgrade);
    }

    return furnaceMap.get(upgrade).getMaxLevel();
  }

  public boolean isUpgradeEnabled(Upgrade upgrade) {
    return furnaceMap.get(upgrade).getEnabled();
  }

  public Map<String, Double> getModifier(int currentLevel, Upgrade upgrade) {
    return upgradeLevels.get(upgrade).getLevelModifiers(currentLevel, upgrade);
  }

}
