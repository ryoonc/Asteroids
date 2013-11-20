package org.newdawn.spaceinvaders;

/**
 * An object representing a player ship
 */
public class ShipGameObject extends GameObject {
	/** The game in which the ship exists */
	private Game game;
    private String playerTag;
	
	/**
	 * Create a new entity to represent the players ship
	 *  
	 * @param game The game in which the ship is being created
	 * @param ref The reference to the sprite to show for the ship
	 * @param x The initial x location of the player's ship
	 * @param y The initial y location of the player's ship
	 */
	public ShipGameObject(Game game, String ref, double x, double y, String playerTag) {
		super(ref,x,y);
		this.playerTag = playerTag;
		this.game = game;
	}
	
	/**
	 * Request that the ship move itself based on an elapsed ammount of
	 * time
	 * 
	 * @param delta The time that has elapsed since last move (ms)
	 */
	public void move(long delta) {
		super.move(delta);
	}

    public void changeAccel (double xAccel, double yAccel)
    {
        incrementdxdyCordCountXY("dx", new Double(xAccel));
        incrementdxdyCordCountXY("dy", new Double(yAccel));
//        this.dx += xAccel;
//        this.dy += yAccel;
    }
	
	/**
	 * Notification that the player's ship has collided with something
	 * 
	 * @param other The entity with which the ship has collided
	 */
	public void collidedWith(GameObject other) {
		// if its an alien, notify the game that the player
		// is dead
        if (other instanceof AsteroidGameObject) {
            if (((AsteroidGameObject) other).isBig()) {
                game.notifyDeath(playerTag);
                game.removeEntity(other);
                game.bigAsteroidDestroyed(other);
                game.notifyAsteroidKilled();

            }
            else {
                game.removeEntity(other);
                game.notifyAsteroidKilled();
                game.notifyDeath(playerTag);
            }
            game.refreshRemoval();
        }
		else if (other instanceof AlienShipGameObject) {
            game.removeEntity(other);
			game.notifyDeath(playerTag);
		}
        else if (other instanceof GravitationalGameObject) {
            game.notifyDeath(playerTag);
        }
        else if (other instanceof ShipGameObject)
        {
            if (game.isFriendlyFireOn()) {
                game.notifyDeath(playerTag);
                game.notifyDeath(((ShipGameObject) other).getPlayerTag());
                game.refreshRemoval();
            }
        }
	}

    public String getPlayerTag() {
        return playerTag;
    }
}