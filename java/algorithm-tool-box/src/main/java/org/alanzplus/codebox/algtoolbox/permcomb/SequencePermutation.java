package org.alanzplus.codebox.algtoolbox.permcomb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SequencePermutation {
  DFS {
    @Override
    public List<List<Integer>> permutation(List<Integer> input) {
      List<List<Integer>> res = new ArrayList<>();
      Collections.sort(input);
      dfs(new boolean[input.size()], input, new ArrayList<>(), res);
      return res;
    }

    private void dfs(boolean[] used, List<Integer> input, List<Integer> perm,
        List<List<Integer>> perms) {
      if (input.isEmpty()) {
        return;
      }
      if (perm.size() == input.size()) {
        perms.add(new ArrayList<>(perm));
        return;
      }
      for (int i = 0; i < input.size(); ++i) {
        if (used[i]) {
          continue;
        }
        if (i > 0 && input.get(i).equals(input.get(i - 1)) && !used[i - 1]) {
          continue;
        }
        used[i] = true;
        perm.add(input.get(i));
        dfs(used, input, perm, perms);
        perm.remove(perm.size() - 1);
        used[i] = false;
      }
    }
  };

  abstract public List<List<Integer>> permutation(List<Integer> input);
}
