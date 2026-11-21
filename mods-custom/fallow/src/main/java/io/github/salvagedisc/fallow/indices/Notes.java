package io.github.salvagedisc.fallow.indices;

import java.util.ArrayList;
import java.util.List;

/**
 * La pile des notes en marge, sans rien du jeu : de quoi la tester seule. Une note porte un message (de type {@code T}),
 * une clé qui dit « le même message qui change » (la clé de traduction, ou le texte sans ses chiffres), l'heure de son
 * arrivée et celle de sa fin.
 *
 * <p>Un message de même clé que la note la plus récente la remplace sur place (les « Coups restants : 4 » d'Overgeared
 * ne s'empilent pas) ; un message déjà affiché plus bas remonte en haut. Au-delà de {@link #MAX} notes, la plus ancienne
 * s'efface vite.
 */
final class Notes<T> {
    /** Notes affichées en même temps, au plus. */
    static final int MAX = 3;
    /** Entrée en fondu et glissé, sortie en fondu, en millisecondes. */
    static final long FADE_IN_MS = 180L;
    static final long FADE_OUT_MS = 500L;

    static final class Note<T> {
        T message;
        String key;
        long start;
        long end;
        /** Position affichée (en rangs, animée vers le rang réel). */
        float row = -1.0F;

        Note(T message, String key, long start, long end) {
            this.message = message;
            this.key = key;
            this.start = start;
            this.end = end;
        }

        /** Opacité de 0 à 1 à l'instant {@code now}. */
        float alpha(long now) {
            if (now >= end) {
                return 0.0F;
            }
            float in = Math.min(1.0F, (now - start) / (float) FADE_IN_MS);
            float out = Math.min(1.0F, (end - now) / (float) FADE_OUT_MS);
            return Math.max(0.0F, Math.min(in, out));
        }

        /** Avancée du glissé d'entrée, de 0 à 1, adoucie. */
        float slide(long now) {
            float t = Math.min(1.0F, Math.max(0.0F, (now - start) / (float) FADE_IN_MS));
            return 1.0F - (1.0F - t) * (1.0F - t);
        }
    }

    /** La plus récente d'abord. */
    private final List<Note<T>> notes = new ArrayList<>();

    /** Durée d'affichage d'un message de {@code length} caractères : de quoi le lire, ni plus ni moins. */
    static long lifetime(int length) {
        return Math.max(3000L, Math.min(6500L, 2200L + 50L * length));
    }

    /** La clé d'un texte sans clé de traduction : ses chiffres retirés, pour qu'un compteur qui change reste une note. */
    static String literalKey(String text) {
        return "#" + text.replaceAll("[0-9.,]+", "0");
    }

    void push(T message, String key, String text, long now) {
        long end = now + lifetime(text.length());
        if (!notes.isEmpty() && notes.get(0).key.equals(key) && notes.get(0).end > now) {
            Note<T> top = notes.get(0);
            top.message = message;
            top.end = Math.max(top.end, end);
            return;
        }
        for (int i = 1; i < notes.size(); i++) {
            Note<T> note = notes.get(i);
            if (note.key.equals(key) && note.end > now) {
                notes.remove(i);
                note.message = message;
                note.end = Math.max(note.end, end);
                notes.add(0, note);
                return;
            }
        }
        notes.add(0, new Note<>(message, key, now, end));
        int alive = 0;
        for (Note<T> note : notes) {
            if (note.end > now && ++alive > MAX) {
                note.end = Math.min(note.end, now + FADE_OUT_MS / 2);
            }
        }
    }

    /** Retire les notes finies et rend celles à dessiner, la plus récente d'abord. */
    List<Note<T>> visible(long now) {
        notes.removeIf(note -> note.end <= now);
        return notes;
    }

    void clear() {
        notes.clear();
    }
}
