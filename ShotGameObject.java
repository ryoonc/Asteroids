package org.newdawn.spaceinvaders;

/**
 * An entity representing a shot fired by the player's ship
 *
 * @author Kevin Glass
 */
public class ShotGameObject extends GameObject {
	/** The vertical speed at which the players shot moves */
	private double moveSpeed = 300;
    /** The counter for the shot dissappearing **/
    private int counter = 0;
	/** The game in which this entity exists */
	private Game game;
	/** True if this shot has been "used", i.e. its hit something */
	private boolean used = false;
    AePlayWave currentSound;
    private String shotBy;

	/**
	 * Create a new shot from the player
	 * 
	 * @param game The game in which the shot has been created
	 * @param sprite The sprite representing this shot
	 * @param x The initial x location of the shot
	 * @param y The initial y location of the shot
	 */
	public ShotGameObject(Game game, String sprite, int x, int y) {
		super(sprite,x,y);
		
		this.game = game;

        sync_cord_dxdy.replace("dy", new Double(moveSpeed));
//		dy = moveSpeed;
	}

    /**
     * Create a new shot from the player
     *
     * @param game The game in which the shot has been created
     * @param sprite The sprite representing this shot
     * @param x The initial x location of the shot
     * @param y The initial y location of the shot
     * @param currentAngle The initial angle of the shot
     */
    public ShotGameObject(Game game, String sprite, int x, int y, int currentAngle, double currentxvel, double currentyvel, String shotBy) {
        super(sprite,x,y, currentAngle);
        this.counter = 0;
        this.game = game;
        this.currentAngle = currentAngle;
        this.shotBy = shotBy;
        // TODO: x and y flipped. verify
        sync_cord_dxdy.replace("dy", new Double(Math.sin(Math.toRadians((double)(currentAngle-90)))*moveSpeed));
        sync_cord_dxdy.replace("dx", new Double(Math.cos(Math.toRadians((double)(currentAngle-90)))*moveSpeed));
//        dy = Math.sin(Math.toRadians((double)(currentAngle-90)))*moveSpeed+currentxvel;
//        dx = Math.cos(Math.toRadians((double)(currentAngle-90)))*moveSpeed+currentyvel;
    }

    /**
     * Create a new shot from the player
     *
     * @param game The game in which the shot has been created
     * @param sprite The sprite representing this shot
     * @param x The initial x location of the shot
     * @param y The initial y location of the shot
     * @param currentAngle The initial angle of the shot
     */
    public ShotGameObject(Game game, String sprite, int x, int y, int currentAngle, double currentxvel, double currentyvel, String shotBy, int in_moveSpeed) {
        super(sprite,x,y, currentAngle);
        this.counter = 0;
        this.game = game;
        this.currentAngle = currentAngle;
        this.shotBy = shotBy;
        // TODO: x and y flipped. verify
        sync_cord_dxdy.replace("dy", new Double(Math.sin(Math.toRadians((double)(currentAngle-90)))*in_moveSpeed));
        sync_cord_dxdy.replace("dx", new Double(Math.cos(Math.toRadians((double)(currentAngle-90)))*in_moveSpeed));
//        dy = Math.sin(Math.toRadians((double)(currentAngle-90)))*moveSpeed+currentxvel;
//        dx = Math.cos(Math.toRadians((double)(currentAngle-90)))*moveSpeed+currentyvel;
    }
    // TODO: change spawn location of spaceship
	/**
	 * Request that this shot moved based on time elapsed
	 * 
	 * @param delta The time that has elapsed since last move
	 */
	public void move(long delta) {
		// proceed with normal move
		super.move(delta);
		// if we shot off the screen, remove ourselfs
        counter++;  // it moves with the timer, so this value might have to change depending on the system
        if (shotBy.equals("p1")) {
            if (counter > 200) {
                game.removeEntity(this);
            }
        }
        else if (shotBy.equals("p2")) {
            if (counter > 200) {
                game.removeEntity(this);
            }
        }
        else if (shotBy.equals("alien")) {
            if (counter > 400) {
                game.removeEntity(this);
            }
        }
	}
	
	/**
	 * Notification that this shot has collided with another
	 * entity
	 * 
	 * @parma other The other entity with which we've collided
	 */
	public void collidedWith(GameObject other) {
		// prevents double kills, if we've already hit something,
		// don't collide
		if (used) {
            if (currentSound != null)
                currentSound.stopMusic();
			return;
		}
		
		// if we've hit an asteroid
		if (other instanceof AsteroidGameObject) {
            if (((AsteroidGameObject) other).isBig())
            {
                game.bigAsteroidDestroyed(this);
                game.removeEntity(this);
                game.removeEntity(other);
                game.notifyAsteroidKilled();
                new AePlayWave("sounds\\bangLarge.wav").start();
                used = true;
            }
            else
            {
                // remove the affected entities
                game.removeEntity(this);
                game.removeEntity(other);

                // notify the game that the alien has been killed
                game.notifyAsteroidKilled();
                new AePlayWave("sounds\\bangSmall.wav").start();
                used = true;
            }
            //System.out.println(shotBy);
            game.increaseScore(shotBy, 0, 1);
		}

        // Now check depending on who fired the shot
        if (shotBy.equals("alien") && other instanceof ShipGameObject)
        {
            game.notifyDeath(((ShipGameObject)other).getPlayerTag());
            game.removeEntity(this);
            game.refreshRemoval();
        }
        else if ((shotBy.equals("p1") || shotBy.equals("p2")) && other instanceof AlienShipGameObject)
        {
            game.removeEntity(this);
            game.removeEntity(other);
            ((AlienShipGameObject)other).stopMusic();
            currentSound = new AePlayWave("sounds\\AlienThud.wav",1);
            currentSound.start();
            game.increaseScore(shotBy,1,game.getCurrentLevel());
            used = true;
        }
        else if ((shotBy.equals("p1") || shotBy.equals("p2")) && other instanceof ShipGameObject)
        {
            if (game.isFriendlyFireOn()) {
                if (!((ShipGameObject) other).getPlayerTag().equals(shotBy)) {
                    game.notifyDeath(((ShipGameObject) other).getPlayerTag());
                    game.removeEntity(this);
                    //game.removeEntity(other);
    //                //((AlienShipGameObject)other).stopMusic();
    //                new AePlayWave("sounds\\ShipsCollide.wav",1);

                    //game.increaseScore(shotBy,1,game.getCurrentLevel());
                    used = true;
                }
            }
        }
	}
}