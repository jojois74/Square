package box.shoe.gameutils;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;

/**
 * Created by Joseph on 12/1/2017.
 * Heavy work in progress. May need to wait on entity redefinition, animation classes
 */
//TODO: make a good way to manage particle effects, such as static method for creation
    //TODO: rename to particle source, becaue it can spawn particles by calling emit() until we stop it.
    //TODO: maybe static functions to emit? or singleton
public class ParticleEffect //implements Paintable
{/*
    private static Rand random = new Rand();
    private Vector emitter;
    public ArrayList<Particle> particles;

    public ParticleEffect(Vector sourcePosition)
    {
        emitter = sourcePosition;
        particles = new ArrayList<>();

        for (int i = 0; i < 10; i++)
        {
            Vector velocity = new Vector(random.intFrom(-5, 5), random.intFrom(-5, 5));
            Particle particle = new Particle(emitter.getX(), emitter.getY(), 0, 0, velocity);
            particles.add(particle);
        }
    }

    @Override
    public void paint(Canvas canvas)
    {
        for (Particle particle : particles)
        {
            particle.paint((int) Math.round(particle.getX()), (int) Math.round(particle.getY()), canvas);
        }
    }

    public void update()
    {
        for (Particle particle : particles)
        {
            particle.update();
        }
    }*/
/*
    public class Particle extends InterpolatableEntity implements Paintable
    {*/
        /*package*//* Particle(double initialX, double initialY, double initialWidth, double initialHeight, Vector initialVelocity)
        {
            super(initialX, initialY, initialWidth, initialHeight, initialVelocity);
        }

        @Override
        public void paint(Canvas canvas)
        {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.GREEN);
            canvas.drawRect((float) getX(), (float) getY(), (float) (getX() + 10), (float) (getY() + 10), paint);
        }

        @SuppressLint("MissingSuperCall")
        @Override
        public void cleanup()
        {

        }
    }*/

    public static class Builder
    {
        /*
        Things we need:
        emitter (mandatory) maybe...
        number of particles per emit
        size of particles
        color
        shape
        duration of each emit
        speed
        effects (fade/disappear)
        some function to change appearance/color
        acceleration of particles,
        emiter location center or line.
        some randomness factors/fudge factors
         */

        private int numberOfParticles = 12;
        private int durationMS = 1000;
        private double particleSpeed = 5;

        public Builder(int UPS)
        {

        }

        public ParticleEffect build()
        {
            return null;
        }

        // Create immutable copy of this builder from which to create more particles.
        public Builder etch()
        {
            return null;
        }

        public Builder number(int numberOfParticles)
        {
            this.numberOfParticles = numberOfParticles;
            return this;
        }

        public Builder speed(double particleSpeed)
        {
            this.particleSpeed = particleSpeed;
            return this;
        }

        public Builder duration(int durationMS)
        {
            this.durationMS = durationMS;
            return this;
        }
    }
}
