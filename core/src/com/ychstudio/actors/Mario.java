package com.ychstudio.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.enemies.Enemy;
import com.ychstudio.actors.items.Item;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * Mario
 */
public class Mario extends RigidBody {
    public enum State {
        STANDING,
        RUNNING,
        JUMPING,
        CROUCHING,
        FALLING,
        GROWING,
        SHRINKING,
        BRAKING,
        DYING,
    }

    private final float radius = 6.8f / GameManager.PPM;

    private final float normalForce = 20.0f;
    private final float normalSpeedMax = 6.0f;
    private final float fastForce = 36.0f;
    private final float fastSpeedMax = 12.0f;

    private float keyPressedTime;

    private State currentState;

    private float stateTime;

    private TextureRegion standingSmall;
    private TextureRegion jumpingSmall;
    private Animation runningSmall;
    private TextureRegion brakingSmall;

    private TextureRegion dying;
    private TextureRegion standingBig;
    private TextureRegion jumpingBig;
    private Animation runningBig;
    private TextureRegion brakingBig;

    private Animation growing;
    private Animation shrinking;
    private TextureRegion crouching;

    private boolean facingRight;

    private boolean isGrownUp;
    private boolean isDead;

    private boolean grounded;
    private boolean jump;
    private boolean die;
    private boolean growUp;
    private boolean shrink;
    private boolean crouch;
    private boolean brake;

    private AssetManager assetManager;

