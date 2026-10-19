package com.velogame.app.util;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class GlowAtlas implements Disposable {
    public final Texture cyan;
    public final Texture white;
    public final Texture red;
    public final Texture magenta;
    public final Texture gold;

    public GlowAtlas() {
        cyan = soft(96, 0f, 1f, 1f);
        white = soft(64, 1f, 1f, 1f);
        red = soft(64, 1f, 0.15f, 0.3f);
        magenta = soft(64, 1f, 0f, 0.7f);
        gold = soft(64, 1f, 0.9f, 0.25f);
    }

    private static Texture soft(int size, float r, float g, float b) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float c = (size - 1) * 0.5f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = (float) Math.sqrt((x - c) * (x - c) + (y - c) * (y - c)) / c;
                float a = Math.max(0f, 1f - d);
                a *= a;
                pm.setColor(r, g, b, a);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    @Override
    public void dispose() {
        cyan.dispose();
        white.dispose();
        red.dispose();
        magenta.dispose();
        gold.dispose();
    }
}
