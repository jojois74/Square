package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.annotation.CallSuper;
import android.util.Log;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static android.support.v7.widget.AppCompatDrawableManager.get;

/** //TODO: maybe each entity should keep track of it's old value, so we can map each entity to it's old value for interpolation, no need for pesky IDs
 * Created by Joseph on 10/24/2017.
 * TODO: Rewrite the whole Entity system and favor more subclasses. It is a mess!
 * TODO: remove confirmable value class!
 * ideas: movable entity, drawable entity, velocity entity.........
 */
public class Entity implements Cleanable
{
    private double x;
    private double y;
    private double paintX;
    private double paintY;
    private Vector velocity;
    private boolean useInterpolation;

    private AbstractPaintable visual;

    public Entity(double initialX, double initialY, AbstractPaintable visual)
    {
        init(initialX, initialY, null, visual, true);
    }

    public Entity(double initialX, double initialY, Vector initialVelocity, AbstractPaintable initialVisual)
    {
        init(initialX, initialY, initialVelocity, initialVisual, true);
    }

    public Entity(double initialX, double initialY, Vector initialVelocity, AbstractPaintable initialVisual, boolean useInterpolation)
    {
        init(initialX, initialY, initialVelocity, initialVisual, useInterpolation);
    }

    private void init(double initialX, double initialY, Vector initialVelocity, AbstractPaintable initialVisual, boolean useInterpolation)
    {
        x = initialX;
        y = initialY;
        if (initialVelocity == null)
        {
            velocity = new Vector(0, 0);
        }
        else
        {
            velocity = initialVelocity;
        }
        visual = initialVisual;
        this.useInterpolation = useInterpolation;
    }

    public boolean usesInterpolation()
    {
        return useInterpolation;
    }

    //Clone constructor
    //TODO: call super annotation, for constructors?
    public Entity(Entity toClone)
    {
        x = toClone.getX();
        y = toClone.getY();
        velocity = toClone.getVelocity(); //No need to clone immutable vector
        visual = toClone.visual; //TODO: clone the visual, or at least provide and use a getter function.
        useInterpolation = toClone.useInterpolation;
    }

    public void setVisual(AbstractPaintable newVisual) //Should the Paintable be able to be changed? Probably.... for entities with different drawing states. But ideally the Paintable itself will change how it is drawn when necessary.
    {
        visual = newVisual; //And then update relevant stuff
    }

    public void paint(Canvas canvas) //Caller simply asks to paint this entity, and it strings the paint through to the visual!
    {
        visual.paint(getPaintX(), getPaintY(), canvas); //And then (or before) can paint any other thing relevant to this entity (additional things should probably be done in subclass which overrides this method (but first calls the super method to paint the Paintable of course!))
    } //TODO: remember to override paint for effects!

    /*
    public void paint(Canvas canvas, double interpolationRatio) //This one will auto paint at the entity's position using interpolation
    {
        //long currentPaintTime = System.nanoTime();
        //Log.d("DIFF", currentPaintTime - lastPaintTime+"");
        //Log.d("DIFF", (getPrevX() + ((getX() - getPrevX()) * interpolationRatio)) - lastPaintX+"");
        //Log.d("DIFF", ((getPrevX() + ((getX() - getPrevX()) * interpolationRatio)) - lastPaintX) / (currentPaintTime - lastPaintTime) +"");
        //lastPaintTime = currentPaintTime;
        //lastPaintX = getPrevX() + ((getX() - getPrevX()) * interpolationRatio);
        //Log.d("TAG", lastPaintX+"");
        //Log.d("TAG", interpolationRatio+"");
        visual.paint(getPrevX() + ((getX() - getPrevX()) * interpolationRatio), getPrevY() + ((getY() - getPrevY()) * interpolationRatio), canvas);
    }*/

    public void moveWithVelocity()
    {
        setX(getX() + velocity.getX());
        setY(getY() + velocity.getY());
    }

    public void setX(double newX)
    {
        x = newX;
    }

    public void setY(double newY)
    {
        y = newY;
    }

    public void setVelocity(Vector newVelocity)
    {
        velocity = newVelocity;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public Vector getVelocity()
    {
        return velocity;
    }

    public int getVisualWidth()
    {
        return visual.getWidth();
    }

    public int getVisualHeight()
    {
        return visual.getHeight();
    }

    public Point getVisualRegistrationPoint()
    {
        return visual.getRegistrationPoint();
    }

    @SuppressLint("MissingSuperCall") //Since this is the top level implementor
    public void cleanup()
    {
        velocity = null;
        visual.cleanup();
        //More cleanup
    }

    public double getPaintX()
    {
        return paintX;
    }

    public void setPaintX(double paintX)
    {
        this.paintX = paintX;
    }

    public double getPaintY()
    {
        return paintY;
    }

    public void setPaintY(double paintY)
    {
        this.paintY = paintY;
    }
}
