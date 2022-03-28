package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.*;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements.*;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;

public class LevelController implements ContactListener {
    /** Reference to player **/
    private final CephalonautModel cephalonaut;

    /** Reference to the play mode **/
    private final PlayMode playMode;

    public LevelController(CephalonautModel cephalonaut, PlayMode playMode) {
        this.cephalonaut = cephalonaut;
        this.playMode = playMode;
    }

    public void update(GameObject object, CephalonautController cephalonautController) {
        if (object instanceof LEBoostPad) {
            boost((LEBoostPad) object);
        } else if (object instanceof LEBlackHole) {
            attract((LEBlackHole) object);
        } else if (object instanceof LETriggerable) {
            ((LETriggerable) object).checkPos();
        } else if (object instanceof LEWormHole && ((LEWormHole) object).getCooldown() > 0) {
            ((LEWormHole) object).setCooldown(((LEWormHole) object).getCooldown() - 1);
        }

        if (object instanceof LevelElement) {
            LevelElement levelElement = ((LevelElement) object);
            if (levelElement.getElement() == LevelElement.Element.FINISH && levelElement.getInContact()) {
                finishLevel();
            }
        }

        if(cephalonaut.getShouldTeleport()) {
            teleport(cephalonautController);
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

    public void teleport(CephalonautController cephalonautController) {
        GrappleModel grapple = cephalonaut.getGrapple();
        if(grapple.isOut()) {
            cephalonautController.removeGrapple(grapple);
        }
        cephalonaut.setPosition(cephalonaut.getTeleportLocation());
        cephalonaut.setShouldTeleport(false);
    }

    public void setTeleport(LEWormHole wormHole) {
        cephalonaut.setTeleportLocation(wormHole.getPosition());
        cephalonaut.setShouldTeleport(true);
    }

    public void boost(LEBoostPad obj) {
        if(!obj.getInContact()) return;

        Vector2 force = new Vector2(0, obj.getBoostPadFactor()).setAngleRad(obj.getAngle() + obj.getBoostPadAngle());
        cephalonaut.addForce(force);
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

            if (contactObject instanceof LETrigger) {
                LETrigger trigger = (LETrigger) contactObject;
                LETriggerable target = (LETriggerable) playMode.getObject(trigger.getTarget());
                target.setActivated(trigger.isActivated());
            }

            if (contactObject instanceof LEWormHole) {
                LEWormHole hole1 = (LEWormHole) contactObject;
                LEWormHole hole2 = (LEWormHole) playMode.getObject(hole1.getTarget());
                if(hole1.getCooldown() == 0 && hole2.getCooldown() == 0) {
                    setTeleport(hole2);
                    hole1.setCooldown(hole1.getWormHoleCooldown());
                    hole2.setCooldown(hole2.getWormHoleCooldown());
                }
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
