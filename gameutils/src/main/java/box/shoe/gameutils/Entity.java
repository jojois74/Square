package box.shoe.gameutils;

import android.graphics.Canvas;
import android.graphics.Point;
import android.support.annotation.CallSuper;
import android.util.Log;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static android.support.v7.widget.AppCompatDrawableManager.get;

/**
 * Created by Joseph on 10/24/2017.
 * TODO: Rewrite the whole Entity system and favor more subclasses. It is a mess!
 * ideas: movable entity, drawable entity, velocity entity.........
 */
public class Entity
{
    private ConfirmableValue<Double> x;
    private ConfirmableValue<Double> y;
    private Vector velocity;

    private AbstractPaintable visual;

    private double lastPaintX = 0;
    private long lastPaintTime = 0;

    public Entity(double initialX, double initialY, AbstractPaintable visual)
    {
        x = new ConfirmableValue<>(initialX);
        y = new ConfirmableValue<>(initialY);
        velocity = new Vector(0, 0);
        this.visual = visual;
    }

    public Entity(double initialX, double initialY, Vector initialVelocity, AbstractPaintable initialVisual)
    {
        x = new ConfirmableValue<>(initialX);
        y = new ConfirmableValue<>(initialY);
        velocity = initialVelocity;
        visual = initialVisual;
    }

    public void setVisual(AbstractPaintable newVisual) //Should the Paintable be able to be changed? Probably.... for entities with different drawing states. But ideally the Paintable itself will change how it is drawn when necessary.
    {
        visual = newVisual; //And then update relevant stuff
    }

    public void paint(Canvas canvas) //Caller simply asks to paint this entity, and it strings the paint through to the visual!
    {
        visual.paint(getX(), getY(), canvas); //And then (or before) can paint any other thing relevant to this entity (additional things should probably be done in subclass which overrides this method (but first calls the super method to paint the Paintable of course!))
    }
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
    }

    public void moveWithVelocity()
    {
        setX(getX() + velocity.getX());
        setY(getY() + velocity.getY());
    }

    public void setX(double newX)
    {
        x.setImpressedValue(newX);
    }

    public void setY(double newY)
    {
        y.setImpressedValue(newY);
    }

    public void setVelocity(Vector newVelocity)
    {
        velocity = newVelocity;
    }

    public double getX()
    {
        return x.getImpressedValue();
    }

    public double getY()
    {
        return y.getImpressedValue();
    }

    public Vector getVelocity()
    {
        return velocity;
    }

    public double getPrevX()
    {
        return x.getConfirmedValue();
    }

    public double getPrevY()
    {
        return y.getConfirmedValue();
    }

    public void saveOldPosition()
    {
        x.confirmValue();
        y.confirmValue();
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

    public void cleanup()
    {
        velocity = null;
        //More cleanup
    }
}
