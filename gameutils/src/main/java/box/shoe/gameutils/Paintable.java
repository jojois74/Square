package box.shoe.gameutils;

import android.graphics.Canvas;

/**
 * Created by Joseph on 12/9/2017.
 */

public interface Paintable extends Cleanable
{
    void paint(int x, int y, Canvas canvas); //TODO" maybe no canvas so it can't outstep its boundaries (it would return a canvas it painted on, but this owuld be slower)

    int getWidth();
    int getHeight();

    void setWidth(int newWidth);
    void setHeight(int newHeight);
}
