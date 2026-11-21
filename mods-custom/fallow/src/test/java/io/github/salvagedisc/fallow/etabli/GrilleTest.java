package io.github.salvagedisc.fallow.etabli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GrilleTest {
    @Test
    void everyCellIsFoundWhereItIsDrawn() {
        for (int secteur = 0; secteur < 4; secteur++) {
            for (int index = 0; index < 9; index++) {
                double[] point = Grille.centreDansLeBloc(index, secteur);
                assertEquals(index, Grille.caseVisee(point[0], point[1], secteur), "secteur " + secteur + ", case " + index);
            }
        }
    }

    @Test
    void theRimOfTheTableIsNoCell() {
        assertEquals(-1, Grille.caseVisee(0.02, 0.02, 0));
        assertEquals(-1, Grille.caseVisee(0.98, 0.5, 1));
    }

    @Test
    void sectorsTurnWithThePlayer() {
        int north = Grille.secteur(0.0, -2.0);
        int east = Grille.secteur(2.0, 0.0);
        int south = Grille.secteur(0.0, 2.0);
        int west = Grille.secteur(-2.0, 0.0);
        assertEquals(4, java.util.Set.of(north, east, south, west).size());
    }
}
