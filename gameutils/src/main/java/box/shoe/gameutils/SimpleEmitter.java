package box.shoe.gameutils;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Joseph on 12/19/2017.
 */

public class SimpleEmitter implements Emitter, Paintable
{
    private Paint paint;
    private HashMap<Entity, Integer> particles;
    private double size;
    private double speed;
    private double startRadians;
    private double endRadians;
    private int duration;

    private SimpleEmitter(double size, double speed, int color, double startRadians, double endRadians, int duration)
    {
        this.size = size;
        this.speed = speed;
        this.startRadians = startRadians;
        this.endRadians = endRadians;
        this.duration = duration;

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        particles = new HashMap<>();
    }

    // Emit so center point is at (xPos, yPos)
    public void emit(double xPos, double yPos)
    {
        Entity particle = new Entity(xPos - size / 2, yPos - size / 2);
        particle.velocity = Vector.fromPolarDegrees(speed, Rand.instance.intFrom((int) Math.toDegrees(startRadians), (int) Math.toDegrees(endRadians))); //TODO: we are turning deg into rad then back then forth. find better way (priority=low)
        particles.put(particle, 0);
    }

    public void update()
    {
        Iterator<Entity> iterator = particles.keySet().iterator();
        while (iterator.hasNext())
        {
            Entity particle = iterator.next();
            if (particles.get(particle) >= duration)
            {
                iterator.remove();
            }
            else
            {
                particle.update();
                particles.put(particle, particles.get(particle) + 1);
            }
        }
    }

    @Override
    public void paint(Canvas canvas)
    {
        for (Entity particle : particles.keySet())
        {
            canvas.drawRect((float) particle._getX(), (float) particle._getY(),
                    (float) (particle._getX() + size),
                    (float) (particle._getY() + size), paint);
        }
    }

    public static class Builder
    {
        private int duration = 1000;
        private double speed = 5;
        private double size = 20;
        private int color;
        private double startRadians;
        private double endRadians;

        public Builder()
        {

        }

        public SimpleEmitter build()
        {
            return new SimpleEmitter(size, speed, color, startRadians, endRadians, duration);
        }

        public Builder speed(double particleSpeed)
        {
            this.speed = particleSpeed;
            return this;
        }

        public Builder duration(int duration)
        {
            this.duration = duration;
            return this;
        }

        public Builder color(int color)
        {
            this.color = color;
            return this;
        }

        //Width/Height or square
        public Builder size(double size)
        {
            this.size = size;
            return this;
        }

        public Builder spanDegrees(double startDegrees, double endDegrees)
        {
            return spanRadians(Math.toRadians(startDegrees), Math.toRadians(endDegrees));
        }

        public Builder spanRadians(double startRadians, double endRadians)
        {
            this.startRadians = startRadians;
            this.endRadians = endRadians;
            return this;
        }
    }
}
