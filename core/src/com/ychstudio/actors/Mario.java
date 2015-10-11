package com.ychstudio.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * Mario
 */
public class Mario extends RigidBody {

    public Mario(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
    }

    @Override
    protected void defBody() {
        float radius = 6.0f / GameManager.PPM;

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());

        body = world.createBody(bodyDef);

        // Mario's body
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameManager.MARIO_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        // Mario's feet
        EdgeShape edgeShape = new EdgeShape();
        fixtureDef.shape = edgeShape;
        edgeShape.set(new Vector2(-radius, -radius), new Vector2(radius, -radius));
        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
        edgeShape.dispose();
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            body.applyLinearImpulse(new Vector2(0.0f, 20.0f), body.getLocalCenter(), true);
        }

        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && body.getLinearVelocity().x > -10.0f) {
            body.applyForceToCenter(new Vector2(-24.0f, 0.0f), true);
        }

        if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && body.getLinearVelocity().x < 10.0f) {
            body.applyForceToCenter(new Vector2(24.0f, 0.0f), true);
        }


    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void update(float delta) {
        handleInput(delta);
    }

    @Override
    public void onCollide(Fixture other) {

//        System.out.println("I collided with " + other.getFilterData().categoryBits);
    }
}
