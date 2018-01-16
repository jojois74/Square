package box.shoe.gameutils;

import java.util.Random;

public class Rand extends Random
{
    /*pack*/ static final Rand instance = new Rand();

    public Rand()
    {
        super();
    }

    public Rand(long seed)
    {
        super(seed);
    }

    public int intFrom(int min, int max)
    {
        return nextInt(max + 1 - min) + min;
    }
}
