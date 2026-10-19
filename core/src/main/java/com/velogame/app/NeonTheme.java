package com.velogame.app;

import com.badlogic.gdx.graphics.Color;

/** Neon cyan / white / night palette. */
public final class NeonTheme {
    public static final Color BG_DEEP = rgb(0x050814);
    public static final Color BG_NIGHT = rgb(0x0D1117);
    public static final Color CYAN = rgb(0x00FFFF);
    public static final Color CYAN_SOFT = rgb(0x00E5FF);
    public static final Color WHITE = Color.WHITE;
    public static final Color ROAD = rgb(0x0A0E18);
    public static final Color BUILDING = rgb(0x0A1220);
    public static final Color NEON_RED = rgb(0xFF1744);
    public static final Color MAGENTA = rgb(0xFF00AA);
    public static final Color GOLD = rgb(0xFFE566);
    public static final Color HUD_DIM = new Color(1f, 1f, 1f, 0.6f);

    private NeonTheme() {
    }

    public static Color rgb(int v) {
        return new Color(((v >> 16) & 255) / 255f, ((v >> 8) & 255) / 255f, (v & 255) / 255f, 1f);
    }

    public static Color a(Color c, float alpha) {
        return new Color(c.r, c.g, c.b, alpha);
    }
}
