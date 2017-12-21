package box.shoe.gameutils;

import android.graphics.Canvas;
import android.os.Bundle;

/**
 * Created by Joseph on 12/9/2017.
 */

public interface Paintable
{
    void paint(int paintX, int paintY, Canvas canvas); //TODO: maybe no canvas so it can't outstep its boundaries (it would return a canvas it painted on, but this would be slower)
                                                //TODO: but I like abstraction of suggesting an x, y (height, width?) and having it draw its interpretation.
/*
    double getPaintWidth(); //TODO: maybe double -> float to work better with canvas draw rect methods?
    double getPaintHeight();

    void setPaintWidth(double newPaintWidth);
    void setPaintHeight(double newPaintHeight);*/

    // Note that putting interpolation functions here means that only Paintables can be Interpolated.
    // This forces each object which has interpolated fiels to hold their own painting instructions.
    // I personally think this is a good thing.
    void putInterpolatables(Bundle request);
    void readInterpolatables(Bundle response);
}
