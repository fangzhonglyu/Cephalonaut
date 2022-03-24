package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.SimpleObstacle;

public class LevelController implements ContactListener {
    /** Reference to player **/
    private final CephalonautModel cephalonaut;

    /** Level element constants. TODO: Should be moved to LevelElement eventually. **/
    private static final float ATTRACT_DIST = 5f;
    private static final float METEOR_SPEED = 2f;
    private static final float DOOR_SIZE = 1f;

    public LevelController(CephalonautModel cephalonaut) {
        this.cephalonaut = cephalonaut;
    }

    public void update(GameObject object) {
        if (object.getClass() == LevelElement.class) {
            switch (((LevelElement) object).getElement()) {
                case BLACK_HOLE:
                    attract(object);
                    break;
                case FLYING_METEOR:
//						updateFlyingMeteor((LevelElement) object);
                    break;
                case BOOST_PAD:
                    boost((LevelElement) object);
                    break;
                case DOOR:
                    if (((LevelElement) object).getActivated()) {
                        openDoor((LevelElement) object);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /** Force from cephalonaut attracted to obj */
    public void attract(GameObject obj) {
        if(Math.abs(obj.getBody().getPosition().cpy().sub(cephalonaut.getPosition().cpy()).len()) < ATTRACT_DIST) {
            Vector2 pos = obj.getBody().getWorldCenter();
            Vector2 objPos = cephalonaut.getBody().getWorldCenter();
            Vector2 force = pos.sub(objPos).clamp(1, 5).nor();
            float strength = 9.81f * cephalonaut.getBody().getMass() / force.len2();
            force.scl(strength);
            cephalonaut.addForce(force);
        }
    }

    public void boost(LevelElement obj) {
        if(!obj.getInContact()) return;

        Vector2 force = new Vector2(0, obj.getBoostPadFactor()).setAngleRad(obj.getAngle() + obj.getBoostPadAngle());
        cephalonaut.addForce(force);
    }

    public void openDoor(LevelElement element) {
        if(element.getOpened()) {
            element.setLinearVelocity(Vector2.Zero);
            return;
        }
        element.setLinearVelocity(new Vector2(0, 1));
        if(element.getBody().getPosition().y >= element.getOriginalPos().y + 1.4  * DOOR_SIZE) {
            element.setOpened(true);
        }
    }

    public void finishLevel() {
        System.out.println("Level finished!");
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        SimpleObstacle bd1 = (SimpleObstacle)body1.getUserData();
        SimpleObstacle bd2 = (SimpleObstacle)body2.getUserData();

        try {
            if (bd1.getClass() == LevelElement.class && bd2.getName().equals("michael")) {
                ((LevelElement) bd1).setInContact(true);
            } else if (bd2.getClass() == LevelElement.class && bd1.getName().equals("michael")) {
                ((LevelElement) bd2).setInContact(true);
            }

            GrappleModel grapple = cephalonaut.getGrapple();
            if (!grapple.isAnchored()) {
                if (bd1.getName().equals("grapple") && !bd2.getName().equals("michael") && bd2.canGrapple()) {
                    grapple.setAnchored(true);
                    grapple.setExtensionLength(1 + cephalonaut.getPosition().dst(bd2.getPosition()));
                    grapple.setAnchorLocation(bd2.getName());
                }
                if (bd2.getName().equals("grapple") && !bd1.getName().equals("michael") && bd1.canGrapple()) {
                    grapple.setAnchored(true);
                    grapple.setExtensionLength(1 + cephalonaut.getPosition().dst(bd1.getPosition()));
                    grapple.setAnchorLocation(bd1.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        SimpleObstacle bd1 = (SimpleObstacle)body1.getUserData();
        SimpleObstacle bd2 = (SimpleObstacle)body2.getUserData();

        try {
            if (bd1.getClass() == LevelElement.class && bd2.getName().equals("michael")) {
                ((LevelElement) bd1).setInContact(false);
                if(bd1.getName().equals("finish")) {
                    finishLevel();
                }
            }
            if (bd2.getClass() == LevelElement.class && bd1.getName().equals("michael")) {
                ((LevelElement) bd2).setInContact(false);
                if(bd2.getName().equals("finish")) {
                    finishLevel();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) { }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }

}
