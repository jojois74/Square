package box.gift.rope;

import android.content.Context;
import android.view.MotionEvent;

import java.util.Iterator;
import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.Entity;
import box.shoe.gameutils.Fireable;
import box.shoe.gameutils.GameTasker;
import box.shoe.gameutils.Rand;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 10/23/2017.
 */

public class Rope extends AbstractGameEngine
{
    private double lastTouchX;
    private double lastTouchY;

    // Consts
    private static final int TARGET_UPS = 30; //No need to make ups too high!

    // Objs
    public Player player;
    public LinkedList<Entity> walls;
    public LinkedList<Entity> coins;
    private Rand rand;
    private GameTasker scheduler;

    public Rope(Context appContext, AbstractGameSurfaceView screen)
    {
        super(appContext, screen, Rope.TARGET_UPS);
        scheduler = new GameTasker(Rope.TARGET_UPS);
        walls = new LinkedList<>();
        coins = new LinkedList<>();
        rand = new Rand();
    }

    @Override
    protected void initialize()
    {
        player = new Player(getGameWidth() / 6, getGameHeight() / 8, new PlayerPaintable(60, 60));
        Fireable generateWallAndCoin = new Fireable()
        {
            @Override
            public void fire()
            {
                int third = getGameHeight() / 3;
                int hole = rand.randomBetween(0, 2);
                int wallWidth = 25;
                WallPaintable wp = new WallPaintable(wallWidth, third);
                if (hole != 0)
                {
                    walls.add(new Entity(getGameWidth(), 0, wp));
                }
                if (hole != 1)
                {
                    walls.add(new Entity(getGameWidth(), third, wp));
                }
                if (hole != 2)
                {
                    walls.add(new Entity(getGameWidth(), 2 * third, wp));
                }

                int rand = random.randomBetween(0, 3);
                int margin = 40;
                int randHeight = random.randomBetween(margin, getGameHeight() - margin);
                if (rand == 0)
                    coins.add(new Entity(getGameWidth() * 1.3, randHeight, new CoinPaintable(80, 80)));
            }
        };
        scheduler.register(2700, 0, generateWallAndCoin);
        generateWallAndCoin.fire();
    }

    @Override
    protected void update()
    {
        scheduler.tick();

        if (player.getY() < 0 || player.getY() > getGameHeight())
        {
            playerDead();
        }

        player.saveOldPosition();
        player.setVelocity(player.getVelocity().add(new Vector(0, 2)));
        player.moveWithVelocity();

        boolean passingWall = false;
        Iterator<Entity> iterator = walls.iterator();
        while (iterator.hasNext())
        {
            Entity wall = iterator.next();
            double oldX = wall.getX();
            wall.saveOldPosition();
            wall.setVelocity(new Vector(-21, 0));
            wall.moveWithVelocity();
            if (oldX > player.getX() && wall.getX() < player.getX())
            {
                passingWall = true;
                if (player.getY() > wall.getY() && player.getY() < wall.getY() + wall.getVisualHeight())
                {
                    playerDead();
                }
            }
            if (wall.getX() + wall.getVisualWidth() < 0)
            {
                iterator.remove();
            }
        }
        if (passingWall)
        {
            score++;
        }

        iterator = coins.iterator();
        while (iterator.hasNext())
        {
            Entity coin = iterator.next();
            double oldX = coin.getX();
            coin.saveOldPosition();
            coin.setVelocity(new Vector(-21, 0));
            coin.moveWithVelocity();
            if (oldX > player.getX() && coin.getX() < player.getX())
            {
                if (player.getY() > coin.getY() && player.getY() < coin.getY() + coin.getVisualHeight())
                {
                    score++;
                    iterator.remove();
                }
            }
        }
    }

    private void playerDead()
    {
        MainActivity.print("DEAD");
        endGame();
    }

    @Override
    protected void cleanup()
    {
        super.cleanup();
        player.cleanup();
        player = null;
        rand = null;
        //Cleanup scheduler TODO
        scheduler = null;
        //Cleanup walls TODO

        dispatchEvent(GameEvent.GAME_OVER);
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
        if (player.getVelocity().getY() > 0)
        {
            player.setVelocity(player.actionVelocity);
        }
        else
        {
            player.setVelocity(player.getVelocity().add(player.actionVelocity.scale(0.65)));
        }
    }
}
