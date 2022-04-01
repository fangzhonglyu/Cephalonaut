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

    public void update(GameObject object) {
        if (object instanceof LEBoostPad) {
            boost((LEBoostPad) object);
        } else if (object instanceof LEBlackHole) {
            attract((LEBlackHole) object);
        } else if (object instanceof LETriggerable) {
            ((LETriggerable) object).checkPos();
        }
        else if (object instanceof LEGlassBarrier) {
            willHit(((LEGlassBarrier) object));
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

        if (blackHolePos.dst(cephalonautPos) < blackHole.getBlackHoleRange() /*||
                blackHolePos.dst(cephalonaut.getGrapple().getPosition()) < blackHole.getBlackHoleRange()*/) {
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

    public void hit(LEGlassBarrier obj) {
        float hitSpeed = (float) Math.sqrt(Math.pow(cephalonaut.getVX(), 2) + Math.pow(cephalonaut.getVY(), 2));
        obj.hit(hitSpeed);
    }

    public void willHit(LEGlassBarrier obj) {
        Vector2 glassBarrierPos = obj.getBody().getWorldCenter();
        Vector2 cephalonautPos = cephalonaut.getBody().getWorldCenter();
        float hitSpeed = (float) Math.sqrt(Math.pow(cephalonaut.getVX(), 2) + Math.pow(cephalonaut.getVY(), 2));
        obj.willBreak(hitSpeed, glassBarrierPos.dst(cephalonautPos));
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
        GrappleModel grapple = cephalonaut.getGrapple();
        GameObject contactObject = getOtherBody(contact, cephalonaut);
        contactObject = contactObject == null ? getOtherBody(contact, grapple) : contactObject;

        if (contactObject != null) {

            if (contactObject instanceof LevelElement) {
                ((LevelElement) contactObject).setInContact(true);
                if(((LevelElement) contactObject).getElement().equals(LevelElement.Element.SPIKE)) {
                    System.out.println("SPIKE");
                }
            }

            if (contactObject instanceof LETrigger) {
                LETrigger trigger = (LETrigger) contactObject;
                LETriggerable target = (LETriggerable) playMode.getObject(trigger.getTarget());
                target.setActivated(trigger.isActivated());
            }

            if (contactObject instanceof LEGlassBarrier) {
                LEGlassBarrier glassBarrier = (LEGlassBarrier) contactObject;
                hit(glassBarrier);
            }

        }

        if (!grapple.isAnchored()) {
            contactObject = getOtherBody(contact, grapple);
            if (contactObject != null && contactObject.canGrapple()) {
                grapple.setAnchored(true);
                SoundController.playSound(0,1);
                grapple.setExtensionLength(1 + cephalonaut.getPosition().dst(contactObject.getPosition()));
                grapple.setAnchorLocation(contactObject.getName());
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        GrappleModel grapple = cephalonaut.getGrapple();
        GameObject contactObject = getOtherBody(contact, cephalonaut);
        contactObject = contactObject == null ? getOtherBody(contact, grapple) : contactObject;

        if (contactObject != null) {
            if (contactObject instanceof LevelElement) {
                ((LevelElement) contactObject).setInContact(false);
            }
        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        GameObject contactObject = getOtherBody(contact, cephalonaut);
        if (contactObject != null) {
            if (contactObject instanceof LEGlassBarrier) {
                LEGlassBarrier glassBarrier = (LEGlassBarrier) contactObject;
                if(glassBarrier.isBroken()) {
                    contact.setEnabled(false);
                    cephalonaut.setVX(cephalonaut.getVX() * .7f);
                }
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

}
