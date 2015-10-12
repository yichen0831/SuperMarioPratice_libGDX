package com.ychstudio.utils;

import com.badlogic.gdx.physics.box2d.*;
import com.ychstudio.actors.Collider;
import com.ychstudio.actors.RigidBody;

/**
 * Created by yichen on 10/11/15.
 *
 * WorldContactListener
 */
public class WorldContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        if (fixtureA.isSensor() || fixtureB.isSensor()) {
            ((RigidBody) fixtureA.getUserData()).onTrigger(new Collider(fixtureB));
            ((RigidBody) fixtureB.getUserData()).onTrigger(new Collider(fixtureA));
        }
        else {
            ((RigidBody) fixtureA.getUserData()).onCollide(new Collider(fixtureB));
            ((RigidBody) fixtureB.getUserData()).onCollide(new Collider(fixtureA));
        }

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
