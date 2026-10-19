package com.velogame.app.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.velogame.app.entity.Cyclist;
import com.velogame.app.entity.WorldEntity;
import com.velogame.app.util.GlowAtlas;

public class SpawnSystem {
    private final Array<WorldEntity> entities = new Array<>();
    private final float worldWidth;
    private final float worldHeight;
    private final GlowAtlas glow;

    private float obstacleTimer;
    private float orbTimer;
    private float powerTimer;
    private float difficulty;

    public SpawnSystem(float worldWidth, float worldHeight, GlowAtlas glow) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.glow = glow;
        reset();
    }

    public void reset() {
        entities.clear();
        obstacleTimer = 1.3f;
        orbTimer = 1.1f;
        powerTimer = 7f;
        difficulty = 0f;
    }

    public Array<WorldEntity> getEntities() {
        return entities;
    }

    public void update(float delta, float scrollSpeed, float distance, Cyclist player) {
        difficulty = MathUtils.clamp(distance / 4000f, 0f, 1f);
        obstacleTimer -= delta;
        orbTimer -= delta;
        powerTimer -= delta;

        if (obstacleTimer <= 0f) {
            spawnObstacles();
            obstacleTimer = MathUtils.lerp(1.4f, 0.55f, difficulty) + MathUtils.random(0f, 0.35f);
        }
        if (orbTimer <= 0f) {
            spawnOrbChain();
            orbTimer = MathUtils.random(1.2f, 2.2f);
        }
        if (powerTimer <= 0f) {
            spawnPowerUp();
            powerTimer = MathUtils.random(6.5f, 11f);
        }

        for (int i = entities.size - 1; i >= 0; i--) {
            WorldEntity e = entities.get(i);
            if (player.hasMagnet() && e.isOrb()) {
                e.pullTo(player.getX() + 50f, player.getDrawY() + 30f, delta);
            }
            e.update(delta, scrollSpeed);
            if (!e.alive) {
                entities.removeIndex(i);
            }
        }
    }

    public void drawGlow(SpriteBatch batch) {
        for (WorldEntity e : entities) {
            e.drawGlow(batch, glow);
        }
    }

    public void draw(ShapeRenderer shapes) {
        for (WorldEntity e : entities) {
            e.draw(shapes);
        }
    }

    private void spawnObstacles() {
        float x = worldWidth + 40f;
        int pattern = MathUtils.random(0, 4);
        switch (pattern) {
            case 0 -> entities.add(new WorldEntity(WorldEntity.Kind.CONE, randomLane(), x, worldHeight));
            case 1 -> entities.add(new WorldEntity(WorldEntity.Kind.BARRIER, randomLane(), x, worldHeight));
            case 2 -> {
                int open = randomLane();
                for (int lane = 0; lane < Lanes.COUNT; lane++) {
                    if (lane != open) {
                        entities.add(new WorldEntity(WorldEntity.Kind.CONE, lane, x, worldHeight));
                    }
                }
            }
            case 3 -> {
                entities.add(new WorldEntity(WorldEntity.Kind.BARRIER, Lanes.BOTTOM, x, worldHeight));
                entities.add(new WorldEntity(WorldEntity.Kind.CONE, Lanes.TOP, x + 90f, worldHeight));
            }
            default -> {
                entities.add(new WorldEntity(WorldEntity.Kind.CONE, randomLane(), x, worldHeight));
                entities.add(new WorldEntity(WorldEntity.Kind.BARRIER, randomLane(), x + 120f, worldHeight));
            }
        }
    }

    private void spawnOrbChain() {
        float x = worldWidth + 30f;
        int lane = randomLane();
        int style = MathUtils.random(0, 2);
        switch (style) {
            case 0 -> {
                for (int i = 0; i < 6; i++) {
                    entities.add(new WorldEntity(WorldEntity.Kind.ORB, lane, x + i * 42f, worldHeight));
                }
            }
            case 1 -> {
                for (int i = 0; i < 7; i++) {
                    int l = MathUtils.clamp((i % 3), 0, 2);
                    entities.add(new WorldEntity(WorldEntity.Kind.ORB, l, x + i * 38f, worldHeight));
                }
            }
            default -> {
                int lane2 = (lane + 1) % Lanes.COUNT;
                for (int i = 0; i < 5; i++) {
                    entities.add(new WorldEntity(WorldEntity.Kind.ORB, lane, x + i * 44f, worldHeight));
                    entities.add(new WorldEntity(WorldEntity.Kind.ORB, lane2, x + i * 44f + 22f, worldHeight));
                }
            }
        }
    }

    private void spawnPowerUp() {
        float x = worldWidth + 40f;
        int lane = randomLane();
        WorldEntity.Kind kind = switch (MathUtils.random(0, 2)) {
            case 0 -> WorldEntity.Kind.MAGNET;
            case 1 -> WorldEntity.Kind.SHIELD;
            default -> WorldEntity.Kind.TURBO;
        };
        entities.add(new WorldEntity(kind, lane, x, worldHeight));
    }

    private static int randomLane() {
        return MathUtils.random(0, Lanes.COUNT - 1);
    }
}
