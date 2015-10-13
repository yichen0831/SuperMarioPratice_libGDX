package com.ychstudio.actors.items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.ychstudio.actors.enemies.Enemy;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/13/15.
 *
 * Mushroom
 */
public class Mushroom extends Item {
    private boolean movingRight;
    private float speed;

    public Mushroom(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        setRegion(new TextureRegion(playScreen.getTextureAtlas().findRegion("Mushroom"), 0, 0, 16, 16));
        setBounds(body.getPosition().x, body.getPosition().y, 16 / GameManager.PPM, 16 / GameManager.PPM);

        movingRight = true;
        speed = 2.0f;

        name = "mushroom";
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
                if (fraction < 1.0f) {
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

        if (destroyed) {
            return;
        }

        if (toBeDestroyed) {
            setBounds(0, 0, 0, 0);
            world.destroyBody(body);
            destroyed = true;
            return;
        }

        checkMovingDirection();

        float velocityY = body.getLinearVelocity().y;
        if (movingRight) {
            body.setLinearVelocity(new Vector2(speed, velocityY));
        }
        else {
            body.setLinearVelocity(new Vector2(-speed, velocityY));
        }

        setPosition(body.getPosition().x - 8 / GameManager.PPM, body.getPosition().y - 8 / GameManager.PPM);
    }

    @Override
    public void use() {
        GameManager.instance.addScore(200);
        queueDestroy();
    }

    @Override
    protected void defBody() {

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(6.8f / GameManager.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameManager.ITEM_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.MARIO_BIT;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
    }
}