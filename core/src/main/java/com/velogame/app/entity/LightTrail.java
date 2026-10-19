package com.velogame.app.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.velogame.app.NeonTheme;
import com.velogame.app.util.GlowAtlas;

/** Cyan-white neon particle trail behind the rear wheel. */
public class LightTrail {
    private static class Particle {
        float x, y, life, maxLife, size, vx, vy;
    }

    private final Array<Particle> particles = new Array<>(64);
    private float emitAcc;
    private float turbo;

    public void reset() {
        particles.clear();
        emitAcc = 0f;
        turbo = 0f;
    }

    public void setTurbo(float amount) {
        turbo = MathUtils.clamp(amount, 0f, 1f);
    }

    public void emit(float x, float y, float delta, float speedRatio) {
        float rate = 45f + speedRatio * 50f + turbo * 80f;
        emitAcc += rate * delta;
        while (emitAcc >= 1f) {
            emitAcc -= 1f;
            spawn(x, y, speedRatio);
        }
        // Cap
        while (particles.size > (int) (40 + turbo * 50)) {
            particles.removeIndex(particles.size - 1);
        }
    }

    private void spawn(float x, float y, float speedRatio) {
        Particle p = new Particle();
        p.x = x + MathUtils.random(-3f, 3f);
        p.y = y + MathUtils.random(-4f, 4f);
        p.maxLife = MathUtils.random(0.25f, 0.55f) + turbo * 0.35f;
        p.life = p.maxLife;
        p.size = MathUtils.random(3f, 8f) + turbo * 6f;
        p.vx = -MathUtils.random(80f, 160f) - speedRatio * 120f - turbo * 180f;
        p.vy = MathUtils.random(-20f, 30f);
        particles.add(p);
    }

    public void update(float delta) {
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.life -= delta;
            p.x += p.vx * delta;
            p.y += p.vy * delta;
            p.vy += 15f * delta;
            if (p.life <= 0f) {
                particles.removeIndex(i);
            }
        }
    }

    public void drawGlow(SpriteBatch batch, GlowAtlas glow) {
        for (Particle p : particles) {
            float t = p.life / p.maxLife;
            float a = t * (0.35f + turbo * 0.35f);
            float s = p.size * (0.8f + t);
            batch.setColor(0f, 1f, 1f, a);
            batch.draw(glow.cyan, p.x - s, p.y - s, s * 2f, s * 2f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void draw(ShapeRenderer shapes) {
        for (Particle p : particles) {
            float t = p.life / p.maxLife;
            if (t > 0.45f) {
                shapes.setColor(NeonTheme.a(NeonTheme.WHITE, t * 0.85f));
            } else {
                shapes.setColor(NeonTheme.a(NeonTheme.CYAN, t * 0.9f));
            }
            float r = p.size * 0.45f * t;
            shapes.circle(p.x, p.y, Math.max(1f, r));
        }
    }
}
