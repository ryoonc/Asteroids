package org.newdawn.spaceinvaders;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * The main hook of our game. This class with both act as a manager
 * for the display and central mediator for the game logic. 
 * 
 * Display management will consist of a loop that cycles round all
 * gameObjects in the game asking them to move and then drawing them
 * in the appropriate place. With the help of an inner class it
 * will also allow the player to control the main p1Ship.
 * 
 * As a mediator it will be informed when gameObjects within our game
 * detect events (e.g. alient killed, played died) and will take
 * appropriate game actions.
 */
public class Game extends Canvas {
	/** The stragey that allows us to use accelerate page flipping */
	private BufferStrategy strategy;
	/** True if the game is currently "running", i.e. the game loop is looping */
	private boolean gameRunning = true;
	/** The list of all the gameObjects that exist in our game */
	private ArrayList gameObjects = new ArrayList();
	/** The list of gameObjects that need to be removed from the game this loop */
	private ArrayList removeList = new ArrayList();
	/** The entity representing the player 1 */
	private GameObject p1Ship;
    /** The entity representing the player 2 */
    private GameObject p2Ship;
	/** The speed at which the player's p1Ship should move (pixels/sec) */
	private double p1_moveSpeedX = 0;
    /** The speed at which the player's p1Ship should move (pixels/sec) */
    private double p1_moveSpeedY = 0;
    /** The speed at which the player's p1Ship should move (pixels/sec) */
    private double p2_moveSpeedX = 0;
    /** The speed at which the player's p1Ship should move (pixels/sec) */
    private double p2_moveSpeedY = 0;
    /** The speed at which the player's p1Ship should rotate (pixels/sec) */
    private int p1_currentAngle = 0;
    /** The speed at which the player's p1Ship should rotate (pixels/sec) */
    private int p2_currentAngle = 0;
	/** The time at which last fired a shot */
	private long p1_lastFire = 0;
    /** The time at which last fired a shot */
    private long p2_lastFire = 0;
	/** The interval between our players shot (ms) */
	private long firingDelay = 300;
	/** The number of aliens left on the screen */
	private int asteroidCount;
    private int p1ShotCount;
    private int p1ScoreCount;
    private int p2ScoreCount;
    private int p2ShotCount;
    private int currentLevel = 1;
    private AePlayWave aulink;
    private boolean diedOnce = false;
    private boolean p2_PlayedDeathOnce = false;
    private boolean gravitationEnabled = true;
    private double gravitationalFactor = 0.15;
    private boolean player2 = false;
    private boolean friendlyFireOn = false;
    private int deathCounter = 0;
    // Delay counter for bg tones
    private int toneCount = 0;
    // Delay counter for p1 thruster tone
    private int p1ThrusterSFX = 0;
    // Delay counter for p2 thruster tone
    private int p2ThrusterSFX = 0;

    // TODO: Implement settings page
    private JCheckBox chk_gravityObject = new JCheckBox("Gravity Object Exists");
    private JCheckBox chk_gravityObjectVisible = new JCheckBox("Gravity Object Visible");
    private JCheckBox chk_unlimitedLives = new JCheckBox("Unlimited Lives");

    // Settings
    private enum Options {
        GRAVITATIONAL_OBJECT,
        LIVES,
        ASTEROIDS,
        HIGH_SCORE,
        LOAD_GAME,
        STARTING_LEVEL
    }

    private boolean startGameSelected = true;
    private boolean optionSelected = false;
    private boolean resetHighScoreSelected = false;
    private boolean waitingForOptionChange = false;
    private int currentOptionIndex = 0;
    private Options currentOption = Options.GRAVITATIONAL_OBJECT;
    private final int optionsCount = 6;
    private int startingAsteroidCount = 2;
    private int startingLivesCount = 3;
    private int livesCount1;
    private int livesCount2;
    final int highScoresCount = 10;
    private Integer[] highScores = new Integer[highScoresCount];
    private String highScoresFileName = new String("highscores.sav");
    private String newHighScoreName;
    private boolean waitingForNewHighScore = false;

	
	/** The message to display which waiting for a key press */
	private String message = "Asteroids v1";
	/** True if we're holding up game play until a key has been pressed */
	//private boolean waitingForKeyPress = true;
    /** True if we're holding up game play until a key has been pressed to start the game */
    private boolean waitingForStartPress = true;
    /** True if we're holding up game play until a key has been pressed to pause/unpause the game */
    private boolean waitingForPausePress = false;
    /** True if we're holding up game play until a key has been pressed to pause/unpause the game */
    private boolean waitingForLevelPress = false;
	/** True if the left cursor key is currently pressed */
	private boolean p1_leftPressed = false;
	/** True if the right cursor key is currently pressed */
	private boolean p1_rightPressed = false;
    /** True if the up button is currently pressed */
    private boolean p1_upPressed = false;
    /** True if the down button is currently pressed */
    private boolean p1_downPressed = false;
	/** True if we are firing */
	private boolean p1_firePressed = false;
    /** True if the left cursor key is currently pressed */
    private boolean p2_leftPressed = false;
    /** True if the right cursor key is currently pressed */
    private boolean p2_rightPressed = false;
    /** True if the up button is currently pressed */
    private boolean p2_upPressed = false;
    /** True if the down button is currently pressed */
    private boolean p2_downPressed = false;
    /** True if we are firing */
    private boolean p2_firePressed = false;
	/** True if game logic needs to be applied this loop, normally as a result of a game event */
	private boolean logicRequiredThisLoop = false;
	
