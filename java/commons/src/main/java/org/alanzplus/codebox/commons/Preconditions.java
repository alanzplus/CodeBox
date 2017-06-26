package org.alanzplus.codebox.commons;

public enum Preconditions {
  ;

  public static void checkArgument(boolean condition, String msg) {
    if (!condition) {
      throw new IllegalArgumentException(msg);
    }
  }
}