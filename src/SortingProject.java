import java.util.Random;
import java.util.Arrays;

public class SortingProject {

    //Helper class for generating and checking arrays
    static class ArrGen {
        private static final Random rng = new Random(212); // fixed seed

        // Random integers in [0, bound)
        static int[] randomArray(int n, int bound) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = rng.nextInt(bound);
            }
            return a;
        }

        // Almost sorted: start sorted, do a few random swaps
        static int[] nearlySorted(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = i;
            }
            int swaps = Math.max(1, n / 50);
            for (int s = 0; s < swaps; s++) {
                int i = rng.nextInt(n);
                int j = rng.nextInt(n);
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
            }
            return a;
        }

        // Strictly reversed
        static int[] reversed(int n) {
            int[] a = new int[n];
            for (int i = 0; i < n; i++) {
                a[i] = n - i;
            }
            return a;
        }

        // Many duplicates: only a few distinct values
        static int[] manyDuplicates(int n, int distinct) {
            int[] a = new int[n];
            int d = Math.max(1, distinct);
            for (int i = 0; i < n; i++) {
                a[i] = rng.nextInt(d);
            }
            return a;
        }

        static int[] copy(int[] a) {
            return Arrays.copyOf(a, a.length);
        }

        static boolean isSorted(int[] a) {
            for (int i = 1; i < a.length; i++) {
                if (a[i - 1] > a[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    //main sorting methods and experimentation
    public static void main(String[] args) {
        int[] sample = ArrGen.randomArray(10, 100);
        System.out.println("Sample: " + Arrays.toString(sample));
        System.out.println("Sorted? " + ArrGen.isSorted(sample));
    }
}
