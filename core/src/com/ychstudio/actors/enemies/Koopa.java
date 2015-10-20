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
 * Created by yichen on 10/15/15.
 *
 * Koopa
 */
public class Koopa extends Enemy {
    public enum State {
        WALKING,
        SHELLING,
        SPINNING,
        AWAKING,
        DYING,
    }

    private Animation walking;
    private TextureRegion shelling;
    private Animation awaking;

    private boolean shell;
    private boolean die;
    private boolean walk;
    private boolean spin;
    private boolean awake;

    private float speed;
    private float stateTime;
    private boolean movingRight;

    private State currentState;

    private Fixture lethalFixture0;
    private Fixture lethalFixture1;
    private Fixture weakFixture;
    private Fixture feetFixture;

    public Koopa(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        for (int i = 0; i < 2; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Koopa"), i * 16, 0, 16, 32));
        }
        walking = new Animation(0.2f, keyFrames);

        keyFrames.clear();

        shelling = new TextureRegion(textureAtlas.findRegion("Koopa"), 16 * 4, 0, 16, 32);

        for (int i = 4; i < 6; i++) {
            keyFrames.add(new TextureRegion(textureAtlas.findRegion("Koopa"), i * 16, 0, 16, 32));
        }
        awaking = new Animation(0.2f, keyFrames);

        setSize(16 / GameManager.PPM, 32 / GameManager.PPM);
        setRegion(walking.getKeyFrame(0.1f, true));

        movingRight = false;
        speed = 3.2f;
        stateTime = 0;

        die = false;
        shell = false;
        spin = false;
        awake = false;
        walk = true;

        currentState = State.WALKING;
    }

    private void becomeShell() {
        Filter filter = lethalFixture0.getFilterData();
        filter.categoryBits = GameManager.ENEMY_WEAKNESS_BIT;
        lethalFixture0.setFilterData(filter);
        lethalFixture1.setFilterData(filter);

        filter = feetFixture.getFilterData();
        filter.categoryBits = GameManager.ENEMY_WEAKNESS_BIT;
        feetFixture.setFilterData(filter);

        filter = weakFixture.getFilterData();
        filter.categoryBits = GameManager.ENEMY_INTERACT_BIT;
        weakFixture.setFilterData(filter);
    }

    private void becomeNoraml() {
        Filter filter = lethalFixture0.getFilterData();
        filter.categoryBits = GameManager.ENEMY_LETHAL_BIT;
        lethalFixture0.setFilterData(filter);
        lethalFixture1.setFilterData(filter);

        filter = feetFixture.getFilterData();
        filter.categoryBits = GameManager.ENEMY_LETHAL_BIT;
        feetFixture.setFilterData(filter);

        filter = weakFixture.getFilterData();
        filter.categoryBits = GameManager.ENEMY_WEAKNESS_BIT;
        weakFixture.setFilterData(filter);
    }

    private void becomeDead() {
        Filter filter = new Filter();
        filter.categoryBits = GameManager.NOTHING_BIT;
        filter.maskBits = GameManager.NOTHING_BIT;
        for (Fixture fixture : body.getFixtureList()) {
            fixture.setFilterData(filter);
        }
    }

