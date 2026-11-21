package io.github.salvagedisc.fallow.etabli.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

/** Un tampon de sommets qui rend tout ce qu'on y dessine à moitié transparent : les fantômes du carnet. */
final class Fondu implements VertexConsumer {
    private final VertexConsumer inner;
    private final float alpha;

    Fondu(VertexConsumer inner, float alpha) {
        this.inner = inner;
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        inner.addVertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alphaValue) {
        inner.setColor(red, green, blue, Math.round(alphaValue * alpha));
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        inner.setUv(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        inner.setUv1(u, v);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        inner.setUv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        inner.setNormal(x, y, z);
        return this;
    }
}
