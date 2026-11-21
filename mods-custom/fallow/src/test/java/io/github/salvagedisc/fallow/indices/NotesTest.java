package io.github.salvagedisc.fallow.indices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class NotesTest {
    private static void push(Notes<String> notes, String text, long now) {
        notes.push(text, Notes.literalKey(text), text, now);
    }

    @Test
    void aCounterThatChangesStaysOneNote() {
        Notes<String> notes = new Notes<>();
        push(notes, "Coups restants : 5", 0L);
        push(notes, "Coups restants : 4", 300L);
        push(notes, "Coups restants : 3", 600L);
        List<Notes.Note<String>> visible = notes.visible(700L);
        assertEquals(1, visible.size());
        assertEquals("Coups restants : 3", visible.get(0).message);
    }

    @Test
    void aNewMessageGoesOnTopAndTheOldestLeavesBeyondThree() {
        Notes<String> notes = new Notes<>();
        push(notes, "Il vous faut une hache.", 0L);
        push(notes, "Il vous a entendu.", 100L);
        push(notes, "La brume se retire.", 200L);
        push(notes, "Vous vous reposez au coin du feu.", 300L);
        List<Notes.Note<String>> visible = notes.visible(300L);
        assertEquals("Vous vous reposez au coin du feu.", visible.get(0).message);
        assertTrue(visible.get(3).end <= 300L + Notes.FADE_OUT_MS, "the fourth note fades out quickly");
        assertEquals(3, notes.visible(300L + Notes.FADE_OUT_MS).size());
    }

    @Test
    void aRepeatedOlderMessageComesBackToTheTop() {
        Notes<String> notes = new Notes<>();
        push(notes, "Il vous faut une hache.", 0L);
        push(notes, "Il vous a entendu.", 100L);
        push(notes, "Il vous faut une hache.", 200L);
        List<Notes.Note<String>> visible = notes.visible(200L);
        assertEquals(2, visible.size());
        assertEquals("Il vous faut une hache.", visible.get(0).message);
    }

    @Test
    void notesLiveLongEnoughToBeReadThenFade() {
        assertEquals(3000L, Notes.lifetime(5));
        assertEquals(6500L, Notes.lifetime(400));
        Notes<String> notes = new Notes<>();
        push(notes, "Il vous faut une hache.", 0L);
        Notes.Note<String> note = notes.visible(0L).get(0);
        assertEquals(0.0F, note.alpha(0L));
        assertEquals(1.0F, note.alpha(1000L));
        assertTrue(note.alpha(note.end - 100L) < 1.0F);
        assertTrue(notes.visible(note.end).isEmpty());
    }
}
