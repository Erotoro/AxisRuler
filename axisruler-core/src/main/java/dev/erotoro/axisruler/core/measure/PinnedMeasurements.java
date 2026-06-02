package dev.erotoro.axisruler.core.measure;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Session-scoped collection of pinned measurements. Mutated on the client tick thread
 * (pin/clear) and read on the render thread, so every accessor is synchronized and list views
 * are immutable snapshots. A monotonic {@link #revision()} lets the HUD invalidate its cache
 * when the set changes. Deliberately minimal: no names, no persistence, no per-item editing.
 */
public final class PinnedMeasurements {
    public static final int MAX_PINNED = 32;

    private final Object lock = new Object();
    private final List<Pinned> entries = new ArrayList<>();
    private int colorCursor;
    private volatile int revision;

    /**
     * Pins a completed, same-world selection. Returns the created entry, or {@code null} if the
     * store is already at {@link #MAX_PINNED}.
     */
    public Pinned pin(MeasurePoint a, MeasurePoint b) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (!a.worldKey().equals(b.worldKey())) {
            throw new IllegalArgumentException("pinned measurement requires both points in the same world");
        }
        synchronized (lock) {
            if (entries.size() >= MAX_PINNED) {
                return null;
            }
            Pinned entry = new Pinned(a, b, a.worldKey(), MeasurementPalette.color(colorCursor++));
            entries.add(entry);
            revision++;
            return entry;
        }
    }

    /**
     * Removes every pinned measurement. Returns how many were removed.
     */
    public int clear() {
        synchronized (lock) {
            int removed = entries.size();
            if (removed > 0) {
                entries.clear();
                colorCursor = 0;
                revision++;
            }
            return removed;
        }
    }

    public List<Pinned> list() {
        synchronized (lock) {
            return List.copyOf(entries);
        }
    }

    public List<Pinned> listForWorld(String worldKey) {
        Objects.requireNonNull(worldKey, "worldKey");
        synchronized (lock) {
            List<Pinned> out = new ArrayList<>();
            for (Pinned entry : entries) {
                if (entry.worldKey().equals(worldKey)) {
                    out.add(entry);
                }
            }
            return List.copyOf(out);
        }
    }

    public int count() {
        synchronized (lock) {
            return entries.size();
        }
    }

    public int countForWorld(String worldKey) {
        Objects.requireNonNull(worldKey, "worldKey");
        synchronized (lock) {
            int count = 0;
            for (Pinned entry : entries) {
                if (entry.worldKey().equals(worldKey)) {
                    count++;
                }
            }
            return count;
        }
    }

    public boolean isEmpty() {
        synchronized (lock) {
            return entries.isEmpty();
        }
    }

    public boolean isFull() {
        synchronized (lock) {
            return entries.size() >= MAX_PINNED;
        }
    }

    public int revision() {
        return revision;
    }
}
