package box.shoe.gameutils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Joseph on 12/1/2017.
 * Heavy work in progress. May need to wait on entity redefinition, animation classes
 */

public class ParticleEffect
{/*
    private InterpolatableEntity emitter;
    private int num;

    public ParticleEffect(InterpolatableEntity source, int numParticles)
    {
        emitter = source;
        num = numParticles;
    }

    public AbstractPaintable getVisual()
    {
        final ArrayList<InterpolatableEntity> particles = new ArrayList<>();
        Rand rand = new Rand();
        for (int i = 0; i < num; i++)
        {
            particles.add(new InterpolatableEntity(emitter.getX(), emitter.getY(), new Vector(rand.randomBetween(-10, 10), 10), new ParticlePaintable(10, 10)));
            Log.d("f", "h");
        }

        AbstractPaintable vis = new ContainPaintable(200, 200, particles);

        return vis;
    }

    private class ParticlePaintable extends AbstractPaintable
    {
        private ParticlePaintable(int w, int h)
        {
            super(w, h);
            setRegistrationPoint(w / 2, h / 2);
        }

        @Override
        protected void blueprintPaint(int width, int height, Canvas canvas)
        {
            paint.setColor(Color.BLACK);
            canvas.drawRect(0, 0, 100, 100, paint);
        }
    }

    private class ContainPaintable extends AbstractPaintable
    {
        ArrayList<InterpolatableEntity> p;
        private ContainPaintable(int w, int h, ArrayList p)
        {
            super(w, h);
            this.p = p;
            setRegistrationPoint(w / 2, h / 2);
        }

        @Override
        protected void blueprintPaint(int width, int height, Canvas canvas)
        {
            for (InterpolatableEntity particle : p)
            {
                particle.paint(canvas);
                particle.saveOldPosition();
                particle.moveWithVelocity();
                particle.setVelocity(new Vector(particle.getVelocity().getX() * .7, particle.getVelocity().getY()));
            }
        }
    }*/
}