    @Override
    public void getDamage(int damage) {
        if (toBeDestroyed || !active) {
            return;
        }

        switch (currentState) {
            case DYING:
                break;

            case WALKING:
                if (damage == 1 || damage == 2) {
                    // hit by mario or by ground
                    shell = true;
                }
                else {
                    // hit by weapon
                    die = true;
                }
                break;

            case SPINNING:
                if (damage == 1 || damage == 2) {
                    // hit by mario or by ground
                    shell =true;

                }
                else {
                    die = true;
                }
                break;

            case AWAKING:
            case SHELLING:
            default:
                die = true;
                break;
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
            destroyed =true;
            return;
        }

        if (playScreen.getMarioPosition().x + GameManager.V_WIDTH / 2 + 4 > body.getPosition().x )
            active = true;

        if (!active) {
            return;
        }

        State previousState = currentState;

        if (die) {
            die = false;
            body.applyLinearImpulse(new Vector2(0.0f, 7.2f), body.getWorldCenter(), true);
            becomeDead();
            GameManager.instance.getAssetManager().get("audio/sfx/stomp.wav", Sound.class).play();
            GameManager.instance.addScore(500);
            playScreen.getScoreIndicator().addScoreItem(getX(), getY(), 500);
            currentState = State.DYING;
        }
        else if (shell) {
            shell = false;
            becomeShell();
            GameManager.instance.getAssetManager().get("audio/sfx/stomp.wav", Sound.class).play();
            GameManager.instance.addScore(500);
            playScreen.getScoreIndicator().addScoreItem(getX(), getY(), 500);
            currentState = State.SHELLING;
        }
        else if (spin) {
            spin = false;
            becomeNoraml();
            GameManager.instance.getAssetManager().get("audio/sfx/kick.ogg", Sound.class).play();
            GameManager.instance.addScore(500);
            playScreen.getScoreIndicator().addScoreItem(getX(), getY(), 500);
            currentState = State.SPINNING;
        }
        else if (awake) {
            awake = false;
            currentState = State.AWAKING;
        }
        else if (walk) { // walk
            walk = false;
            becomeNoraml();
            currentState = State.WALKING;
        }

        if (previousState != currentState) {
            stateTime = 0;
        }

        switch (currentState) {
            case WALKING:
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

            case SHELLING:
                setRegion(shelling);
                // stop moving
                Vector2 velocity = body.getLinearVelocity();
                velocity.x = 0;
                body.setLinearVelocity(velocity);
                if (stateTime > 5.0f) {
                    awake = true;
                }
                break;

            case AWAKING:
                setRegion(awaking.getKeyFrame(stateTime, true));
                if (stateTime > 2.0f) {
                    walk = true;
                }
                break;

            case SPINNING:
                setRegion(shelling);
                checkMovingDirection();
                velocity = body.getLinearVelocity();
                velocity.x = 8.0f * (movingRight ? 1 : -1);
                body.setLinearVelocity(velocity);
                break;

            case DYING:
            default:
                setRegion(shelling);
                if (stateTime > 2.0f) {
                    queueDestroy();
                }
                break;
        }

        stateTime += delta;

        if (movingRight) {
            flip(true, false);
        }

        if (currentState == State.DYING) {
            flip(false, true);
            setPosition(body.getPosition().x - 8 / GameManager.PPM, body.getPosition().y - 24 / GameManager.PPM);
        }
        else {
            setPosition(body.getPosition().x - 8 / GameManager.PPM, body.getPosition().y - 8 / GameManager.PPM);
        }
    }

    @Override
    public void interact(Mario mario) {
        movingRight = mario.getX() <= getX();
        spin = true;
    }

    @Override
    protected void defBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());

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
        feetFixture = body.createFixture(fixtureDef);
        feetFixture.setUserData(this);

        // lethal
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(2.0f / GameManager.PPM);
        circleShape.setPosition(new Vector2(-6, 0).scl(1 / GameManager.PPM));

        fixtureDef.shape = circleShape;
        fixtureDef.filter.categoryBits = GameManager.ENEMY_LETHAL_BIT;
        fixtureDef.filter.maskBits = GameManager.MARIO_BIT | GameManager.WEAPON_BIT;
        lethalFixture0 = body.createFixture(fixtureDef);
        lethalFixture0.setUserData(this);

        circleShape.setPosition(new Vector2(6, 0).scl(1 / GameManager.PPM));
        lethalFixture1 = body.createFixture(fixtureDef);
        lethalFixture1.setUserData(this);

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

        weakFixture = body.createFixture(fixtureDef);
        weakFixture.setUserData(this);

        circleShape.dispose();
        edgeShape.dispose();
        polygonShape.dispose();

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
                if (fraction < 1.0f) {
                    if (currentState == State.SPINNING && fixture.getUserData() instanceof Enemy) {
                        ((Enemy) fixture.getUserData()).getDamage(3);
                    }
                    else if (fixture.getUserData().getClass() != Mario.class) {
                        if (currentState == State.SPINNING) {
                            float cameraX = playScreen.getCamera().position.x;
                            float distanceRatio = (body.getPosition().x - cameraX) / GameManager.V_WIDTH * 2;
                            float pan = MathUtils.clamp(distanceRatio, -1, 1);
                            float volume = MathUtils.clamp(2.0f - (float)Math.sqrt(Math.abs(distanceRatio)), 0, 1);
                            GameManager.instance.getAssetManager().get("audio/sfx/bump.wav", Sound.class).play(volume, 1.0f, pan);
                        }
                        movingRight = !movingRight;
                    }
                }
                return 0;
            }
        };

        if (movingRight) {
            p1 = new Vector2(body.getPosition().x + 6.8f / GameManager.PPM, body.getPosition().y);
            p2 = new Vector2(p1).add(0.2f, 0);

            world.rayCast(rayCastCallback, p1, p2);
        }
        else {
            p1 = new Vector2(body.getPosition().x - 6.8f / GameManager.PPM, body.getPosition().y);
            p2 = new Vector2(p1).add(-0.2f, 0);

            world.rayCast(rayCastCallback, p1, p2);
        }
    }


}
