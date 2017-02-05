package org.alanzplus.codebox.algtoolbox.permcomb;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class SetCombinationTest {
  private static final List<List<Integer>> TEST_CASES = Arrays.asList(
      Arrays.asList(1, 2, 3)
  );

  private static void validate(SetCombination comb) throws Exception {
    for (List<Integer> test : TEST_CASES) {
      expectEquals(
          SetCombination.BIT.solve(test),
          comb.solve(test)
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
  public void testCombinationBITSolver() throws Exception {
    System.out.println(SetCombination.BIT.solve(Arrays.asList(1, 2, 3)));
  }

  @Test
  public void testCombinationDFSSolver() throws Exception {
    validate(SetCombination.DFS);
  }

  @Test
  public void testCombinationRecurrenceSolver() throws Exception {
    validate(SetCombination.RECURRENCE);
  }

  @Test
  public void testCombinationLoopSolver() throws Exception {
    validate(SetCombination.LOOP);
  }
}