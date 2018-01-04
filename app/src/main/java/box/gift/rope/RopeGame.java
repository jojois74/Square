package box.gift.rope;

import android.content.Context;

import java.util.Iterator;
import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.EntityCollisions;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.TaskScheduler;
import box.shoe.gameutils.L;
import box.shoe.gameutils.ParticleEffect;
import box.shoe.gameutils.Rand;

/**
 * Created by Joseph on 10/23/2017.
 */

public class RopeGame extends AbstractGameEngine
{
    // Consts
    private static final int TARGET_UPS = 25; //No need to make ups too high!

    // Objs
    private Player player;
    private Entity topBar;
    private Entity botBar;
    private LinkedList<Wall> walls;
    private LinkedList<Coin> coins;
    private Rand rand;
    private TaskScheduler scheduler;
    private ParticleEffect part;
    private LinkedList<ParticleEffect> effects;

    public RopeGame(Context appContext, AbstractGameSurfaceView screen)
    {
        super(appContext, RopeGame.TARGET_UPS, screen);
        scheduler = new TaskScheduler(RopeGame.TARGET_UPS);
        walls = new LinkedList<>();
        coins = new LinkedList<>();
        effects = new LinkedList<>();
        rand = new Rand();
    }

    @Override
    protected void initialize()
    {
        int barHeight = 10;
        topBar = new Entity(0, getGameHeight(), Integer.MAX_VALUE, barHeight); //TODO: need some way to attach to camera - make fixed.
        botBar = new Entity(0, 0, Integer.MAX_VALUE, barHeight);

        player = new Player(0, 7 * getGameHeight() / 8);
        Runnable generateWallAndCoin = new Runnable()
        {
            @Override
            public void run()
            {
                int wallHeight = getGameHeight() / 3;
                int wallWidth = 20;
                double wallX = player.getX() + getGameWidth();
                int holePosition = rand.randomBetween(0, 2);
                if (holePosition != 0)
                {
                    Wall wall = new Wall(wallX, 0, wallWidth, wallHeight);
                    walls.add(wall);
                }
                if (holePosition != 1)
                {
                    Wall wall = new Wall(wallX, wallHeight, wallWidth, wallHeight);
                    walls.add(wall);
                }
                if (holePosition != 2)
                {
                    Wall wall = new Wall(wallX, 2 * wallHeight, wallWidth, wallHeight);
                    walls.add(wall);
                }

                int rand = random.randomBetween(0, 0);
                int margin = 40;
                int randHeight = random.randomBetween(margin, getGameHeight() - margin);
                if (rand == 0)
                {
                    coins.add((new Coin(wallX + getGameWidth() * .3, randHeight, 80, 80)));
                }
            }
        };
        scheduler.schedule(2700, 0, generateWallAndCoin);
        generateWallAndCoin.run();
    }

    @Override
    protected void update()
    {
        scheduler.tick();

        if (EntityCollisions.collideRectangle(player, topBar) || EntityCollisions.collideRectangle(player, botBar))
        {
            playerDead();
        }

        if (screenTouched)
        {
            player.velocity = Player.jumpVelocity;
        }

        double oldPlayerX = player.getX();
        player.update();

        boolean passingWall = false;
        Iterator<Wall> wallIterator = walls.iterator();
        while (wallIterator.hasNext())
        {
            Wall wall = wallIterator.next();
            if (oldPlayerX < wall.getX() && player.getX() > wall.getX())
            {
                passingWall = true;
            }
            if (EntityCollisions.collideRectangle(player, wall))
            {
                playerDead();
            }
            if (wall.getX() - wall.registration.getX() + wall.width < 0)
            {
                wallIterator.remove();
            }
        }
        if (passingWall)
        {
            score++; //TODO: score should go up a bit later, so you do not gain a point upon dying against a wall
        }

        Iterator<Coin> coinIterator = coins.iterator();
        while (coinIterator.hasNext())
        {
            Coin coin = coinIterator.next();
            if (EntityCollisions.collideRectangle(player, coin)) //TODO: use circle collision for coins?
            {
                score++;
                coinIterator.remove();
            }
        }

        /*
        for (ParticleEffect effect : effects)
        {
            effect.update();
        }*/
    }

    @Override
    protected void saveGameState(GameState gameState)
    {
        // Store the player for painting.
        gameState.put("player", player);

        // Store the walls for painting.
        gameState.put("walls", walls);

        // Store the coins for painting.
        gameState.put("coins", coins);

        // Store the score for painting.
        gameState.put("score", score);

        //Store the bars for painting.
        gameState.put("top", topBar);
        gameState.put("bot", botBar);

/*
        // Save the effect.
        for (ParticleEffect effect : effects)
        {
            for (ParticleEffect.Particle particle : effect.particles)
            {
                gameState.saveInterpolatableEntity(particle);
            }
        }
        gameState.saveData("effects", effects);*/
    }

    private void playerDead()
    {
        L.d("DEAD", "lifecycleRope");
        //stopGame();
    }

    @Override
    public void cleanup()
    {
        // Cleanup the parent first to stop all threads, so we can be sure that the Entities are not used.
        super.cleanup();
        player.cleanup();
        player = null;
        rand = null;
        //Cleanup scheduler TODO
        scheduler = null;
        //Cleanup walls TODO
    }
}
