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
    private Joint grappleJoint1, grappleJoint2, grappleJoint3;

    public CephalonautController(World world, CephalonautModel cephalonaut) {
        this.world = world;
        this.cephalonaut = cephalonaut;
    }

    public void update(boolean grappleButton, boolean refill, Vector2 crossHair, boolean thrusterApplied,
                       float rotation) {
        if (refill)
            cephalonaut.setInk(1);

        updateGrapple(grappleButton, crossHair);
        cephalonaut.setInking(thrusterApplied);

        if (cephalonaut.getGrapple().isLocked() == 0)
            cephalonaut.setRotationalDirection(rotation);

        cephalonaut.applyRotation();
        cephalonaut.applyForce();
    }

    private void updateGrapple(boolean grappleButton, Vector2 crossHair) {
        GrappleModel grapple = cephalonaut.getGrapple();
        if (grappleButton) {
            grapple.setOut(!grapple.isOut());
            if (grapple.isOut()) {
                grapple.setPosition(cephalonaut.getPosition().cpy());
                grapple.setLinearVelocity(crossHair.cpy().sub(grapple.getPosition().cpy()).nor().scl(15));
                grapple.setActive(true);
            }
        }

        if (grapple.isOut() && !grapple.isAnchored())
            grapple.addTrace(cephalonaut.getPosition());

        if (grapple.isLocked() > 0 && grapple.isLocked() < 8)
            grapple.setLocked(grapple.isLocked() + 0.4f);;

        float distance = cephalonaut.getPosition().cpy().dst(grapple.getPosition());
        if (grapple.isAnchored()) {
            grapple.setBodyType(BodyDef.BodyType.StaticBody);
            if (distance > grapple.getExtensionLength() && !grapple.isGrappling()) {
                Vector2 swing = cephalonaut.getPosition().cpy().sub(grapple.getPosition()).rotate90(0);
                if (grapple.isLocked() < 8)
                    grapple.setLocked(1);
                float dot = swing.dot(cephalonaut.getLinearVelocity());
                if (dot != 0) {
                    // Experimental: Conserve velocity when rotating around point behind cephalonaut
                    float newAngle = swing.angleRad() + (dot < 0 ? (float) Math.PI : 0);
                    cephalonaut.setLinearVelocity(cephalonaut.getLinearVelocity().setAngleRad(newAngle));
                }


                DistanceJointDef anchor1 = new DistanceJointDef();
                DistanceJointDef anchor2 = new DistanceJointDef();
                DistanceJointDef anchor3 = new DistanceJointDef();
                anchor1.bodyA = grapple.getBody();
                anchor1.bodyB = cephalonaut.getBody();
                anchor1.localAnchorB.set(0,-20);
                anchor1.collideConnected = false;
                anchor1.length = (float)Math.sqrt(distance*distance+400);
                anchor1.dampingRatio=0.6f;
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
                anchor1.frequencyHz = 3f;
                anchor2.frequencyHz = 3f;
                anchor3.dampingRatio=0.8f;
                anchor3.frequencyHz=0.6f;
                grappleJoint1 = world.createJoint(anchor1);
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
            if (grappleJoint1 != null) {
                world.destroyJoint(grappleJoint1);
                grappleJoint1 = null;
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
