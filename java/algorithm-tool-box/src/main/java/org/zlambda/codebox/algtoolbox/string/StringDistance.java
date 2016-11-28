package org.zlambda.codebox.algtoolbox.string;

public interface StringDistance {
    int distance(String str1, String str2);
    default String explain() {
        throw new UnsupportedOperationException("unsupported operation");
    }
}