	/**
	 * Construct our game and set it running.
	 */
	public Game() {
		// create a frame to contain our game
		JFrame container = new JFrame("Asteroids");
		
		// get hold the content of the frame and set up the resolution of the game
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(800,600));
		panel.setLayout(null);
		
		// setup our canvas size and put it into the content of the frame
		setBounds(0,0,800,600);
		panel.add(this);

		
		// Tell AWT not to bother repainting our canvas since we're
		// going to do that our self in accelerated mode
		setIgnoreRepaint(true);
		
		// finally make the window visible 
		container.pack();
		container.setResizable(false);
		container.setVisible(true);

		// create the buffering strategy which will allow AWT
		// to manage our accelerated graphics
		createBufferStrategy(2);
		strategy = getBufferStrategy();

        // add a key input system (defined below) to our canvas
        // so we can respond to key pressed
        addKeyListener(new KeyInputHandler());

        // request the focus so key events come to us
        requestFocus();

        // add a listener to respond to the user closing the window. If they
        // do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // TODO: reenable
        // Play music
        aulink = new AePlayWave("sounds\\startmusic.wav", 1);
        aulink.start();

		// initialise the gameObjects in our game so there's something
		// to see at startup
		initGameObjects();
	}
	
	/**
	 * Start a fresh game, this should clear out any old data and
	 * create a new set.
	 */
	private void startGame() {
		// clear out any existing gameObjects and initialise a new set
		gameObjects.clear();

		// blank out any keyboard settings we might currently have
		p1_leftPressed = false;
		p1_rightPressed = false;
		p1_firePressed = false;
        p1_upPressed = false;
        p1_moveSpeedX = 0;
        p1_moveSpeedY = 0;
        p1_currentAngle = 0;
        p1ShotCount = 0;
        p1ScoreCount = 0;
        p2_leftPressed = false;
        p2_rightPressed = false;
        p2_firePressed = false;
        p2_upPressed = false;
        p2_moveSpeedX = 0;
        p2_moveSpeedY = 0;
        p2_currentAngle = 0;
        p2ShotCount = 0;
        p2ScoreCount = 0;
        currentLevel = 1;
        diedOnce = false;
        p2_PlayedDeathOnce = false;
        livesCount1 = 3;
        if (player2)
            livesCount2 = 3;
        else        // TODO
            livesCount2 = 0;

        // init objects AFTER level has been reset
        initGameObjects();
	}

    /**
     * Level up! Time to reset the board
     */
    private void startNewLevel() {
        // clear out any existing gameObjects and initialise a new set
        gameObjects.clear();
        initGameObjects();

        // blank out any keyboard settings we might currently have
        p1_leftPressed = false;
        p1_rightPressed = false;
        p1_firePressed = false;
        p1_upPressed = false;
        p1_moveSpeedX = 0;
        p1_moveSpeedY = 0;
        p1_currentAngle = 0;
        p1ShotCount = 0;
        p2_leftPressed = false;
        p2_rightPressed = false;
        p2_firePressed = false;
        p2_upPressed = false;
        p2_moveSpeedX = 0;
        p2_moveSpeedY = 0;
        p2_currentAngle = 0;
        p2ShotCount = 0;

        startingAsteroidCount += 2;
    }

	
	/**
	 * Initialise the starting state of the gameObjects (p1Ship and aliens). Each
	 * entitiy will be added to the overall list of gameObjects in the game.
	 */
	private void initGameObjects() {
		// create the player p1Ship and place it roughly in the center of the screen
        if (livesCount1 > 0) {
            p1Ship = new ShipGameObject(this,"sprites\\ShipNormal.png",nextIntInRange(300, 500),nextIntInRange(400,500), "p1");
            gameObjects.add(p1Ship);
        }
        if (livesCount2 > 0 && player2) {   // TODO
            p2Ship = new ShipGameObject(this,"sprites\\ShipNormal2.png",nextIntInRange(300, 500),nextIntInRange(100,200), "p2");
            gameObjects.add(p2Ship);
        }
		
		// create a block of aliens (5 rows, by 12 aliens, spaced evenly)
//		asteroidCount = 0;
//		for (int row=0;row<5;row++) {
//			for (int x=0;x<12;x++) {
//				GameObject alien = new AlienGameObject(this,"sprites\\alien.gif",100+(x*50),(50)+row*30);
//				gameObjects.add(alien);
//				asteroidCount++;
//			}
//		}

        // Create Asteroids
        asteroidCount = 0;
        for (int x=0;x<currentLevel*2;x++) {
            // Left side
            if (asteroidCount < startingAsteroidCount) {
                int randomX1 = nextIntInRange(0, 300);
                int randomY1 = nextIntInRange(0, 600);

                GameObject asteroid = new AsteroidGameObject(this,"sprites\\Asteroid1.png", randomX1, randomY1, (int)(Math.random()*360), true, currentLevel);
                gameObjects.add(asteroid);
                asteroidCount++;
            }

            // Right side
            if (asteroidCount < startingAsteroidCount) {
                int randomX2 = nextIntInRange(500, 800);
                int randomY2 = nextIntInRange(0, 600);
                GameObject asteroid = new AsteroidGameObject(this,"sprites\\Asteroid1.png", randomX2, randomY2, (int)(Math.random()*360), true, currentLevel);
                gameObjects.add(asteroid);
                asteroidCount ++;
            }
        }



        if (gravitationEnabled) {
            // Create gravitation object
            GameObject gravObject = new GravitationalGameObject(this, "sprites\\GravitationalObject.png", 380, 280);
            gameObjects.add(gravObject);
        }
	}

    int nextIntInRange(int min, int max) {
        Random rng = new Random();
        if (min > max) {
            throw new IllegalArgumentException("Cannot draw random int from invalid range [" + min + ", " + max + "].");
        }
        int diff = max - min;
        if (diff >= 0 && diff != Integer.MAX_VALUE) {
            return (min + rng.nextInt(diff + 1));
        }
        int i;
        do {
            i = rng.nextInt();
        } while (i < min || i > max);
        return i;
    }

    private void drawStringOptions(Graphics2D g) {
        int i;

        StringBuilder[] stringOptions = new StringBuilder[optionsCount];
        stringOptions[0] = new StringBuilder("  Gravitational Object");
        stringOptions[1] = new StringBuilder("  Starting number of lives: " + Integer.toString(startingLivesCount));
        stringOptions[2] = new StringBuilder("  Starting number of asteroids: " + Integer.toString(startingAsteroidCount));
        stringOptions[3] = new StringBuilder("  Reset high score");
        stringOptions[4] = new StringBuilder("  Load game");
        stringOptions[5] = new StringBuilder("  Starting level");

        stringOptions[currentOptionIndex].setCharAt(0, '>');

        for (i = 0; i < optionsCount; i++) {
            g.drawString(stringOptions[i].toString(),(800-g.getFontMetrics().stringWidth(stringOptions[i].toString()))/2,300 + 20 * i);
        }
    }
	
	/**
	 * Notification from a game entity that the logic of the game
	 * should be run at the next opportunity (normally as a result of some
	 * game event)
	 */
	public void updateLogic() {
		logicRequiredThisLoop = true;
	}
	
	/**
	 * Remove an gameObject from the game. The gameObject removed will
	 * no longer move or be drawn.
	 * 
	 * @param gameObject The gameObject that should be removed
	 */
    // TODO: rename this
	public void removeEntity(GameObject gameObject) {
		removeList.add(gameObject);
	}

    /**
     * Ensures instant removal of entity not based on timer
     */
    public void refreshRemoval() {
        gameObjects.removeAll(removeList);
        removeList.clear();
    }

    public void bigAsteroidDestroyed(GameObject gameObject) {
        for (int i = 0; i < 3; i++) {
            GameObject smallAsteroid = new AsteroidGameObject(this,"sprites\\SmallAsteroid1.png", gameObject.getX(), gameObject.getY(), (int)(Math.random()*360), false, currentLevel);
            gameObjects.add(smallAsteroid);
            asteroidCount++;
        }
    }

    /**
     * Notification that player 1 has died.
     */
    public void notifyDeath(String whichPlayer) {
        new AePlayWave("sounds\\EXPLODE1.wav").start();

        if (whichPlayer.equals("p1")) {
            this.removeEntity(getP1Ship());

            if (livesCount1 > 0) {
                livesCount1--;
                p1Ship = new ShipGameObject(this,"sprites\\ShipNormal.png",375,175, "p1");
                gameObjects.add(p1Ship);
                p1_leftPressed = false;
                p1_rightPressed = false;
                p1_firePressed = false;
                p1_upPressed = false;
                p1_currentAngle = 0;
                //diedOnce = true;

            }
        }
        else if (whichPlayer.equals("p2")) {
            this.removeEntity(getP2Ship());

            if (livesCount2 > 0) {
                livesCount2--;
                p2Ship = new ShipGameObject(this,"sprites\\ShipNormal2.png",375,375, "p2");
                gameObjects.add(p2Ship);
                //diedOnce = true;
                p2_leftPressed = false;
                p2_rightPressed = false;
                p2_firePressed = false;
                p2_upPressed = false;
                p2_currentAngle = 0;

            }
        }

        checkGameOver();
    }

    /**
     * Notification that player 2 has died.
     */
