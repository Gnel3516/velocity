package com.velogame.app.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.velogame.app.NeonTheme;
import com.velogame.app.entity.Cyclist;
import com.velogame.app.entity.PowerUpType;

public class Hud implements Disposable {
    private final BitmapFont font = new BitmapFont();
    private final GlyphLayout layout = new GlyphLayout();
    private final float w;
    private final float h;

    public Hud(float worldWidth, float worldHeight) {
        this.w = worldWidth;
        this.h = worldHeight;
        font.setUseIntegerPositions(false);
    }

    public void drawIcons(ShapeRenderer shapes, Cyclist player) {
        float x = 20f;
        float y = h - 78f;
        if (player.hasMagnet()) {
            drawChip(shapes, x, y, player.getMagnetTimer() / PowerUpType.MAGNET.duration, NeonTheme.MAGENTA);
            y -= 28f;
        }
        if (player.hasShield()) {
            drawChip(shapes, x, y, 1f, NeonTheme.CYAN);
            y -= 28f;
        }
        if (player.isTurbo()) {
            drawChip(shapes, x, y, player.getTurboTimer() / PowerUpType.TURBO.duration, NeonTheme.GOLD);
        }

        // Speed bar
        shapes.setColor(NeonTheme.a(NeonTheme.CYAN, 0.15f));
        shapes.rect(20f, h - 48f, 170f, 7f);
    }

    public void drawSpeedFill(ShapeRenderer shapes, float norm) {
        shapes.setColor(NeonTheme.CYAN_SOFT);
        shapes.rect(20f, h - 48f, 170f * Math.max(0f, Math.min(1f, norm)), 7f);
        shapes.setColor(NeonTheme.a(NeonTheme.WHITE, 0.65f));
        shapes.rect(20f, h - 48f, 170f, 1.3f);
    }

    private void drawChip(ShapeRenderer shapes, float x, float y, float fill, com.badlogic.gdx.graphics.Color color) {
        float bw = 140f;
        float bh = 12f;
        shapes.setColor(NeonTheme.a(NeonTheme.WHITE, 0.12f));
        shapes.rect(x, y, bw, bh);
        shapes.setColor(NeonTheme.a(color, 0.9f));
        shapes.rect(x, y, bw * Math.max(0f, Math.min(1f, fill)), bh);
        shapes.setColor(NeonTheme.a(color, 0.55f));
        shapes.rectLine(x, y, x + bw, y, 1.2f);
        shapes.rectLine(x, y + bh, x + bw, y + bh, 1.2f);
    }

    public void drawText(SpriteBatch batch, float score, float speed, Cyclist player, boolean gameOver) {
        font.getData().setScale(1.15f);
        font.setColor(NeonTheme.WHITE);
        font.draw(batch, String.format("SCORE  %.0f", score), w * 0.5f, h - 18f, 0, Align.center, false);

        font.getData().setScale(0.95f);
        font.setColor(NeonTheme.CYAN_SOFT);
        font.draw(batch, String.format("SPEED  %.0f", speed), 22f, h - 22f);

        font.setColor(NeonTheme.HUD_DIM);
        font.draw(batch, "W/S lanes   SPACE jump", w * 0.5f, 20f, 0, Align.center, false);

        font.getData().setScale(0.85f);
        float ty = h - 70f;
        if (player.hasMagnet()) {
            font.setColor(NeonTheme.MAGENTA);
            font.draw(batch, "MAGNET  " + String.format("%.1fs", player.getMagnetTimer()), 24f, ty);
            ty -= 28f;
        }
        if (player.hasShield()) {
            font.setColor(NeonTheme.CYAN);
            font.draw(batch, "SHIELD  READY", 24f, ty);
            ty -= 28f;
        }
        if (player.isTurbo()) {
            font.setColor(NeonTheme.GOLD);
            font.draw(batch, "TURBO  " + String.format("%.1fs", player.getTurboTimer()), 24f, ty);
        }

        if (gameOver) {
            font.getData().setScale(2.1f);
            font.setColor(NeonTheme.CYAN);
            String title = "GAME OVER";
            layout.setText(font, title);
            font.draw(batch, title, (w - layout.width) * 0.5f, h * 0.62f);

            font.getData().setScale(1.2f);
            font.setColor(NeonTheme.WHITE);
            String line = String.format("Final Score  %.0f", score);
            layout.setText(font, line);
            font.draw(batch, line, (w - layout.width) * 0.5f, h * 0.50f);

            font.setColor(NeonTheme.CYAN_SOFT);
            String hint = "Press  R  or  SPACE  to Restart";
            layout.setText(font, hint);
            font.draw(batch, hint, (w - layout.width) * 0.5f, h * 0.38f);
        }
        font.getData().setScale(1f);
    }

    public void drawGameOverPanel(ShapeRenderer shapes) {
        shapes.setColor(NeonTheme.a(NeonTheme.BG_DEEP, 0.72f));
        shapes.rect(0f, 0f, w, h);
        float pw = w * 0.55f;
        float ph = h * 0.38f;
        float px = (w - pw) * 0.5f;
        float py = h * 0.3f;
        shapes.setColor(NeonTheme.a(NeonTheme.BG_NIGHT, 0.95f));
        shapes.rect(px, py, pw, ph);
        shapes.setColor(NeonTheme.a(NeonTheme.CYAN, 0.65f));
        shapes.rectLine(px, py, px + pw, py, 2f);
        shapes.rectLine(px, py + ph, px + pw, py + ph, 2f);
        shapes.rectLine(px, py, px, py + ph, 2f);
        shapes.rectLine(px + pw, py, px + pw, py + ph, 2f);
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}
