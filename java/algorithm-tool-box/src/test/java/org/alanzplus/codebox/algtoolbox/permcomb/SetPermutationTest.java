package org.alanzplus.codebox.algtoolbox.permcomb;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class SetPermutationTest {
  private static final List<List<Integer>> TEST_CASES = Arrays.asList(
      Arrays.asList(1, 2, 3)
  );

  private static void validate(SetPermutation perm) throws Exception {
    for (List<Integer> test : TEST_CASES) {
      expectEquals(
          SetPermutation.DFS.solve(test),
          perm.solve(test)
      );
    }
  }

  private static void expectEquals(List<List<Integer>> expected, List<List<Integer>> actual)
      throws Exception {
    List<String> e = expected.stream()
        .map(i -> i.stream().sorted().map(Object::toString).collect(Collectors.joining("")))
        .sorted()
        .collect(Collectors.toList());
    List<String> a = actual.stream()
        .map(i -> i.stream().sorted().map(Object::toString).collect(Collectors.joining("")))
        .sorted()
        .collect(Collectors.toList());
    Assert.assertEquals(e, a);
  }

  @Test
  public void test() throws Exception {
    System.out.println(
        SetPermutation.DFS.solve(Arrays.asList(1, 2, 3))
    );
  }

  @Test
  public void testInsertion() throws Exception {
    validate(SetPermutation.INSERTION);
  }

  @Test
  public void testRecurrence() throws Exception {
    validate(SetPermutation.RECURRENCE);
  }
}