package com.github.esoty6.upgradablefurnaces.constants;

import net.md_5.bungee.api.chat.BaseComponent;

public enum Upgrade {

  EFFICIENCY("Efficiency", "efficiency", Messages.MAX_SPEED_LEVEL),

  FORTUNE("Fortune", "fortune", Messages.MAX_FORTUNE_LEVEL),

  FUEL_EFFICIENCY("Fuel Efficiency", "fuelEfficiency", "fuel_efficiency",
      Messages.MAX_FUEL_EFFICIENCY_LEVEL);

  private String name;
  private String path;
  private String pdcPath;
  private BaseComponent message;

  Upgrade(String name, String path, String pdcPath, BaseComponent message) {
    this(name, path, message);
    this.pdcPath = pdcPath;
  }

  Upgrade(String name, String path, BaseComponent message) {
    this.name = name;
    this.path = path;
    this.message = message;
  }

  public String getName() {
    return name;
  }

  public String getPath() {
    return path;
  }

  public String getPdcPath() {
    return pdcPath == null ? getPath() : pdcPath;
  }

  public BaseComponent getMessage() {
    return message;
  }

}
