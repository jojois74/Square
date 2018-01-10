package box.gift.rope;

import android.content.Context;

import java.util.Iterator;
import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.EntityCollisions;
import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.TaskScheduler;
import box.shoe.gameutils.Rand;

/**
 * Created by Joseph on 10/23/2017.
 */

public class RopeGame extends AbstractGameEngine
{
    // Consts
    // Updates per second that we would like to get from the engine.
    private static final int TARGET_UPS = 25; //No need to make ups too high!

    // Objs
    // Player is controlled by the human.
    private Player player;
    // Barriers on the top and bottom of the screen with which the player dies upon contact.
    private Wall topBar;
    private Wall botBar;
    // Keep track of the generated Entities.
    private LinkedList<Wall> walls;
    private LinkedList<Coin> coins;
    // Use a single Rand instance for RNG.
    public static Rand random = new Rand();
    // Spawns the walls and coins at the specified intervals.
    private TaskScheduler scheduler;

    // Etc.
    // Make sure we only dispatch the GAME_OVER event once.
    private boolean dispatchedGameOverEvent = false;
    // Score indicates how well the use is doing - increases upon collecting coin, and passing wall.
    private int score = 0;

    public RopeGame(Context appContext, AbstractGameSurfaceView screen)
    {
        super(appContext, RopeGame.TARGET_UPS, screen);
        scheduler = new TaskScheduler(RopeGame.TARGET_UPS);
        walls = new LinkedList<>();
        coins = new LinkedList<>();
    }

    @Override
    protected void initialize()
    {
        int barHeight = 10;
        topBar = new Wall(0, getGameHeight() - barHeight, getGameWidth(), barHeight, false);
        botBar = new Wall(0, 0, getGameWidth(), barHeight, false);

        player = new Player(getGameWidth() / 7, getGameHeight() / 7);
        Runnable spawnWall = new Runnable()
        {
            @Override
            public void run()
            {
                int wallHeight = getGameHeight() / 3;
                int wallWidth = 10;
                double wallX = getGameWidth();
                int holePosition = random.intFrom(0, 2);
                if (holePosition != 0)
                {
                    Wall wall = new Wall(wallX, 0, wallWidth, wallHeight, true);
                    walls.add(wall);
                }
                if (holePosition != 1)
                {
                    Wall wall = new Wall(wallX, wallHeight, wallWidth, wallHeight, true);
                    walls.add(wall);
                }
                if (holePosition != 2)
                {
                    Wall wall = new Wall(wallX, 2 * wallHeight, wallWidth, wallHeight, true);
                    walls.add(wall);
                }
            }
        };
        final Runnable spawnCoin = new Runnable()
        {
            @Override
            public void run()
            {
                int rand = random.intFrom(0, 3);
                int margin = 80;
                int randHeight = random.intFrom(margin, getGameHeight() - margin);
                if (rand == 0)
                {
                    coins.add((new Coin(getGameWidth(), randHeight, 80, 80)));
                }
            }
        };
        final int wallDelay = 2700;
        Runnable beginSpawnCoins = new Runnable()
        {
            @Override
            public void run()
            {
                scheduler.schedule(wallDelay, 0, spawnCoin);
                spawnCoin.run();
            }
        };
        scheduler.schedule(wallDelay, 0, spawnWall);
        scheduler.schedule(wallDelay / 2, 1, beginSpawnCoins);
        spawnWall.run();
    }

    @Override
    protected void update()
    {
        // Let tasks run....
        scheduler.tick();

        // Check for player going too high or too low.
        if (EntityCollisions.entityEntity(player, topBar) || EntityCollisions.entityEntity(player, botBar))
        {
            playerDead();
        }

        // On input, jump!
        if (screenTouched)
        {
            player.velocity = Player.jumpVelocity;
        }

        // Keep track if we passed a wall this update, so we can increase the score.
        boolean passingWall = false;

        // Updates and collisions for Walls.
        Iterator<Wall> wallIterator = walls.iterator();
        while (wallIterator.hasNext())
        {
            Wall wall = wallIterator.next();
            double oldWallX = wall.getX();

            // Player hit a wall
            if (EntityCollisions.entityEntity(player, wall))
            {
                playerDead();
            }

            // Wall ran off the screen, allow it to be garbage collected.
            if (wall.getX() - wall.registration.getX() + wall.width < 0)
            {
                wallIterator.remove();
            }

            wall.update();

            // Wall passes by the player.
            if (oldWallX > player.getX() && wall.getX() < player.getX())
            {
                passingWall = true;
            }
        }

        if (passingWall)
        {
            score++;
        }

        // Updates and collisions for Coins.
        Iterator<Coin> coinIterator = coins.iterator();
        while (coinIterator.hasNext())
        {
            Coin coin = coinIterator.next();
            if (EntityCollisions.entityEntity(player, coin)) //TODO: use circle collision for coins?
            {
                score++;
                coinIterator.remove();
            }
            if (coin.getX() - coin.registration.getX() + coin.width < 0)
            {
                coinIterator.remove();
            }
            coin.update();
        }

        // Move the player.
        player.update();
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
    }

    // Kills the player (stops the game).
    private void playerDead()
    {
        // Make sure we do not dispatch the GAME_OVER event more than once.
        if (!dispatchedGameOverEvent)
        {
            dispatchedGameOverEvent = true;

            // Tell all listeners that the game is over.
            dispatchEvent(GameEvents.GAME_OVER);
        }
    }

    @Override
    public int getResult()
    {
        return score;
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        topBar.cleanup();
        topBar = null;
        botBar.cleanup();
        botBar = null;

        player.cleanup();
        player = null;

        scheduler.cleanup();
        scheduler = null;

        for (Wall wall : walls)
        {
            wall.cleanup();
        }
        walls.clear();
        walls = null;

        for (Coin coin : coins)
        {
            coin.cleanup();
        }
        coins.clear();
        coins = null;
    }
}
