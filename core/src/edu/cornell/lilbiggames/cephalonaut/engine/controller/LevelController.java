package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LEBlackHole;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LEBoostPad;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.LevelElement;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.SimpleObstacle;

import java.util.logging.Level;

public class LevelController implements ContactListener {
    /** Reference to player **/
    private final CephalonautModel cephalonaut;

    /** Level element constants. TODO: Should be moved to LevelElement eventually. **/
    private static final float METEOR_SPEED = 2f;
    private static final float DOOR_SIZE = 1f;

    public LevelController(CephalonautModel cephalonaut) {
        this.cephalonaut = cephalonaut;
    }

    public void update(GameObject object) {
        if (object instanceof LEBoostPad) {
            boost((LEBoostPad) object);
        } else if (object instanceof LEBlackHole) {
            attract((LEBlackHole) object);
        }

        if (object instanceof LevelElement) {
            LevelElement levelElement = ((LevelElement) object);
            if (levelElement.getElement() == LevelElement.Element.FINISH && levelElement.getInContact()) {
                finishLevel();
            }
        }

//        if (object.getClass() == LevelElement.class) {
//            switch (((LevelElement) object).getElement()) {
//                case BLACK_HOLE:
//                    attract(object);
//                    break;
//                case FLYING_METEOR:
////						updateFlyingMeteor((LevelElement) object);
//                    break;
//                case BOOST_PAD:
//                    boost((LEBoostPad) object);
//                    break;
//                case DOOR:
//                    if (((LevelElement) object).getActivated()) {
//                        openDoor((LevelElement) object);
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
    }

    /** Force from cephalonaut attracted to obj */
    public void attract(LEBlackHole blackHole) {
        Vector2 blackHolePos = blackHole.getBody().getWorldCenter();
        Vector2 cephalonautPos = cephalonaut.getBody().getWorldCenter();

        if (blackHolePos.dst(cephalonautPos) < blackHole.getBlackHoleRange()) {
            Vector2 force = blackHolePos.sub(cephalonautPos).clamp(1, 5).nor();
            float strength = blackHole.getBlackHoleAttractFactor() * cephalonaut.getMass() / force.len2();
            cephalonaut.addForce(force.scl(strength));
        }
    }

    public void boost(LEBoostPad obj) {
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

    private GameObject getOtherBody(Contact contact, GameObject object) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        GameObject bd1 = (GameObject) body1.getUserData();
        GameObject bd2 = (GameObject) body2.getUserData();

        if (bd1 == object) {
            return bd2;
        } else if (bd2 == object) {
            return bd1;
        } else {
            return null;
        }
    }

    public void beginContact(Contact contact) {
        GameObject contactObject = getOtherBody(contact, cephalonaut);
        if (contactObject != null) {
            if (contactObject instanceof LevelElement) {
                ((LevelElement) contactObject).setInContact(true);
            }
        }

        GrappleModel grapple = cephalonaut.getGrapple();
        if (!grapple.isAnchored()) {
            contactObject = getOtherBody(contact, grapple);
            if (contactObject != null && contactObject.canGrapple()) {
                grapple.setAnchored(true);
                grapple.setExtensionLength(1 + cephalonaut.getPosition().dst(contactObject.getPosition()));
                grapple.setAnchorLocation(contactObject.getName());
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        Body body1 = contact.getFixtureA().getBody();
        Body body2 = contact.getFixtureB().getBody();

        GameObject bd1 = (GameObject) body1.getUserData();
        GameObject bd2 = (GameObject) body2.getUserData();

        GameObject contactObject = null;
        if (bd1 instanceof CephalonautModel) {
            contactObject = bd2;
        } else if (bd2 instanceof CephalonautModel) {
            contactObject = bd1;
        }

        if (contactObject != null) {
            if (contactObject instanceof LevelElement) {
                ((LevelElement) contactObject).setInContact(false);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) { }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) { }

}
