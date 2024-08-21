package com.github.esoty6.upgradablefurnaces.registry;

import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import com.github.esoty6.upgradablefurnaces.config.UpgradeBlockConfig;

public class UpgradeBlocksRegistration {

  protected final Plugin plugin;
  private UpgradeBlockConfig config;

  public UpgradeBlocksRegistration(Plugin plugin) {
    this.plugin = plugin;
  }

  public UpgradeBlockConfig getConfig() {
    if (config == null) {
      config = loadFullConfig(plugin.getConfig());
    }

    return config;
  }

  protected final UpgradeBlockConfig loadFullConfig(FileConfiguration fileConfiguration) {
    String path = "upgradeBlocks";
    ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection(path);

    if (configurationSection == null) {
      configurationSection = fileConfiguration.createSection(path);
    }

    return new UpgradeBlockConfig(configurationSection);
  }

  public Set<Material> getMaterials() {
    return getConfig().getBlocks();
  }

}
