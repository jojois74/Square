package box.shoe.gameutils;

import android.graphics.Canvas;
import android.os.Bundle;

/**
 * Created by Joseph on 12/9/2017.
 */

public interface Paintable
{
    void paint(Canvas canvas); //TODO: maybe no canvas so it can't outstep its boundaries (it would return a canvas it painted on, but this would be slower)
                                                //TODO: but I like abstraction of suggesting an x, y (height, width?) and having it draw its interpretation.
/*
    double getPaintWidth(); //TODO: maybe double -> float to work better with canvas draw rect methods?
    double getPaintHeight();

    void setPaintWidth(double newPaintWidth);
    void setPaintHeight(double newPaintHeight);*/
}
