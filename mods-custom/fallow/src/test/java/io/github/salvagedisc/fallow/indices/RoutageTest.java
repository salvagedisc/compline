package io.github.salvagedisc.fallow.indices;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RoutageTest {
    private static final List<String> PREFIXES = List.of("record.nowPlaying", "tooltip.spelunkery.");

    @Test
    void untranslatedTextAndListedKeysStayInTheBar() {
        Routage routage = new Routage();
        assertTrue(routage.inBar(null, PREFIXES, 0L));
        assertTrue(routage.inBar("record.nowPlaying", PREFIXES, 0L));
        assertTrue(routage.inBar("tooltip.spelunkery.depth_gauge_depth", PREFIXES, 0L));
        assertFalse(routage.inBar("comforts.skipping_day", PREFIXES, 0L));
    }

    @Test
    void fallowMessagesAreAlwaysNotes() {
        Routage routage = new Routage();
        for (int i = 0; i < 10; i++) {
            assertFalse(routage.inBar("message.fallow.outils.hache", List.of("message."), i * 50L));
        }
    }

    @Test
    void aKeyRepeatedQuicklyBecomesAReadoutThenANoteAgainAfterSilence() {
        Routage routage = new Routage();
        String key = "message.supplementaries.flute";
        assertFalse(routage.inBar(key, PREFIXES, 0L));
        assertFalse(routage.inBar(key, PREFIXES, 500L));
        assertTrue(routage.inBar(key, PREFIXES, 1000L));
        assertTrue(routage.inBar(key, PREFIXES, 4000L));
        assertTrue(routage.inBar(key, PREFIXES, 8500L));
        assertFalse(routage.inBar(key, PREFIXES, 20000L));
    }

    @Test
    void slowRepeatsStayNotes() {
        Routage routage = new Routage();
        for (int i = 0; i < 6; i++) {
            assertFalse(routage.inBar("lootr.message.cannot_insert", PREFIXES, i * 1500L));
        }
    }
}
