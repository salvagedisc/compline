package io.github.salvagedisc.fallow.mixin.compat;

import dev.worldgen.lithostitched.worldgen.blockpredicate.InStructurePredicate;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Le prédicat {@code lithostitched:in_structure} (Lithostitched 1.8.1) lit les structures voisines dans le
 * monde du serveur : {@code level.getChunk(...)} puis {@code level.structureManager()}. Pendant la génération
 * il tourne sur le fil des tâches de génération, et chaque lecture attend alors le fil du serveur. Huit décors
 * de grotte de Regions Unexplored s'en servent, jusqu'à cent fois par chunk : la génération s'arrête pendant
 * des minutes dès que le fil du serveur est occupé (prégénération Chunky figée vers 32 %, fil « Worker-Main »
 * en attente dans {@code ServerChunkCache.getChunk}).
 * <p>
 * Pendant la génération, la même question est posée ici à la région en cours, comme le fait le jeu pour ses
 * propres décors ({@code WorldGenRegion.getChunk}) : les chunks voisins y sont déjà, sans changer de fil. La région donne les références de
 * structures du chunk en cours et de ses voisins directs, et les départs de structures jusqu'à huit chunks. Hors génération (commande, engrais),
 * le code d'origine reste en place. {@code require = 0} : sans effet si une version future change ce code.
 */
@Mixin(value = InStructurePredicate.class, remap = false)
abstract class InStructureMixin {
    @Inject(method = "test(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/core/BlockPos;)Z",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void fallow$testInRegion(WorldGenLevel level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        InStructurePredicate self = (InStructurePredicate) (Object) this;
        BoundingBox box = self.searchRange().box().moved(pos.getX(), pos.getY(), pos.getZ());
        ChunkPos center = region.getCenter();
        Structure wanted = self.structure().map(holder -> holder.value()).orElse(null);
        for (int cx = SectionPos.blockToSectionCoord(box.minX()); cx <= SectionPos.blockToSectionCoord(box.maxX()); cx++) {
            for (int cz = SectionPos.blockToSectionCoord(box.minZ()); cz <= SectionPos.blockToSectionCoord(box.maxZ()); cz++) {
                // La région ne garantit les références de structures que pour le chunk en cours et ses voisins directs.
                if (center.getChessboardDistance(cx, cz) > 1) {
                    continue;
                }
                Map<Structure, LongSet> references = region.getChunk(cx, cz, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
                for (Map.Entry<Structure, LongSet> reference : references.entrySet()) {
                    if (wanted != null && !wanted.equals(reference.getKey())) {
                        continue;
                    }
                    for (long startPos : reference.getValue()) {
                        int sx = ChunkPos.getX(startPos);
                        int sz = ChunkPos.getZ(startPos);
                        // Un départ de structure hors de la région (à neuf chunks, vu depuis un voisin) n'est pas lisible
                        // sans changer de fil : il est laissé de côté.
                        if (!region.hasChunk(sx, sz)) {
                            continue;
                        }
                        StructureStart start = region.getChunk(sx, sz, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(reference.getKey());
                        if (start != null && start.isValid() && fallow$touches(start, box)) {
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
            }
        }
        cir.setReturnValue(false);
    }

    private static boolean fallow$touches(StructureStart start, BoundingBox box) {
        for (StructurePiece piece : start.getPieces()) {
            if (piece.getBoundingBox().intersects(box)) {
                return true;
            }
        }
        return false;
    }
}
