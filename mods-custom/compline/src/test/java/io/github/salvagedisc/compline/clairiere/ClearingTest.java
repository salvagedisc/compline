package io.github.salvagedisc.compline.clairiere;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class ClearingTest {
    private static final int RING_TICKS = 10;

    @Test
    void opensOneRingAtATime() {
        Clearing clearing = new Clearing(BlockPos.ZERO, 32, 100L, Clearing.OPEN);
        assertEquals(0, clearing.radiusAt(99L, RING_TICKS));
        assertEquals(Clearing.RING, clearing.radiusAt(100L, RING_TICKS));
        assertEquals(2 * Clearing.RING, clearing.radiusAt(110L, RING_TICKS));
        assertEquals(32, clearing.radiusAt(10_000L, RING_TICKS));
    }

    @Test
    void closesFromTheEdgeInward() {
        Clearing closing = new Clearing(BlockPos.ZERO, 32, 0L, Clearing.OPEN).closedAt(1000L);
        assertTrue(closing.closing());
        assertEquals(32, closing.radiusAt(999L, RING_TICKS));
        assertEquals(32 - Clearing.RING, closing.radiusAt(1000L, RING_TICKS));
        assertEquals(0, closing.radiusAt(5000L, RING_TICKS));
    }

    @Test
    void closingBeforeFullyOpenNeverGrows() {
        Clearing closing = new Clearing(BlockPos.ZERO, 64, 0L, 20L);
        int previous = Integer.MAX_VALUE;
        for (long t = 20; t < 400; t++) {
            int radius = closing.radiusAt(t, RING_TICKS);
            assertTrue(radius <= previous, "radius grew while closing at t=" + t);
            previous = radius;
        }
    }

    @Test
    void smoothRadiusFollowsTheSteppedOne() {
        Clearing clearing = new Clearing(BlockPos.ZERO, 32, 0L, Clearing.OPEN);
        for (long t = 0; t < 200; t++) {
            assertEquals(clearing.radiusAt(t, RING_TICKS), clearing.smoothRadiusAt(t, RING_TICKS), Clearing.RING);
        }
    }

    @Test
    void indexFindsClearingsAcrossSquareBorders() {
        Clearing a = new Clearing(new BlockPos(60, 70, 60), 16, 0L, Clearing.OPEN);
        Clearing b = new Clearing(new BlockPos(-300, 70, 500), 32, 0L, Clearing.OPEN);
        ClearingIndex index = new ClearingIndex(List.of(a, b));
        assertTrue(index.covers(70.0, 70.0, null), "inside a, across the 64-block square border");
        assertTrue(index.covers(-300.0, 530.0, null));
        assertFalse(index.covers(100.0, 100.0, null));
        assertFalse(index.covers(60.0, 60.0, a), "the excluded clearing is not counted");
        assertFalse(index.coversAt(70.0, 70.0, -1L, RING_TICKS), "not open yet");
        assertTrue(index.coversAt(70.0, 70.0, 1000L, RING_TICKS));
    }
}
