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

    //Instrumentation counters
    class Metrics {
        long comparisons = 0;
        long moves = 0;

        int cmp(int x, int y) {
            comparisons++;
            return Integer.compare(x, y);
        }

        void swap(int[] a, int i, int j) {
            if (i == j) return;
            int t = a[i]; a[i] = a[j]; a[j] = t;
            moves += 3; // three writes
        }

        void moveSet(int[] a, int idx, int val) {
            a[idx] = val;
            moves++;
        }

        void add(Metrics other) {
            this.comparisons += other.comparisons;
            this.moves += other.moves;
        }
    }

    //insertion sort (for small partitions)
    class SmallSort {
         static void insertion(int[] a, int lo, int hi, Metrics m) {
             for (int i = lo + 1; i <= hi; i++) {
                 int key = a[i]; m.moves++;
                 int j = i - 1;
                 while (j >= lo && m.cmp(a[j], key) > 0) {
                     m.moveSet(a, j + 1, a[j]);
                     j--;
                 }
                 m.moveSet(a, j + 1, key);
             }
         }
    }



    //Heapsort
    class HeapSort {
        static void sort(int[] a, Metrics m) {
            int n = a.length;
            // build max-heap
            for (int i = (n/2) - 1; i >= 0; i--) siftDown(a, n, i, m);
            // extract
            for (int end = n - 1; end > 0; end--) {
                m.swap(a, 0, end);
                siftDown(a, end, 0, m);
            }
        }

        private static void siftDown(int[] a, int n, int i, Metrics m) {
            while (true) {
                int left = 2*i + 1, right = 2*i + 2, largest = i;
                if (left < n && m.cmp(a[left], a[largest]) > 0) largest = left;
                if (right < n && m.cmp(a[right], a[largest]) > 0) largest = right;
                if (largest == i) break;
                m.swap(a, i, largest);
                i = largest;
            }
        }
    }


    


    }
}
