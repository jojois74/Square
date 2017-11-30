package box.shoe.gameutils;

import java.util.Random;

public class Rand extends Random
{
    public Rand()
    {
        super();
    }

    public int randomBetween(int min, int max)
    {
        return nextInt(max + 1 - min) + min;
    }
}
