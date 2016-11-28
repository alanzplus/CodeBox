package org.zlambda.codebox.algtoolbox.string;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.zlambda.codebox.commons.SimpleBenchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LevenshteinTest {
    /**
     * Tested by Leetcode, so use it as a ground true
     */
    public class Leetcode {
        public int minDistance(String word1, String word2) {
            int[][] dis = new int[word1.length() + 1][word2.length() + 1];
            for (int i = 0; i <= word1.length(); ++i) {
                dis[i][0] = i;
            }
            for (int j = 0; j <= word2.length(); ++j) {
                dis[0][j] = j;
            }
            for (int i = 1; i <= word1.length(); ++i) {
                for (int j = 1; j <= word2.length(); ++j) {
                    int insert = dis[i][j - 1] + 1;
                    int delete = dis[i - 1][j] + 1;
                    int replace = word1.charAt(i - 1) == word2.charAt(j - 1) ? dis[i - 1][j - 1] : dis[i - 1][j - 1] + 1;
                    dis[i][j] = Math.min(insert, Math.min(delete, replace));
                }
            }
            return dis[word1.length()][word2.length()];
        }
    }

    @Test
    public void testDistance() throws Exception {
        SimpleBenchmark benchmark = new SimpleBenchmark.Builder().timeUnit(TimeUnit.MILLISECONDS).build();

        {
            String str1 = "";
            String str2 = "";

            benchmark.eval(
                    (t) -> new Levenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(0, (int) res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new Levenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(0, (int) res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "teacher";
            String str2 = "heaver";

            benchmark.eval(
                    (t) -> new Levenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(3, (int) res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new Levenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(3, (int) res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "a";
            String str2 = "bc";

            benchmark.eval(
                    (t) -> new Levenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(2, (int) res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new Levenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(2, (int) res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }

        {
            String str1 = "teacher";
            String str2 = "er";

            benchmark.eval(
                    (t) -> new Levenshtein(true).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(5, (int) res),
                    100,
                    String.format("Levenshtein Iterative (\"%s\", \"%s\")", str1, str2)
            );

            benchmark.eval(
                    (t) -> new Levenshtein(false).distance(str1, str2),
                    (res, t) -> Assert.assertEquals(5, (int) res),
                    100,
                    String.format("Levenshtein Recursive (\"%s\", \"%s\")", str1, str2)
            );
        }
    }

    @Test
    public void randomStringDistanceTest() throws Exception {
        Random random = new Random();
        SimpleBenchmark benchmark = new SimpleBenchmark.Builder().timeUnit(TimeUnit.MILLISECONDS).build();

        int times = 100;

        List<String[]> inputs = new ArrayList<>();
        for (int i = 0; i < times; ++i) {
            String str1 = RandomStringUtils.randomAlphanumeric(1 + random.nextInt(100));
            String str2 = RandomStringUtils.randomAlphanumeric(1 + random.nextInt(100));
            inputs.add(new String[] { str1, str2 });
        }

        benchmark.eval(
                (t) -> {
                    String str1 = inputs.get(t)[0];
                    String str2 = inputs.get(t)[1];
                    return new Levenshtein().distance(str1, str2);
                },
                (res, t) -> {
                    String str1 = inputs.get(t)[0];
                    String str2 = inputs.get(t)[1];
                    Assert.assertEquals(new Leetcode().minDistance(str1, str2), (int)res);
                },
                times,
                "Levenshtein Iterative Random"
        );

        benchmark.eval(
                (t) -> {
                    String str1 = inputs.get(t)[0];
                    String str2 = inputs.get(t)[1];
                    return new Levenshtein(false).distance(str1, str2);
                },
                (res, t) -> {
                    String str1 = inputs.get(t)[0];
                    String str2 = inputs.get(t)[1];
                    Assert.assertEquals(new Leetcode().minDistance(str1, str2), (int)res);
                },
                times,
                "Levenshtein Recurisve Random"
        );
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