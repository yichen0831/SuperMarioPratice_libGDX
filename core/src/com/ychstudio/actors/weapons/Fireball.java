package com.ychstudio.actors.weapons;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.Collider;
import com.ychstudio.actors.RigidBody;
import com.ychstudio.actors.enemies.Enemy;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/18/15.
 *
 * FireBall
 */
public class Fireball extends RigidBody {

    private Animation firing;
    private TextureRegion hitting;

    private boolean movingRight;

    private boolean hit;

    private float stateTime;

    private float prevX;

    public Fireball(PlayScreen playScreen, float x, float y, boolean movingRight) {
        super(playScreen, x, y);

        this.movingRight = movingRight;

        TextureAtlas textureAtlas = playScreen.getTextureAtlas();

        // firing animation
        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireBall"), 0, 0, 8, 8));
        keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireBall"), 8, 0, 8, 8));
        keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireBall"), 0, 8, 8, 8));
        keyFrames.add(new TextureRegion(textureAtlas.findRegion("FireBall"), 8, 8, 8, 8));
        firing = new Animation(0.1f, keyFrames);

        // hitting
        hitting = new TextureRegion(textureAtlas.findRegion("FireBall"), 20, 4, 8, 8);

        setRegion(firing.getKeyFrame(0, true));
        setBounds(getX(), getY(), 8 / GameManager.PPM, 8 / GameManager.PPM);

        prevX = body.getPosition().x;

        stateTime = 0;
        hit = false;
    }

    @Override
    protected void defBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(getX(), getY());

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(4 / GameManager.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameManager.WEAPON_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.ENEMY_LETHAL_BIT | GameManager.ENEMY_WEAKNESS_BIT | GameManager.ENEMY_INTERACT_BIT;
        fixtureDef.restitution = 0.1f;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(new Vector2(-4 / GameManager.PPM, -4 / GameManager.PPM), new Vector2(4 / GameManager.PPM, -4 / GameManager.PPM));

        fixtureDef.shape = edgeShape;
        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
        edgeShape.dispose();
    }

    @Override
    public void update(float delta) {
        if (destroyed) {
            return;
        }

        if (toBeDestroyed) {

            if (stateTime > 0.01f) {
                world.destroyBody(body);
                body = null;
                destroyed = true;
                return;
            }
        }

        if ((movingRight && prevX > body.getPosition().x) || (!movingRight && prevX < body.getPosition().x)) {
            hit = true;
        }

        stateTime += delta;

        if (!hit) {
            setRegion(firing.getKeyFrame(stateTime, true));

            float speed = 16.0f;
            if (movingRight) {
                body.applyLinearImpulse(new Vector2((speed - body.getLinearVelocity().x) * body.getMass(), 0), body.getWorldCenter(), true);
            }
            else {
                body.applyLinearImpulse(new Vector2((-speed - body.getLinearVelocity().x) * body.getMass(), 0), body.getWorldCenter(), true);
                setFlip(true, false);
            }
        }
        else {
            if (!toBeDestroyed) {
                setRegion(hitting);
                stateTime = 0;
                queueDestroy();
            }
        }

        prevX = body.getPosition().x;

        // if the fireball leaves the screen, queueDestroy
        if (Math.abs(body.getPosition().x - playScreen.getCamera().position.x) > GameManager.V_WIDTH / 2) {
            queueDestroy();
        }

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
    }

    @Override
    public void onCollide(Collider other) {
        if (other.getUserData() instanceof Enemy) {
            ((Enemy) other.getUserData()).getDamage(3);
            hit = true;
        }
        else {
            body.applyLinearImpulse(new Vector2(0, body.getMass() * (8.0f - body.getLinearVelocity().y)), body.getWorldCenter(), true);
        }

    }
}
