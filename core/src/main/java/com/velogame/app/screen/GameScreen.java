package com.velogame.app.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.velogame.app.Main;
import com.velogame.app.NeonTheme;
import com.velogame.app.entity.Cyclist;
import com.velogame.app.entity.PowerUpType;
import com.velogame.app.entity.WorldEntity;
import com.velogame.app.ui.Hud;
import com.velogame.app.util.GlowAtlas;
import com.velogame.app.world.ParallaxCity;
import com.velogame.app.world.SpawnSystem;

/** VelocitY — neon side-scrolling highway runner. */
public class GameScreen extends ScreenAdapter {
    public static final float WORLD_WIDTH = 960f;
    public static final float WORLD_HEIGHT = 540f;

    private static final float MIN_SPEED = 220f;
    private static final float MAX_SPEED = 520f;
    private static final float BASE_SPEED = 280f;

    private final Main game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapes;

    private GlowAtlas glow;
    private ParallaxCity city;
    private SpawnSystem spawner;
    private Cyclist cyclist;
    private Hud hud;

    private float scrollSpeed = BASE_SPEED;
    private float distance;
    private float bonusScore;
    private boolean gameOver;
    private boolean upWasDown;
    private boolean downWasDown;

    public GameScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapes = new ShapeRenderer();
    }

    @Override
    public void show() {
        glow = new GlowAtlas();
        city = new ParallaxCity(WORLD_WIDTH, WORLD_HEIGHT);
        spawner = new SpawnSystem(WORLD_WIDTH, WORLD_HEIGHT, glow);
        cyclist = new Cyclist(WORLD_HEIGHT, glow);
        hud = new Hud(WORLD_WIDTH, WORLD_HEIGHT);
        restart();
    }

    private void restart() {
        scrollSpeed = BASE_SPEED;
        distance = 0f;
        bonusScore = 0f;
        gameOver = false;
        upWasDown = false;
        downWasDown = false;
        cyclist.reset();
        spawner.reset();
        city.reset();
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f);
        if (gameOver) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                restart();
            }
        } else {
            handleInput();
            update(delta);
        }
        draw();
    }

    private void handleInput() {
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        if (up && !upWasDown) {
            cyclist.laneUp();
        }
        if (down && !downWasDown) {
            cyclist.laneDown();
        }
        upWasDown = up;
        downWasDown = down;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            cyclist.jump();
        }
    }

    private void update(float delta) {
        float target = BASE_SPEED + MathUtils.clamp(distance / 5000f, 0f, 1f) * 160f;
        if (cyclist.isTurbo()) {
            target = Math.min(MAX_SPEED, target + 160f);
        }
        scrollSpeed = MathUtils.clamp(MathUtils.lerp(scrollSpeed, target, 2f * delta), MIN_SPEED, MAX_SPEED);

        float speedRatio = (scrollSpeed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
        distance += scrollSpeed * delta;
        city.update(delta, scrollSpeed);
        cyclist.update(delta, speedRatio);
        spawner.update(delta, scrollSpeed, distance, cyclist);
        resolveCollisions();
    }

    private void resolveCollisions() {
        for (WorldEntity e : spawner.getEntities()) {
            if (!e.alive || !e.getBounds().overlaps(cyclist.getBounds())) {
                continue;
            }
            if (e.isObstacle()) {
                // Jump clears low-ish obstacles if feet are high enough
                if (cyclist.getDrawY() > e.y + 30f) {
                    continue;
                }
                e.alive = false;
                if (!cyclist.absorbHit()) {
                    gameOver = true;
                }
            } else if (e.isOrb()) {
                e.alive = false;
                bonusScore += 10f;
            } else {
                PowerUpType power = e.asPowerUp();
                if (power != null) {
                    e.alive = false;
                    cyclist.activate(power);
                    bonusScore += 40f;
                }
            }
        }
    }

    private float score() {
        return distance * 0.12f + bonusScore;
    }

    private void draw() {
        ScreenUtils.clear(NeonTheme.BG_DEEP.r, NeonTheme.BG_DEEP.g, NeonTheme.BG_DEEP.b, 1f);
        viewport.apply();
        camera.update();

        SpriteBatch batch = game.getBatch();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        batch.begin();
        city.draw(batch);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        city.drawNeonLines(shapes);
        shapes.end();

        batch.begin();
        spawner.drawGlow(batch);
        if (!gameOver) {
            cyclist.drawGlow(batch);
        }
        batch.end();

        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        spawner.draw(shapes);
        if (!gameOver) {
            cyclist.draw(shapes);
        }
        float norm = (scrollSpeed - MIN_SPEED) / (MAX_SPEED - MIN_SPEED);
        hud.drawSpeedFill(shapes, norm);
        hud.drawIcons(shapes, cyclist);
        if (gameOver) {
            hud.drawGameOverPanel(shapes);
        }
        shapes.end();

        batch.begin();
        hud.drawText(batch, score(), scrollSpeed * 0.35f, cyclist, gameOver);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (glow != null) {
            glow.dispose();
        }
        if (city != null) {
            city.dispose();
        }
        if (hud != null) {
            hud.dispose();
        }
        shapes.dispose();
    }
}
