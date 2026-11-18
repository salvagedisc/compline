package io.github.salvagedisc.compline.vent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WindFieldTest {
    @Test
    void noiseStaysBetweenZeroAndOne() {
        for (int i = 0; i < 20_000; i++) {
            double value = WindField.smoothNoise(i * 0.137 - 500.0);
            assertTrue(value >= 0.0 && value <= 1.0, "noise out of range: " + value);
        }
    }

    @Test
    void noiseIsContinuousAcrossIntegerSteps() {
        for (int i = -50; i < 50; i++) {
            double before = WindField.smoothNoise(i - 1e-9);
            double after = WindField.smoothNoise(i + 1e-9);
            assertEquals(before, after, 1e-6, "jump at t=" + i);
        }
    }

    @Test
    void sameTimeGivesSameWindOnEverySide() {
        assertEquals(WindField.angle(123_456L, 0.3F), WindField.angle(123_456L, 0.3F));
        assertEquals(WindField.strength(123_456L, 0.5F, 0.0F, 70.0), WindField.strength(123_456L, 0.5F, 0.0F, 70.0));
    }

    @Test
    void directionChangesSmoothlyFromTickToTick() {
        double previous = WindField.angle(0L, 0.0F);
        double largest = 0.0;
        for (long tick = 1; tick < 20L * 60 * 60 * 6; tick += 20) {
            double angle = WindField.angle(tick, 0.0F);
            double step = Math.abs(Math.atan2(Math.sin(angle - previous), Math.cos(angle - previous)));
            largest = Math.max(largest, step);
            previous = angle;
        }
        assertTrue(largest < 0.35, "the wind turned by " + largest + " rad in one second");
    }

    @Test
    void everyQuarterOfTheCompassGetsWind() {
        int[] quarters = new int[4];
        for (long tick = 0; tick < 20L * 60 * 60 * 24 * 40; tick += 20 * 60) {
            double angle = WindField.angle(tick, 0.0F);
            double turn = (angle / (2.0 * Math.PI)) % 1.0;
            quarters[(int) Math.floor((turn < 0 ? turn + 1.0 : turn) * 4.0) % 4]++;
        }
        int total = quarters[0] + quarters[1] + quarters[2] + quarters[3];
        for (int count : quarters) {
            assertTrue(count > total / 10, "a quarter of the compass is starved: " + java.util.Arrays.toString(quarters));
        }
    }

    @Test
    void stormAndAltitudeStrengthenTheWind() {
        float calm = WindField.strength(1000L, 0.0F, 0.0F, 64.0);
        float storm = WindField.strength(1000L, 1.0F, 1.0F, 64.0);
        float high = WindField.strength(1000L, 0.0F, 0.0F, 250.0);
        assertTrue(storm > calm + 0.6F);
        assertTrue(high > calm);
        assertEquals(high, WindField.strength(1000L, 0.0F, 0.0F, 400.0), 1e-6F, "altitude bonus is capped");
    }
}
