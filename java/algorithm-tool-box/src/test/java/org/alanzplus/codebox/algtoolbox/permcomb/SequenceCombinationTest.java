package org.alanzplus.codebox.algtoolbox.permcomb;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class SequenceCombinationTest {
  private static final List<List<Integer>> TEST_CASES = Arrays.asList(
      Arrays.asList(1, 2, 3),
      Arrays.asList(1, 1, 2, 2, 3)
  );

  private static void validate(SequenceCombination comb) throws Exception {
    for (List<Integer> test : TEST_CASES) {
      expectEquals(
          SequenceCombination.SORT_AND_DFS.combination(test),
          comb.combination(test)
      );
    }
  }

  private static void expectEquals(List<List<Integer>> expected, List<List<Integer>> actual)
      throws Exception {
    Set<List<Integer>> e = expected.stream()
        .map(i -> i.stream().sorted().collect(Collectors.toList()))
        .collect(Collectors.toSet());
    Set<List<Integer>> a = actual.stream()
        .map(i -> i.stream().sorted().collect(Collectors.toList()))
        .collect(Collectors.toSet());
    Assert.assertEquals(e, a);
  }

  @Test
  public void testLoop() throws Exception {
    validate(SequenceCombination.LOOP);
  }
}