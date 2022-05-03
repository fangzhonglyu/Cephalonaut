package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.*;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.elements.*;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.util.ScreenListener;

public class LevelController implements ContactListener {
    /*Exit code for completed level */
    public static int COMPLETE_LEVEL = 50;

    /** Reference to player **/
    private final CephalonautModel cephalonaut;

    /** Reference to the play mode **/
    private final PlayMode playMode;

    /** Listener that will update the screen when we are done */
    private ScreenListener listener;

    public LevelController(ScreenListener listener, CephalonautModel cephalonaut, PlayMode playMode) {
        this.cephalonaut = cephalonaut;
        this.playMode = playMode;
        this.listener = listener;
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
        } else if (object instanceof LEGlassBarrier) {
            willHit((LEGlassBarrier) object);
        }


        if (object instanceof LevelElement) {
            LevelElement levelElement = ((LevelElement) object);
            if (levelElement.getElement() == LevelElement.Element.FINISH && levelElement.getInContact() && cephalonaut.isAlive()) {
                finishLevel();
            }
        }

        // TODO: Consider:
        // Do we need to string in a CephalonautController though the update function? I think that's more for player
        // controlled movement. I would be fine having the LevelController do all the teleporting without involving it.
        // Maybe we should be able to 'removeGrapple' from the cephalonaut model itself, as that is the model that is
        // storing all the cephalonaut data.
        if (cephalonaut.getShouldTeleport()) {
            teleport(cephalonautController);
        }
        if (!cephalonaut.isAlive()) {
            GrappleModel grapple = cephalonaut.getGrapple();
            if (grapple.isOut()) {
                cephalonautController.removeGrapple(grapple);
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
        if (blackHolePos.dst(cephalonautPos) < blackHole.getBlackHoleRange() * 1.5f){
            SoundController.setBlackHoleSound(true,1f-Math.min(1f,blackHolePos.dst(cephalonautPos)/blackHole.getBlackHoleRange()));
        }
        else{
            SoundController.setBlackHoleSound(false,0f );
        }


        if (blackHolePos.dst(cephalonautPos) < blackHole.getBlackHoleRange() /*||
                blackHolePos.dst(cephalonaut.getGrapple().getPosition()) < blackHole.getBlackHoleRange()*/) {
            Vector2 delta = blackHolePos.sub(cephalonautPos).clamp(1f, 50f);
            float strength = 10f * blackHole.getBlackHoleAttractFactor() * cephalonaut.getMass() / delta.len2();
            cephalonaut.addForce(delta.setLength(strength));
        }
    }

    public void teleport(CephalonautController cephalonautController) {
        GrappleModel grapple = cephalonaut.getGrapple();
        if (grapple.isOut()) {
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
        if (!obj.getInContact()) return;

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
        if (listener != null) {
            listener.exitScreen(playMode, COMPLETE_LEVEL);
        }
    }

    private void openDialogue(int part) {
        playMode.nextDialogue(part);
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

        // TODO: These next few lines are kinda off-putting to me, can we refactor this?
        boolean grappleContact = contactObject == null;
        contactObject = grappleContact ? getOtherBody(contact, grapple) : contactObject;

        if (contactObject != null && (!grappleContact || contactObject instanceof LETrigger)) {
            if (contactObject instanceof LevelElement) {

                ((LevelElement) contactObject).setInContact(true);
                if (((LevelElement) contactObject).getElement().equals(LevelElement.Element.SPIKE)||((LevelElement) contactObject).getElement().equals(LevelElement.Element.ESPIKE)||((LevelElement) contactObject).getElement().equals(LevelElement.Element.SPIKEBALL)) {
                    cephalonaut.setAlive(false);
                }
                if (((LevelElement) contactObject).getElement().equals(LevelElement.Element.REFILL)) {
                    cephalonaut.refillInk();
                }
            }

            if (contactObject instanceof  LEDialogueTrigger) {
                LEDialogueTrigger dialogueTrigger = (LEDialogueTrigger) contactObject;
                if(dialogueTrigger.isActive()) {
                    openDialogue(dialogueTrigger.getTarget());
                    dialogueTrigger.deactivate();
                }
            }

            if (contactObject.getRestitution()>1) {
                SoundController.playSound(1,1);
            }

            if (contactObject instanceof LEBlackHole) {
                cephalonaut.setAlive(false);
            }

            if (contactObject instanceof LETrigger) {
                LETrigger trigger = (LETrigger) contactObject;
                LETriggerable target = (LETriggerable) playMode.getObject(trigger.getTarget());
                target.setActivated(trigger.isActivated());
            }

            if (contactObject instanceof LEWormHole) {
                LEWormHole hole1 = (LEWormHole) contactObject;
                if (hole1.getCooldown() == 0 && hole1.getTarget() != -1) {
                    LEWormHole hole2 = (LEWormHole) playMode.getObject(hole1.getTarget());
                    setTeleport(hole2);
                    hole1.setCooldown(hole1.getWormHoleCooldown());
                    hole2.setCooldown(hole2.getWormHoleCooldown());
                }
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

        // TODO: Same comment as above in beginContact
        boolean grappleContact = contactObject == null;
        contactObject = grappleContact ? getOtherBody(contact, grapple) : contactObject;

        if (contactObject != null && (!grappleContact || contactObject instanceof LETrigger)) {
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
