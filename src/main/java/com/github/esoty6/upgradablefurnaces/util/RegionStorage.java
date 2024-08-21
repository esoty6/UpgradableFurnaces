package com.github.esoty6.upgradablefurnaces.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class RegionStorage extends YamlConfiguration {

  private final Path dataDir;
  private final Region region;

  public RegionStorage(Path dataDir, Region region) {
    this.dataDir = dataDir;
    this.region = region;
  }

  public RegionStorage(Plugin plugin, Region region) {
    this.dataDir = plugin.getDataFolder().toPath().resolve("data");
    this.region = region;
  }

  public void load() throws IOException, InvalidConfigurationException {
    File dataFile = getDataFile();
    if (dataFile.exists()) {
      load(dataFile);
    }
  }

  public void save() throws IOException {
    save(getDataFile());
  }

  @Override
  public void save(File file) throws IOException {
    Files.createDirectories(file.toPath().normalize().getParent());

    String yamlData = saveToString();

    try (OutputStreamWriter writer =
        new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
      writer.write(yamlData);
    }
  }

  public File getDataFile() {
    return dataDir
        .resolve(
            Path.of(region.worldName(), String.format("%1$s_%2$s.yml", region.x(), region.z())))
        .toFile();
  }

  public Region getRegion() {
    return this.region;
  }

}
