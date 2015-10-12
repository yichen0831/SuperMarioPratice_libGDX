package com.ychstudio.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
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
        FALLING,
    }

    private State currentState;

    private float stateTime;

    private TextureRegion standing;
    private TextureRegion jumping;
    private Animation running;

    private boolean facingRight;

    private boolean grownUp;

    private AssetManager assetManager;

    public Mario(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
        TextureAtlas textureAtlas = playScreen.getTextureAtlas();

        standing = new TextureRegion(textureAtlas.findRegion("Mario_small"), 0, 0, 16, 16);
        jumping = new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * 5, 0, 16, 16);

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Mario_small"), 16 * i, 0, 16, 16));
        }
        running = new Animation(0.1f, keyFrames);

        setRegion(standing);
        setBounds(getX(), getY(), 16 / GameManager.PPM, 16 / GameManager.PPM);

        currentState = State.STANDING;
        stateTime = 0;

        facingRight = true;
        grownUp = false;

        assetManager = GameManager.instance.getAssetManager();
    }

    @Override
    protected void defBody() {
        float radius = 6.8f / GameManager.PPM;

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
        edgeShape.set(new Vector2(-radius / 2, -radius), new Vector2(radius / 2, -radius));
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

    private void handleInput() {

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && canJump()) {
            body.applyLinearImpulse(new Vector2(0.0f, 20.0f), body.getLocalCenter(), true);
            assetManager.get("audio/sfx/jump_small.wav", Sound.class).play();
        }

        // Move left
        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && body.getLinearVelocity().x > -10.0f) {
            body.applyForceToCenter(new Vector2(-36.0f, 0.0f), true);
        }

        // Move right
        if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && body.getLinearVelocity().x < 10.0f) {
            body.applyForceToCenter(new Vector2(36.0f, 0.0f), true);
        }

    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public boolean isGrownUp() {
        return grownUp;
    }

    private boolean canJump() {
        return !(currentState == State.FALLING || currentState == State.JUMPING);
    }

    @Override
    public void update(float delta) {
        handleInput();

        State previousState = currentState;

        // checking state
        if (body.getLinearVelocity().y > 0.01f || (body.getLinearVelocity().y < -0.01f && previousState == State.JUMPING)) {
            currentState = State.JUMPING;
        }
        else if (body.getLinearVelocity().y < -0.01f) {
            currentState = State.FALLING;
        }
        else if (body.getLinearVelocity().x != 0) {
            currentState = State.RUNNING;
        }
        else {
            currentState = State.STANDING;
        }

        switch (currentState) {
            case RUNNING:
                setRegion(running.getKeyFrame(stateTime, true));
                break;
            case JUMPING:
                setRegion(jumping);
                break;
            case FALLING:
            case STANDING:
            default:
                setRegion(standing);
                break;
        }


        if ((body.getLinearVelocity().x < -0.01f || !facingRight)) {
            flip(true, false);
            facingRight = false;
        }

        if (body.getLinearVelocity().x > 0.01f){
            facingRight = true;
        }

        stateTime = previousState == currentState ? stateTime + delta : 0;

        if (body.getPosition().x < 0.5f) {
            body.setTransform(0.5f, body.getPosition().y, 0);
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }
        else if (body.getPosition().x > playScreen.getMapWidth() - 0.5f) {
            body.setTransform(playScreen.getMapWidth() - 0.5f, body.getPosition().y, 0);
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        }

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);

    }

    @Override
    public void onCollide(Collider other) {

//        System.out.println("I collided with " + other.getFilterData().categoryBits);
    }
}
