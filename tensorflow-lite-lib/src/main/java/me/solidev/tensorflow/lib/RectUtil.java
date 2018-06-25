package me.solidev.tensorflow.lib;

import android.graphics.RectF;

/**
 * @author _SOLID
 * @since 2018/5/9.
 */
class RectUtil {
    static Float getRectOverlapRatio(RectF a, RectF b) {
        if (a.intersect(b)) {
            RectF insetRect = new RectF(Math.max(a.left, b.left), Math.max(a.top, b.top),
                    Math.min(a.right, b.right),
                    Math.min(a.bottom, b.bottom));
            float insetArea = getRectArea(insetRect);
            return insetArea / (getRectArea(a) + getRectArea(b) - insetArea);
        } else {
            return 0f;
        }


    }

    static Float getRectArea(RectF rectF) {
        return (rectF.right - rectF.left) * (rectF.bottom - rectF.top);
    }
}