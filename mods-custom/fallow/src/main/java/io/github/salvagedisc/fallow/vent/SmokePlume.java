package io.github.salvagedisc.fallow.vent;

/** Une particule de fumée de feu de camp que Fallow mène (ajouté par {@code vent.CampfireSmokeParticleMixin}). */
public interface SmokePlume {
    /**
     * plume : taille du panache (1 pour un feu seul) ; soul : feu des âmes ; (mergeX, mergeZ) : le centre des feux
     * voisins, vers lequel les fumées se rejoignent.
     */
    void fallow$plume(float plume, boolean soul, double mergeX, double mergeZ);
}
