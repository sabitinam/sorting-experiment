import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Sorting {

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

        static int[] nearlySorted(int n, int bound, int swaps) {
            int[] a = new int[n];

            // Fill with numbers 0..n-1 (fully sorted baseline)
            for (int i = 0; i < n; i++) {
                a[i] = i;
            }

            // Perform a small number of random swaps to make it "nearly sorted"
            Random rng = ArrGen.rng;
            for (int s = 0; s < swaps; s++) {
                int i = rng.nextInt(n);
                int j = rng.nextInt(n);
                int tmp = a[i];
                a[i] = a[j];
                a[j] = tmp;
            }

            return a;
        }

    }



    //main sorting methods and experimentation
    public static void main(String[] args) {
        int[] sample = ArrGen.randomArray(10, 100);
        System.out.println("Sample: " + Arrays.toString(sample));
        System.out.println("Sorted? " + ArrGen.isSorted(sample));
    }



    //Instrumentation counters
    static class Metrics {
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



    //small insertion sort (for small partitions)
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



    //Mergesort
    class MergeSort {
        static void sort(int[] a, Metrics m) {
            int[] tmp = new int[a.length];
            sortRec(a, 0, a.length - 1, tmp, m);
        }

        private static void sortRec(int[] a, int lo, int hi, int[] tmp, Metrics m) {
            if (lo >= hi) return;
            int mid = lo + (hi - lo)/2;
            sortRec(a, lo, mid, tmp, m);
            sortRec(a, mid + 1, hi, tmp, m);
            merge(a, lo, mid, hi, tmp, m);
        }

        private static void merge(int[] a, int lo, int mid, int hi, int[] tmp, Metrics m) {
            int i = lo, j = mid + 1, k = lo;
            while (i <= mid && j <= hi) {
                if (m.cmp(a[i], a[j]) <= 0) { tmp[k++] = a[i++]; m.moves++; }
                else { tmp[k++] = a[j++]; m.moves++; }
            }

            while (i <= mid) { tmp[k++] = a[i++]; m.moves++; }
            while (j <= hi) { tmp[k++] = a[j++]; m.moves++; }
            for (k = lo; k <= hi; k++) { a[k] = tmp[k]; m.moves++; }
        }
    }



    //Quicksort (Hoare partition + insertion cutoff)
    class QuickSort {
        private static final int CUTOFF = 16;

        static void sort(int[] a, Metrics m) {
            sortRec(a, 0, a.length - 1, m);
            SmallSort.insertion(a, 0, a.length - 1, m);
        }

        private static void sortRec(int[] a, int lo, int hi, Metrics m) {
            while (lo < hi) {
                if (hi - lo + 1 <= CUTOFF) return; // defer to insertion
                int p = partitionHoare(a, lo, hi, m);

                if (p - lo < hi - p) {
                    sortRec(a, lo, p, m);
                    lo = p + 1;
                } else {
                    sortRec(a, p + 1, hi, m);
                    hi = p;
                }
            }
        }

        private static int partitionHoare(int[] a, int lo, int hi, Metrics m) {
            int pivot = a[lo + (hi - lo)/2]; // median-of-middle
            int i = lo - 1, j = hi + 1;
            while (true) {
                do { i++; } while (m.cmp(a[i], pivot) < 0);
                do { j--; } while (m.cmp(a[j], pivot) > 0);
                if (i >= j) return j;
                m.swap(a, i, j);
            }
        }
    }



    //Tree sort via TreeMap (supports duplicates)
    class TreeSort {
        static int[] sort(int[] a, Metrics m) {
            TreeMap<Integer, Integer> map = new TreeMap<>();
            for (int v : a) {
                map.put(v, map.getOrDefault(v, 0) + 1);
                m.moves++; // counting map write as a move
            }
            int[] out = new int[a.length];
            int idx = 0;
            for (Map.Entry<Integer,Integer> e : map.entrySet()) {
                int v = e.getKey(), c = e.getValue();
                for (int i = 0; i < c; i++) { out[idx++] = v; m.moves++; }
            }

            return out;
        }
    }



    //Intro sort (quick + depth limit -> heapsort)
    class IntroSort {
        private static final int CUTOFF = 16;

        static void sort(int[] a, Metrics m) {
            int maxDepth = 2 * (int) (Math.log(a.length) / Math.log(2));
            intro(a, 0, a.length - 1, maxDepth, m);
            SmallSort.insertion(a, 0, a.length - 1, m);
        }

        private static void intro(int[] a, int lo, int hi, int depth, Metrics m) {
            while (lo < hi) {
                int size = hi - lo + 1;
                if (size <= CUTOFF) return; // insertion later
                if (depth == 0) {
                    // heapsort the subarray
                    heapRange(a, lo, hi, m);
                    return;
                }
                int p = QuickSortPartition.partitionHoare(a, lo, hi, m);
                if (p - lo < hi - p) {
                    intro(a, lo, p, depth - 1, m);
                    lo = p + 1;
                } else {
                    intro(a, p + 1, hi, depth - 1, m);
                    hi = p;
                }
            }
        }

        private static void heapRange(int[] a, int lo, int hi, Metrics m) {
            int n = hi - lo + 1;
            for (int i = (n/2) - 1; i >= 0; i--) siftDown(a, lo, n, i, m);
            for (int end = n - 1; end > 0; end--) {
                m.swap(a, lo, lo + end);
                siftDown(a, lo, end, 0, m);
            }
        }

        private static void siftDown(int[] a, int base, int n, int i, Metrics m) {
            while (true) {
                int left = 2*i + 1, right = 2*i + 2, largest = i;
                if (left < n && m.cmp(a[base + left], a[base + largest]) > 0) largest = left;
                if (right < n && m.cmp(a[base + right], a[base + largest]) > 0) largest = right;
                if (largest == i) break;
                m.swap(a, base + i, base + largest);
                i = largest;
            }
        }
    }

    // small helper to reuse Hoare partition without cutoff from QuickSort
    class QuickSortPartition {
        static int partitionHoare(int[] a, int lo, int hi, Metrics m) {
            int pivot = a[lo + (hi - lo)/2];
            int i = lo - 1, j = hi + 1;
            while (true) {
                do { i++; } while (m.cmp(a[i], pivot) < 0);
                do { j--; } while (m.cmp(a[j], pivot) > 0);
                if (i >= j) return j;
                m.swap(a, i, j);
            }
        }
    }

    //Experiment configurations
    enum DatasetType { RANDOM, NEARLY_SORTED, REVERSED, MANY_DUPES }

    class DatasetFactory {
        static int[] make(DatasetType type, int n) {
            switch (type) {
                case RANDOM:
                    return ArrGen.randomArray(n, n * 5);

                case NEARLY_SORTED:
                    return ArrGen.nearlySorted(n, n * 5, Math.max(1, n / 50));

                case REVERSED:
                    return ArrGen.reversed(n);

                case MANY_DUPES:
                    return ArrGen.manyDuplicates(n, Math.max(3, n / 50));

                default:
                    throw new IllegalArgumentException();
            }
        }
    }



    //per-run wrapper
    static class RunResult {
        String algo;
        DatasetType dataset;
        int n;
        long millis;
        long comparisons;
        long moves;
        boolean sorted;

        RunResult(String algo, DatasetType dataset, int n, long millis, long comparisons, long moves, boolean sorted) {
            this.algo = algo; this.dataset = dataset; this.n = n;
            this.millis = millis; this.comparisons = comparisons; this.moves = moves; this.sorted = sorted;
        }
    }

    class Runner {
        static RunResult runAlgo(String algo, int[] base, DatasetType ds) {
            int[] a = ArrGen.copy(base);
            Metrics m = new Metrics();
            long t0 = System.currentTimeMillis();
            switch (algo) {
                case "HEAP":     HeapSort.sort(a, m); break;
                case "MERGE":    MergeSort.sort(a, m); break;
                case "QUICK":    QuickSort.sort(a, m); break;
                case "TREE":     a = TreeSort.sort(a, m); break;
                case "INTRO":    IntroSort.sort(a, m); break;
                default: throw new IllegalArgumentException("Unknown algo: " + algo);
            }
            long t1 = System.currentTimeMillis();
            boolean ok = ArrGen.isSorted(a);
            return new RunResult(algo, ds, a.length, (t1 - t0), m.comparisons, m.moves, ok);
        }
    }



    //batch runner across sizes, trials, datasets
    class Batch {
        static final String[] ALGOS = {"HEAP", "MERGE", "QUICK", "TREE", "INTRO"};

        static List<RunResult> runAll(int[] sizes, int trialsPerSize, DatasetType[] types) {
            List<RunResult> out = new ArrayList<>();
            for (DatasetType ds : types) {
                for (int n : sizes) {
                    for (int trial = 0; trial < trialsPerSize; trial++) {
                        int[] base = DatasetFactory.make(ds, n);
                        for (String algo : ALGOS) {
                            out.add(Runner.runAlgo(algo, base, ds));
                        }
                    }
                }
            }
            return out;
        }
    }



    //console table printer
    class Table {
        static void print(List<RunResult> results) {
            System.out.printf("%-7s %-14s %8s %8s %14s %14s %8s%n",
                    "Algo", "Dataset", "N", "ms", "comparisons", "moves", "sorted");
            for (RunResult r : results) {
                System.out.printf("%-7s %-14s %8d %8d %14d %14d %8s%n",
                        r.algo, r.dataset, r.n, r.millis, r.comparisons, r.moves, r.sorted);
            }
        }
    }



    //CSV writer
    class CSV {
        static void write(String path, List<RunResult> results) throws IOException {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
                pw.println("algo,dataset,n,millis,comparisons,moves,sorted");
                for (RunResult r : results) {
                    pw.printf("%s,%s,%d,%d,%d,%d,%s%n",
                            r.algo, r.dataset, r.n, r.millis, r.comparisons, r.moves, r.sorted);
                }
            }
        }
    }



    //visualization – Heapsort on [8,6,7,5,3,0,9]
    class Viz {
        static int[] base() { return new int[]{8,6,7,5,3,0,9}; }

        static void vizHeap() {
            int[] a = base();
            System.out.println("HEAP: build max-heap from [8,6,7,5,3,0,9]");
            // Show the array-level heap building steps
            // For brevity, we show the states after each siftDown starting from last parent
            System.out.println("Initial: " + Arrays.toString(a));
            // Manually emulate key steps:
            // parent indices: (n/2)-1 = 2,1,0
            // After siftDown i=2
            stepHeap(a, 2);
            // After siftDown i=1
            stepHeap(a, 1);
            // After siftDown i=0
            stepHeap(a, 0);
            System.out.println("Max-heap built (conceptual).");
        }
        private static void stepHeap(int[] a, int i) {
            // This is a didactic print, not a full simulator. We just announce the operation.
            System.out.println("siftDown at i=" + i + " -> (heap shape updated)");
        }
    }



    //visualization – Mergesort
    class VizMerge {
        static void viz() {
            int[] a = Viz.base();
            System.out.println("MERGE: split recursively then merge:");
            System.out.println("Start: " + Arrays.toString(a));
            System.out.println("Split -> [8,6,7] | [5,3,0,9]");
            System.out.println("Split -> [8] [6,7] | [5,3] [0,9]");
            System.out.println("Merge [6,7] -> [6,7]; Merge [5,3] -> [3,5]; Merge [0,9] -> [0,9]");
            System.out.println("Merge [8] & [6,7] -> [6,7,8]");
            System.out.println("Merge [3,5] & [0,9] -> [0,3,5,9]");
            System.out.println("Final merge [6,7,8] & [0,3,5,9] -> [0,3,5,6,7,8,9]");
        }
    }


    //visualization – Quicksort (Hoare)
    class VizQuick {
        static void viz() {
            System.out.println("QUICK: Hoare partition on [8,6,7,5,3,0,9]");
            System.out.println("Pivot = middle element (index 3) -> 5");
            System.out.println("Partition step: swap elements to put <5 on left, >5 on right");
            System.out.println("Subarrays recurse: [<=5] and [>=5], then insertion on tiny parts");
            System.out.println("Conceptual result: [0,3,5,6,7,8,9]");
        }
    }

    //visualization – Tree sort
    class VizTree {
        static void viz() {
            System.out.println("TREE: insert into ordered map (with counts), then emit in order:");
            System.out.println("Insert order: 8,6,7,5,3,0,9");
            System.out.println("In-order traversal yields: [0,3,5,6,7,8,9]");
        }
    }


    //visualization – Intro sort
    class VizIntro {
        static void viz() {
            System.out.println("INTRO: starts like Quicksort; if recursion too deep, fallback to Heapsort.");
            System.out.println("On [8,6,7,5,3,0,9], behaves like quicksort with small insertion cutoffs.");
        }
    }

    


















}
