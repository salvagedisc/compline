package io.github.salvagedisc.fallow.indices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Où va un message de la barre d'action. Les indices (un conseil, un refus, un état qu'on vient de demander) deviennent
 * des notes en marge ; les relevés, eux, restent au-dessus de la barre d'inventaire, à côté de l'objet ou de la monture
 * dont ils parlent : l'altitude d'un altimètre, la vitesse, l'allure du cheval, le disque qui joue, un rail qu'on pose.
 *
 * <p>Un relevé se reconnaît de trois façons : sa clé commence par un préfixe de {@code indices.inBar} ; il n'a pas de clé
 * de traduction (un texte assemblé à la volée, presque toujours un nombre) ; ou la même clé revient sans cesse (trois
 * fois en deux secondes), ce qu'un indice ne fait guère. Une clé ainsi repérée reste un relevé tant qu'elle revient, et
 * redevient une note après cinq secondes de silence. Les messages de Fallow sont toujours des notes.
 */
final class Routage {
    static final long WINDOW_MS = 2000L;
    static final int REPEATS = 3;
    static final long QUIET_MS = 5000L;

    private final Map<String, long[]> seen = new HashMap<>();
    private final Map<String, Long> readouts = new HashMap<>();

    /** Vrai : le message reste dans la barre du jeu. {@code key} est null pour un texte sans clé de traduction. */
    boolean inBar(String key, List<? extends String> prefixes, long now) {
        if (key == null) {
            return true;
        }
        if (key.contains(".fallow.") || key.startsWith("fallow.")) {
            return false;
        }
        for (String prefix : prefixes) {
            if (!prefix.isEmpty() && key.startsWith(prefix)) {
                return true;
            }
        }
        Long last = readouts.get(key);
        if (last != null) {
            if (now - last <= QUIET_MS) {
                readouts.put(key, now);
                return true;
            }
            readouts.remove(key);
        }
        long[] entry = seen.computeIfAbsent(key, k -> new long[] {now, 0L});
        if (now - entry[0] > WINDOW_MS) {
            entry[0] = now;
            entry[1] = 0L;
        }
        if (++entry[1] >= REPEATS) {
            readouts.put(key, now);
            seen.remove(key);
            return true;
        }
        return false;
    }

    void clear() {
        seen.clear();
        readouts.clear();
    }
}
