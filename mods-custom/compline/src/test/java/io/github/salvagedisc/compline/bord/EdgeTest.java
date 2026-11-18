package io.github.salvagedisc.compline.bord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

class EdgeTest {
    private final BordModule.Edge edge = new BordModule.Edge(100.0, -200.0, 6000.0);

    @Test
    void insideTheCircleNothingHappens() {
        assertNull(edge.inside(100.0, 64.0, -200.0));
        assertNull(edge.inside(100.0 + 5999.0, 64.0, -200.0));
        assertFalse(edge.isNear(100.0, -200.0));
    }

    @Test
    void outsideIsBroughtBackJustInsideOnTheSameBearing() {
        Vec3 back = edge.inside(100.0 + 7000.0, 80.0, -200.0);
        assertNotNull(back);
        assertEquals(80.0, back.y);
        assertEquals(-200.0, back.z, 1e-9);
        assertTrue(back.x < 100.0 + 6000.0 && back.x > 100.0 + 5990.0, "brought back to " + back.x);
        assertNull(edge.inside(back.x, back.y, back.z), "the point it brings back to is inside");
    }

    @Test
    void theWarningBandStartsBeforeTheEdge() {
        assertTrue(edge.isNear(100.0 + 5999.0, -200.0));
        assertTrue(edge.isNear(100.0 + 9000.0, -200.0));
    }

    @Test
    void aTinyCircleIsNearEverywhere() {
        BordModule.Edge tiny = new BordModule.Edge(0.0, 0.0, 1.0);
        assertTrue(tiny.isNear(0.0, 0.0));
    }
}
