package io.github.salvagedisc.compline.vent;

/** Une particule de fumée de feu de camp que Compline mène (ajouté par {@code vent.CampfireSmokeParticleMixin}). */
public interface SmokePlume {
    /**
     * plume : taille du panache (1 pour un feu seul) ; soul : feu des âmes ; (mergeX, mergeZ) : le centre des feux
     * voisins, vers lequel les fumées se rejoignent.
     */
    void compline$plume(float plume, boolean soul, double mergeX, double mergeZ);
}
