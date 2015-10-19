package com.ychstudio.actors.effects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/14/15.
 *
 * BrickDebris
 */
public class BrickDebris extends Effect {

    private float radius = 0.2f;

    public BrickDebris(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        // choose a random texture
        setRegion(new TextureRegion(textureAtlas.findRegion("Debris"), 16 * MathUtils.random(1, 2), 0, 16, 16));
        setSize(16 / GameManager.PPM, 16 / GameManager.PPM);

        // apply random force
        Vector2 force = new Vector2(MathUtils.random() * 8.0f - 4.0f, MathUtils.random() * 6.0f + 12.0f);
        body.applyLinearImpulse(force, body.getWorldCenter(), true);

    }

    @Override
    protected void defBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.restitution = 0.1f;
        fixtureDef.filter.categoryBits = GameManager.NOTHING_BIT;
        fixtureDef.filter.maskBits = GameManager.NOTHING_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
    }

    @Override
    public void update(float delta) {
        if (destroyed) {
            return;
        }

        if (toBeDestroyed) {
            world.destroyBody(body);
            body = null;
            destroyed = true;
            return;
        }

        if (body.getPosition().y < -2.0f) {
            queueDestroy();
        }

        setPosition(body.getPosition().x - radius, body.getPosition().y - radius);
    }
}
