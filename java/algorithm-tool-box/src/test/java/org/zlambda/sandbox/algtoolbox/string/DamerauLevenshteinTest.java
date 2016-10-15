package org.zlambda.sandbox.algtoolbox.string;

import org.junit.Assert;
import org.junit.Test;
import org.zlambda.sandbox.commons.SimpleBenchmark;

import java.util.concurrent.TimeUnit;

public class DamerauLevenshteinTest {
    @Test
    public void testDistance() throws Exception {
        SimpleBenchmark benchmark = new SimpleBenchmark.Builder().timeUnit(TimeUnit.MILLISECONDS).build();

        {
            String str1 = "";
            String str2 = "";

            benchmark.eval(
                    (t) -> new DamerauLevenshtein().distance(str1, str2),
                    (res, t) -> Assert.assertEquals(0, (int) res),
                    100,
                    String.format("Damerau-Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(0, (int) res),
                    100,
                    String.format("Damerau-Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "aab";
            String str2 = "aba";

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(1, (int) res),
                    100,
                    String.format("Damerau-Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(1, (int) res),
                    100,
                    String.format("Damerau-Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "teacher";
            String str2 = "heaver";

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(3, (int)res),
                    100,
                    String.format("Damerau-Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(3, (int)res),
                    100,
                    String.format("Damerau-Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "a";
            String str2 = "bc";

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(2, (int)res),
                    100,
                    String.format("Damerau-Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(2, (int)res),
                    100,
                    String.format("Damerau-Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "teacher";
            String str2 = "er";

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(5, (int)res),
                    100,
                    String.format("Damerau-Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new DamerauLevenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(5, (int)res),
                    100,
                    String.format("Damerau-Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }
    }

    @Test
    public void testExplain() throws Exception {
        {
            String str1 = "teacher";
            String str2 = "heaver";
            StringDistance iterative = new DamerauLevenshtein();
            StringDistance recursive = new DamerauLevenshtein(false);

            int dis1 = iterative.distance(str1, str2);
            int dis2 = recursive.distance(str1, str2);

            Assert.assertEquals(dis1, dis2);
            System.out.println(iterative.explain());
            Assert.assertEquals(iterative.explain(), recursive.explain());
        }

        {
            String str1 = "teacher helol";
            String str2 = "heaver hello";
            StringDistance iterative = new DamerauLevenshtein();
            StringDistance recursive = new DamerauLevenshtein(false);

            int dis1 = iterative.distance(str1, str2);
            int dis2 = recursive.distance(str1, str2);

            Assert.assertEquals(dis1, dis2);
            System.out.println(iterative.explain());
            Assert.assertEquals(iterative.explain(), recursive.explain());
        }
    }
}
