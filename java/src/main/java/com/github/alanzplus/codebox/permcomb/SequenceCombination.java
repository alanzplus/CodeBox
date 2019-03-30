package com.github.alanzplus.codebox.permcomb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SequenceCombination {
    SORT_AND_DFS {
        @Override
        public List<List<Integer>> combination(final List<Integer> input) {
            final List<List<Integer>> combs = new ArrayList<>();
            Collections.sort(input);
            dfs(input, new ArrayList<>(), combs);
            return combs;
        }

        private void dfs(final List<Integer> input, final List<Integer> comb, final List<List<Integer>> combs) {
            if (input.isEmpty()) {
                combs.add(new ArrayList<>(comb));
                return;
            }
            comb.add(input.get(0));
            dfs(input.subList(1, input.size()), comb, combs);
            comb.remove(comb.size() - 1);
            int i = 0;
            for (; i < input.size() && input.get(i).equals(input.get(0)); ++i) {}

            dfs(input.subList(i, input.size()), comb, combs);
        }
    },
    LOOP {
        @Override
        public List<List<Integer>> combination(final List<Integer> input) {
            Collections.sort(input);
            final List<int[]> cnts = new ArrayList<>();
            int cnt = 0;
            for (int i = 0; i < input.size(); ++i) {
                if (0 == i) {
                    cnt = 1;
                } else if (input.get(i).equals(input.get(i - 1))) {
                    ++cnt;
                } else {
                    cnts.add(new int[] {input.get(i - 1), cnt});
                    cnt = 1;
                }
            }
            if (cnt > 0) {
                cnts.add(new int[] {input.get(input.size() - 1), cnt});
            }
            List<List<Integer>> combs = new ArrayList<>();
            combs.add(new ArrayList<>());
            for (final int[] eleK : cnts) {
                final int ele = eleK[0];
                final int k = eleK[1];
                final List<List<Integer>> tmp = new ArrayList<>();
                for (final List<Integer> comb : combs) {
                    for (int j = 0; j <= k; ++j) {
                        tmp.add(addDuplicate(new ArrayList<>(comb), ele, j));
                    }
                }
                combs = tmp;
            }
            return combs;
        }

        private List<Integer> addDuplicate(final List<Integer> comb, final int ele, final int k) {
            for (int i = 0; i < k; ++i) {
                comb.add(ele);
            }
            return comb;
        }
    };

    public abstract List<List<Integer>> combination(List<Integer> input);
}
