package org.newdawn.spaceinvaders;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The object representing all other objects in game.
 *
 * Position coordinates are doubles to preserve accuracy
 */
public abstract class GameObject {
    protected ConcurrentMap<String, Double> sync_cord_xy =  new ConcurrentHashMap<String, Double>(2);
    protected ConcurrentMap<String, Double> sync_cord_dxdy =  new ConcurrentHashMap<String, Double>(2);
//    /** The current x location of this entity */
//	protected double x;
//	/** The current y location of this entity */
//	protected double y;
    /** The current angle (in degrees) of this entity */
    protected int currentAngle;
	/** The sprite that represents this entity */
	protected Sprite sprite;
//	/** The current speed of this entity horizontally (pixels/sec) */
//	protected double dx;
//	/** The current speed of this entity vertically (pixels/sec) */
//	protected double dy;
	/** The rectangle used for this entity during collisions  resolution */
	private Rectangle me = new Rectangle();
	/** The rectangle used for other entities during collision resolution */
	private Rectangle him = new Rectangle();
	
	/**
	 * Construct a game object given a sprite name and location
	 * 
	 * @param ref The reference to the image to be displayed for this entity
 	 * @param x The initial x location of this entity
	 * @param y The initial y location of this entity
	 */
	public GameObject(String ref, int x, int y) {
		this.sprite = SpriteStore.get().getSprite(ref);
        this.currentAngle = 0;
        sync_cord_xy.put("x", new Double(x));
        sync_cord_xy.put("y", new Double(y));
        sync_cord_dxdy.put("dx", new Double(0));
        sync_cord_dxdy.put("dy", new Double(0));
//		this.x = x;
//		this.y = y;
	}

    public GameObject(String ref, double x, double y) {
        this.sprite = SpriteStore.get().getSprite(ref);
        this.currentAngle = 0;
        sync_cord_xy.put("x", new Double(x));
        sync_cord_xy.put("y", new Double(y));
        sync_cord_dxdy.put("dx", new Double(0));
        sync_cord_dxdy.put("dy", new Double(0));
    }

    /**
     * Construct a entity based on a sprite image and a location and rotation angle.
     *
     * @param ref The reference to the image to be displayed for this entity
     * @param x The initial x location of this entity
     * @param y The initial y location of this entity
     * @param currentAngle The initial angle of this entity
     */
    public GameObject(String ref, int x, int y, int currentAngle) {
        this.sprite = SpriteStore.get().getSprite(ref);
        sync_cord_xy.put("x", new Double(x));
        sync_cord_xy.put("y", new Double(y));
        this.currentAngle = currentAngle;
        sync_cord_dxdy.put("dx", new Double(0));
        sync_cord_dxdy.put("dy", new Double(0));
    }

    protected void incrementCordCountXY(String key, Double addValue) {
        Double oldVal, newVal;
        do {
            oldVal = sync_cord_xy.get(key);
            newVal = (oldVal == null) ? 1 : (oldVal + addValue);
        } while (!sync_cord_xy.replace(key, oldVal, newVal));
    }

    protected void incrementdxdyCordCountXY(String key, Double addValue) {
        Double oldVal, newVal;
        do {
            oldVal = sync_cord_dxdy.get(key);
            newVal = (oldVal == null) ? 1 : (oldVal + addValue);
        } while (!sync_cord_dxdy.replace(key, oldVal, newVal));
    }

    /**
     * Get the width of the drawn sprite
     *
     * @return The width in pixels of this sprite
     */
    public int getWidth() {
        return sprite.getWidth();
    }

    /**
     * Get the height of the drawn sprite
     *
     * @return The height in pixels of this sprite
     */
    public int getHeight() {
        return sprite.getHeight();
    }
	
