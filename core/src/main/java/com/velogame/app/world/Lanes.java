package com.velogame.app.world;

import com.badlogic.gdx.math.MathUtils;

/** Three horizontal highway lanes (bottom / mid / top). */
public final class Lanes {
    public static final int COUNT = 3;
    public static final int BOTTOM = 0;
    public static final int MID = 1;
    public static final int TOP = 2;

    private Lanes() {
    }

    public static float y(int lane, float worldHeight) {
        float roadTop = worldHeight * 0.48f;
        float roadBottom = worldHeight * 0.06f;
        float span = roadTop - roadBottom;
        float t = lane / (COUNT - 1f);
        return roadBottom + span * t + 8f;
    }

    public static int clamp(int lane) {
        return MathUtils.clamp(lane, 0, COUNT - 1);
    }

    public static float roadTop(float worldHeight) {
        return worldHeight * 0.48f;
    }

    public static float roadBottom(float worldHeight) {
        return worldHeight * 0.06f;
    }
}
