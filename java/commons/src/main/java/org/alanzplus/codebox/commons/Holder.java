package org.alanzplus.codebox.commons;

public class Holder<T> {
  private T val;

  public Holder() {
    this(null);
  }

  public Holder(T val) {
    this.val = val;
  }

  public T get() {
    return val;
  }

  public Holder set(T val) {
    this.val = val;
    return this;
  }
}