	/**
	 * Request that this entity move itself based on a certain amount
	 * of time passing.
	 * 
	 * @param delta The amount of time that has passed in milliseconds
	 */
	public void move(long delta) {
        // TODO: improve for velocity
		// update the location of the entity based on move speeds
        incrementCordCountXY("x", new Double((delta * sync_cord_dxdy.get("dx")) / 1000));
        incrementCordCountXY("y", new Double((delta * sync_cord_dxdy.get("dy")) / 1000));
//		x += (delta * dx) / 1000;
//		y += (delta * dy) / 1000;

        // if we're moving left and have reached the left hand side
        // of the screen, don't move
        if (sync_cord_xy.get("x").intValue() < 0) {
            sync_cord_xy.replace("x", new Double(800));
        }
        // if we're moving right and have reached the right hand side
        // of the screen, don't move
        if (sync_cord_xy.get("x").intValue() > 800) {
            sync_cord_xy.replace("x", new Double(0));
        }
        // if we're moving up and have reached the top side
        // of the screen, don't move
        if (sync_cord_xy.get("y").intValue() < 0) {
            sync_cord_xy.replace("y", new Double(600));
        }
        // if we're moving down and have reached the bottom side
        // of the screen, don't move
        if (sync_cord_xy.get("y").intValue() > 600) {
            sync_cord_xy.replace("y", new Double(0));
        }
        //currentAngle += (delta * dcurrentangle) / 1000;
	}
	
	/**
	 * Set the horizontal speed of this entity
	 * 
	 * @param dx The horizontal speed of this entity (pixels/sec)
	 */
	public void setHorizontalMovement(double dx) {
        sync_cord_dxdy.replace("dx", new Double(dx));
//		this.dx = dx;
	}

    /**
     * Set the 2D speed of this entity
     *
     * @param currentAngle The angle of this entity
     * @param dx The x velocity of this entity (pixels/sec)
     * @param dy The y velocity of this entity (pixels/sec)
     */
    public void set2DMovement(int currentAngle, double dx, double dy) {
        sync_cord_dxdy.replace("dx", new Double(dx));
        sync_cord_dxdy.replace("dy", new Double(dy));
    }

	/**
	 * Set the vertical speed of this entity
	 * 
	 * @param dy The vertical speed of this entity (pixels/sec)
	 */
	public void setVerticalMovement(double dy) {
        sync_cord_dxdy.replace("dy", new Double(dy));
	}

    /**
     * Set the rotation of this entity
     *
     * @param currentAngle The rotational angle of this entity
     */
    public void setRotatedAngle(int currentAngle) {
        this.currentAngle = currentAngle;
    }
	
	/**
	 * Get the horizontal speed of this entity
	 * 
	 * @return The horizontal speed of this entity (pixels/sec)
	 */
	public double getHorizontalMovement() {
		return sync_cord_dxdy.get("dx");
	}

	/**
	 * Get the vertical speed of this entity
	 * 
	 * @return The vertical speed of this entity (pixels/sec)
	 */
	public double getVerticalMovement() {
		return sync_cord_dxdy.get("dy");
	}

    /**
     * Get the rotational speed of this entity
     *
     * @return The vertical speed of this entity (pixels/sec)
     */
    public int getRotationalMovement() {
        return currentAngle;
    }
	
	/**
	 * Draw this entity to the graphics context provided
	 * 
	 * @param g The graphics context on which to draw
	 */
	public void draw(Graphics g) {
		sprite.draw(g,sync_cord_xy.get("x").intValue(), sync_cord_xy.get("y").intValue(), currentAngle);
	}
	
	/**
	 * Do the logic associated with this entity. This method
	 * will be called periodically based on game events
	 */
	public void doLogic() {
	}
	
	/**
	 * Get the x location of this entity
	 * 
	 * @return The x location of this entity
	 */
	public int getX() {
		return sync_cord_xy.get("x").intValue();
	}

	/**
	 * Get the y location of this entity
	 * 
	 * @return The y location of this entity
	 */
	public int getY() {
		return (int) sync_cord_xy.get("y").intValue();
	}

    /**
     * Get the angle of this entity
     *
     * @return The angle of this entity
     */
    public int getAngle() {
        return (int) currentAngle;
    }
	
	/**
	 * Check if this entity collised with another.
	 * 
	 * @param other The other entity to check collision against
     * @return True if the entities collide with each other
	 */
	public boolean collidesWith(GameObject other) {
		me.setBounds(getX(),getY(),sprite.getWidth()-5,sprite.getHeight()-5);
		him.setBounds((int) other.getX(),(int) other.getY(),other.sprite.getWidth()-5,other.sprite.getHeight()-5);

		return me.intersects(him);
	}
	
	/**
	 * Notification that this entity collided with another.
	 * 
	 * @param other The entity with which this entity collided.
	 */
	public abstract void collidedWith(GameObject other);
}