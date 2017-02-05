package org.alanzplus.codebox.algtoolbox.permcomb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SetPermutation {
  INSERTION {
    @Override
    public List<List<Integer>> solve(List<Integer> input) {
      List<List<Integer>> perms = new ArrayList<>();
      for (int i = 0; i < input.size(); ++i) {
        if (0 == i) {
          perms.add(Collections.singletonList(input.get(i)));
          continue;
        }
        List<List<Integer>> newPerms = new ArrayList<>();
        for (List<Integer> oldPerm : perms) {
          for (int j = 0; j <= i; ++j) {
            List<Integer> newPerm = new ArrayList<>();
            for (int k = 0; k <= i; ++k) {
              if (k < j) {
                newPerm.add(oldPerm.get(k));
              } else if (k == j) {
                newPerm.add(input.get(i));
              } else {
                newPerm.add(oldPerm.get(k - 1));
              }
            }
            newPerms.add(newPerm);
          }
        }
        perms = newPerms;
      }
      return perms;
    }
  },
  DFS {
    @Override
    public List<List<Integer>> solve(List<Integer> input) {
      List<List<Integer>> perms = new ArrayList<>();
      helper(input, perms, 0);
      return perms;
    }

    private void helper(List<Integer> input, List<List<Integer>> perms, int depth) {
      if (input.size() == depth) {
        perms.add(new ArrayList<>(input));
        return;
      }
      for (int i = depth; i < input.size(); ++i) {
        swap(input, depth, i);
        helper(input, perms, depth + 1);
        swap(input, depth, i);
      }
    }

    private void swap(List<Integer> input, int i, int j) {
      int tmp = input.get(i);
      input.set(i, input.get(j));
      input.set(j, tmp);
    }
  },
  RECURRENCE {
    @Override
    public List<List<Integer>> solve(List<Integer> input) {
      if (input.isEmpty()) {
        List<List<Integer>> perms = new ArrayList<>();
        perms.add(new ArrayList<>());
        return perms;
      }
      List<List<Integer>> newPerms = new ArrayList<>();
      for (int i = 0; i < input.size(); ++i) {
        swap(input, 0, i);
        List<List<Integer>> perms = solve(input.subList(1, input.size()));
        for (List<Integer> pem : perms) {
          pem.add(input.get(0));
        }
        swap(input, 0, i);
        newPerms.addAll(perms);
      }
      return newPerms;
    }

    private void swap(List<Integer> input, int i, int j) {
      int tmp = input.get(i);
      input.set(i, input.get(j));
      input.set(j, tmp);
    }
  };

  public abstract List<List<Integer>> solve(List<Integer> input);
}
