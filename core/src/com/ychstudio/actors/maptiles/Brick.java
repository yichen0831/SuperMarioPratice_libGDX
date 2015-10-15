package com.ychstudio.actors.maptiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.ychstudio.actors.Collider;
import com.ychstudio.actors.Mario;
import com.ychstudio.actors.effects.BrickDebris;
import com.ychstudio.actors.enemies.Enemy;
import com.ychstudio.actors.items.Mushroom;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * Brick
 */
public class Brick extends MapTileObject {

    private boolean hit;
    private boolean explode;
    private boolean lethal;

    private float stateTime;

    private Vector2 originalPosition;
    private Vector2 movablePosition;
    private Vector2 targetPosition;

    private TextureRegion debris;

    public Brick(PlayScreen playScreen, float x, float y, TiledMapTileMapObject mapObject) {
        super(playScreen, x, y, mapObject);

        originalPosition = new Vector2(x, y);
        movablePosition = new Vector2(x, y + 0.2f);

        targetPosition = originalPosition;

        debris = new TextureRegion(playScreen.getTextureAtlas().findRegion("Debris"), 0, 0, 16, 16);

        hit = false;
        lethal = false;
        explode = false;
        stateTime = 0;
    }

    @Override
    protected void defBody() {

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.KinematicBody;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(16 / GameManager.PPM / 2, 16 / GameManager.PPM / 2);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.filter.categoryBits = GameManager.GROUND_BIT;
        fixtureDef.shape = shape;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
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

        stateTime += delta;

        float x = body.getPosition().x;
        float y = body.getPosition().y;
        Vector2 dist = new Vector2(x, y).sub(targetPosition);
        if (dist.len2() > 0.0001f) {
            body.setTransform(new Vector2(x, y).lerp(targetPosition, 0.6f), 0);
        }
        else {
            body.setTransform(targetPosition, 0);
            if (hit) {
                hit = false;
                targetPosition = originalPosition;
            }
        }

        if (lethal) {
            lethal = false;

            RayCastCallback raycastCallback = new RayCastCallback() {
                @Override
                public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
                    if (fraction <= 1.0f) {
                        if (fixture.getUserData() instanceof Enemy) {
                            ((Enemy) fixture.getUserData()).getDamage(2);
                        } else if (fixture.getUserData() instanceof Mushroom) {
                            ((Mushroom) fixture.getUserData()).bounce();
                        }
                        return 0;
                    }
                    return 0;
                }
            };

            // damage the enemy or push up mushroom above when hit
            for (int i = 0; i < 3; i++) {
                Vector2 p1 = new Vector2(body.getPosition().x - (i - 1) * 0.5f, body.getPosition().y + 0.4f);
                Vector2 p2 = new Vector2(p1).add(0, 0.6f);
                world.rayCast(raycastCallback, p1, p2);
            }
        }

        if (explode) {
            setRegion(debris);
            if (stateTime > 0.015f) {
                queueDestroy();
                for (int i = 0; i < 4; i++) {
                    playScreen.addSpawnEffect(body.getPosition().x, body.getPosition().y + 0.5f, BrickDebris.class);
                }
            }
        }

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);

    }


    @Override
    public void onTrigger(Collider other) {

        if (other.getFilter().categoryBits == GameManager.MARIO_HEAD_BIT) {
            targetPosition = movablePosition;

            if (((Mario)other.getUserData()).isGrownUp()) {
                GameManager.instance.getAssetManager().get("audio/sfx/breakblock.wav", Sound.class).play();
                GameManager.instance.addScore(200);
                hit = true;
                lethal = true;
                explode = true;
                stateTime = 0;
            }
            else {
                GameManager.instance.getAssetManager().get("audio/sfx/bump.wav", Sound.class).play();
                hit = true;
                lethal = true;
            }

        }
    }
}
