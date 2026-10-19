package com.velogame.app.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.velogame.app.NeonTheme;

/** Multi-layer neon city parallax + scrolling road. */
public class ParallaxCity implements Disposable {
    private final float worldWidth;
    private final float worldHeight;
    private final float roadTop;

    private final Texture sky;
    private final Texture far;
    private final Texture mid;
    private final Texture near;
    private final Texture road;
    private final Texture fog;

    private float farOff;
    private float midOff;
    private float nearOff;
    private float roadOff;
    private float pulse;

    public ParallaxCity(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.roadTop = Lanes.roadTop(worldHeight);
        this.sky = buildSky((int) worldWidth, (int) (worldHeight - roadTop));
        this.far = buildSkyline((int) worldWidth, (int) (worldHeight - roadTop), 48, 0.45f, 11);
        this.mid = buildSkyline((int) worldWidth, (int) (worldHeight - roadTop), 64, 0.7f, 29);
        this.near = buildSkyline((int) worldWidth, (int) (worldHeight - roadTop), 80, 1f, 47);
        this.road = buildRoad((int) worldWidth, (int) roadTop);
        this.fog = buildFog((int) worldWidth, (int) (worldHeight * 0.3f));
    }

    public void reset() {
        farOff = midOff = nearOff = roadOff = 0f;
        pulse = 0f;
    }

    public void update(float delta, float speed) {
        farOff = wrap(farOff + speed * 0.2f * delta, far.getWidth());
        midOff = wrap(midOff + speed * 0.45f * delta, mid.getWidth());
        nearOff = wrap(nearOff + speed * 0.75f * delta, near.getWidth());
        roadOff = wrap(roadOff + speed * delta, road.getWidth());
        pulse += delta * 1.5f;
    }

    public void draw(SpriteBatch batch) {
        batch.setColor(1f, 1f, 1f, 1f);
        batch.draw(sky, 0f, roadTop, worldWidth, worldHeight - roadTop);
        drawLayer(batch, far, farOff, roadTop);
        drawLayer(batch, mid, midOff, roadTop);
        drawLayer(batch, near, nearOff, roadTop);
        drawLayer(batch, road, roadOff, 0f);

        float a = 0.12f + 0.05f * (float) Math.sin(pulse);
        batch.setColor(0f, 0.85f, 1f, a);
        batch.draw(fog, 0f, roadTop * 0.4f, worldWidth, roadTop * 0.8f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawNeonLines(ShapeRenderer shapes) {
        shapes.setColor(NeonTheme.a(NeonTheme.CYAN_SOFT, 0.25f));
        shapes.rectLine(0f, roadTop + 1f, worldWidth, roadTop + 1f, 2f);
        for (int lane = 0; lane < Lanes.COUNT; lane++) {
            float y = Lanes.y(lane, worldHeight) - 6f;
            shapes.setColor(NeonTheme.a(NeonTheme.CYAN, 0.08f));
            shapes.rectLine(0f, y, worldWidth, y, 1f);
        }
    }

    private void drawLayer(SpriteBatch batch, Texture tex, float offset, float y) {
        float w = tex.getWidth();
        float h = tex.getHeight();
        batch.draw(tex, -offset, y, w, h);
        batch.draw(tex, -offset + w, y, w, h);
    }

    private static float wrap(float v, float w) {
        return v >= w ? v - w : v;
    }

    private static Texture buildSky(int width, int height) {
        height = Math.max(32, height);
        Pixmap pm = new Pixmap(Math.max(64, width), height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            float t = (float) y / (height - 1);
            pm.setColor(0.02f + 0.02f * t, 0.03f + 0.05f * (1f - t), 0.06f + 0.05f * (1f - t), 1f);
            pm.drawLine(0, y, width, y);
        }
        pm.setColor(1f, 1f, 1f, 0.5f);
        for (int i = 0; i < 35; i++) {
            pm.fillRectangle((i * 97 + 13) % width, height / 2 + (i * 53) % Math.max(1, height / 2), 1 + i % 2, 1);
        }
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Texture buildSkyline(int width, int height, int baseW, float bright, int seed) {
        height = Math.max(48, height);
        Pixmap pm = new Pixmap(Math.max(256, width), height, Pixmap.Format.RGBA8888);
        pm.setColor(0f, 0f, 0f, 0f);
        pm.fill();
        int x = 6;
        while (x < width - 16) {
            int bw = (int) (baseW * (0.5f + ((seed * 37 + x) % 50) / 100f));
            int bh = (int) (height * (0.28f + bright * 0.35f + ((seed * 13 + x * 3) % 40) / 100f));
            bh = Math.min(height - 2, Math.max(28, bh));
            pm.setColor(NeonTheme.a(NeonTheme.BUILDING, 0.92f));
            pm.fillRectangle(x, 0, bw, bh);
            pm.setColor(0f, 0.9f * bright, 1f, 0.5f * bright);
            pm.drawRectangle(x, 0, bw, bh);
            pm.setColor(0f, 0.95f, 1f, 0.35f + 0.4f * bright);
            for (int wy = 8; wy < bh - 8; wy += 12) {
                for (int wx = x + 6; wx < x + bw - 6; wx += 10) {
                    if (((wx + wy + seed) % 5) != 0) {
                        pm.fillRectangle(wx, wy, 4, 6);
                    }
                }
            }
            if (((x + seed) % 3) == 0) {
                pm.setColor(NeonTheme.CYAN);
                pm.drawLine(x + bw / 2, bh, x + bw / 2, Math.min(height - 1, bh + 12));
                pm.fillCircle(x + bw / 2, Math.min(height - 2, bh + 12), 2);
            }
            x += bw + 8 + ((seed + x) % 16);
        }
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Texture buildRoad(int width, int height) {
        height = Math.max(32, height);
        Pixmap pm = new Pixmap(Math.max(256, width), height, Pixmap.Format.RGBA8888);
        pm.setColor(NeonTheme.ROAD);
        pm.fill();
        pm.setColor(0f, 0.7f, 0.9f, 0.22f);
        pm.fillRectangle(0, height - 6, width, 6);
        pm.setColor(NeonTheme.a(NeonTheme.CYAN_SOFT, 0.4f));
        for (int yFrac = 1; yFrac <= 2; yFrac++) {
            int y = height * yFrac / 3;
            for (int x = 0; x < width; x += 52) {
                pm.fillRectangle(x, y - 2, 30, 4);
            }
        }
        pm.setColor(NeonTheme.a(NeonTheme.CYAN, 0.55f));
        pm.fillRectangle(0, 4, width, 2);
        pm.fillRectangle(0, height - 10, width, 2);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Texture buildFog(int width, int height) {
        height = Math.max(16, height);
        Pixmap pm = new Pixmap(Math.max(64, width), height, Pixmap.Format.RGBA8888);
        for (int y = 0; y < height; y++) {
            float t = (float) y / (height - 1);
            float a = (1f - Math.abs(t - 0.5f) * 2f) * 0.55f;
            pm.setColor(0f, 0.75f, 0.95f, a);
            pm.drawLine(0, y, width, y);
        }
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    @Override
    public void dispose() {
        sky.dispose();
        far.dispose();
        mid.dispose();
        near.dispose();
        road.dispose();
        fog.dispose();
    }
}
