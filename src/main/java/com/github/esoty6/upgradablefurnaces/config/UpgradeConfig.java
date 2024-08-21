package com.github.esoty6.upgradablefurnaces.config;

import org.bukkit.configuration.ConfigurationSection;

public abstract class UpgradeConfig {
  protected final ConfigurationSection section;
  private final Boolean enabled;
  private final Integer maxLevel;
  private final Integer baseFuelPenaltyModifier;
  private final Double levelUpMultiplier;
  private final Double baseModifier;

  protected UpgradeConfig(ConfigurationSection configurationSection, String key) {
    section = configurationSection.getConfigurationSection(key);

    enabled = section.getBoolean("enabled");
    levelUpMultiplier = section.getDouble("levelUpMultiplier");
    maxLevel = section.getInt("maxLevel");
    baseModifier = section.getDouble("baseModifier");
    baseFuelPenaltyModifier = section.getInt("baseFuelPenaltyModifier");
  }

  public ConfigurationSection getSection() {
    return section;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public Double getLevelUpMultiplier() {
    return levelUpMultiplier;
  }

  public Integer getMaxLevel() {
    return maxLevel;
  }

  public Double getBaseModifier() {
    return baseModifier;
  }

  public Integer getBaseFuelPenaltyModifier() {
    return baseFuelPenaltyModifier;
  }

}
