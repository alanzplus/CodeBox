package com.github.alanzplus.codebox.string;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * It is also called edit distance. Given two strings, str1 of length l1, and str2 of length l2. And we want to change str1 into str2 by
 * only using insert, delete and replace operation.
 *
 * <p>Then the Levenshtein distance of str1 and str2, `dis[l1, l2]`, is the minimum operations we have to perform to change str1 into str2.
 *
 * <p>To be concretely, `dis[i,j]` means the Levenshtein distance between `str1[0...i)` and `str2[0...j)` and we have the following formula
 *
 * <p>dis[i,j] = { => max(i,j) if min(i,j) = 0 => min { dis[i,j-1] + 1, insert str2[j-1] after str1[i] dis[i-1,j] + 1, delete str1[i-1]
 * str1[i-1] == str2[j-1] ? => dis[i-1,j-1] => dis[i-1,j-1] + 1 } }
 *
 * <p>Recursive solution with memo is faster than iterative solution since it doesn't need to explore all possible state
 */
public class Levenshtein implements StringDistance {
    private final boolean iterative;
    private Solver solver;

    public Levenshtein() {
        this(true);
    }

    public Levenshtein(final boolean iterative) {
        this.iterative = iterative;
    }

    @Override
    public int distance(final String str1, final String str2) {
        solver = iterative ? new Iterative(str1, str2) : new Recursive(str1, str2);
        return solver.distance();
    }

    @Override
    public String explain() {
        return solver.explain();
    }

    private enum Operation {
        INSERT {
            @Override
            State execute(final State currState, final String str1, final String str2) {
                return new State(
                        currState,
                        currState.i,
                        currState.j + 1,
                        currState.score + 1,
                        String.format("INSERT \"%c\"", str2.charAt(currState.j)));
            }
        },
        DELETE {
            @Override
            State execute(final State currState, final String str1, final String str2) {
                return new State(
                        currState,
                        currState.i + 1,
                        currState.j,
                        currState.score + 1,
                        String.format("DELETE \"%c\"", str1.charAt(currState.i)));
            }
        },
        REPLACE_OR_KEEP {
            @Override
            State execute(final State currState, final String str1, final String str2) {
                final char c1 = str1.charAt(currState.i);
                final char c2 = str2.charAt(currState.j);
                final int score = c1 == c2 ? currState.score : currState.score + 1;
                final String operation = c1 == c2 ? String.format("KEEP \"%c\"", c1) : String.format("REPLACE \"%c\" BY \"%c\"", c1, c2);
                return new State(currState, currState.i + 1, currState.j + 1, score, operation);
            }
        };

        abstract State execute(State currState, String str1, String str2);
    }

    private interface Solver {
        int distance();

        String explain();
    }

    private static class Iterative implements Solver {
        private final State[][] states;
        private final String str1;
        private final String str2;

        private Iterative(final String str1, final String str2) {
            states = new State[str1.length() + 1][str2.length() + 1];
            this.str1 = str1;
            this.str2 = str2;
        }

        @Override
        public int distance() {
            states[0][0] = new State(null, 0, 0, 0, "");
            for (int i = 1; i <= str1.length(); ++i) {
                states[i][0] = new State(null, i, 0, i, String.format("DELETE \"%s\"", str1.substring(0, i)));
            }
            for (int j = 1; j <= str2.length(); ++j) {
                states[0][j] = new State(null, 0, j, j, String.format("ADD \"%s\"", str2.substring(0, j)));
            }

            for (int i = 1; i <= str1.length(); ++i) {
                for (int j = 1; j <= str2.length(); ++j) {
                    states[i][j] =
                            Stream.of(
                                            Operation.INSERT.execute(states[i][j - 1], str1, str2),
                                            Operation.DELETE.execute(states[i - 1][j], str1, str2),
                                            Operation.REPLACE_OR_KEEP.execute(states[i - 1][j - 1], str1, str2))
                                    .min((s1, s2) -> Integer.compare(s1.score, s2.score))
                                    .get();
                }
            }

            return states[str1.length()][str2.length()].score;
        }

        @Override
        public String explain() {
            List<String> ops = new ArrayList<>();
            State curr = states[str1.length()][str2.length()];
            while (null != curr) {
                ops.add(curr.operation);
                curr = curr.prevState;
            }
            ops = ops.stream().filter(str -> !"".equals(str)).collect(Collectors.toList());
            Collections.reverse(ops);
            return String.join(", ", ops);
        }
    }

    private static class Recursive implements Solver {
        private final State[][] states;
        private final String str1;
        private final String str2;

        private Recursive(final String str1, final String str2) {
            states = new State[str1.length() + 1][str2.length() + 1];
            this.str1 = str1;
            this.str2 = str2;
        }

        private State distance(final int i, final int j) {
            if (null != states[i][j]) {
                return states[i][j];
            }

            if (Math.min(i, j) == 0) {
                if (i == j) {
                    states[i][j] = new State(null, i, j, 0, "");
                } else if (0 == i) {
                    states[i][j] = new State(null, i, j, j, String.format("INSERT \"%s\"", str2.substring(0, j)));
                } else {
                    states[i][j] = new State(null, i, j, i, String.format("DELETE \"%s\"", str1.substring(0, i)));
                }
            } else {
                states[i][j] =
                        Stream.of(
                                        Operation.INSERT.execute(distance(i, j - 1), str1, str2),
                                        Operation.DELETE.execute(distance(i - 1, j), str1, str2),
                                        Operation.REPLACE_OR_KEEP.execute(distance(i - 1, j - 1), str1, str2))
                                .min((s1, s2) -> Integer.compare(s1.score, s2.score))
                                .get();
            }

            return states[i][j];
        }

        @Override
        public int distance() {
            return distance(str1.length(), str2.length()).score;
        }

        @Override
        public String explain() {
            List<String> ops = new ArrayList<>();
            State curr = states[str1.length()][str2.length()];
            while (null != curr) {
                ops.add(curr.operation);
                curr = curr.prevState;
            }
            ops = ops.stream().filter(str -> !"".equals(str)).collect(Collectors.toList());
            Collections.reverse(ops);
            return String.join(", ", ops);
        }
    }

    private static class State {
        private final State prevState;
        private final int i;
        private final int j;
        private final int score;
        private final String operation;

        private State(final State prevState, final int i, final int j, final int score, final String operation) {
            this.prevState = prevState;
            this.i = i;
            this.j = j;
            this.score = score;
            this.operation = operation;
        }
    }
}
