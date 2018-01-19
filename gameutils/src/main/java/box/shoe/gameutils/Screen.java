package box.shoe.gameutils;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by Joseph on 1/1/2018.
 */

public interface Screen
{
    // Called before drawing begins, once the game, and thus the screen, has dimensions.
    void initialize();
    boolean hasInitialized();

    void preparePaint();
    void unpreparePaint();
    boolean hasPreparedPaint();

    Bitmap getScreenshot();

    void paintFrame(GameState gameState);
    void paintStatic(Bitmap bitmap);
    void paintStatic(int color);

    int getWidth();
    int getHeight();

    View asView();

    void setOnTouchListener(View.OnTouchListener onTouchListener);

    void unregisterReadyForPaintingListener();
}
