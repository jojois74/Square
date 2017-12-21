package box.shoe.gameutils;

/**
 * Created by Joseph on 12/19/2017.
 */

public class ParticleSource
{
    private ParticleSource()
    {

    }

    public void emit()
    {

    }

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

        private int numberOfParticles = 12; //TODO: maby emit should only emit one at a time, so this is not necessary?
        private int durationMS = 1000;
        private double particleSpeed = 5;

        public Builder(int UPS)
        {

        }

        public ParticleEffect build()
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
