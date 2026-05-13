package com.proyectofinal.fintech.domain.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Directed weighted graph of transfer routes between user wallets.
 *
 * <p>Internal storage uses {@link TablaHash}{@code <String, AdjacencyEntry>} for
 * adjacency mapping and {@link MiLista}{@code <EdgeNode>} per node for outgoing edges.
 * Both of these are custom domain structures — no JDK collections are used internally.
 *
 * <p>{@code java.util.ArrayList} is used ONLY inside boundary helpers
 * {@link #outEdges(String)}, {@link #frequentRoutes(int)}, the iteration support
 * of {@link #nodes()}, and for constructing the returned cycle lists in {@link #findCycles()}.
 *
 * <p>Public API records:
 * <ul>
 *   <li>{@link Edge} — snapshot of a single directed edge leaving a node</li>
 *   <li>{@link Route} — snapshot of a directed edge with source information</li>
 * </ul>
 *
 * <p>Not thread-safe.
 */
public class GrafoTransferencias {

    // ── Public records ────────────────────────────────────────────────────────

    /**
     * Immutable snapshot of a directed edge leaving a node.
     *
     * @param target      destination node id
     * @param count       number of transfers on this edge
     * @param totalAmount cumulative transfer amount
     */
    public record Edge(String target, int count, double totalAmount) {}

    /**
     * Immutable snapshot of a directed edge with its source node id.
     *
     * @param source      origin node id
     * @param target      destination node id
     * @param count       number of transfers on this edge
     * @param totalAmount cumulative transfer amount
     */
    public record Route(String source, String target, int count, double totalAmount) {}

    // ── Package-private mutable edge accumulator ──────────────────────────────

    /** Mutable internal edge node — records cannot be used here due to accumulation. */
    static class EdgeNode {
        final String target;
        int count;
        double totalAmount;

        EdgeNode(String target, double amount) {
            this.target = target;
            this.count = 1;
            this.totalAmount = amount;
        }
    }

    /** Per-node adjacency entry holding its outgoing edge list. */
    static class AdjacencyEntry {
        final MiLista<EdgeNode> out = new MiLista<>();
    }

    // ── Internal state ────────────────────────────────────────────────────────

    private final TablaHash<String, AdjacencyEntry> adjacency = new TablaHash<>();
    private int edgeCount;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Adds {@code userId} as a node if not already present. Idempotent.
     *
     * @param userId wallet id — must not be null or blank
     * @throws IllegalArgumentException if {@code userId} is null or blank
     */
    public void addNode(String userId) {
        validateId(userId);
        if (!adjacency.containsKey(userId)) {
            adjacency.put(userId, new AdjacencyEntry());
        }
    }

    /**
     * Records a transfer from {@code source} to {@code target} with the given
     * {@code amount}. Auto-creates missing nodes. Accumulates count and total amount
     * when the edge already exists.
     *
     * @param source origin wallet id — must not be null or blank
     * @param target destination wallet id — must not be null or blank
     * @param amount transfer amount — must be &gt; 0
     * @throws IllegalArgumentException if any id is null/blank or amount &le; 0
     */
    public void addEdge(String source, String target, double amount) {
        validateId(source);
        validateId(target);
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be positive, got: " + amount);
        }
        addNode(source);
        addNode(target);

        AdjacencyEntry entry = adjacency.get(source).orElseThrow();
        // Scan existing edges for accumulation
        for (EdgeNode en : entry.out) {
            if (en.target.equals(target)) {
                en.count++;
                en.totalAmount += amount;
                return;
            }
        }
        // New edge
        entry.out.add(new EdgeNode(target, amount));
        edgeCount++;
    }

    /**
     * Returns an {@link Iterable} over all node ids in this graph. O(n).
     *
     * @return node ids
     */
    public Iterable<String> nodes() {
        return adjacency.keys();
    }

    /**
     * Returns an {@link Iterable} of {@link Edge}s leaving {@code userId}.
     * Returns an empty iterable for unknown nodes. O(k) where k = out-degree.
     *
     * @param userId source node id
     * @return outgoing edges as Edge records
     */
    public Iterable<Edge> outEdges(String userId) {
        ArrayList<Edge> result = new ArrayList<>();
        adjacency.get(userId).ifPresent(entry -> {
            for (EdgeNode en : entry.out) {
                result.add(new Edge(en.target, en.count, en.totalAmount));
            }
        });
        return result;
    }

    /**
     * Returns the number of nodes in this graph. O(1).
     *
     * @return node count
     */
    public int nodeCount() {
        return adjacency.size();
    }

    /**
     * Returns the number of unique directed edges in this graph. O(1).
     *
     * @return edge count
     */
    public int edgeCount() {
        return edgeCount;
    }

    /**
     * Returns all routes where {@code count >= minTransfers}. O(nodes + edges).
     *
     * @param minTransfers minimum transfer count threshold (inclusive)
     * @return matching routes as Route records
     */
    public Iterable<Route> frequentRoutes(int minTransfers) {
        ArrayList<Route> result = new ArrayList<>();
        for (String source : adjacency.keys()) {
            AdjacencyEntry entry = adjacency.get(source).orElseThrow();
            for (EdgeNode en : entry.out) {
                if (en.count >= minTransfers) {
                    result.add(new Route(source, en.target, en.count, en.totalAmount));
                }
            }
        }
        return result;
    }

    /**
     * Detects all simple cycles using DFS with WHITE/GRAY/BLACK coloring.
     * Each cycle is rotated to start at its lexicographically smallest node id.
     * Self-loops are included as single-element cycles.
     *
     * @return list of cycles; each cycle is a list of node ids (no repeated first element)
     */
    public List<List<String>> findCycles() {
        // Color states: 0 = WHITE (unvisited), 1 = GRAY (in stack), 2 = BLACK (done)
        TablaHash<String, Integer> color = new TablaHash<>();
        TablaHash<String, String> parent = new TablaHash<>();
        MiLista<String> stack = new MiLista<>();
        List<List<String>> result = new ArrayList<>();

        for (String node : adjacency.keys()) {
            color.put(node, 0);
        }

        for (String start : adjacency.keys()) {
            if (color.get(start).orElse(0) == 0) {
                dfsVisit(start, color, parent, stack, result);
            }
        }
        return result;
    }

    private void dfsVisit(String u,
                          TablaHash<String, Integer> color,
                          TablaHash<String, String> parent,
                          MiLista<String> stack,
                          List<List<String>> result) {
        color.put(u, 1); // GRAY
        stack.add(u);

        AdjacencyEntry entry = adjacency.get(u).orElse(null);
        if (entry != null) {
            for (EdgeNode en : entry.out) {
                String v = en.target;
                if (v.equals(u)) {
                    // Self-loop
                    List<String> cycle = new ArrayList<>();
                    cycle.add(u);
                    result.add(cycle);
                    continue;
                }
                if (color.get(v).orElse(0) == 1) {
                    // Back edge → cycle found
                    List<String> cycle = new ArrayList<>();
                    // Reconstruct from v to u in the stack
                    boolean collecting = false;
                    for (String node : stack) {
                        if (node.equals(v)) collecting = true;
                        if (collecting) cycle.add(node);
                    }
                    // Normalize: rotate to start at smallest
                    if (!cycle.isEmpty()) {
                        int minIdx = 0;
                        for (int i = 1; i < cycle.size(); i++) {
                            if (cycle.get(i).compareTo(cycle.get(minIdx)) < 0) {
                                minIdx = i;
                            }
                        }
                        Collections.rotate(cycle, -minIdx);
                        result.add(cycle);
                    }
                } else if (color.get(v).orElse(0) == 0) {
                    parent.put(v, u);
                    dfsVisit(v, color, parent, stack, result);
                }
            }
        }

        stack.removeLast();
        color.put(u, 2); // BLACK
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private static void validateId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Wallet id must not be null or blank, got: " + id);
        }
    }
}
