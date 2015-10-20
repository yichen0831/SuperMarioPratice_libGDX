package com.ychstudio.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
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
        FIREMARIOING,
        CLIMBING,
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
    private Animation climbingSmall;

    private TextureRegion standingBig;
    private TextureRegion jumpingBig;
    private Animation runningBig;
    private TextureRegion brakingBig;
    private TextureRegion crouchingBig;
    private Animation climbingBig;

    private TextureRegion standingFireMario;
    private TextureRegion jumpingFireMario;
    private Animation runningFireMario;
    private TextureRegion brakingFireMario;
    private TextureRegion crouchingFireMario;
    private Animation climbingFireMario;

    private TextureRegion[] standingSmallInvincible;
    private TextureRegion[] jumpingSmallInvincible;
    private Animation[] runningSmallInvincible;
    private TextureRegion[] brakingSmallInvincible;
    private Animation[] climbingSmallInvincible;

    private TextureRegion[] standingBigInvincible;
    private TextureRegion[] jumpingBigInvincible;
    private Animation[] runningBigInvincible;
    private TextureRegion[] brakingBigInvincible;
    private TextureRegion[] crouchingBigInvincible;
    private Animation[] firingBigInvincible;
    private Animation[] climbingBigInvincible;

    private TextureRegion dying;
    private Animation growing;
    private Animation fireMarioing;
    private Animation shrinking;
    private Animation firingAnimation;

    private boolean showFiringAnimation;

    private boolean facingRight;

    private boolean isGrownUp;
    private boolean isFireMario;
    private boolean isDead;
    private boolean isLevelCompleted;

    private boolean ground;
    private boolean jump;
    private boolean die;
    private boolean growUp;
    private boolean shrink;
    private boolean crouch;
    private boolean brake;
    private boolean fireMario;
    private boolean climb;

    private boolean isInvincible;
    private float invincibleCountDown;

    private boolean smallJump = false;
    private boolean bigJump = false;
    private float jumpSoundTimer = 0f;

    private final float fireInterval = 0.3f;
    private float fireTimer = 0f;

    private AssetManager assetManager;

    public Mario(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
        TextureAtlas textureAtlas = playScreen.getTextureAtlas();

        standingSmall = new TextureRegion(textureAtlas.findRegion("Mario_small"), 0, 0, 16, 16);
        standingBig = new TextureRegion(textureAtlas.findRegion("Mario_big"), 0, 0, 16, 32);
        standingFireMario = new TextureRegion(textureAtlas.findRegion("FireMario"), 0, 0, 16, 32);

        jumpingSmall = new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * 5, 0, 16, 16);
        jumpingBig = new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 5, 0, 16, 32);
        jumpingFireMario = new TextureRegion(textureAtlas.findRegion("FireMario"), 16 * 5, 0, 16, 32);

        brakingSmall = new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * 4, 0, 16, 16);
        brakingBig = new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 4, 0, 16, 32);
        brakingFireMario = new TextureRegion(textureAtlas.findRegion("FireMario"), 16 * 4, 0, 16, 32);

        // flip braking image for correct displaying
        brakingSmall.flip(true, false);
        brakingBig.flip(true, false);
        brakingFireMario.flip(true, false);

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
        for (int i = 1; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireMario"), 16 * i, 0, 16, 32));
        }
        runningFireMario = new Animation(0.1f, keyFrames);

        keyFrames.clear();

        // climbing animation
        for (int i = 7; i < 9; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * i, 0, 16, 16));
        }
        climbingSmall = new Animation(0.1f, keyFrames);

        keyFrames.clear();
        for (int i = 7; i < 9; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * i, 0, 16, 32));
        }
        climbingBig = new Animation(0.1f, keyFrames);

        keyFrames.clear();
        for (int i = 7; i < 9; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireMario"), 16 * i, 0, 16, 32));
        }
        climbingFireMario = new Animation(0.1f, keyFrames);

        keyFrames.clear();

        // growing up animation
        for (int i = 0; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 15, 0, 16, 32));
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 0, 0, 16, 32));
        }
        growing = new Animation(0.1f, keyFrames);

        keyFrames.clear();
        // becoming FireMario animation
        for (int i = 0; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireMario"), 16 * 15, 0, 16, 32));
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireMario"), 0, 0, 16, 32));
        }
        fireMarioing = new Animation(0.1f, keyFrames);

        // firing animation
        keyFrames.clear();
        for (int i = 16; i < 19; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireMario"), 16 * i, 0, 16, 32));
        }
        firingAnimation = new Animation(0.1f, keyFrames);

        keyFrames.clear();
        // shrinking animation
        for (int i = 0; i < 3; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 0, 0, 16, 32));
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 15, 0, 16, 32));
        }
        shrinking = new Animation(0.1f, keyFrames);

        dying = new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * 6, 0, 16, 16);

        crouchingBig = new TextureRegion(textureAtlas.findRegion("Mario_big"), 16 * 6, 0, 16, 32);
        crouchingFireMario = new TextureRegion(textureAtlas.findRegion("FireMario"), 16 * 6, 0, 16, 32);


        // invincible animations
        standingSmallInvincible = new TextureRegion[4];
        standingSmallInvincible[0] = standingSmall;
        standingSmallInvincible[1] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible1"), 0, 0, 16, 16);
        standingSmallInvincible[2] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible2"), 0, 0, 16, 16);
        standingSmallInvincible[3] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible3"), 0, 0, 16, 16);

        jumpingSmallInvincible = new TextureRegion[4];
        jumpingSmallInvincible[0] = jumpingSmall;
        jumpingSmallInvincible[1] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible1"), 16 * 5, 0, 16, 16);
        jumpingSmallInvincible[2] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible2"), 16 * 5, 0, 16, 16);
        jumpingSmallInvincible[3] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible3"), 16 * 5, 0, 16, 16);

        brakingSmallInvincible = new TextureRegion[4];
        brakingSmallInvincible[0] = brakingSmall;
        brakingSmallInvincible[1] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible1"), 16 * 4, 0, 16, 16);
        brakingSmallInvincible[2] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible2"), 16 * 4, 0, 16, 16);
        brakingSmallInvincible[3] = new TextureRegion(textureAtlas.findRegion("Mario_small_invincible3"), 16 * 4, 0, 16, 16);
        brakingSmallInvincible[1].flip(true, false);
        brakingSmallInvincible[2].flip(true, false);
        brakingSmallInvincible[3].flip(true, false);

        runningSmallInvincible = new Animation[4];
        runningSmallInvincible[0] = runningSmall;
        for (int j = 1; j < 4; j++) {
            keyFrames.clear();
            for (int i = 1; i < 4; i++) {
                keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_small_invincible" + j), 16 * i, 0, 16, 16));
            }
            runningSmallInvincible[j] = new Animation(0.1f, keyFrames);
        }

        climbingSmallInvincible = new Animation[4];
        climbingSmallInvincible[0] = climbingSmall;
        for (int j = 1; j < 4; j++) {
            keyFrames.clear();
            for (int i = 7; i < 9; i++) {
                keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_small_invincible" + j), 16 * i, 0, 16, 16));
            }
            climbingSmallInvincible[j] = new Animation(0.1f, keyFrames);
        }

        standingBigInvincible = new TextureRegion[4];
        standingBigInvincible[0] = standingBig;
        standingBigInvincible[1] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible1"), 0, 0, 16, 32);
        standingBigInvincible[2] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible2"), 0, 0, 16, 32);
        standingBigInvincible[3] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible3"), 0, 0, 16, 32);

        jumpingBigInvincible = new TextureRegion[4];
        jumpingBigInvincible[0] = jumpingBig;
        jumpingBigInvincible[1] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible1"), 16 * 5, 0, 16, 32);
        jumpingBigInvincible[2] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible2"), 16 * 5, 0, 16, 32);
        jumpingBigInvincible[3] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible3"), 16 * 5, 0, 16, 32);

        brakingBigInvincible = new TextureRegion[4];
        brakingBigInvincible[0] = brakingBig;
        brakingBigInvincible[1] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible1"), 16 * 4, 0, 16, 32);
        brakingBigInvincible[2] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible2"), 16 * 4, 0, 16, 32);
        brakingBigInvincible[3] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible3"), 16 * 4, 0, 16, 32);
        brakingBigInvincible[1].flip(true, false);
        brakingBigInvincible[2].flip(true, false);
        brakingBigInvincible[3].flip(true, false);

        crouchingBigInvincible = new TextureRegion[4];
        crouchingBigInvincible[0] = crouchingBig;
        crouchingBigInvincible[1] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible1"), 16 * 6, 0, 16, 32);
        crouchingBigInvincible[2] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible2"), 16 * 6, 0, 16, 32);
        crouchingBigInvincible[3] = new TextureRegion(textureAtlas.findRegion("Mario_big_invincible3"), 16 * 6, 0, 16, 32);

        runningBigInvincible = new Animation[4];
        runningBigInvincible[0] = runningBig;
        for (int j = 1; j < 4; j++) {
            keyFrames.clear();
            for (int i = 1; i < 4; i++) {
                keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big_invincible" + j), 16 * i, 0, 16, 32));
            }
            runningBigInvincible[j] = new Animation(0.1f, keyFrames);
        }

        firingBigInvincible = new Animation[4];
        firingBigInvincible[0] = firingAnimation;
        for (int j = 1; j < 4; j++) {
            keyFrames.clear();
            for (int i = 16; i < 19; i++) {
                keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big_invincible" + j), 16 * i, 0, 16, 32));
            }
            firingBigInvincible[j] = new Animation(0.1f, keyFrames);
        }

        climbingBigInvincible = new Animation[4];
        climbingBigInvincible[0] = climbingBig;
        for (int j = 1; j < 4; j++) {
            keyFrames.clear();
            for (int i = 7; i < 9; i++) {
                keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_big_invincible" + j), 16 * i, 0, 16, 32));
            }
            climbingBigInvincible[j] = new Animation(0.1f, keyFrames);
        }


        setRegion(standingSmall);
        setBounds(getX(), getY(), 16 / GameManager.PPM, 16 / GameManager.PPM);

        currentState = State.STANDING;
        stateTime = 0;

        facingRight = true;
        isGrownUp = false;
        isFireMario = false;
        jump = false;
        die = false;
        shrink = false;
        growUp = false;
        crouch = false;
        climb = false;

        isInvincible = false;
        invincibleCountDown = 0;

        isLevelCompleted = false;

        showFiringAnimation = false;

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
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.ENEMY_WEAKNESS_BIT | GameManager.ENEMY_INTERACT_BIT | GameManager.ENEMY_LETHAL_BIT | GameManager.ITEM_BIT | GameManager.FLAGPOLE_BIT;

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
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.ENEMY_WEAKNESS_BIT | GameManager.ENEMY_INTERACT_BIT | GameManager.ENEMY_LETHAL_BIT | GameManager.ITEM_BIT | GameManager.FLAGPOLE_BIT;

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
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.ENEMY_WEAKNESS_BIT | GameManager.ENEMY_INTERACT_BIT | GameManager.ENEMY_LETHAL_BIT | GameManager.ITEM_BIT | GameManager.FLAGPOLE_BIT;

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

        // Cheat code
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            growUp = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            fireMario = true;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            isInvincible = true;
            invincibleCountDown = 10.0f;
        }

        // Accelerate
        if (Gdx.input.isKeyPressed(Input.Keys.X) && ground) {
            maxSpeed = fastSpeedMax;
            force = fastForce;
        }

        // Fire fireball
        if (Gdx.input.isKeyJustPressed(Input.Keys.X) && isFireMario && fireTimer > fireInterval && currentState != State.CROUCHING) {
            float x = facingRight ? 0.8f : -0.8f;
            float y = 0.8f;
            GameManager.instance.getAssetManager().get("audio/sfx/fireball.ogg", Sound.class).play();
            playScreen.addSpawnFireball(body.getPosition().x + x, body.getPosition().y + y, facingRight);
            fireTimer = 0;
            showFiringAnimation = true;
        }

        // Jump
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.C)) && ground) {
            body.applyLinearImpulse(new Vector2(0.0f, 16.0f), body.getWorldCenter(), true);
            jumpSoundTimer = 0;
            jump = true;
            smallJump = true;
            keyPressedTime = 0;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isKeyPressed(Input.Keys.C)) && currentState == State.JUMPING) {
            if (keyPressedTime > 0.1f && keyPressedTime < 0.15f) {
                body.applyLinearImpulse(new Vector2(0.0f, 5.0f), body.getWorldCenter(), true);
                keyPressedTime = 99.0f;
                bigJump = true;
            }
        }

        if (smallJump && jumpSoundTimer > 0.15f) {
            if (bigJump) {
                assetManager.get("audio/sfx/jump_super.wav", Sound.class).play();
            }
            else {
                assetManager.get("audio/sfx/jump_small.wav", Sound.class).play();
            }
            smallJump = false;
            bigJump = false;
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
        return isGrownUp || isFireMario;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public boolean isDead() {
        return isDead;
    }

    public void suddenDeath() {
        die = true;
    }

    public void levelCompleted() {
        if (isLevelCompleted) {
            return;
        }

        isLevelCompleted = true;
        climb  = true;

        int point = (int) MathUtils.clamp(getY(), 2.0f, 10.0f) * 100;
        GameManager.instance.addScore(point);
        playScreen.getScoreIndicator().addScoreItem(getX(), getY(), point);
    }

    public void handleLevelCompletedActions() {
        if (climb) {
            facingRight = true;
            body.setTransform(196.0f, body.getPosition().y, 0);
            body.setLinearVelocity(new Vector2(0, -9f));
        }
        else {
            if (getX() < 201.0f)
            body.applyLinearImpulse(new Vector2(body.getMass() * (4.0f - body.getLinearVelocity().x), 0f), body.getWorldCenter(), true);
        }
    }

    private void checkGrounded() {
        ground = false;

        Vector2 p1;
        Vector2 p2;

        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fixture.getUserData().getClass() == Mario.class) {
                    return 1;
                }

                if (fraction < 1) {
                    ground = true;
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

    public void invincibleKill() {
        Vector2 p1;
        Vector2 p2;

        RayCastCallback rayCastCallback = new RayCastCallback() {
            @Override
            public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                if (fraction <= 1 && fixture.getUserData() instanceof Enemy) {
                    ((Enemy) fixture.getUserData()).getDamage(3);
                    return 0;
                }
                return -1;
            }
        };

        for (int i = 0; i < 2; i++) {
            p1 = new Vector2(body.getPosition().x - (i==0 ? -radius : radius), body.getPosition().y);
            p2 = new Vector2(p1.x - (i==0 ? -0.2f :0.2f), p1.y);
            world.rayCast(rayCastCallback, p1, p2);
        }
    }

    @Override
    public void update(float delta) {
        checkGrounded();

        jumpSoundTimer += delta;
        fireTimer += delta;

        if (isInvincible) {
            invincibleKill();
            invincibleCountDown -= delta;
            if (invincibleCountDown <= 0) {
                isInvincible = false;
            }
        }

        if (fireTimer > 0.1f) {
            showFiringAnimation = false;
        }

        // die when falling below ground
        if (body.getPosition().y < -2.0f) {
            die = true;
        }

        if (!isDead && !isLevelCompleted) {
            keyPressedTime += delta;
            handleInput();
        }
        else if (isLevelCompleted) {
            handleLevelCompletedActions();
        }

        State previousState = currentState;

        if (die) {
            if (!isDead) {
                assetManager.get("audio/sfx/mariodie.wav", Sound.class).play();
                body.applyLinearImpulse(new Vector2(0.0f, body.getMass() * (12f -body.getLinearVelocity().y)), body.getWorldCenter(), true);
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
            isFireMario = false;
        }
        else if (growUp) {
            currentState = State.GROWING;
            isGrownUp = true;
            setBounds(body.getPosition().x, body.getPosition().y, 16 / GameManager.PPM, 32 / GameManager.PPM);
        }
        else if (fireMario) {
            currentState = State.FIREMARIOING;
            isGrownUp = true;
            isFireMario = true;
            setBounds(body.getPosition().x, body.getPosition().y, 16 / GameManager.PPM, 32 / GameManager.PPM);
        }
        else if (crouch) {
            currentState = State.CROUCHING;
        }
        else if (climb) {
            currentState = State.CLIMBING;
        }
        else if (!ground) {
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

        int invincibleFrame = 0;
        if (isInvincible) {
            invincibleFrame = (int) (invincibleCountDown * (invincibleCountDown < 4 ? 4 : 10)) % 4;
        }

        switch (currentState) {
            case DYING:
                setRegion(dying);
                setSize(16 / GameManager.PPM, 16 / GameManager.PPM);
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
                if (isInvincible) {
                    setRegion(crouchingBigInvincible[invincibleFrame]);
                }
                else if (isFireMario) {
                    setRegion(crouchingFireMario);
                }
                else {
                    setRegion(crouchingBig);
                }
                break;

            case GROWING:
                setRegion(growing.getKeyFrame(stateTime, false));
                if (growing.isAnimationFinished(stateTime)) {
                    growUp = false;
                    defBigMario();
                }
                break;

            case FIREMARIOING:
                setRegion(fireMarioing.getKeyFrame(stateTime, false));
                if (fireMarioing.isAnimationFinished(stateTime)) {
                    fireMario = false;
                    defBigMario();
                }
                break;

            case RUNNING:
                if (isGrownUp) {
                    if (isInvincible) {
                        if (showFiringAnimation) {
                            setRegion(firingBigInvincible[invincibleFrame].getKeyFrame(stateTime, true));
                        }
                        else {
                            setRegion(runningBigInvincible[invincibleFrame].getKeyFrame(stateTime, true));
                        }
                    }
                    else if (isFireMario) {
                        if (showFiringAnimation) {
                            setRegion(firingAnimation.getKeyFrame(stateTime, true));
                        }
                        else {
                            setRegion(runningFireMario.getKeyFrame(stateTime, true));
                        }
                    }
                    else {
                        setRegion(runningBig.getKeyFrame(stateTime, true));
                    }
                }
                else {
                    if (isInvincible) {
                        setRegion(runningSmallInvincible[invincibleFrame].getKeyFrame(stateTime, true));
                    }
                    else {
                        setRegion(runningSmall.getKeyFrame(stateTime, true));
                    }
                }
                break;

            case BRAKING:
                if (isGrownUp) {
                    if (isInvincible) {
                        if (showFiringAnimation) {
                            setRegion(firingBigInvincible[invincibleFrame].getKeyFrame(0, true));
                        }
                        else {
                            setRegion(brakingBigInvincible[invincibleFrame]);
                        }
                    }
                    else if (isFireMario) {
                        if (showFiringAnimation) {
                            setRegion(firingAnimation.getKeyFrame(0, true));
                        }
                        else {
                            setRegion(brakingFireMario);
                        }
                    }
                    else {
                        setRegion(brakingBig);
                    }
                }
                else {
                    if (isInvincible) {
                        setRegion(brakingSmallInvincible[invincibleFrame]);
                    }
                    else {
                        setRegion(brakingSmall);
                    }
                }
                break;

            case JUMPING:
                if (isGrownUp) {
                    if (isInvincible) {
                        if (showFiringAnimation) {
                            setRegion(firingBigInvincible[invincibleFrame].getKeyFrame(0, true));
                        }
                        else {
                            setRegion(jumpingBigInvincible[invincibleFrame]);
                        }
                    }
                    else if (isFireMario) {
                        if (showFiringAnimation) {
                            setRegion(firingAnimation.getKeyFrame(0, true));
                        }
                        else {
                            setRegion(jumpingFireMario);
                        }
                    }
                    else {
                        setRegion(jumpingBig);
                    }
                }
                else {
                    if (isInvincible) {
                        setRegion(jumpingSmallInvincible[invincibleFrame]);
                    } else {
                        setRegion(jumpingSmall);
                    }
                }
                break;

            case CLIMBING:
                if (isGrownUp) {
                    if (isInvincible) {
                        setRegion(climbingBigInvincible[invincibleFrame].getKeyFrame(stateTime, true));
                    }
                    else {
                        if (isFireMario) {
                            setRegion(climbingFireMario.getKeyFrame(stateTime, true));
                        } else {
                            setRegion(climbingBig.getKeyFrame(stateTime, true));
                        }
                    }
                }
                else {
                    if (isInvincible) {
                        setRegion(climbingSmallInvincible[invincibleFrame].getKeyFrame(stateTime, true));
                    }
                    else {
                        setRegion(climbingSmall.getKeyFrame(stateTime, true));
                    }
                }

                if (stateTime > 1.0f) {
                    climb = false;
                }
                break;

            case FALLING:
            case STANDING:
            default:
                if (isGrownUp) {
                    if (isInvincible) {
                        if (showFiringAnimation) {
                            setRegion(firingBigInvincible[invincibleFrame].getKeyFrame(0, true));
                        }
                        else {
                            setRegion(standingBigInvincible[invincibleFrame]);
                        }
                    }
                    else if (isFireMario) {
                        if (showFiringAnimation) {
                            setRegion(firingAnimation.getKeyFrame(0, true));
                        }
                        else {
                            setRegion(standingFireMario);
                        }
                    }
                    else {
                        setRegion(standingBig);
                    }
                }
                else {
                    if (isInvincible) {
                        setRegion(standingSmallInvincible[invincibleFrame]);
                    }
                    else {
                        setRegion(standingSmall);
                    }
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
            if (isInvincible) {
                ((Enemy) other.getUserData()).getDamage(3);
            }
            else {
                ((Enemy) other.getUserData()).getDamage(1);
                float force = body.getMass() * (8.0f - body.getLinearVelocity().y);
                body.applyLinearImpulse(new Vector2(0.0f, force), body.getWorldCenter(), true);
            }
        }
        else if (other.getFilter().categoryBits == GameManager.ENEMY_LETHAL_BIT) {
            if (isInvincible) {
                ((Enemy) other.getUserData()).getDamage(3);
            }
            else {

                // temporarily invincible when shrinking
                if (shrink) {
                    return;
                }

                if (!isGrownUp) {
                    die = true;
                } else {
                    assetManager.get("audio/sfx/powerdown.wav", Sound.class).play();
                    shrink = true;
                }
            }
        }
        else if (other.getFilter().categoryBits == GameManager.ENEMY_INTERACT_BIT) {
            if (isInvincible) {
                ((Enemy) other.getUserData()).getDamage(3);
            }
            else {
                ((Enemy) other.getUserData()).interact(this);
                float force = body.getMass() * (8.0f - body.getLinearVelocity().y);
                body.applyLinearImpulse(new Vector2(0.0f, force), body.getWorldCenter(), true);
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
            else if (item.getName().equals("flower")) {
                if (!isFireMario) {
                    assetManager.get("audio/sfx/powerup.wav", Sound.class).play();
                    fireMario = true;
                }
                else {
                    assetManager.get("audio/sfx/stomp.wav", Sound.class).play();

                }
            }
            else if (item.getName().equals("star")) {
                isInvincible = true;
                invincibleCountDown = 10.0f;
            }

        }
    }
}
