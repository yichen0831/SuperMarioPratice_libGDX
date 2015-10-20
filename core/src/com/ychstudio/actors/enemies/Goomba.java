package com.ychstudio.actors.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.Mario;
import com.ychstudio.actors.items.Item;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/13/15.
 *
 * Goomba
 */
public class Goomba extends Enemy {

    public enum State {
        WALKING,
        STOMPED,
        DYING,
    }

    private Animation walking;
    private float stateTime;

    private boolean movingRight;
    private float speed;

    private State currentState;

    private boolean walk;
    private boolean stomped;
    private boolean die;

    public Goomba(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();

        for (int i = 0; i < 2; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Goomba"), 16 * i, 0, 16, 16));
        }
        walking = new Animation(0.2f, keyFrames);

        setRegion(keyFrames.get(0));
        setBounds(getX() - 8.0f / GameManager.PPM, getY() - 8.0f / GameManager.PPM, 16.0f / GameManager.PPM, 16.0f / GameManager.PPM);

        movingRight = false;
        speed = 3.2f;
        stateTime = 0;

        walk = true;
        stomped = false;
        die = false;
    }

    public void checkMovingDirection() {
        Vector2 p1;
        Vector2 p2;

        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getUserData() == this || fixture.getUserData() instanceof Item) {
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
            p2 = new Vector2(p1).add(0.1f, 0);

            world.rayCast(rayCastCallback, p1, p2);
        }
        else {
            p1 = new Vector2(body.getPosition().x - 8.0f / GameManager.PPM, body.getPosition().y);
            p2 = new Vector2(p1).add(-0.1f, 0);

            world.rayCast(rayCastCallback, p1, p2);
        }
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

        if (playScreen.getMarioPosition().x + GameManager.V_WIDTH / 2 + 4 > body.getPosition().x )
            active = true;

        if (!active) {
            return;
        }

        State previousState = currentState;

        if (stomped) {
            stomped = false;
            currentState = State.STOMPED;
            becomeStomped();

            GameManager.instance.getAssetManager().get("audio/sfx/stomp.wav", Sound.class).play();
            GameManager.instance.addScore(100);
            playScreen.getScoreIndicator().addScoreItem(getX(), getY(), 100);
        }
        else if (die) {
            die = false;
            currentState = State.DYING;

            body.applyLinearImpulse(new Vector2(0.0f, 7.2f), body.getWorldCenter(), true);
            becomeDead();

            float cameraX = playScreen.getCamera().position.x;
            float distanceRatio = (body.getPosition().x - cameraX) / GameManager.V_WIDTH * 2;
            float pan = MathUtils.clamp(distanceRatio, -1, 1);
            float volume = MathUtils.clamp(2.0f - (float)Math.sqrt(Math.abs(distanceRatio)), 0, 1);
            GameManager.instance.getAssetManager().get("audio/sfx/stomp.wav", Sound.class).play(volume, 1.0f, pan);

            GameManager.instance.addScore(100);
            playScreen.getScoreIndicator().addScoreItem(getX(), getY(), 100);
        }
        else if (walk) {
            walk = false;
            currentState = State.WALKING;
        }

        if (previousState != currentState) {
            stateTime = 0;
        }

        switch (currentState) {
            case STOMPED:
                setRegion(new TextureRegion(textureAtlas.findRegion("Goomba"), 16 * 2, 0, 16, 16));
                if (stateTime > 1.0f) {
                    queueDestroy();
                }
                break;

            case DYING:
                setRegion(walking.getKeyFrame(stateTime, true));
                setFlip(false, true);
                if (stateTime > 2.0f) {
                    queueDestroy();
                }
                break;

            case WALKING:
            default:
                setRegion(walking.getKeyFrame(stateTime, true));
                checkMovingDirection();

                float velocityY = body.getLinearVelocity().y;
                if (movingRight) {
                    body.setLinearVelocity(new Vector2(speed, velocityY));
                }
                else {
                    body.setLinearVelocity(new Vector2(-speed, velocityY));
                }
                break;
        }

        stateTime += delta;

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
    }


    private void becomeStomped() {
        Filter filter = new Filter();
        filter.maskBits = GameManager.GROUND_BIT;
        for (Fixture fixture : body.getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }

    private void becomeDead() {
        Filter filter;
        for (Fixture fixture : body.getFixtureList()) {
            filter = fixture.getFilterData();
            filter.categoryBits = GameManager.NOTHING_BIT;
            filter.maskBits = GameManager.NOTHING_BIT;
            fixture.setFilterData(filter);
        }
    }

    @Override
    public void getDamage(int damage) {
        if (toBeDestroyed || currentState == State.STOMPED || currentState == State.DYING || !active) {
            return;
        }

        // hit by Mario on head
        if (damage == 1) {
            stomped = true;
        }
        else {
            die = true;
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
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.MARIO_BIT | GameManager.WEAPON_BIT;
        body.createFixture(fixtureDef).setUserData(this);


        // lethal
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(2.0f / GameManager.PPM);
        circleShape.setPosition(new Vector2(-6, 0).scl(1 / GameManager.PPM));

        fixtureDef.shape = circleShape;
        fixtureDef.filter.categoryBits = GameManager.ENEMY_LETHAL_BIT;
        fixtureDef.filter.maskBits = GameManager.MARIO_BIT | GameManager.WEAPON_BIT;
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
        fixtureDef.filter.categoryBits = GameManager.ENEMY_WEAKNESS_BIT;
        fixtureDef.filter.maskBits = GameManager.MARIO_BIT | GameManager.WEAPON_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        circleShape.dispose();
        edgeShape.dispose();
        polygonShape.dispose();

    }

}
