package com.github.esoty6.upgradablefurnaces.constants;

import org.bukkit.NamespacedKey;

public final class NamespacedKeys {

  private static final String NAMESPACE = "upgradable_furnaces";

  public static final NamespacedKey getData(Upgrade upgrade, Key key) {
    return new NamespacedKey(NAMESPACE, key.getKey() + "." + upgrade.getPdcPath());
  }

}
