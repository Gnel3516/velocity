package com.velogame.app.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.velogame.app.NeonTheme;
import com.velogame.app.util.GlowAtlas;
import com.velogame.app.world.Lanes;

/** Side-view neon cyclist with lane switching, jump and light trail. */
public class Cyclist {
    private static final float LANE_LERP = 12f;
    private static final float JUMP_V = 520f;
    private static final float GRAVITY = 1600f;

    private final float worldHeight;
    private final GlowAtlas glow;
    private final LightTrail trail = new LightTrail();
    private final Rectangle bounds = new Rectangle();

    private float x;
    private int targetLane;
    private float visualY;
    private float jumpOffset;
    private float jumpV;
    private boolean airborne;
    private float wheelSpin;
    private float anim;
    private float hitFlash;

    private float magnetTimer;
    private boolean shieldActive;
    private float turboTimer;

    public Cyclist(float worldHeight, GlowAtlas glow) {
        this.worldHeight = worldHeight;
        this.glow = glow;
        reset();
    }

    public void reset() {
        x = 150f;
        targetLane = Lanes.MID;
        visualY = Lanes.y(targetLane, worldHeight);
        jumpOffset = 0f;
        jumpV = 0f;
        airborne = false;
        wheelSpin = 0f;
        anim = 0f;
        hitFlash = 0f;
        magnetTimer = 0f;
        shieldActive = false;
        turboTimer = 0f;
        trail.reset();
    }

    public void laneUp() {
        targetLane = Lanes.clamp(targetLane + 1);
    }

    public void laneDown() {
        targetLane = Lanes.clamp(targetLane - 1);
    }

    public void jump() {
        if (!airborne) {
            airborne = true;
            jumpV = JUMP_V;
        }
    }

    public void activate(PowerUpType type) {
        switch (type) {
            case MAGNET -> magnetTimer = type.duration;
            case SHIELD -> shieldActive = true;
            case TURBO -> turboTimer = type.duration;
        }
    }

    public boolean absorbHit() {
        hitFlash = 0.4f;
        if (shieldActive) {
            shieldActive = false;
            return true;
        }
        return false;
    }

    public void update(float delta, float speedRatio) {
        anim += delta;
        float laneY = Lanes.y(targetLane, worldHeight);
        visualY = MathUtils.lerp(visualY, laneY, Math.min(1f, LANE_LERP * delta));

        if (airborne) {
            jumpV -= GRAVITY * delta;
            jumpOffset += jumpV * delta;
            if (jumpOffset <= 0f) {
                jumpOffset = 0f;
                jumpV = 0f;
                airborne = false;
            }
        }

        if (magnetTimer > 0f) {
            magnetTimer -= delta;
        }
        if (turboTimer > 0f) {
            turboTimer -= delta;
        }

        wheelSpin += delta * (14f + speedRatio * 10f + (isTurbo() ? 8f : 0f));
        trail.setTurbo(isTurbo() ? 1f : speedRatio * 0.45f);
        float rearX = x + 18f;
        float rearY = getDrawY() + 18f;
        trail.emit(rearX, rearY, delta, speedRatio);
        trail.update(delta);

        bounds.set(x + 8f, getDrawY() + 6f, 70f, 48f);
        if (hitFlash > 0f) {
            hitFlash -= delta;
        }
    }

    public float getDrawY() {
        return visualY + jumpOffset;
    }

    public float getX() {
        return x;
    }