//    public void p2_notifyDeath() {
//        if (!p2_PlayedDeathOnce)
//        {
//            new AePlayWave("sounds\\EXPLODE1.wav").start();
//            p2_PlayedDeathOnce = true;
//            p2_leftPressed = false;
//            p2_rightPressed = false;
//            p2_firePressed = false;
//            p2_upPressed = false;
//        }
//
//        if (!waitingForStartPress) {
//            livesCount2--;
////            if (livesCount2 == 0) {
////                updateHighScores();
////            }
//            checkGameOver();
//        }
//
//        //waitingForStartPress = true;
//    }

    public void checkGameOver() {
        if (livesCount1 <= 0 && livesCount2 <= 0) {
            message = "You have died.";
            waitingForStartPress = true;
            updateHighScores();
        }
    }
	
	/**
	 * Notification that both players have died.
	 */
//	public void bothPlayers_notifyDeath() {
//		message = "You have died. Try again?";
//		//waitingForKeyPress = true;
//        //gameRunning = false;
//        if (!diedOnce)
//        {
//            new AePlayWave("sounds\\EXPLODE1.wav").start();
//            diedOnce = true;
//            p1_leftPressed = false;
//            p1_rightPressed = false;
//            p1_firePressed = false;
//            p1_upPressed = false;
//        }
//
//        if (!p2_PlayedDeathOnce)
//        {
//            new AePlayWave("sounds\\EXPLODE1.wav").start();
//            p2_PlayedDeathOnce = true;
//            p2_leftPressed = false;
//            p2_rightPressed = false;
//            p2_firePressed = false;
//            p2_upPressed = false;
//        }
//
//        if (!waitingForStartPress) {
//            livesCount--;
//            if (livesCount == 0) {
//                updateHighScores();
//            }
//        }
//
//        waitingForStartPress = true;
//	}

    private void updateHighScores() {
        int i, j;
        for (i = 0; i < highScoresCount; i++) {
            if (highScores[i] == null) {
                highScores[i] = p1ScoreCount;
                waitingForNewHighScore = true;
                break;
            } else if (highScores[i] < p1ScoreCount) {
                waitingForNewHighScore = true;
                if (i == highScoresCount - 1) {
                    highScores[i] = p1ScoreCount;
                } else {
                    int temp = highScores[i];
                    highScores[i] = p1ScoreCount;

                    for (j = i + 1; j < highScoresCount; j++) {
                        if (highScores[j] == null) {
                            highScores[j] = temp;
                            break;
                        }
                        int temp2 = highScores[j];
                        highScores[j] = temp;
                        temp = temp2;
                    }
                }
                break;
            }
        }
        saveHighScores();
    }

    private void saveHighScores() {
        File file= new File(highScoresFileName);
        FileOutputStream fStream;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            fStream = new FileOutputStream(file);

            int i;
            for (i = 0; i < highScoresCount; i++) {
                if (highScores[i] == null) { break; }
                else {
                    fStream.write((highScores[i].toString() + "\n").getBytes());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHighScores() {
        File file = new File(highScoresFileName);
        FileReader inputStream = null;

        if (!file.exists()) { return; }
        try {
            FileInputStream fstream = new FileInputStream(file);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            int i = 0;
            while ((strLine = br.readLine()) != null)   {
                highScores[i] = new Integer(strLine);
                i++;
            }
        } catch (Exception e) {
        }
    }
	
	/**
	 * Notification that the player has won since all the aliens
	 * are dead.
	 */
	public void notifyWin() {
        // TODO: when you win and also kill yourself
        new AePlayWave("sounds\\levelup.wav").start();
		message = "Level Complete!";
        currentLevel++;
        // TODO
        increaseScore("p1", 3, currentLevel);
        if (player2)
            increaseScore("p2", 3, currentLevel);
		waitingForLevelPress = true;
        p1_leftPressed = false;
        p1_rightPressed = false;
        p1_firePressed = false;
        p1_upPressed = false;
        p2_leftPressed = false;
        p2_rightPressed = false;
        p2_firePressed = false;
        p2_upPressed = false;
	}
	
	/**
	 * Notification that an asteroid has been killed
	 */
	public void notifyAsteroidKilled() {
		// reduce the alient count, if there are none left, the player has won!
		asteroidCount--;
		
		if (asteroidCount == 0) {
			notifyWin();
		}
		
		// if there are still some aliens left then they all need to get faster, so
		// speed up all the existing aliens
//		for (int i=0;i<gameObjects.size();i++) {
//			GameObject gameObject = (GameObject) gameObjects.get(i);
//
//			if (gameObject instanceof AlienGameObject) {
//				// speed up by 2%
//				gameObject.setHorizontalMovement(gameObject.getHorizontalMovement() * 1.02);
//			}
//		}
	}
	
	/**
	 * Attempt to fire a shot from player 1. Due to the timer, we must check
     * whether or not the player has burst-shot
	 */
	public void p2TryToFire() {
		// check that we have waiting long enough to fire
		if (p2ShotCount == 4) {
            if (System.currentTimeMillis() - p2_lastFire < firingDelay*2) {
                return;
            }
            p2ShotCount = 0;
            return;
        }
        if (System.currentTimeMillis() - p2_lastFire < firingDelay/2) {
            return;
        }
        p2ShotCount++;
		
		// if we waited long enough, create the shot entity, and record the time.
		p2_lastFire = System.currentTimeMillis();
		ShotGameObject shot = new ShotGameObject(this,"sprites\\Bullet2.png", p2Ship.getX()+ p2Ship.getWidth()/2-3, p2Ship.getY()+ p2Ship.getHeight()/2-3, p2Ship.getAngle(), p2_moveSpeedX +100, p2_moveSpeedY +100, "p2");
        new AePlayWave("sounds\\asteroids_shoot.wav").start();
		gameObjects.add(shot);
	}

    /**
     * Attempt to fire a shot from player 1. Due to the timer, we must check
     * whether or not the player has burst-shot
     */
    public void p1TryToFire() {
        // check that we have waiting long enough to fire
        if (p1ShotCount == 4) {
            if (System.currentTimeMillis() - p1_lastFire < firingDelay*2) {
                return;
            }
            p1ShotCount = 0;
            return;
        }
        if (System.currentTimeMillis() - p1_lastFire < firingDelay/2) {
            return;
        }
        p1ShotCount++;

        // if we waited long enough, create the shot entity, and record the time.
        p1_lastFire = System.currentTimeMillis();
        ShotGameObject shot = new ShotGameObject(this,"sprites\\Bullet.png", p1Ship.getX()+ p1Ship.getWidth()/2-3, p1Ship.getY()+ p1Ship.getHeight()/2-3, p1_currentAngle, p1_moveSpeedX +100, p1_moveSpeedY +100, "p1");
        new AePlayWave("sounds\\asteroids_shoot.wav").start();
        gameObjects.add(shot);
    }
	
	/**
	 * The main game loop. This loop is running during all game
	 * play as is responsible for the following activities:
	 * <p>
	 * - Working out the speed of the game loop to update moves
	 * - Moving the game gameObjects
	 * - Drawing the screen contents (gameObjects, text)
	 * - Updating game events
	 * - Checking Input
	 * <p>
	 */
	public void gameLoop() {
		long lastLoopTime = System.currentTimeMillis();
		
		// keep looping round til the game ends
		while (gameRunning) {
			// work out how long its been since the last update, this
			// will be used to calculate how far the gameObjects should
			// move this loop
			long delta = System.currentTimeMillis() - lastLoopTime;
			lastLoopTime = System.currentTimeMillis();
			
			// Get hold of a graphics context for the accelerated 
			// surface and blank it out
			Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

			g.setColor(Color.black);
			g.fillRect(0,0,800,600);

            // draw the score in the top left and right corner
            g.setColor(Color.white);
            g.drawString(Integer.toString(p1ScoreCount),20,20);
            g.drawString(Integer.toString(p2ScoreCount),760,20);
            // draw level in center top
            g.drawString(Integer.toString(currentLevel),398,20);

            // draw lives on top
            g.drawString("P1 Lives: " + Integer.toString(livesCount1), 100, 20);
            g.drawString("P2 Lives: " + Integer.toString(livesCount2), 650, 20);
			
			// cycle round asking each entity to move itself
			if (!waitingForPausePress && !waitingForStartPress && !waitingForLevelPress) {
				for (int i=0;i< gameObjects.size();i++) {
					GameObject gameObject = (GameObject) gameObjects.get(i);
					
					gameObject.move(delta);
				}
                // Randomly create an alien ship
                if (Math.random()*100000 < 50) {
                    GameObject alien = new AlienShipGameObject(this, "sprites\\AlienShip.png", (int)(Math.random()*600 + 100), (int)(Math.random()*400 + 100), (int)(Math.random()*360), currentLevel, 500);
                    gameObjects.add(alien);
                }
			}
			
			// cycle round drawing all the gameObjects we have in the game
			for (int i=0;i< gameObjects.size();i++) {
				GameObject gameObject = (GameObject) gameObjects.get(i);
				
				gameObject.draw(g);
			}
			
			// brute force collisions, compare every entity against
			// every other entity. If any of them collide notify 
			// both gameObjects that the collision has occured
			for (int p=0;p< gameObjects.size();p++) {
				for (int s=p+1;s< gameObjects.size();s++) {
					GameObject me = (GameObject) gameObjects.get(p);
					GameObject him = (GameObject) gameObjects.get(s);
					
					if (me.collidesWith(him)) {
						me.collidedWith(him);
						him.collidedWith(me);
					}
				}
			}

            if (gravitationEnabled) {
                // Gravitational pull from center
                int centerX = 400;
                int centerY = 300;

                // Player 1 gravitational pull
                if (p1Ship != null) {
                    if (p1Ship.getX()+ p1Ship.getWidth()/2 > centerX && p1Ship.getY()+ p1Ship.getHeight()/2 > centerY) {
                        ((ShipGameObject) p1Ship).changeAccel(-gravitationalFactor,-gravitationalFactor);
                    }
                    else if (p1Ship.getX()+ p1Ship.getWidth()/2 < centerX && p1Ship.getY()+ p1Ship.getHeight()/2 > centerY) {
                        ((ShipGameObject) p1Ship).changeAccel(gravitationalFactor,-gravitationalFactor);
                    }
                    else if (p1Ship.getX()+ p1Ship.getWidth()/2 == 0 && p1Ship.getY()+ p1Ship.getHeight()/2 > centerY) {
                        ((ShipGameObject) p1Ship).changeAccel(0,-gravitationalFactor);
                    }
                    else if (p1Ship.getX()+ p1Ship.getWidth()/2 == 0 && p1Ship.getY()+ p1Ship.getHeight()/2 < centerY) {
                        ((ShipGameObject) p1Ship).changeAccel(0,gravitationalFactor);
                    }
                    else if (p1Ship.getX()+ p1Ship.getWidth()/2 < centerX && p1Ship.getY()+ p1Ship.getHeight()/2 < centerY) {
                        ((ShipGameObject) p1Ship).changeAccel(gravitationalFactor,gravitationalFactor);
                    }
                    else if (p1Ship.getX()+ p1Ship.getWidth()/2 > centerX && p1Ship.getY()+ p1Ship.getHeight()/2 < centerY) {
                        ((ShipGameObject) p1Ship).changeAccel(-gravitationalFactor,gravitationalFactor);
                    }
                    else if (p1Ship.getX()+ p1Ship.getWidth()/2 > centerX && p1Ship.getY()+ p1Ship.getHeight()/2 == 0) {
                        ((ShipGameObject) p1Ship).changeAccel(-gravitationalFactor,0);
                    }
                    else if (p1Ship.getX()+ p1Ship.getWidth()/2 < centerX && p1Ship.getY()+ p1Ship.getHeight()/2 == 0) {
                        ((ShipGameObject) p1Ship).changeAccel(gravitationalFactor,0);
                    }
                }

                // Player 2 gravitational pull
                // TODO: do this if statement for all player 2 operations
                if (player2 && p2Ship != null) {
                    if (p2Ship.getX()+ p2Ship.getWidth()/2 > centerX && p2Ship.getY()+ p2Ship.getHeight()/2 > centerY) {
                        ((ShipGameObject) p2Ship).changeAccel(-gravitationalFactor,-gravitationalFactor);
                    }
                    else if (p2Ship.getX()+ p2Ship.getWidth()/2 < centerX && p2Ship.getY()+ p2Ship.getHeight()/2 > centerY) {
                        ((ShipGameObject) p2Ship).changeAccel(gravitationalFactor,-gravitationalFactor);
                    }
                    else if (p2Ship.getX()+ p2Ship.getWidth()/2 == 0 && p2Ship.getY()+ p2Ship.getHeight()/2 > centerY) {
                        ((ShipGameObject) p2Ship).changeAccel(0,-gravitationalFactor);
                    }
                    else if (p2Ship.getX()+ p2Ship.getWidth()/2 == 0 && p2Ship.getY()+ p2Ship.getHeight()/2 < centerY) {
                        ((ShipGameObject) p2Ship).changeAccel(0,gravitationalFactor);
                    }
                    else if (p2Ship.getX()+ p2Ship.getWidth()/2 < centerX && p2Ship.getY()+ p2Ship.getHeight()/2 < centerY) {
                        ((ShipGameObject) p2Ship).changeAccel(gravitationalFactor,gravitationalFactor);
                    }
                    else if (p2Ship.getX()+ p2Ship.getWidth()/2 > centerX && p2Ship.getY()+ p2Ship.getHeight()/2 < centerY) {
                        ((ShipGameObject) p2Ship).changeAccel(-gravitationalFactor,gravitationalFactor);
                    }
                    else if (p2Ship.getX()+ p2Ship.getWidth()/2 > centerX && p2Ship.getY()+ p2Ship.getHeight()/2 == 0) {
                        ((ShipGameObject) p2Ship).changeAccel(-gravitationalFactor,0);
                    }
                    else if (p2Ship.getX()+ p2Ship.getWidth()/2 < centerX && p2Ship.getY()+ p2Ship.getHeight()/2 == 0) {
                        ((ShipGameObject) p2Ship).changeAccel(gravitationalFactor,0);
                    }
                }
            }
			// remove any entity that has been marked for clear up
			gameObjects.removeAll(removeList);
			removeList.clear();

			// if a game event has indicated that game logic should
			// be resolved, cycle round every entity requesting that
			// their personal logic should be considered.
			if (logicRequiredThisLoop) {
				for (int i=0;i< gameObjects.size();i++) {
					GameObject gameObject = (GameObject) gameObjects.get(i);
					gameObject.doLogic();
				}
				
				logicRequiredThisLoop = false;
			}
			
			// if we're waiting for an "any key" press then draw the 
			// current message
            if (waitingForStartPress) {
                // Overlay transparent bg onto game area
                g.setColor(new Color(0f, 0f, 0f, .75f));
                g.fillRect(0,0,800,600);

                // Draw the logo
                BufferedImage logo = null;
                try {
                    logo = ImageIO.read(getClass().getResourceAsStream("images\\logo.png"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                };
                g.drawImage(logo, (800 - logo.getWidth()) / 2, 20, null);

                //frame.add(new ImagePanel());

                g.setColor(Color.white);
                g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);


                if (waitingForNewHighScore) {
                    g.drawString("New High Scores!", (800-g.getFontMetrics().stringWidth("New High Scores!"))/2, 260);
                    g.drawString("Name: " + newHighScoreName, (800-g.getFontMetrics().stringWidth("Name: " + newHighScoreName))/2, 280);
                } else {
                    // draw highscore string
                    loadHighScores();
                    int i;
                    g.drawString("Rank", 20, 230);
                    for (i = 0; i < highScoresCount; i++) {
                        g.drawString(Integer.toString(i + 1) + ". " + highScores[i], 20, 250 + i * 20);
                    }
                }

                if (waitingForOptionChange) {
                    if (resetHighScoreSelected) {

                        resetHighScoreSelected = false;
                    } else {
                        drawStringOptions(g);
                    }
                } else {
                    if (startGameSelected) {
                        g.drawString("> Start game",(800-g.getFontMetrics().stringWidth("> Start game"))/2,300);
                        g.drawString("  Option",(800-g.getFontMetrics().stringWidth("  Option"))/2,320);
                    } else if (optionSelected) {
                        g.drawString("  Start game",(800-g.getFontMetrics().stringWidth("  Start game"))/2,300);
                        g.drawString("> Option",(800-g.getFontMetrics().stringWidth("> Option"))/2,320);
                    }
                }
            }
			else if (waitingForLevelPress || waitingForPausePress) {
                g.setColor(new Color(.3f, .4f, .5f, .6f));
                g.fillRect(0,0,800,600);
				g.setColor(Color.white);
				g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
				g.drawString("Press any key",(800-g.getFontMetrics().stringWidth("Press any key"))/2,300);
			}
			
			// finally, we've completed drawing so clear up the graphics
			// and flip the buffer over
			g.dispose();
			strategy.show();
			
			// resolve the movement of the p1Ship. First assume the p1Ship
			// isn't moving. If either cursor key is pressed then
			// update the movement appropraitely
			//p1Ship.setHorizontalMovement(0);
            //p1Ship.setRotatedAngle(0);

			if ((p1_leftPressed) && (!p1_rightPressed)) {
                //p1_moveSpeedX += -5;
                p1_currentAngle += -2;
                p1Ship.setRotatedAngle(p1_currentAngle);
				//p1Ship.setHorizontalMovement(p1_moveSpeedX);
			}
            else if ((p1_rightPressed) && (!p1_leftPressed)) {
                //p1_moveSpeedX += 5;
                p1_currentAngle += 2;
                p1Ship.setRotatedAngle(p1_currentAngle);
				//p1Ship.setHorizontalMovement(p1_moveSpeedX);
			}
            if ((p1_upPressed) && (!p1_downPressed)) {
                ((ShipGameObject) p1Ship).changeAccel(Math.cos(Math.toRadians(p1_currentAngle - 90)) * 2, Math.sin(Math.toRadians(p1_currentAngle - 90)) * 2);
                p1Ship.set2DMovement(p1_currentAngle, ((ShipGameObject) p1Ship).getHorizontalMovement(), ((ShipGameObject) p1Ship).getVerticalMovement());
                if (p1ThrusterSFX == 0) {
                    new AePlayWave("sounds\\asteroids_thrust.wav").start();
                }
                p1ThrusterSFX++;
                if (p1ThrusterSFX == 25) {
                    p1ThrusterSFX = 0;
                }

            }
            else if ((!p1_upPressed) && (p1_downPressed)) {
                //p1_moveSpeedY += 5;
                //p1Ship.setVerticalMovement(p1_moveSpeedY);
            }
			
			// if we're pressing fire, attempt to fire
			if (p1_firePressed) {
				p1TryToFire();
			}

            // Player 2 controls
            if ((p2_leftPressed) && (!p2_rightPressed)) {
                //p1_moveSpeedX += -5;
                p2_currentAngle += -2;
                p2Ship.setRotatedAngle(p2_currentAngle);
                //p1Ship.setHorizontalMovement(p1_moveSpeedX);
            }
            else if ((p2_rightPressed) && (!p2_leftPressed)) {
                //p1_moveSpeedX += 5;
                p2_currentAngle += 2;
                p2Ship.setRotatedAngle(p2_currentAngle);
                //p1Ship.setHorizontalMovement(p1_moveSpeedX);
            }
            if ((p2_upPressed) && (!p2_downPressed)) {
                ((ShipGameObject) p2Ship).changeAccel(Math.cos(Math.toRadians(p2_currentAngle - 90)) * 2, Math.sin(Math.toRadians(p2_currentAngle - 90)) * 2);
                p2Ship.set2DMovement(p2_currentAngle, ((ShipGameObject) p2Ship).getHorizontalMovement(), ((ShipGameObject) p2Ship).getVerticalMovement());
                if (p2ThrusterSFX == 0) {
                    new AePlayWave("sounds\\asteroids_thrust.wav").start();
                }
                p2ThrusterSFX++;
                if (p2ThrusterSFX == 25) {
                    p2ThrusterSFX = 0;
                }

            }
            else if ((!p2_upPressed) && (p2_downPressed)) {
                //p1_moveSpeedY += 5;
                //p1Ship.setVerticalMovement(p1_moveSpeedY);
            }

            // if we're pressing fire, attempt to fire
            if (p2_firePressed) {
                p2TryToFire();
            }

            // background tones that get faster with each passing level
            if (!waitingForLevelPress && !waitingForPausePress && !waitingForStartPress) {
                int toneFrequency = 100-(currentLevel*2);
                if (toneCount == toneFrequency)
                    new AePlayWave("sounds\\asteroids_tonehi.wav").start();
                else if (toneCount == toneFrequency * 2) {
                    new AePlayWave("sounds\\asteroids_tonelo.wav").start();
                    toneCount = 0;
                }
                toneCount++;
            }
			
			// finally pause for a bit. Note: this should run us at about
			// 100 fps but on windows this might vary each loop due to
			// a bad implementation of timer
			try { Thread.sleep(10); } catch (Exception e) {}
		}
	}

    public void increaseScore(String whichPlayer, int objectIdentifier, int level) {
        int scoreIncrease = 0;
        switch(objectIdentifier)
        {
            case 0: // Asteroid
                scoreIncrease += 5;
                break;
            case 1: // Alien Ship
                scoreIncrease += 100;
                break;
            case 2: // Player Spaceship
                scoreIncrease += 500;
                break;
            case 3: // Level increase
                scoreIncrease += level * 100;
                break;
        }
        if (whichPlayer.equals("p1"))
        {
            p1ScoreCount += scoreIncrease;
        }
        else if (whichPlayer.equals("p2"))
        {
            p2ScoreCount += scoreIncrease;
        }
    }
	
	/**
	 * Handles keyboard input
	 */
	private class KeyInputHandler extends KeyAdapter {
		/** The number of key presses we've had while waiting for an "any key" press */
		private int pressCount = 0;
		
		/**
		 * Notification from AWT that a key has been pressed. Note that
		 * a key being pressed is equal to being pushed down but *NOT*
		 * released. Thats where keyTyped() comes in.
		 *
		 * @param e The details of the key that was pressed 
		 */
        public void keyPressed(KeyEvent e) {
            // if we're waiting for an "any key" typed then we don't
            // want to do anything with just a "press"
            if (waitingForLevelPress || waitingForPausePress) {
                return;
            }

            if (waitingForStartPress) {
                if (waitingForOptionChange) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        waitingForOptionChange = false;
                    } else if (e.getKeyCode() == KeyEvent.VK_DOWN && currentOptionIndex < optionsCount - 1) {
                        currentOptionIndex++;
                        currentOption = Options.values()[currentOption.ordinal() + 1];
                    } else if (e.getKeyCode() == KeyEvent.VK_UP && currentOptionIndex > 0) {
                        currentOptionIndex--;
                        currentOption = Options.values()[currentOption.ordinal() - 1];
                    } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        if (currentOption == Options.ASTEROIDS && startingAsteroidCount > 1) {
                            startingAsteroidCount--;
                        } else if (currentOption == Options.LIVES && startingLivesCount > 1) {
                            startingLivesCount--;
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        if (currentOption == Options.ASTEROIDS) {
                            startingAsteroidCount++;
                        } else if (currentOption == Options.LIVES) {
                            startingLivesCount++;
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (currentOption == Options.HIGH_SCORE) {
                            resetHighScoreSelected = true;
                        }
                    }
                }
                else {
                    if (startGameSelected) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            waitingForStartPress = false;
                            aulink.stopMusic();
                            startGame();
                            pressCount = 0;
                        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                            startGameSelected = false;
                            optionSelected = true;
                        }
                    } else if (optionSelected) {
                        if (e.getKeyCode() == KeyEvent.VK_UP) {
                            startGameSelected = true;
                            optionSelected = false;
                        }
                        else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            waitingForOptionChange = true;
                        }
                    }
                }
                return;
            }
            if (livesCount2 > 0) {
                if (e.getKeyCode() == KeyEvent.VK_A) {
                    p2_leftPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_D) {
                    p2_rightPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_W) {
                    p2_upPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    p2_downPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    p2_firePressed = true;
                }
            }
            if (livesCount1 > 0) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    p1_leftPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    p1_rightPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    p1_upPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    p1_downPressed = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    p1_firePressed = true;
                }
            }
        }
		
		/**
		 * Notification from AWT that a key has been released.
		 *
		 * @param e The details of the key that was released 
		 */
		public void keyReleased(KeyEvent e) {
			// if we're waiting for an "any key" typed then we don't 
			// want to do anything with just a "released"
			if (waitingForStartPress || waitingForLevelPress || waitingForPausePress) {
				return;
			}
            if (e.getKeyCode() == KeyEvent.VK_A) {
                p2_leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                p2_rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_W) {
                p2_upPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                p2_downPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                p2_firePressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                p1_upPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                p1_downPressed = false;
            }
			if (e.getKeyCode() == KeyEvent.VK_LEFT) {
				p1_leftPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
				p1_rightPressed = false;
			}
			if (e.getKeyCode() == KeyEvent.VK_SPACE) {
				p1_firePressed = false;
			}
//            if (e.getKeyCode() == KeyEvent.VK_P) {
//                pausePressed = false;
//            }
		}

		/**
		 * Notification from AWT that a key has been typed. Note that
		 * typing a key means to both press and then release it.
		 *
		 * @param e The details of the key that was typed. 
		 */
		public void keyTyped(KeyEvent e) {
			// if we're waiting for a "any key" type then
			// check if we've recieved any recently. We may
			// have had a keyType() event from the user releasing
			// the shoot or move keys, hence the use of the "pressCount"
			// counter.
			if (waitingForStartPress) {
				if (pressCount == 1) {
					// since we've now recieved our key typed
					// event we can mark it as such and start 
					// our new game
					waitingForStartPress = false;
                    aulink.stopMusic();

					startGame();
					pressCount = 0;
				} else {
					pressCount++;
				}
			}
            else if (waitingForLevelPress) {
                if (pressCount == 1) {
                    // since we've now recieved our key typed
                    // event we can mark it as such and start
                    // our new game
                    waitingForLevelPress = false;
                    startNewLevel();
                    pressCount = 0;
                } else {
                    pressCount++;
                }
            }
            else if (waitingForPausePress) {
                if (pressCount == 1) {
                    // since we've now recieved our key typed
                    // event we can mark it as such and start
                    // our new game
                    waitingForPausePress = false;
                    startNewLevel();
                    pressCount = 0;
                } else {
                    pressCount++;
                }
            }


            // if we hit escape, then quit the game
			if (e.getKeyChar() == 27) {
				System.exit(0);
			}
		}
	}

    public ArrayList getGameObjects() {
        return gameObjects;
    }

    public GameObject getP1Ship() {
        return p1Ship;
    }

    public GameObject getP2Ship() {
        return p2Ship;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public boolean isFriendlyFireOn() {
        return friendlyFireOn;
    }

    /**
	 * The entry point into the game. We'll simply create an
	 * instance of class which will start the display and game
	 * loop.
	 * 
	 * @param argv The arguments that are passed into our game
	 */
	public static void main(String argv[]) {
		Game g =new Game();

		// Start the main game loop, note: this method will not
		// return until the game has finished running. Hence we are
		// using the actual main thread to run the game.
		g.gameLoop();
	}
}
