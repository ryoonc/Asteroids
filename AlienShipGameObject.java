package org.newdawn.spaceinvaders;

// TODO: make it vulnerable to asteroids
public class AlienShipGameObject extends GameObject {
	/** The speed at which the alient moves horizontally */
	private double moveSpeed = 75;
	/** The game in which the entity exists */
	private Game game;
    private int shotTimer;
    private int counter;
    private int currentAngle;
    private int hoverSoundTimer;
    private AePlayWave aulink;

	/**
	 * Create a new alien entity
	 *
	 * @param game The game in which this entity is being created
	 * @param ref The sprite which should be displayed for this alien
	 * @param x The intial x location of this alien
	 * @param y The intial y location of this alient
	 */
	public AlienShipGameObject(Game game, String ref, int x, int y, int currentAngle, int currentLevel, int shotTimer) {
		super(ref,x,y);
		this.shotTimer = shotTimer;
		this.game = game;
        this.currentAngle = currentAngle;
        counter = 0;
        hoverSoundTimer = 0;
        moveSpeed = Math.random()*25*currentLevel;
        setHorizontalMovement(Math.sin(Math.toRadians((double)currentAngle))*moveSpeed);
        setVerticalMovement(Math.cos(Math.toRadians((double)currentAngle))*moveSpeed);
//		dx = -moveSpeed;
        aulink = null;
	}

	/**
	 * Request that this alien moved based on time elapsed
	 * 
	 * @param delta The time that has elapsed since last move
	 */
	public void move(long delta) {
		// if we have reached the left hand side of the screen and
		// are moving left then request a logic update 
//		if ((dx < 0) && (x < 10)) {
//			game.updateLogic();
//		}
//		// and vice vesa, if we have reached the right hand side of
//		// the screen and are moving right, request a logic update
//		if ((dx > 0) && (x > 750)) {
//			game.updateLogic();
//		}
		game.updateLogic();
		// proceed with normal move
		super.move(delta);
	}
	
	/**
	 * Update the game logic related to aliens
	 */
	public void doLogic() {
        if (counter == shotTimer)
        {
            ShipGameObject playerShip = (ShipGameObject)game.getP1Ship();
//            int xDiff = getX() - playerShip.getX();
//            int yDiff = getY() - playerShip.getY();
////            int xDiff;
////            int yDiff;
////            if (playerShip.getX() > getX()) {
////                xDiff = playerShip.getX() - getX();
////            }
////            else {
////                xDiff = getX() - playerShip.getX();
////            }
////            if (playerShip.getY() > getY()) {
////                yDiff = playerShip.getY() - getY();
////            }
////            else {
////                yDiff = getY() - playerShip.getY();
////            }
//            int angleToShip = (int)Math.toDegrees(Math.atan2((double)yDiff,(double)xDiff));
//            if (angleToShip < 0)
//                angleToShip += 360;

            double dx = playerShip.getX() - getX();
            // Minus to correct for coord re-mapping
            double dy = -(playerShip.getY() - getY());

            double inRads = Math.atan2(dy,dx);

            // We need to map to coord system when 0 degree is at 3 O'clock, 270 at 12 O'clock
            if (inRads < 0)
                inRads = Math.abs(inRads);
            else
                inRads = 2*Math.PI - inRads;

            int angleToShip = (int)Math.toDegrees(inRads)+90;


            //System.out.println("xDiff = " + xDiff + " yDiff = " + yDiff + "angleToShip = " + angleToShip);
            ShotGameObject shot = new ShotGameObject(game,"sprites\\EnemyBullet.png",getX()+getWidth()/2-3,getY()+getHeight()/2-3, angleToShip, 100, 100, "alien", 100);
            new AePlayWave("sounds\\aliengun.wav", 1).start();
            game.getGameObjects().add(shot);
            counter = 0;
        }
        else
            counter++;

        if (hoverSoundTimer == 0) {
            aulink = new AePlayWave("sounds\\asteroids_saucer.wav");
            aulink.start();
            hoverSoundTimer = 200;
        }
        else
            hoverSoundTimer--;
		// swap over horizontal movement and move down the
		// screen a bit
//		dx = -dx;
//		y += 10;
//
//		// if we've reached the bottom of the screen then the player
//		// dies
//		if (y > 570) {
//			game.bothPlayers_notifyDeath();
//		}

	}

    public void stopMusic() {
        if (aulink != null)
            aulink.stopMusic();
    }
	
	/**
	 * Notification that this alien has collided with another entity
	 * 
	 * @param other The other entity
	 */
	public void collidedWith(GameObject other) {
		// collisions with aliens are handled elsewhere
        if (other instanceof AsteroidGameObject) {
            if (((AsteroidGameObject) other).isBig()) {
                game.removeEntity(this);
                stopMusic();
                game.removeEntity(other);
                game.bigAsteroidDestroyed(other);
                game.notifyAsteroidKilled();

            }
            else {
                game.removeEntity(other);
                game.notifyAsteroidKilled();
                game.removeEntity(this);
                stopMusic();
            }
            game.refreshRemoval();
        }
	}
}