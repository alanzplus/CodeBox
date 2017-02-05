package org.alanzplus.codebox.commons;

public enum StrUtils {
  ;

  public static boolean isBlank(String str) {
    return (null == str || "".equals(str));
  }
}
