package com.github.esoty6.upgradablefurnaces.registry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import com.github.esoty6.upgradablefurnaces.registry.UpgradableFurnaceManager.RegionStorageData;
import com.github.esoty6.upgradablefurnaces.util.Region;
import com.github.esoty6.upgradablefurnaces.util.RegionStorage;

record RegionLoadFunction(UpgradableFurnaceManager manager, Path dataDir, Logger logger)
    implements BiFunction<Region, Boolean, RegionStorageData> {

  @Override
  public RegionStorageData apply(Region region, Boolean create) {
    RegionStorage storage = new RegionStorage(dataDir(), region);

    if (!storage.getDataFile().exists() && Boolean.FALSE.equals(create)) {
      return null;
    }

    try {
      storage.load();
    } catch (IOException | InvalidConfigurationException e) {
      logger().log(Level.WARNING, e, e::getMessage);
    }

    return manager().new RegionStorageData(storage);
  }

}
