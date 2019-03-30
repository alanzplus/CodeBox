package com.github.alanzplus.codebox.permcomb;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Input is * a set of elements (no duplicate) * each element can be chosen at most one time */
public enum SetCombination {
    BIT {
        @Override
        public List<List<Integer>> solve(final List<Integer> input) {
            BigInteger bigInt = BigInteger.ZERO;
            final List<List<Integer>> combs = new ArrayList<>();
            while (!bigInt.testBit(input.size())) {
                final List<Integer> comb = new ArrayList<>();
                for (int i = 0; i < input.size(); ++i) {
                    if (bigInt.testBit(i)) {
                        comb.add(input.get(i));
                    }
                }
                combs.add(comb);
                bigInt = bigInt.add(BigInteger.ONE);
            }
            return combs;
        }
    },
    LOOP {
        @Override
        public List<List<Integer>> solve(final List<Integer> input) {
            List<List<Integer>> combs = new ArrayList<>();
            combs.add(new ArrayList<>());
            for (int i = 0; i < input.size(); ++i) {
                final List<List<Integer>> newCombs = new ArrayList<>();
                for (final List<Integer> ele : combs) {
                    /** select */
                    final List<Integer> newComb = new ArrayList<>(ele);
                    newComb.add(input.get(i));
                    /** skip */

                    /** collect new combs */
                    newCombs.add(newComb);
                    newCombs.add(ele);
                }
                combs = newCombs;
            }
            return combs;
        }
    },
    RECURRENCE {
        @Override
        public List<List<Integer>> solve(final List<Integer> input) {
            if (input.isEmpty()) {
                final List<List<Integer>> combs = new ArrayList<>();
                combs.add(new ArrayList<>());
                return combs;
            }
            final List<List<Integer>> combs = solve(input.subList(1, input.size()));
            final int ele = input.get(0);
            final int len = combs.size();
            for (int i = 0; i < len; ++i) {
                final List<Integer> newComb = new ArrayList<>(combs.get(i));
                newComb.add(ele);
                combs.add(newComb);
            }
            combs.add(Collections.singletonList(ele));
            return combs;
        }
    },
    DFS {
        @Override
        public List<List<Integer>> solve(final List<Integer> input) {
            final List<List<Integer>> combs = new ArrayList<>();
            helper(input, new ArrayList<>(), combs);
            return combs;
        }

        private void helper(final List<Integer> input, final List<Integer> comb, final List<List<Integer>> combs) {
            if (input.isEmpty()) {
                combs.add(new ArrayList<>(comb));
                return;
            }
            helper(input.subList(1, input.size()), comb, combs);
            comb.add(input.get(0));
            helper(input.subList(1, input.size()), comb, combs);
            comb.remove(comb.size() - 1);
        }
    };

    public abstract List<List<Integer>> solve(List<Integer> input);
}
