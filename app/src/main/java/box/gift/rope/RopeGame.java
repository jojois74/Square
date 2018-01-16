package box.gift.rope;

import android.content.Context;
import android.graphics.Color;

import java.util.Iterator;
import java.util.LinkedList;

import box.shoe.gameutils.AbstractGameEngine;
import box.shoe.gameutils.AbstractGameSurfaceView;
import box.shoe.gameutils.EntityCollisions;
import box.shoe.gameutils.GameEvents;
import box.shoe.gameutils.GameState;
import box.shoe.gameutils.SimpleEmitter;
import box.shoe.gameutils.TaskScheduler;
import box.shoe.gameutils.Rand;
import box.shoe.gameutils.Vector;

/**
 * Created by Joseph on 10/23/2017.
 */

public class RopeGame extends AbstractGameEngine //TODO: slowdown before boost ends //TODO: coin gap btwn circles smaller
{
    //> Consts
    // Updates per second that we would like to get from the engine.
    private static final int TARGET_UPS = 25; //No need to make ups too high!

    //> Objs
    // Player is controlled by the human.
    private Player player;

    // Barriers on the top and bottom of the screen with which the player dies upon contact.
    private Wall botBar;
    private Wall topBar;

    // Keep track of the generated Entities.
    private LinkedList<Wall> walls;
    private LinkedList<Coin> coins;
    private LinkedList<Boost> boosts;

    // Use a single Rand instance for RNG.
    public static Rand random = new Rand();

    // Spawns the walls and coins at the specified intervals.
    private TaskScheduler scheduler;

    private Runnable boostEnd;
    private Vector boostVelocity = new Vector(-35, 0);

    private SimpleEmitter coinEffectEmitter;
    private SimpleEmitter boostEffectEmitter;

    //> Etc.
    // Make sure we only dispatch the GAME_OVER event once.
    private boolean dispatchedGameOverEvent = false;
    // Score indicates how well the use is doing - increases upon collecting coin, and passing wall.
    private int score = 0;
    private int wallSeparationDistance = 1000;

    public RopeGame(Context appContext, AbstractGameSurfaceView screen)
    {
        super(appContext, RopeGame.TARGET_UPS, screen);
        scheduler = new TaskScheduler(RopeGame.TARGET_UPS);
        walls = new LinkedList<>();
        coins = new LinkedList<>();
        boosts = new LinkedList<>();

        coinEffectEmitter = new SimpleEmitter.Builder()
                .size(11)
                .speed(50)
                .color(Color.WHITE)
                .spanDegrees(1, 360)
                .duration(3)
                .build();

        boostEffectEmitter = new SimpleEmitter.Builder()
                .size(11)
                .speed(60)
                .color(Color.WHITE)
                .spanDegrees(145, 215)
                .duration(3)
                .build();

        boostEnd = new Runnable()
        {
            @Override
            public void run()
            {
                player.boosting--;
            }
        };
    }

    @Override
    protected void initialize()
    {
        int barHeight = 15;
        botBar = new Wall(0, getGameHeight() - barHeight, getGameWidth(), barHeight, false);
        topBar = new Wall(0, 0, getGameWidth(), barHeight, false);

        player = new Player(getGameWidth() / 6, getGameHeight() / 7);

        spawnWall();
        spawnPowerup();
    }

    @Override
    protected void update()
    {
        // Let tasks run....
        scheduler.tick();

        // Move the particles, for visual effects!
        coinEffectEmitter.update();
        boostEffectEmitter.update();

        // Check for player going too high or too low.
        if (EntityCollisions.entityEntity(player, botBar) || EntityCollisions.entityEntity(player, topBar))
        {
            playerDead();
        }

        // On input, jump!
        if (screenTouched)
        {
            player.velocity = Player.jumpVelocity;
        }

        if (player.boosting > 0)
        {
            boostEffectEmitter.emit(player.getX(), player.getY() + player.height / 2);
        }

        // Keep track if we passed a wall this update, so we can increase the score.
        boolean passingWall = false;

        // Updates and collisions for Walls.
        double lastWallX = 0;
        Iterator<Wall> wallIterator = walls.iterator();
        while (wallIterator.hasNext())
        {
            Wall wall = wallIterator.next();
            double oldWallX = lastWallX = wall.getX();

            // Player hit a wall
            if (EntityCollisions.entityEntity(player, wall) && !(player.boosting > 0))
            {
                playerDead();
            }

            // Wall ran off the screen, allow it to be garbage collected.
            if (wall.getX() - wall.registration.getX() + wall.width < 0)
            {
                wallIterator.remove();
            }

            if (player.boosting > 0)
            {
                wall.velocity = wall.velocity.add(boostVelocity);
                wall.update();
                wall.velocity = wall.velocity.subtract(boostVelocity);
            }
            else
            {
                wall.update();
            }

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

        if (lastWallX != 0 && lastWallX < getGameWidth() - wallSeparationDistance)
        {
            spawnWall();
            spawnPowerup();
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
                for (int i = 0; i < 25; i++)
                {
                    coinEffectEmitter.emit(coin.getX(), coin.getY());
                }
            }
            if (coin.getX() - coin.registration.getX() + coin.width < 0)
            {
                coinIterator.remove();
            }

            if (player.boosting > 0)
            {
                coin.velocity = coin.velocity.add(boostVelocity);
                coin.update();
                coin.velocity = coin.velocity.subtract(boostVelocity);
            }
            else
            {
                coin.update();
            }
        }

        // Updates and collisions for Boosts.
        Iterator<Boost> boostIterator = boosts.iterator();
        while (boostIterator.hasNext())
        {
            Boost boost = boostIterator.next();
            if (EntityCollisions.entityEntity(player, boost))
            {
                score++;
                player.boosting++;
                boostIterator.remove();
                scheduler.schedule(2000, 1, boostEnd);
            }
            if (boost.getX() - boost.registration.getX() + boost.width < 0)
            {
                boostIterator.remove();
            }

            if (player.boosting > 0)
            {
                boost.velocity = boost.velocity.add(boostVelocity);
                boost.update();
                boost.velocity = boost.velocity.subtract(boostVelocity);
            }
            else
            {
                boost.update();
            }
        }

        // Move the player.
        player.update();
    }

    private void spawnWall()
    {
        int wallHeight = getGameHeight() / 3;
        int wallWidth = 15;
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

    private void spawnPowerup()
    {
        int margin = Math.max(getGameHeight() / 10, 76);
        int randHeight = random.intFrom(margin, getGameHeight() - margin);
        int rand = random.intFrom(0, 2);
        double initialX = getGameWidth() + wallSeparationDistance * 0.5;
        if (rand == 0)
        {
            rand = random.intFrom(0, 3);
            if (rand == 0)
            {
                boosts.add(new Boost(initialX, randHeight));
            }
            else
            {
                coins.add(new Coin(initialX, randHeight));
            }
        }
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

        // Store the boosts for painting.
        gameState.put("boosts", boosts);

        // Store the score for painting.
        gameState.put("score", score);

        // Store the bars for painting.
        gameState.put("top", botBar);
        gameState.put("bot", topBar);

        gameState.put("coinEffectEmitter", coinEffectEmitter);
        gameState.put("boostEffectEmitter", boostEffectEmitter);
    }

    // Alerts the GameActivity that the game is over.
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

        botBar.cleanup();
        botBar = null;
        topBar.cleanup();
        topBar = null;

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
