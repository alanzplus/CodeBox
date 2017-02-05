package org.alanzplus.codebox.algtoolbox.permcomb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum KCombination {
  /**
   * No duplicate element and each element can be chosen one time
   */
  NO_REPETITION_ONE_TIME {
    @Override
    public List<List<Integer>> combination(List<Integer> input, int k) {
      List<List<Integer>> combs = new ArrayList<>();
      helper(input, k, new ArrayList<>(), combs);
      return combs;
    }

    private void helper(List<Integer> input, int k, List<Integer> comb, List<List<Integer>> combs) {
      if (0 == k) {
        combs.add(new ArrayList<>(comb));
        return;
      }
      if (input.isEmpty()) {
        return;
      }
      comb.add(input.get(0));
      helper(input.subList(1, input.size()), k - 1, comb, combs);
      comb.remove(comb.size() - 1);
      helper(input.subList(1, input.size()), k, comb, combs);
    }
  },
  /**
   * No duplicate element and each element can be chosen multiple times
   */
  NO_REPETITION_MULTIPLE_TIME {
    @Override
    public List<List<Integer>> combination(List<Integer> input, int k) {
      return null;
    }
    private void helper(List<Integer> input, int k, List<Integer> comb, List<List<Integer>> combs) {
      if (0 == k) {
        combs.add(new ArrayList<>(comb));
        return;
      }
      if (input.isEmpty()) {
        return;
      }
      comb.add(input.get(0));
      helper(input, k - 1, comb, combs);
      comb.remove(input.size() - 1);
      helper(input.subList(1, input.size()), k, comb, combs);
    }
  },
  /**
   * Contains duplicate elements
   */
  REPETITION {
    @Override
    public List<List<Integer>> combination(List<Integer> input, int k) {
      List<List<Integer>> combs = new ArrayList<>();
      Collections.sort(input);
      helper(input, k, new ArrayList<>(), combs);
      return combs;
    }

    private void helper(List<Integer> input, int k, List<Integer> comb,
        List<List<Integer>> combs) {
      if (0 == k) {
        combs.add(new ArrayList<>(comb));
        return;
      }
      if (input.isEmpty()) {
        return;
      }
      comb.add(input.get(0));
      helper(input.subList(1, input.size()), k - 1, comb, combs);
      comb.remove(input.size() - 1);
      int i = 1;
      for (; i < input.size(); ++i) {
        if (!input.get(i).equals(input.get(0))) {
          break;
        }
      }
      helper(input.subList(i, input.size()), k - 1, comb, combs);
    }
  };

  abstract public List<List<Integer>> combination(List<Integer> input, int k);
}
