package box.shoe.gameutils;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;

/**
 * Created by Joseph on 12/1/2017.
 * Heavy work in progress. May need to wait on entity redefinition, animation classes
 */

public class ParticleEffect
{
    private Entity emitter;
    private int num;

    public ParticleEffect(Entity source, int numParticles)
    {
        emitter = source;
        num = numParticles;
    }

    public AbstractPaintable getVisual()
    {
        final ArrayList<Entity> particles = new ArrayList<>();
        Rand rand = new Rand();
        for (int i = 0; i < num; i++)
        {
            particles.add(new Entity(emitter.getX(), emitter.getY(), new Vector(rand.randomBetween(-10, 10), 10), new AbstractPaintable()
            {
                @Override
                protected void blueprintPaint(int width, int height, Canvas canvas)
                {
                    paint.setColor(Color.BLACK);
                    canvas.drawRect(0, 0, 10, 10, paint);
                }
            }));
        }

        AbstractPaintable vis = new AbstractPaintable()
        {
            @Override
            protected void blueprintPaint(int width, int height, Canvas canvas)
            {
                for (Entity particle : particles)
                {
                    particle.paint(canvas);
                    particle.saveOldPosition();
                    particle.moveWithVelocity();
                    particle.setVelocity(new Vector(particle.getVelocity().getX() * .7, particle.getVelocity().getY()));
                }
            }
        };

        return vis;
    }
}
