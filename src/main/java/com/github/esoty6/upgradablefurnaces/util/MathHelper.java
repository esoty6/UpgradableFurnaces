package com.github.esoty6.upgradablefurnaces.util;

public final class MathHelper {

  public static short clampPositiveShort(double value) {
    return clampPositiveShort((long) value);
  }

  public static short clampPositiveShort(long value) {
    return (short) Math.max(0, Math.min(Short.MAX_VALUE, value));
  }

  public static double sigmoid(double initialValue, double x, double flavor) {
    return initialValue * (1D + (x / (flavor + Math.abs(x))));
  }

}
