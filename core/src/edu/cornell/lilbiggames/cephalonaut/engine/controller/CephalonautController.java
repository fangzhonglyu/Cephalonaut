package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import edu.cornell.lilbiggames.cephalonaut.engine.gameobject.GameObject;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.Obstacle;
import edu.cornell.lilbiggames.cephalonaut.util.PooledList;

public class CephalonautController {
    /** The box2d world **/
    private World world;

    /** Reference to the cephalonaut's model */
    private CephalonautModel cephalonaut;

    /** The joint of the grapple */
    private Joint grappleJoint,grappleJoint2,grappleJoint3;

    public CephalonautController(World world, CephalonautModel cephalonaut) {
        this.world = world;
        this.cephalonaut = cephalonaut;
    }

    public void update(boolean grappleButton, boolean directionalGrapple, PooledList<GameObject> anchoringPoints,
                          Vector2 crossHair, boolean thrusterApplied, float rotation) {
        updateGrapple(grappleButton, directionalGrapple, anchoringPoints, crossHair);

        cephalonaut.setInking(thrusterApplied);
        if(!cephalonaut.getGrapple().isLocked)
            cephalonaut.setRotationalDirection(rotation);
        cephalonaut.applyRotation();
        cephalonaut.applyForce();
    }

    private void updateGrapple(boolean grappleButton, boolean directionalGrapple, PooledList<GameObject> anchoringPoints,
                                  Vector2 crossHair) {
        GrappleModel grapple = cephalonaut.getGrapple();
        if (grappleButton) {
            grapple.setOut(!grapple.isOut());
            if (grapple.isOut()) {
                grapple.setPosition(cephalonaut.getPosition().cpy());
                // grapple travels 15 units/time in direction of mouse
//                if (directionalGrapple) {
                    // maybe set velocity is a parameter
                grapple.setLinearVelocity(crossHair.cpy().sub(grapple.getPosition().cpy()).nor().scl(15));
//                } else {
//                    grapple.closestAnchor(anchoringPoints);
//                }
                grapple.setActive(true);
            }
        }

        if(grapple.isOut()&&!grapple.isAnchored()){
            grapple.addTrace(cephalonaut.getPosition());
        }

        float distance = cephalonaut.getPosition().cpy().dst(grapple.getPosition());
        if (grapple.isAnchored()) {
            grapple.setBodyType(BodyDef.BodyType.StaticBody);
            if (distance > grapple.getExtensionLength() && !grapple.isGrappling()) {
                Vector2 swing = cephalonaut.getPosition().cpy().sub(grapple.getPosition()).rotate90(0);
                grapple.isLocked = true;
                float dot = swing.dot(cephalonaut.getLinearVelocity());
                if (dot != 0) {
                    // Experimental: Conserve velocity when rotating around point behind cephalonaut
                    float newAngle = swing.angleRad() + (dot < 0 ? (float) Math.PI : 0);
                    cephalonaut.setLinearVelocity(cephalonaut.getLinearVelocity().setAngleRad(newAngle));
                }


                DistanceJointDef anchor = new DistanceJointDef();
                DistanceJointDef anchor2 = new DistanceJointDef();
                DistanceJointDef anchor3 = new DistanceJointDef();
                anchor.bodyA = grapple.getBody();
                anchor.bodyB = cephalonaut.getBody();
                anchor.localAnchorB.set(0,-20);
                anchor.collideConnected = false;
                anchor.length = (float)Math.sqrt(distance*distance+400);
                anchor.dampingRatio=0.6f;
                anchor2.bodyA = grapple.getBody();
                anchor2.bodyB = cephalonaut.getBody();
                anchor2.dampingRatio=0.6f;
                anchor2.localAnchorB.set(0,20);
                anchor2.collideConnected = false;
                anchor2.length = (float)Math.sqrt(distance*distance+400);
                anchor3.bodyA = grapple.getBody();
                anchor3.bodyB = cephalonaut.getBody();
                anchor3.collideConnected = false;
                anchor3.length = distance;
                anchor.frequencyHz = 3f;
                anchor2.frequencyHz = 3f;
                grappleJoint = world.createJoint(anchor);
                grappleJoint2 = world.createJoint(anchor2);
                grappleJoint3 = world.createJoint(anchor3);
                grapple.setGrappling(true);
            }
        }
        grapple.setExtensionLength(distance);

        // "pull in" the grapple if requested, or if it has stretched its max length
        // and still hasn't anchored
        if ((grappleButton && !grapple.isOut()) ||
                (grapple.isOut() && grapple.isFullyExtended() && !grapple.isAnchored())) {
            if (grappleJoint != null) {
                world.destroyJoint(grappleJoint);
                grappleJoint = null;
                world.destroyJoint(grappleJoint2);
                grappleJoint2 = null;
                world.destroyJoint(grappleJoint3);
                grappleJoint3 = null;
            }
            grapple.reset();
            grapple.setPosition(cephalonaut.getPosition().cpy());
        }
    }
}
