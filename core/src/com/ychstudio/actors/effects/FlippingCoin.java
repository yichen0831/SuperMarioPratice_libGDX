package com.ychstudio.actors.effects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/14/15.
 *
 * FlippingCoin
 */
public class FlippingCoin extends Effect {

    private Animation flipping;
    private float stateTime;

    public FlippingCoin(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        for (int i = 0; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("FlippingCoin"), 16 * i, 0, 16, 16));
        }
        flipping = new Animation(0.05f, keyFrames);

        stateTime = 0;

        setSize(16 / GameManager.PPM, 16 / GameManager.PPM);

        body.applyLinearImpulse(new Vector2(0, 12.0f), body.getWorldCenter(), true);
    }

    @Override
    protected void defBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(0.1f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameManager.NOTHING_BIT;
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
            setBounds(0, 0, 0, 0);
            destroyed = true;
            return;
        }

        stateTime += delta;
        if (stateTime > 0.5f) {
            queueDestroy();
        }

        setRegion(flipping.getKeyFrame(stateTime, true));
        setPosition(body.getPosition().x - 8 / GameManager.PPM, body.getPosition().y - 8 / GameManager.PPM);
    }
}