    public Mario(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
        TextureAtlas textureAtlas = playScreen.getTextureAtlas();

        standingSmall = new TextureRegion(textureAtlas.findRegion("Mario_small"), 0, 0, 16, 16);
        standingBig = new TextureRegion(textureAtlas.findRegion("Mario_big"), 0, 0, 16, 32);

        jumpingSmall = new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * 5, 0, 16, 16);
        jumpingBig = new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 5, 0, 16, 32);

        brakingSmall = new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * 4, 0, 16, 16);
        brakingBig = new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 4, 0, 16, 32);

        // flip braking image for correct displaying
        brakingSmall.flip(true, false);
        brakingBig.flip(true, false);

        // running animation
        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * i, 0, 16, 16));
        }
        runningSmall = new Animation(0.1f, keyFrames);

        keyFrames.clear();
        for (int i = 1; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * i, 0, 16, 32));
        }
        runningBig = new Animation(0.1f, keyFrames);

        keyFrames.clear();
        // growing up animation
        for (int i = 0; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 15, 0, 16, 32));
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 0, 0, 16, 32));
        }
        growing = new Animation(0.1f, keyFrames);

        keyFrames.clear();
        // shrinking animation
        for (int i = 0; i < 3; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 0, 0, 16, 32));
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 15, 0, 16, 32));
        }
        keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_small"), 0, 0, 16, 32));
        shrinking = new Animation(0.1f, keyFrames);

        dying = new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * 6, 0, 16, 16);

        crouching = new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 6, 0, 16, 32);

        setRegion(standingSmall);
        setBounds(getX(), getY(), 16 / GameManager.PPM, 16 / GameManager.PPM);

        currentState = State.STANDING;
        stateTime = 0;

        facingRight = true;
        isGrownUp = false;
        jump = false;
        die = false;
        shrink = false;
        growUp = false;
        crouch = false;

        keyPressedTime = 99.0f;

        assetManager = GameManager.instance.getAssetManager();
    }


    @Override
    protected void defBody() {

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
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.ENEMY_WEAKNESS_BIT | GameManager.ENEMY_LETHAL_BIT | GameManager.ITEM_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        // Mario's feet
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(new Vector2(-radius, -radius), new Vector2(radius, -radius));
        fixtureDef.shape = edgeShape;
        body.createFixture(fixtureDef).setUserData(this);

        // Mario's head
        edgeShape.set(new Vector2(-radius / 6, radius), new Vector2(radius / 6, radius));
        fixtureDef.shape = edgeShape;
        fixtureDef.filter.categoryBits = GameManager.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
        edgeShape.dispose();
    }

    private void defSmallMario() {
        Vector2 position = body.getPosition();
        Vector2 velocity = body.getLinearVelocity();

        world.destroyBody(body);

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);

        body = world.createBody(bodyDef);
        body.setLinearVelocity(velocity);

        // Mario's body
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameManager.MARIO_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.ENEMY_WEAKNESS_BIT | GameManager.ENEMY_LETHAL_BIT | GameManager.ITEM_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        // Mario's feet
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(new Vector2(-radius, -radius), new Vector2(radius, -radius));
        fixtureDef.shape = edgeShape;
        body.createFixture(fixtureDef).setUserData(this);

        // Mario's head
        edgeShape.set(new Vector2(-radius / 6, radius), new Vector2(radius / 6, radius));
        fixtureDef.shape = edgeShape;
        fixtureDef.filter.categoryBits = GameManager.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
        edgeShape.dispose();
    }

    private void defBigMario() {
        Vector2 position = new Vector2(body.getPosition());
        Vector2 velocity = body.getLinearVelocity();

        world.destroyBody(body);

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position.x, position.y);

        body = world.createBody(bodyDef);
        body.setLinearVelocity(velocity);

        // Mario's body
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        shape.setPosition(new Vector2(0, 0));

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameManager.MARIO_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.ENEMY_WEAKNESS_BIT | GameManager.ENEMY_LETHAL_BIT | GameManager.ITEM_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        shape.setPosition(new Vector2(0, radius * 2));
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);

        // Mario's feet
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(new Vector2(-radius, -radius), new Vector2(radius, -radius));
        fixtureDef.shape = edgeShape;
        body.createFixture(fixtureDef).setUserData(this);

        // Mario's head
        edgeShape.set(new Vector2(-radius / 6, radius * 3), new Vector2(radius / 6, radius * 3));
        fixtureDef.shape = edgeShape;
        fixtureDef.filter.categoryBits = GameManager.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
        edgeShape.dispose();
    }

    private void handleInput() {
        float maxSpeed = normalSpeedMax;
        float force = normalForce;

        // Accelerate
        if (Gdx.input.isKeyPressed(Input.Keys.Z) && grounded) {
            maxSpeed = fastSpeedMax;
            force = fastForce;
        }


        // Jump
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.X)) && grounded) {
            body.applyLinearImpulse(new Vector2(0.0f, 16.0f), body.getWorldCenter(), true);
            assetManager.get("audio/sfx/jump_small.wav", Sound.class).play();
            jump = true;
            keyPressedTime = 0;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.X)) && currentState == State.JUMPING) {
            if (keyPressedTime > 0.1f && keyPressedTime < 0.15f) {
                body.applyLinearImpulse(new Vector2(0.0f, 5.0f), body.getWorldCenter(), true);
                keyPressedTime = 99.0f;
            }
        }

        // crouch
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            if (!crouch) {
                crouch = isGrownUp;
                if (crouch) {
                    defSmallMario();
                }
            }
        }
        else {
            if (crouch) {
                defBigMario();
            }
            crouch = false;
        }


        // Move left
        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && body.getLinearVelocity().x > -maxSpeed && !crouch) {
            body.applyForceToCenter(new Vector2(-force, 0.0f), true);
            if (body.getLinearVelocity().x > normalSpeedMax || (currentState == State.BRAKING && body.getLinearVelocity().x > 0)) {
                brake = true;
            }
        }

        // Move right
        if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && body.getLinearVelocity().x < maxSpeed && !crouch) {
            body.applyForceToCenter(new Vector2(force, 0.0f), true);
            if (body.getLinearVelocity().x < -normalSpeedMax || (currentState == State.BRAKING && body.getLinearVelocity().x < 0)) {
                brake = true;
            }
        }

    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public boolean isGrownUp() {
        return isGrownUp;
    }

    public boolean isDead() {
        return isDead;
    }

    private void checkGrounded() {
        grounded = false;

        Vector2 p1;
        Vector2 p2;

        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getUserData().getClass() == Mario.class) {
                    return 1;
                }

                if (fraction < 1) {
                    grounded = true;
                    return 0;
                }
                return 0;
            }
        };

        for (int i = 0; i < 3; i++) {
            p1 = new Vector2(body.getPosition().x - radius * (1 - i), body.getPosition().y - radius);
            p2 = new Vector2(p1.x, p1.y - 0.05f);
            world.rayCast(rayCastCallback, p1, p2);
        }

    }

    @Override
    public void update(float delta) {
        checkGrounded();

        // die when falling below ground
        if (body.getPosition().y < -2.0f) {
            die = true;
        }

        if (!isDead) {
            keyPressedTime += delta;
            handleInput();
        }


        State previousState = currentState;


        if (die) {
            if (!isDead) {
                assetManager.get("audio/sfx/mariodie.wav", Sound.class).play();
                body.applyLinearImpulse(new Vector2(0.0f, 12.0f), body.getWorldCenter(), true);
            }
            isDead = true;
            // do not collide with anything anymore
            for (Fixture fixture : body.getFixtureList()) {
                Filter filter = fixture.getFilterData();
                filter.maskBits = GameManager.NOTHING_BIT;
                fixture.setFilterData(filter);
            }

            if (stateTime < 0.2f) {
                GameManager.setTimeScale(0.1f);
            }
            else {
                GameManager.setTimeScale(1.0f);
            }

            currentState = State.DYING;
        }
        else if (shrink) {
            currentState = State.SHRINKING;
            isGrownUp = false;
        }
        else if (growUp) {
            currentState = State.GROWING;
            isGrownUp = true;
            setBounds(body.getPosition().x, body.getPosition().y, 16 / GameManager.PPM, 32 / GameManager.PPM);
        }
        else if (crouch) {
            currentState = State.CROUCHING;
        }
        else if (!grounded) {
            if (jump) {
                currentState = State.JUMPING;
            }
            else {
                currentState = State.FALLING;
            }

        }
        else {
            if (currentState == State.JUMPING) {
                jump = false;
            }
            if (brake) {
                currentState = State.BRAKING;
                brake = false;
            }
            else if (body.getLinearVelocity().x != 0) {
                currentState = State.RUNNING;
            }
            else {
                currentState = State.STANDING;
            }
        }

        float v = 1.0f + Math.abs(body.getLinearVelocity().x) / fastSpeedMax;
        stateTime = previousState == currentState ? stateTime + delta * v : 0;

        switch (currentState) {
            case DYING:
                setRegion(dying);
                break;
            case SHRINKING:
                setRegion(shrinking.getKeyFrame(stateTime, false));
                // temporarily not collide with enemies
                for (Fixture fixture : body.getFixtureList()) {
                    Filter filter = fixture.getFilterData();
                    filter.maskBits = GameManager.GROUND_BIT | GameManager.ITEM_BIT;
                    fixture.setFilterData(filter);
                }

                if (shrinking.isAnimationFinished(stateTime)) {
                    setBounds(body.getPosition().x, body.getPosition().y, 16 / GameManager.PPM, 16 / GameManager.PPM);
                    shrink = false;
                    defSmallMario();
                }
                break;
            case CROUCHING:
                setRegion(crouching);
                break;
            case GROWING:
                setRegion(growing.getKeyFrame(stateTime, false));
                if (growing.isAnimationFinished(stateTime)) {
                    growUp = false;
                    defBigMario();
                }
                break;
            case RUNNING:
                if (isGrownUp) {
                    setRegion(runningBig.getKeyFrame(stateTime, true));
                }
                else {
                    setRegion(runningSmall.getKeyFrame(stateTime, true));
                }
                break;
            case BRAKING:
                if (isGrownUp) {
                    setRegion(brakingBig);
                }
                else {
                    setRegion(brakingSmall);
                }
                break;
            case JUMPING:
                if (isGrownUp) {
                    setRegion(jumpingBig);
                }
                else {
                    setRegion(jumpingSmall);
                }
                break;
            case FALLING:
            case STANDING:
            default:
                if (isGrownUp) {
                    setRegion(standingBig);
                }
                else {
                    setRegion(standingSmall);
                }
                break;
        }


        if ((body.getLinearVelocity().x < -0.01f || !facingRight)) {
            flip(true, false);
            facingRight = false;
        }

        if (body.getLinearVelocity().x > 0.01f){
            facingRight = true;
        }


        // limit Mario's moving area
        if (body.getPosition().x < 0.5f) {
            body.setTransform(0.5f, body.getPosition().y, 0);
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
        else if (body.getPosition().x > playScreen.getMapWidth() - 0.5f) {
            body.setTransform(playScreen.getMapWidth() - 0.5f, body.getPosition().y, 0);
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - radius);
    }

    @Override
    public void onCollide(Collider other) {
        if (other.getFilter().categoryBits == GameManager.ENEMY_WEAKNESS_BIT) {
            ((Enemy) other.getUserData()).getDamage(1);
        }
        else if (other.getFilter().categoryBits == GameManager.ENEMY_LETHAL_BIT) {
            // temporarily invincible when shrinking
            if (shrink) {
                return;
            }

            if (!isGrownUp) {
                die = true;
            }
            else {
                assetManager.get("audio/sfx/powerdown.wav", Sound.class).play();
                shrink = true;
            }
        }
        else if (other.getFilter().categoryBits == GameManager.ITEM_BIT) {
            Item item = (Item) other.getUserData();
            item.use();
            if (item.getName().equals("mushroom")) {
                if (!isGrownUp) {
                    assetManager.get("audio/sfx/powerup.wav", Sound.class).play();
                    growUp = true;
                }
                else {
                    assetManager.get("audio/sfx/stomp.wav", Sound.class).play();
                }

            }

        }
    }
}
