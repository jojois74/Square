package box.gift.rope;

import android.content.Context;
import android.view.MotionEvent;

import java.util.Iterator;
import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.AbstractPaintable;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.InterpolatableEntity;
import box.shoe.gameutils.EntityCollisions;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.GameTasker;
import box.shoe.gameutils.Paintable;
import box.shoe.gameutils.ParticleEffect;
import box.shoe.gameutils.Rand;
import box.shoe.gameutils.Vector;
import box.shoe.gameutils.VisualizableEntity;

/**
 * Created by Joseph on 10/23/2017.
 */

public class Rope extends AbstractGameEngine
{
    private double lastTouchX;
    private double lastTouchY;

    // Consts
    private static final int TARGET_UPS = 30; //No need to make ups too high!
    private Paintable wallPaintable;
    private Paintable coinPaintable;

    // Objs
    public Player player;
    public LinkedList<VisualizableEntity> walls;
    public LinkedList<VisualizableEntity> coins;
    private Rand rand;
    private GameTasker scheduler;
    private ParticleEffect part;
    public InterpolatableEntity effect;

    public Rope(Context appContext, AbstractGameSurfaceView screen)
    {
        super(appContext, Rope.TARGET_UPS, screen);
        scheduler = new GameTasker(Rope.TARGET_UPS);
        walls = new LinkedList<>();
        coins = new LinkedList<>();
        rand = new Rand();
    }

    @Override
    protected void initialize()
    {
        wallPaintable = new WallPaintable(25, getGameHeight() / 3);
        coinPaintable = new CoinPaintable(80, 80);
        //player = new Player(getGameWidth() / 6, getGameHeight() / 8, new PlayerPaintable(60, 60));
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
                    VisualizableEntity wall = new VisualizableEntity(getGameWidth(), 0, wallPaintable);
                    walls.add(wall);
                }
                if (hole != 1)
                {
                    VisualizableEntity wall = new VisualizableEntity(getGameWidth(), third, wallPaintable);
                    walls.add(wall);
                }
                if (hole != 2)
                {
                    VisualizableEntity wall = new VisualizableEntity(getGameWidth(), 2 * third, wallPaintable);
                    walls.add(wall);
                }

                int rand = random.randomBetween(0, 3);
                int margin = 40;
                int randHeight = random.randomBetween(margin, getGameHeight() - margin);
                if (rand == 0)
                {
                    coins.add((new VisualizableEntity(getGameWidth() * 1.3, randHeight, 80, 80, coinPaintable))
                            .setDisplayWidth(80)
                            .setDisplayHeight(80));
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

        if (player.y < 0 || player.y > getGameHeight())
        {
            playerDead();
        }

        player.velocity = (player.velocity.add(new Vector(0, 2)));
        player.update();

        boolean passingWall = false;
        Iterator<VisualizableEntity> iterator = walls.iterator();
        while (iterator.hasNext())
        {
            VisualizableEntity wall = iterator.next();
            double oldX = wall.x;
            wall.velocity = new Vector(-21, 0);
            wall.update();
            if (oldX > player.x && wall.x < player.x)
            {
                passingWall = true;
                if (player.y > wall.y && player.y < wall.y + wall.getDisplayHeight())
                {
                    playerDead();
                }
            }
            if (wall.x + wall.getDisplayWidth() < 0)
            {
                iterator.remove();
            }
        }
        if (passingWall)
        {
            score++; //TODO: score should go up a bit later, so you do not gain a point upon dying against a wall
        }

        iterator = coins.iterator();
        while (iterator.hasNext())
        {
            InterpolatableEntity coin = iterator.next();
            double oldX = coin.x;
            coin.velocity = (new Vector(-21, 0));
            coin.update();
            if (EntityCollisions.collideRectangle(player, coin)) //TODO: use circle collision for coins?
            {
                score++;
                iterator.remove();
            }
        }
    }

    @Override
    protected void saveGameState(GameState gameState)
    {
        gameState.saveInterpolatableEntity(player);
        for (InterpolatableEntity wall : walls)
        {
            gameState.saveInterpolatableEntity(wall);
        }
    }

    private void playerDead()
    {
        MainActivity.print("DEAD");
        stopGame();
        MainActivity.print("stop GAME RETURNED");
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

    @Override
    public void onTouchEvent(MotionEvent event)
    {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN)
        {
            lastTouchX = event.getX();
            lastTouchY = event.getY();
            actionDown();
        }
    }

    private void actionDown()
    {
        player.velocity = player.actionVelocity;
    }
}
