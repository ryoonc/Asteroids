package org.newdawn.spaceinvaders;

import java.awt.*;

/**
 * An object representing an asteroid
 */
public class AsteroidGameObject extends GameObject {
	/** The speed at which the alient moves horizontally */
	private double moveSpeed;
    private int currentAngle;
    private boolean big;
	/** The game in which the entity exists */
	private Game game;

	/**
	 * Create a new alien entity
	 *
	 * @param game The game in which this entity is being created
	 * @param ref The sprite which should be displayed for this alien
	 * @param x The intial x location of this alien
	 * @param y The intial y location of this alient
	 */
	public AsteroidGameObject(Game game, String ref, int x, int y, int currentAngle, boolean big, int currentLevel) {
		super(ref,x,y, currentAngle);
		this.big = big;
		this.game = game;
        this.currentAngle = currentAngle;
        moveSpeed = Math.random()*25*currentLevel;
		setHorizontalMovement(Math.sin(Math.toRadians((double)currentAngle))*moveSpeed);
        setVerticalMovement(Math.cos(Math.toRadians((double)currentAngle))*moveSpeed);
	}

	/**
	 * Request that this alien moved based on time elapsed
	 * 
	 * @param delta The time that has elapsed since last move
	 */
	public void move(long delta) {
		// if we have reached the left hand side of the screen and
		// are moving left then request a logic update
        // if we're moving left and have reached the left hand side
        // of the screen, don't move
        game.updateLogic();

//		if ((dx < 0) && (x < 10)) {
//			game.updateLogic();
//		}
//		// and vice vesa, if we have reached the right hand side of
//		// the screen and are moving right, request a logic update
//		if ((dx > 0) && (x > 750)) {
//			game.updateLogic();
//		}
		
		// proceed with normal move
		super.move(delta);
	}

    public void draw(Graphics g) {
        sprite.draw(g,getX(),getY(), currentAngle);
    }
	
	/**
	 * Update the game logic related to aliens
	 */
	public void doLogic() {
        currentAngle += moveSpeed/50;
        super.setRotatedAngle(currentAngle);
		// swap over horizontal movement and move down the
		// screen a bit
//		dx = -dx;
//		y += 10;
		
		// if we've reached the bottom of the screen then the player
		// dies
//		if (y > 570) {
//			game.bothPlayers_notifyDeath();
//		}


	}
	
	/**
	 * Notification that this alien has collided with another entity
	 * 
	 * @param other The other entity
	 */
	public void collidedWith(GameObject other) {
		// collisions with aliens are handled elsewhere
	}

    public boolean isBig() {
        return big;
    }
}