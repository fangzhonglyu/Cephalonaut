package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;

public class CephalonautController {
    private World world;
    private CephalonautModel cephalonaut;

    private Joint grappleJoint;

    public CephalonautController(World world, CephalonautModel cephalonaut) {
        this.world = world;
        this.cephalonaut = cephalonaut;
    }

    public void update(boolean grappleButton, Vector2 crossHair, boolean thrusterApplied, float rotation) {
        updateGrapple(grappleButton, crossHair);

        cephalonaut.setInking(thrusterApplied);
        cephalonaut.setRotationalDirection(rotation);
        cephalonaut.applyRotation();
        cephalonaut.applyForce();
    }

    private void updateGrapple(boolean grappleButton, Vector2 crossHair) {
        GrappleModel grapple = cephalonaut.getGrapple();

        if (grappleButton) {
            grapple.setGrappling(!grapple.isGrappling());
            // grapple is still in the process of extending
            if (grapple.isGrappling()) {
                grapple.setPosition(crossHair);
                Vector2 normal = cephalonaut.getPosition().cpy().sub(grapple.getPosition());
                grapple.setExtensionLength(normal.len());
                grapple.setActive(true);
                DistanceJointDef anchor = new DistanceJointDef();
                anchor.bodyA = grapple.getBody();
                anchor.bodyB = cephalonaut.getBody();
                anchor.collideConnected = false;
                grapple.setAnchor(anchor);
            }
            else {
                // grapple is no longer active but is anchored
                if (grapple.isAnchored()) {
                    world.destroyJoint(grappleJoint);
                    grapple.setAnchored(false);
                }
                grapple.setActive(false);
            }
        }

        if (grapple.isGrappling()) {
            float distance = cephalonaut.getPosition().dst(grapple.getPosition());;
            // cephalonaut is moving away from desired anchor point, start rotating
            if (distance > grapple.getExtensionLength() && !grapple.isAnchored()) {
                Vector2 swing = cephalonaut.getPosition().cpy().sub(grapple.getPosition()).rotate90(0);

                float dot = swing.dot(cephalonaut.getLinearVelocity());
                if (dot != 0) {
                    // Experimental: Conserve velocity when rotating around point behind cephalonaut
                    float newAngle = swing.angleRad() + (dot < 0 ? (float) Math.PI : 0);
                    cephalonaut.setLinearVelocity(cephalonaut.getLinearVelocity().setAngleRad(newAngle));

                    DistanceJointDef anchor = grapple.getAnchor();
                    anchor.length = distance;
                    grappleJoint = world.createJoint(anchor);
                    grapple.setAnchored(true);
                }
            }
            grapple.setExtensionLength(distance);
        }
    }
}
