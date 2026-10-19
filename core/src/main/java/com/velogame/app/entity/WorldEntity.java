package com.velogame.app.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.velogame.app.NeonTheme;
import com.velogame.app.util.GlowAtlas;
import com.velogame.app.world.Lanes;

public class WorldEntity {
    public enum Kind {
        CONE, BARRIER, ORB, MAGNET, SHIELD, TURBO
    }

    public final Kind kind;
    public final int lane;
    public float x;
    public float y;
    public boolean alive = true;

    private final float w;
    private final float h;
    private final Rectangle bounds = new Rectangle();
    private float phase = MathUtils.random(0f, MathUtils.PI2);

    public WorldEntity(Kind kind, int lane, float x, float worldHeight) {
        this.kind = kind;
        this.lane = lane;
        this.x = x;
        this.y = Lanes.y(lane, worldHeight);
        switch (kind) {
            case CONE -> {
                w = 36f;
                h = 48f;
            }
            case BARRIER -> {
                w = 70f;
                h = 28f;
            }
            case ORB -> {
                w = 28f;
                h = 28f;
                y += 18f;
            }
            default -> {
                w = 40f;
                h = 40f;
                y += 16f;
            }
        }
        syncBounds();
    }

    public void update(float delta, float scrollSpeed) {
        x -= scrollSpeed * delta;
        phase += delta * 5f;
        syncBounds();
        if (x + w < -40f) {
            alive = false;
        }
    }

    public void pullTo(float tx, float ty, float delta) {
        if (kind != Kind.ORB) {
            return;
        }
        x = MathUtils.lerp(x, tx, Math.min(1f, 8f * delta));
        y = MathUtils.lerp(y, ty, Math.min(1f, 8f * delta));
        syncBounds();
    }

    private void syncBounds() {
        float inset = kind == Kind.ORB ? 4f : 6f;
        bounds.set(x + inset, y + inset * 0.5f, w - inset * 2f, h - inset);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isObstacle() {
        return kind == Kind.CONE || kind == Kind.BARRIER;
    }

    public boolean isOrb() {
        return kind == Kind.ORB;
    }

    public PowerUpType asPowerUp() {
        return switch (kind) {
            case MAGNET -> PowerUpType.MAGNET;
            case SHIELD -> PowerUpType.SHIELD;
            case TURBO -> PowerUpType.TURBO;
            default -> null;
        };
    }

    public void drawGlow(SpriteBatch batch, GlowAtlas glow) {
        switch (kind) {
            case ORB -> {
                batch.setColor(0f, 1f, 1f, 0.45f);
                batch.draw(glow.cyan, x - 10f, y - 10f, w + 20f, h + 20f);
            }
            case CONE -> {
                batch.setColor(1f, 0.2f, 0.3f, 0.3f);
                batch.draw(glow.red, x - 12f, y - 8f, w + 24f, h + 20f);
            }
            case BARRIER -> {
                batch.setColor(1f, 1f, 1f, 0.22f);
                batch.draw(glow.white, x - 12f, y - 8f, w + 24f, h + 16f);
            }
            case MAGNET -> {
                batch.setColor(1f, 0f, 0.7f, 0.4f);
                batch.draw(glow.magenta, x - 12f, y - 12f, w + 24f, h + 24f);
            }
            case SHIELD -> {
                batch.setColor(0f, 1f, 1f, 0.4f);
                batch.draw(glow.cyan, x - 12f, y - 12f, w + 24f, h + 24f);
            }
            case TURBO -> {
                batch.setColor(1f, 0.8f, 0.2f, 0.4f);
                batch.draw(glow.gold, x - 12f, y - 12f, w + 24f, h + 24f);
            }
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void draw(ShapeRenderer shapes) {
        switch (kind) {
            case CONE -> drawCone(shapes);
            case BARRIER -> drawBarrier(shapes);
            case ORB -> drawOrb(shapes);
            case MAGNET -> drawMagnet(shapes);
            case SHIELD -> drawShieldPickup(shapes);
            case TURBO -> drawTurbo(shapes);
        }
    }

    private void drawCone(ShapeRenderer shapes) {
        shapes.setColor(NeonTheme.NEON_RED);
        shapes.triangle(x + w * 0.5f, y + h, x, y, x + w, y);
        shapes.setColor(NeonTheme.WHITE);
        shapes.triangle(x + w * 0.5f, y + h * 0.7f, x + w * 0.25f, y + h * 0.35f, x + w * 0.75f, y + h * 0.35f);
    }

    private void drawBarrier(ShapeRenderer shapes) {
        shapes.setColor(0.08f, 0.02f, 0.05f, 0.95f);
        shapes.rect(x, y, w, h);
        shapes.setColor(NeonTheme.NEON_RED);
        shapes.rectLine(x, y, x + w, y, 2f);
        shapes.rectLine(x, y + h, x + w, y + h, 2f);
        shapes.rectLine(x, y, x, y + h, 2f);
        shapes.rectLine(x + w, y, x + w, y + h, 2f);
        shapes.setColor(NeonTheme.WHITE);
        for (int i = 0; i < 4; i++) {
            float px = x + 8f + i * 14f;
            shapes.rectLine(px, y + 3f, px + 8f, y + h - 3f, 2f);
        }
    }

    private void drawOrb(ShapeRenderer shapes) {
        float bob = MathUtils.sin(phase) * 5f;
        shapes.setColor(NeonTheme.a(NeonTheme.CYAN, 0.35f));
        shapes.circle(x + w * 0.5f, y + h * 0.5f + bob, w * 0.7f);
        shapes.setColor(NeonTheme.CYAN);
        shapes.circle(x + w * 0.5f, y + h * 0.5f + bob, w * 0.42f);
        shapes.setColor(NeonTheme.WHITE);
        shapes.circle(x + w * 0.5f, y + h * 0.5f + bob, w * 0.18f);
    }

    private void drawMagnet(ShapeRenderer shapes) {
        float bob = MathUtils.sin(phase) * 4f;
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f + bob;
        shapes.setColor(NeonTheme.MAGENTA);
        shapes.rectLine(cx - 10f, cy + 10f, cx - 10f, cy - 8f, 3.5f);
        shapes.rectLine(cx + 10f, cy + 10f, cx + 10f, cy - 8f, 3.5f);
        shapes.rectLine(cx - 10f, cy - 8f, cx + 10f, cy - 8f, 3.5f);
        shapes.setColor(NeonTheme.WHITE);
        shapes.rect(cx - 12f, cy + 8f, 5f, 5f);
        shapes.rect(cx + 7f, cy + 8f, 5f, 5f);
    }

    private void drawShieldPickup(ShapeRenderer shapes) {
        float bob = MathUtils.sin(phase) * 4f;
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f + bob;
        shapes.setColor(NeonTheme.a(NeonTheme.CYAN, 0.4f));
        shapes.circle(cx, cy, 18f);
        shapes.setColor(NeonTheme.CYAN);
        shapes.circle(cx, cy, 12f);
        shapes.setColor(NeonTheme.WHITE);
        shapes.circle(cx, cy, 5f);
    }

    private void drawTurbo(ShapeRenderer shapes) {
        float bob = MathUtils.sin(phase) * 4f;
        float cx = x + w * 0.5f;
        float cy = y + h * 0.5f + bob;
        shapes.setColor(NeonTheme.GOLD);
        shapes.triangle(cx + 14f, cy, cx - 10f, cy + 12f, cx - 10f, cy - 12f);
        shapes.setColor(NeonTheme.WHITE);
        shapes.triangle(cx + 6f, cy, cx - 6f, cy + 6f, cx - 6f, cy - 6f);
    }
}
