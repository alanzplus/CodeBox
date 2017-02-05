package org.alanzplus.codebox.commons;

public enum SizeEstimator {
  ;
  private static final long INT_SIZE = 4;
  private static final long LONG_SIZE = 8;
  private static final long REFERENCE_SIZE = 8;

  public static long intArrShallow(int length) {
    return INT_SIZE * length;
  }

  public static long refArrShallow(int length) {
    return REFERENCE_SIZE * length;
  }
}
