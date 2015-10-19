package com.ychstudio.actors.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.Collider;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/19/15.
 *
 * Star
 */
public class Star extends Item {

    private Animation animation;
    private float stateTime;

    private boolean movingRight;

    public Star(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        TextureAtlas textureAtlas = playScreen.getTextureAtlas();
        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        for (int i = 0; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Star"), 16 * i, 0, 16, 16));
        }
        animation = new Animation(0.1f, keyFrames);

        stateTime = 0;

        movingRight = true;

        setRegion(animation.getKeyFrame(0, true));
        setBounds(getX(), getY(), 16 / GameManager.PPM, 16 / GameManager.PPM);

        name = "star";
    }

    @Override
    public void use() {
        GameManager.instance.addScore(1000);
        playScreen.getScoreIndicator().addScoreItem(getX(), getY(), 1000);
        queueDestroy();
    }

    @Override
    protected void defBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());

        body = world.createBody(bodyDef);

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(8 / GameManager.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.filter.categoryBits = GameManager.ITEM_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.MARIO_BIT;
        fixtureDef.restitution = 0.1f;

        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(new Vector2(-8.0f, -8.0f).scl(1 / GameManager.PPM), new Vector2(8.0f, -8.0f).scl(1 / GameManager.PPM));
        fixtureDef.shape  = edgeShape;
        body.createFixture(fixtureDef).setUserData(this);

        circleShape.dispose();
        edgeShape.dispose();
    }

    public void checkMovingDirection() {
        Vector2 p1 = new Vector2();
        Vector2 p2 = new Vector2();

        RayCastCallback rayCastCallbackCheckRight = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getUserData() == this || fixture.getUserData() instanceof Item) {
                    return 1;
                }
                if (fraction < 1.0f && (fixture.getFilterData().categoryBits == GameManager.GROUND_BIT)) {
                    movingRight = false;
                }
                return 0;
            }
        };

        RayCastCallback rayCastCallbackCheckLeft = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getUserData() == this || fixture.getUserData() instanceof Item) {
                    return 1;
                }
                if (fraction < 1.0f && (fixture.getFilterData().categoryBits == GameManager.GROUND_BIT)) {
                    movingRight = true;
                }
                return 0;
            }
        };

        if (movingRight) {
            for (int i = 0; i < 3; i++) {
                p1.set(body.getPosition().x + 8.0f / GameManager.PPM, body.getPosition().y + (1 - i) * 0.3f);
                p2.set(body.getPosition().x + 8.0f / GameManager.PPM + 0.1f, body.getPosition().y + (1 - i) * 0.3f);
                world.rayCast(rayCastCallbackCheckRight, p1, p2);
            }

        }
        else {
            for (int i = 0; i < 3; i++) {
                p1.set(body.getPosition().x + 8.0f / GameManager.PPM, body.getPosition().y + (1 - i) * 0.3f);
                p2.set(body.getPosition().x + 8.0f / GameManager.PPM - 0.1f, body.getPosition().y + (1 - i) * 0.3f);
                world.rayCast(rayCastCallbackCheckLeft, p1, p2);
            }
        }
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

        checkMovingDirection();

        if (movingRight) {
            body.applyLinearImpulse(new Vector2(body.getMass() * (6.0f - body.getLinearVelocity().x), 0), body.getWorldCenter(), true);
        }
        else {
            body.applyLinearImpulse(new Vector2(body.getMass() * (-6.0f - body.getLinearVelocity().x), 0), body.getWorldCenter(), true);
        }

        if (body.getPosition().y < -2.0f) {
            queueDestroy();
        }

        stateTime += delta;
        setRegion(animation.getKeyFrame(stateTime, true));
        setPosition(body.getPosition().x - 8 / GameManager.PPM, body.getPosition().y - 8 / GameManager.PPM);
    }

    @Override
    public void onCollide(Collider other) {
        if (other.getFilter().categoryBits == GameManager.GROUND_BIT) {
            Vector2 velocity = body.getLinearVelocity();
            body.setLinearVelocity(velocity.x, 10.0f);
        }
    }
}
