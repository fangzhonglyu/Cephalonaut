package edu.cornell.lilbiggames.cephalonaut.engine.controller;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import edu.cornell.lilbiggames.cephalonaut.engine.model.CephalonautModel;
import edu.cornell.lilbiggames.cephalonaut.engine.model.GrappleModel;
import edu.cornell.lilbiggames.cephalonaut.engine.obstacle.Obstacle;
import edu.cornell.lilbiggames.cephalonaut.util.PooledList;

public class CephalonautController {
    private World world;
    private CephalonautModel cephalonaut;

    private Joint grappleJoint;

    public CephalonautController(World world, CephalonautModel cephalonaut) {
        this.world = world;
        this.cephalonaut = cephalonaut;
    }

    public void update(boolean grappleButton, boolean directionalGrapple, PooledList<Obstacle> anchoringPoints,
                          Vector2 crossHair, boolean thrusterApplied, float rotation) {
        updateGrapple(grappleButton, directionalGrapple, anchoringPoints, crossHair);

        cephalonaut.setInking(thrusterApplied);
        cephalonaut.setRotationalDirection(rotation);
        cephalonaut.applyRotation();
        cephalonaut.applyForce();
    }

    private void updateGrapple(boolean grappleButton, boolean directionalGrapple, PooledList<Obstacle> anchoringPoints,
                                  Vector2 crossHair) {
        GrappleModel grapple = cephalonaut.getGrapple();

        if (grappleButton) {
            grapple.setOut(!grapple.isOut());
            if (grapple.isOut()) {
                grapple.setPosition(cephalonaut.getPosition().cpy());
                // grapple travels 15 units/time in direction of mouse
                if (directionalGrapple) {
                    grapple.setLinearVelocity(crossHair.cpy().sub(grapple.getPosition().cpy()).nor().scl(15));
                } else {
                    grapple.closestAnchor(anchoringPoints);
                }
                grapple.setActive(true);
            }
        }

        float distance = cephalonaut.getPosition().cpy().dst(grapple.getPosition());
        if (grapple.isAnchored()) {
            grapple.setBodyType(BodyDef.BodyType.StaticBody);
            if (distance > grapple.getExtensionLength() && !grapple.isGrappling()) {
                Vector2 swing = cephalonaut.getPosition().cpy().sub(grapple.getPosition()).rotate90(0);

                float dot = swing.dot(cephalonaut.getLinearVelocity());
                if (dot != 0) {
                    // Experimental: Conserve velocity when rotating around point behind cephalonaut
                    float newAngle = swing.angleRad() + (dot < 0 ? (float) Math.PI : 0);
                    cephalonaut.setLinearVelocity(cephalonaut.getLinearVelocity().setAngleRad(newAngle));
                }

                DistanceJointDef anchor = new DistanceJointDef();
                anchor.bodyA = grapple.getBody();
                anchor.bodyB = cephalonaut.getBody();
                anchor.collideConnected = false;
                anchor.length = distance;
                grappleJoint = world.createJoint(anchor);
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
            }
            grapple.reset();
            grapple.setPosition(cephalonaut.getPosition().cpy());
        }
    }
}
