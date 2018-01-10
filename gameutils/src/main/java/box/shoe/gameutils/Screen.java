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

    void paintFrame(GameState gameState);
    void paintBitmap(Bitmap bitmap);

    int getWidth();
    int getHeight();

    void setOnTouchListener(View.OnTouchListener onTouchListener);

    boolean isVisible(Paintable paintable);
    boolean isInbounds(Entity entity);
}
