package com.ychstudio.actors.maptiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ychstudio.actors.Collider;
import com.ychstudio.actors.Mario;
import com.ychstudio.actors.effects.BrickDebris;
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

        if (explode) {
            setRegion(debris);
            if (stateTime > 0.015f) {
                queueDestroy();
                for (int i = 0; i < 4; i++) {
                    playScreen.addSpawnEffect(body.getPosition().x, body.getPosition().y + 0.5f, BrickDebris.class);
                }
            }
        }

        float x = body.getPosition().x;
        float y = body.getPosition().y;
        if (Math.abs(y - targetPosition.y) > 0.01f) {
            y = MathUtils.lerp(y, targetPosition.y, 0.6f);
            body.setTransform(x, y, 0);
        }
        else {
            if (hit) {
                hit = false;
                targetPosition = originalPosition;
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
                explode = true;
                stateTime = 0;
            }
            else {
                GameManager.instance.getAssetManager().get("audio/sfx/bump.wav", Sound.class).play();
                hit = true;
            }

        }
    }
}
