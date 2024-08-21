package com.github.esoty6.upgradablefurnaces.constants;

public enum Key {

  CURRENT_LEVEL_KEY("current_level"), NEXT_LEVEL_KEY("next_level"), LEVEL_PROGRESS_KEY(
      "level_progress");

  String path;

  Key(String path) {
    this.path = path;
  }

  public String getKey() {
    return path;
  }

}
