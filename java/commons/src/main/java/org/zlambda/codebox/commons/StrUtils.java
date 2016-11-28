package org.zlambda.codebox.commons;

public enum StrUtils {
    ;

    public static boolean isBlank(String str) {
        return (null == str || "".equals(str));
    }
}
