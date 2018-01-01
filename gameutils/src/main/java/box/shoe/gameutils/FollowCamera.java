package box.shoe.gameutils;

import android.graphics.Canvas;

/**
 * Created by Joseph on 1/1/2018.
 */

public class FollowCamera implements Camera
{
    // Follow type consts.
    public static final int FOLLOW_X = 0;
    public static final int FOLLOW_Y = 1;
    public static final int FOLLOW_XY = 2;

    private Entity follow;
    private int type;
    //private Vector offset;

    public FollowCamera(int followType)
    {
        type = followType;
        follow = null;
    }

    public void follow(Entity entity)
    {
        this.follow = entity;
    }

    public void setFollowType(int followType)
    {
        type = followType;
    }

    /*public void setOffset(Vector offset)
    {
        this.offset = offset;
    }*/

    @Override
    public void view(Canvas canvas)
    {
        if (follow == null)
        {
            throw new IllegalStateException("Must first call follow(Entity)!");
        }
        canvas.save();
        switch (type)
        {
            case FOLLOW_X:
                canvas.translate((float) (follow._position.getX()), 0);
                break;

            case FOLLOW_Y:
                canvas.translate(0, (float) -follow._position.getY());
                break;

            case FOLLOW_XY:
                canvas.translate((float) -follow._position.getX(), (float) -follow._position.getY());
                break;
        }
    }

    @Override
    public void unview(Canvas canvas)
    {
        canvas.restore();
    }
}
