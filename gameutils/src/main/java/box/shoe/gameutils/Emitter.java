package box.shoe.gameutils;

import android.graphics.Canvas;

/**
 * Created by Joseph on 1/15/2018.
 */

public interface Emitter
{
    void emit(double xPos, double yPos);
    void update();
    void paint(Canvas canvas);
}
