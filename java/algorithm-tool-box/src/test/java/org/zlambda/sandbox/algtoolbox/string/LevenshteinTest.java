package org.zlambda.sandbox.algtoolbox.string;

import org.junit.Assert;
import org.junit.Test;
import org.zlambda.sandbox.commons.SimpleBenchmark;

import java.util.concurrent.TimeUnit;

public class LevenshteinTest {
    @Test
    public void testDistance() throws Exception {
        SimpleBenchmark benchmark = new SimpleBenchmark.Builder().timeUnit(TimeUnit.MILLISECONDS).build();

        {
            String str1 = "";
            String str2 = "";

            benchmark.eval(
                    () -> new Levenshtein(true).distance(str1, str2),
                    (res) -> Assert.assertEquals(0, (int)res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    () -> new Levenshtein(false).distance(str1, str2),
                    (res) -> Assert.assertEquals(0, (int)res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "teacher";
            String str2 = "heaver";

            benchmark.eval(
                    () -> new Levenshtein(true).distance(str1, str2),
                    (res) -> Assert.assertEquals(3, (int)res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    () -> new Levenshtein(false).distance(str1, str2),
                    (res) -> Assert.assertEquals(3, (int)res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "a";
            String str2 = "bc";

            benchmark.eval(
                    () -> new Levenshtein(true).distance(str1, str2),
                    (res) -> Assert.assertEquals(2, (int)res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    () -> new Levenshtein(false).distance(str1, str2),
                    (res) -> Assert.assertEquals(2, (int)res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "teacher";
            String str2 = "er";

            benchmark.eval(
                    () -> new Levenshtein(true).distance(str1, str2),
                    (res) -> Assert.assertEquals(5, (int)res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    () -> new Levenshtein(false).distance(str1, str2),
                    (res) -> Assert.assertEquals(5, (int)res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }
    }

    @Test
    public void testExplain() throws Exception {
        {
            String str1 = "teacher";
            String str2 = "heaver";
            StringDistance iterative = new Levenshtein(true);
            StringDistance recursive = new Levenshtein(false);

            int dis1 = iterative.distance(str1, str2);
            int dis2 = recursive.distance(str1, str2);

            Assert.assertEquals(dis1, dis2);
            System.out.println(iterative.explain());
            Assert.assertEquals(iterative.explain(), recursive.explain());
        }
    }
}