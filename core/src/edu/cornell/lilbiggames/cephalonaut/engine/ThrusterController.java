/*
 * WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.lilbiggames.cephalonaut.engine;



/**
 * Base class for a thruster controller controller.
 *
 * This controls thruster physics for the octopus.
 */
public class ThrusterController  {
    /** Reference to the cephalonaut */
    private CephalonautModel player;

    /**
     * Constructor
     * @param player
     */
    public ThrusterController(CephalonautModel player){
        this.player = player;
    }

    /**
     * Set the cephalonaut model to use
     * @param player the cephalonaut model object
     */
    public void setPlayer(CephalonautModel player){
        this.player = player;
    }


    /** Apply ink-thrust */
    public void startInking(){
        player.setInking(true);
    }

    /** Stop ink-thrust */
    public void stopInking(){
        player.setInking(false);
    }

    /**
     * Sets the rotation of the octopus
     * @param rotation 1.0 for clockwise rotation, 0 for no rotation, and -1.0 for counterclockwise rotation
     *
     */
    public void setRotation(float rotation){
        player.setRotationalDirection(rotation);
    }
}