    public int getLane() {
        return targetLane;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean hasMagnet() {
        return magnetTimer > 0f;
    }

    public boolean hasShield() {
        return shieldActive;
    }

    public boolean isTurbo() {
        return turboTimer > 0f;
    }

    public float getMagnetTimer() {
        return magnetTimer;
    }

    public float getTurboTimer() {
        return turboTimer;
    }

    public LightTrail getTrail() {
        return trail;
    }

    public void drawGlow(SpriteBatch batch) {
        float y = getDrawY();
        batch.setColor(0f, 1f, 1f, 0.28f + (isTurbo() ? 0.2f : 0f));
        batch.draw(glow.cyan, x - 20f, y - 10f, 120f, 90f);
        trail.drawGlow(batch, glow);
        if (shieldActive) {
            float pulse = 0.35f + 0.2f * MathUtils.sin(anim * 8f);
            batch.setColor(0f, 1f, 1f, pulse);
            batch.draw(glow.cyan, x - 15f, y - 15f, 110f, 100f);
        }
        if (hasMagnet()) {
            batch.setColor(1f, 0f, 0.7f, 0.2f);
            batch.draw(glow.magenta, x - 40f, y - 20f, 160f, 120f);
        }
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void draw(ShapeRenderer shapes) {
        trail.draw(shapes);
        float y = getDrawY();

        // Shadow
        float shrink = MathUtils.clamp(1f - jumpOffset / 120f, 0.35f, 1f);
        shapes.setColor(0f, 0f, 0f, 0.35f * shrink);
        shapes.ellipse(x + 10f, visualY - 2f, 70f * shrink, 12f * shrink);

        drawBike(shapes, x, y);

        if (shieldActive) {
            shapes.setColor(NeonTheme.a(NeonTheme.CYAN, 0.35f + 0.2f * MathUtils.sin(anim * 7f)));
            shapes.circle(x + 45f, y + 35f, 48f);
            shapes.setColor(NeonTheme.a(NeonTheme.WHITE, 0.25f));
            shapes.circle(x + 45f, y + 35f, 42f);
        }

        if (hitFlash > 0f) {
            shapes.setColor(1f, 0.2f, 0.35f, hitFlash * 0.5f);
            shapes.rect(x, y, 90f, 70f);
        }
    }

    private void drawBike(ShapeRenderer shapes, float x, float y) {
        // Wheels
        drawWheel(shapes, x + 22f, y + 18f, 16f);
        drawWheel(shapes, x + 72f, y + 18f, 16f);

        float fw = 2.4f;
        // Frame
        shapes.setColor(NeonTheme.CYAN_SOFT);
        shapes.rectLine(x + 22f, y + 18f, x + 48f, y + 42f, fw); // seat tube
        shapes.rectLine(x + 22f, y + 18f, x + 55f, y + 22f, fw); // chainstay
        shapes.rectLine(x + 48f, y + 42f, x + 72f, y + 18f, fw); // down tube
        shapes.rectLine(x + 48f, y + 42f, x + 68f, y + 48f, fw); // top tube
        shapes.rectLine(x + 68f, y + 48f, x + 72f, y + 18f, fw); // head tube / fork
        shapes.rectLine(x + 55f, y + 22f, x + 48f, y + 42f, fw); // seat stay accent

        // Pedal
        shapes.setColor(NeonTheme.WHITE);
        float ped = MathUtils.sin(wheelSpin * 1.2f);
        shapes.rectLine(x + 48f, y + 20f, x + 48f + ped * 10f, y + 20f + ped * 8f, 2f);
        shapes.circle(x + 48f, y + 20f, 3.5f);

        // Seat
        shapes.setColor(NeonTheme.WHITE);
        shapes.rect(x + 40f, y + 42f, 14f, 4f);

        // Handlebar
        shapes.setColor(NeonTheme.CYAN);
        shapes.rectLine(x + 68f, y + 48f, x + 78f, y + 56f, 2.2f);
        shapes.rectLine(x + 74f, y + 56f, x + 82f, y + 52f, 2f);

        // Rider - crouched sport pose
        shapes.setColor(0.05f, 0.12f, 0.18f, 1f);
        // Legs
        shapes.rectLine(x + 48f, y + 24f, x + 42f, y + 40f, 3.5f);
        shapes.rectLine(x + 48f, y + 24f, x + 58f, y + 38f, 3.5f);
        // Torso leaning forward
        shapes.setColor(NeonTheme.CYAN);
        shapes.rectLine(x + 50f, y + 40f, x + 68f, y + 52f, 5f);
        shapes.setColor(0.04f, 0.1f, 0.16f, 1f);
        shapes.rect(x + 52f, y + 42f, 16f, 12f);
        // Suit neon trim
        shapes.setColor(NeonTheme.CYAN_SOFT);
        shapes.rectLine(x + 52f, y + 42f, x + 68f, y + 52f, 1.5f);
        // Arms to bars
        shapes.setColor(NeonTheme.a(NeonTheme.WHITE, 0.85f));
        shapes.rectLine(x + 60f, y + 48f, x + 76f, y + 54f, 2.5f);

        // Helmet
        shapes.setColor(0.04f, 0.1f, 0.16f, 1f);
        shapes.circle(x + 70f, y + 60f, 9f);
        shapes.setColor(NeonTheme.CYAN);
        shapes.circle(x + 70f, y + 61f, 7f);
        shapes.setColor(NeonTheme.WHITE);
        shapes.circle(x + 71f, y + 61f, 3f);
        // Visor
        shapes.setColor(NeonTheme.a(NeonTheme.CYAN_SOFT, 0.7f));
        shapes.rect(x + 70f, y + 58f, 8f, 4f);
    }

    private void drawWheel(ShapeRenderer shapes, float cx, float cy, float r) {
        shapes.setColor(NeonTheme.a(NeonTheme.CYAN, 0.28f));
        shapes.circle(cx, cy, r * 1.3f);
        shapes.setColor(0.03f, 0.05f, 0.08f, 1f);
        shapes.circle(cx, cy, r);
        shapes.setColor(NeonTheme.CYAN);
        shapes.circle(cx, cy, r * 0.85f);
        shapes.setColor(0.03f, 0.05f, 0.08f, 1f);
        shapes.circle(cx, cy, r * 0.7f);

        shapes.setColor(NeonTheme.a(NeonTheme.CYAN_SOFT, 0.9f));
        for (int i = 0; i < 6; i++) {
            float a = wheelSpin + i * MathUtils.PI / 6f;
            float c = MathUtils.cos(a);
            float s = MathUtils.sin(a);
            shapes.rectLine(cx - c * r * 0.65f, cy - s * r * 0.65f,
                cx + c * r * 0.65f, cy + s * r * 0.65f, Math.max(1f, r * 0.08f));
        }
        shapes.setColor(NeonTheme.WHITE);
        shapes.circle(cx, cy, r * 0.25f);
    }
}
