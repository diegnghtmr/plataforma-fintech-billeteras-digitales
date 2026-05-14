package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Structure benchmarks — @Tag("benchmark").
 * Excluded from default ./mvnw test run (pom.xml excludedGroups=benchmark).
 * Run explicitly: ./mvnw test -Dgroups=benchmark
 *
 * Methodology:
 * - Warmup: 3 iterations discarded.
 * - Measure: 5 iterations; report median elapsed nanos.
 * - Timing: System.nanoTime() deltas.
 * - Output format: [BENCHMARK] <name> | op=<op> | N=<int> | elapsed=<long>ns | ops/sec=<long>
 */
@Tag("benchmark")
class StructureBenchmarkTest {

    private static final int[] SIZES = {1_000, 10_000, 100_000};
    private static final int WARMUP = 3;
    private static final int MEASURE = 5;
    private static final Random RNG = new Random(42);

    // ── Helper: run and measure ───────────────────────────────────────────────

    private long medianNs(Runnable task) {
        long[] times = new long[MEASURE];
        for (int w = 0; w < WARMUP; w++) task.run();
        for (int m = 0; m < MEASURE; m++) {
            long start = System.nanoTime();
            task.run();
            times[m] = System.nanoTime() - start;
        }
        java.util.Arrays.sort(times);
        return times[MEASURE / 2];
    }

    private void print(String name, String op, int n, long elapsedNs) {
        long opsPerSec = elapsedNs > 0 ? (long) (n * 1_000_000_000.0 / elapsedNs) : Long.MAX_VALUE;
        System.out.printf("[BENCHMARK] %-30s | op=%-15s | N=%-7d | elapsed=%10dns | ops/sec=%12d%n",
                name, op, n, elapsedNs, opsPerSec);
    }

    // ── Pair 1: TablaHash vs HashMap ─────────────────────────────────────────

    @Test
    void benchmarkTablaHash_vs_HashMap() {
        for (int n : SIZES) {
            String[] keys = new String[n];
            for (int i = 0; i < n; i++) keys[i] = "key-" + i;

            // TablaHash put+get
            long tablaHashNs = medianNs(() -> {
                TablaHash<String, Integer> th = new TablaHash<>();
                for (int i = 0; i < n; i++) th.put(keys[i], i);
                for (int i = 0; i < n; i++) th.get(keys[i]);
            });
            print("TablaHash", "put+get", n, tablaHashNs);

            // HashMap put+get
            long hashMapNs = medianNs(() -> {
                HashMap<String, Integer> hm = new HashMap<>();
                for (int i = 0; i < n; i++) hm.put(keys[i], i);
                for (int i = 0; i < n; i++) hm.get(keys[i]);
            });
            print("HashMap", "put+get", n, hashMapNs);
        }
    }

    // ── Pair 2: MiLista vs ArrayList ─────────────────────────────────────────

    @Test
    void benchmarkMiLista_vs_ArrayList() {
        for (int n : SIZES) {
            // MiLista add + full traversal
            long miListaNs = medianNs(() -> {
                MiLista<Integer> ml = new MiLista<>();
                for (int i = 0; i < n; i++) ml.add(i);
                long sum = 0;
                for (Integer v : ml) sum += v;
                // prevent dead-code elimination
                if (sum < 0) throw new RuntimeException("unexpected");
            });
            print("MiLista", "add+traverse", n, miListaNs);

            // ArrayList add + full traversal
            long arrayListNs = medianNs(() -> {
                ArrayList<Integer> al = new ArrayList<>();
                for (int i = 0; i < n; i++) al.add(i);
                long sum = 0;
                for (Integer v : al) sum += v;
                if (sum < 0) throw new RuntimeException("unexpected");
            });
            print("ArrayList", "add+traverse", n, arrayListNs);
        }
    }

    // ── Pair 3: ArbolBST vs Collections.sort ─────────────────────────────────

    @Test
    void benchmarkArbolBST_vs_CollectionsSort() {
        for (int n : SIZES) {
            int[] data = new int[n];
            for (int i = 0; i < n; i++) data[i] = RNG.nextInt(n * 10);

            // ArbolBST insert N random + in-order traversal
            long bstNs = medianNs(() -> {
                ArbolBST<Integer> bst = new ArbolBST<>();
                for (int v : data) bst.insert(v);
                Iterable<Integer> inOrder = bst.inOrder();
                int cnt = 0;
                for (Integer ignored : inOrder) cnt++;
                if (cnt == 0 && n > 0) {
                    // BST deduplicates — still valid even if cnt < n
                }
            });
            print("ArbolBST", "insert+inOrder", n, bstNs);

            // ArrayList + Collections.sort
            long sortNs = medianNs(() -> {
                ArrayList<Integer> al = new ArrayList<>();
                for (int v : data) al.add(v);
                Collections.sort(al);
                if (al.isEmpty() && n > 0) throw new RuntimeException("unexpected");
            });
            print("ArrayList+sort", "add+sort", n, sortNs);
        }
    }

    // ── Pair 4: ColaPrioridad vs PriorityQueue ────────────────────────────────

    @Test
    void benchmarkColaPrioridad_vs_PriorityQueue() {
        for (int n : SIZES) {
            int[] data = new int[n];
            for (int i = 0; i < n; i++) data[i] = RNG.nextInt(n * 10);

            // ColaPrioridad insert N + extract-all
            long colaMs = medianNs(() -> {
                ColaPrioridad<Integer> cp = new ColaPrioridad<>(Comparator.naturalOrder());
                for (int v : data) cp.add(v);
                while (!cp.isEmpty()) cp.poll().orElse(null);
            });
            print("ColaPrioridad", "add+extractAll", n, colaMs);

            // PriorityQueue insert N + extract-all
            long pqMs = medianNs(() -> {
                PriorityQueue<Integer> pq = new PriorityQueue<>();
                for (int v : data) pq.add(v);
                while (!pq.isEmpty()) pq.poll();
            });
            print("PriorityQueue", "add+extractAll", n, pqMs);
        }
    }
}
