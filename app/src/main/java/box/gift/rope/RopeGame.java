package box.gift.rope;

import android.content.Context;
import android.view.MotionEvent;

import java.util.Iterator;
import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.InterpolatableEntity;
import box.shoe.gameutils.EntityCollisions;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.GameTasker;
import box.shoe.gameutils.L;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.ParticleEffect;
import box.shoe.gameutils.Rand;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 10/23/2017.
 */

public class RopeGame extends AbstractGameEngine
{
    // Consts
    private static final int TARGET_UPS = 25; //No need to make ups too high!
    private Paintable wallPaintable;
    private Paintable coinPaintable;

    // Objs
    private Player player;
    private LinkedList<Wall> walls;
    private LinkedList<Coin> coins;
    private Rand rand;
    private GameTasker scheduler;
    private ParticleEffect part;
    private LinkedList<ParticleEffect> effects;

    public RopeGame(Context appContext, AbstractGameSurfaceView screen)
    {
        super(appContext, RopeGame.TARGET_UPS, screen);
        scheduler = new GameTasker(RopeGame.TARGET_UPS);
        walls = new LinkedList<>();
        coins = new LinkedList<>();
        effects = new LinkedList<>();
        rand = new Rand();
    }

    @Override
    protected void initialize()
    {
        player = new Player(getGameWidth() / 6, getGameHeight() / 8);
        Runnable generateWallAndCoin = new Runnable()
        {
            @Override
            public void run()
            {
                int third = getGameHeight() / 3;
                int hole = rand.randomBetween(0, 2);
                if (hole != 0)
                {
                    Wall wall = new Wall(getGameWidth(), 0, 25, third);
                    walls.add(wall);
                }
                if (hole != 1)
                {
                    Wall wall = new Wall(getGameWidth(), third, 25, third);
                    walls.add(wall);
                }
                if (hole != 2)
                {
                    Wall wall = new Wall(getGameWidth(), 2 * third, 25, third);
                    walls.add(wall);
                }

                int rand = random.randomBetween(0, 0);
                int margin = 40;
                int randHeight = random.randomBetween(margin, getGameHeight() - margin);
                if (rand == 0)
                {
                    coins.add((new Coin(getGameWidth() * 1.3, randHeight, 80, 80)));
                }
            }
        };
        scheduler.register(2700, 0, generateWallAndCoin);
        generateWallAndCoin.run();
    }

    @Override
    protected void update()
    {
        scheduler.tick();

        if (player.getY() < 0 || player.getY() > getGameHeight())
        {
            playerDead();
        }

        if (screenTouched)
        {
            player.velocity = Player.jumpVelocity;
        }
        player.update();

        boolean passingWall = false;
        Iterator<Wall> wallIterator = walls.iterator();
        while (wallIterator.hasNext())
        {
            Wall wall = wallIterator.next();
            double oldX = wall.getX();
            wall.velocity = new Vector(-21, 0);
            wall.update();
            if (oldX > player.getX() && wall.getX() < player.getX())
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
            coin.velocity = (new Vector(-21, 0));
            coin.update();
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
