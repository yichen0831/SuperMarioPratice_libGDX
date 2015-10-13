package com.ychstudio.actors.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.Mario;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/13/15.
 *
 * Goomba
 */
public class Goomba extends Enemy {

    private Animation walking;
    private float stateTime;

    private boolean movingRight;
    private float speed;

    public Goomba(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();

        for (int i = 0; i < 2; i++) {
            keyFrames.add(new TextureRegion(playScreen.getTextureAtlas().findRegion("Goomba"), 16 * i, 0, 16, 16));
        }

        setRegion(keyFrames.get(0));
        setBounds(getX() - 8.0f / GameManager.PPM, getY() - 8.0f / GameManager.PPM, 16.0f / GameManager.PPM, 16.0f / GameManager.PPM);

        walking = new Animation(0.4f, keyFrames);

        movingRight = false;
        speed = 2.0f;
        stateTime = 0;

    }

    public void checkMovingDirection() {

        Vector2 p1;
        Vector2 p2;

        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getUserData() == this) {
                    return 1;
                }
                if (fraction < 1.0f && fixture.getUserData().getClass() != Mario.class) {
                    movingRight = ! movingRight;
                }
                return 0;
            }
        };

        if (movingRight) {
            p1 = new Vector2(body.getPosition().x + 8.0f / GameManager.PPM, body.getPosition().y);
            p2 = new Vector2(p1).add(0.05f, 0);

            world.rayCast(rayCastCallback, p1, p2);
        }
        else {
            p1 = new Vector2(body.getPosition().x - 8.0f / GameManager.PPM, body.getPosition().y);
            p2 = new Vector2(p1).add(-0.05f, 0);

            world.rayCast(rayCastCallback, p1, p2);
        }
    }

    @Override
    public void update(float delta) {
        if (playScreen.getMarioPosition().x + GameManager.V_WIDTH / 2 > body.getPosition().x )
            active = true;

        if (!active) {
            return;
        }

        if (destroyed) {
            return;
        }

        stateTime += delta;

        if (toBeDestroyed) {

            setRegion(new TextureRegion(playScreen.getTextureAtlas().findRegion("Goomba"), 16 * 2, 0, 16, 16));

            if (stateTime > 1.0f) {
                world.destroyBody(body);
                setBounds(0, 0, 0, 0);
                destroyed = true;
            }
        }
        else {
            setRegion(walking.getKeyFrame(stateTime, true));
            checkMovingDirection();

            float velocityY = body.getLinearVelocity().y;
            if (movingRight) {
                body.setLinearVelocity(new Vector2(speed, velocityY));
            }
            else {
                body.setLinearVelocity(new Vector2(-speed, velocityY));
            }
        }

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
    }

    @Override
    public void getDamage(int damage) {

        hp -= damage;

        if (hp <= 0) {
            Filter filter = new Filter();
            filter.maskBits = GameManager.GROUND_BIT;
            for (Fixture fixture : body.getFixtureList()) {
                fixture.setFilterData(filter);
            }
            stateTime = 0;

            GameManager.instance.getAssetManager().get("audio/sfx/stomp.wav", Sound.class).play();
            GameManager.instance.addScore(200);
            queueDestroy();
        }
    }

    @Override
    protected void defBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();

        // feet
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(
                new Vector2(-7.0f, -7.0f).scl(1 / GameManager.PPM),
                new Vector2(7.0f, -7.0f).scl(1 / GameManager.PPM)
                );

        fixtureDef.shape = edgeShape;
        fixtureDef.filter.categoryBits = GameManager.ENEMY_LETHAL_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.MARIO_BIT;
        body.createFixture(fixtureDef).setUserData(this);


        // lethal
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(2.0f / GameManager.PPM);
        circleShape.setPosition(new Vector2(-6, 0).scl(1 / GameManager.PPM));

        fixtureDef.shape = circleShape;
        fixtureDef.filter.categoryBits = GameManager.ENEMY_LETHAL_BIT;
        fixtureDef.filter.maskBits = GameManager.MARIO_BIT;
        body.createFixture(fixtureDef).setUserData(this);

        circleShape.setPosition(new Vector2(6, 0).scl(1 / GameManager.PPM));
        body.createFixture(fixtureDef).setUserData(this);

        // weakness
        Vector2[] vertices = {
                new Vector2(-6.8f, 7.0f).scl(1 / GameManager.PPM),
                new Vector2(6.8f, 7.0f).scl(1 / GameManager.PPM),
                new Vector2(-2.0f, -2.0f).scl(1 / GameManager.PPM),
                new Vector2(2.0f, -2.0f).scl(1 / GameManager.PPM),
        };
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.set(vertices);

        fixtureDef.shape = polygonShape;
        fixtureDef.restitution = 0.6f;
        fixtureDef.filter.categoryBits = GameManager.ENEMY_WEAKNESS_BIT;
        fixtureDef.filter.maskBits = GameManager.MARIO_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        circleShape.dispose();
        edgeShape.dispose();
        polygonShape.dispose();

    }

}
