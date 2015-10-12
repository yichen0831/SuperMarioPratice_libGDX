package com.ychstudio.actors.maptiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.ychstudio.actors.Collider;
import com.ychstudio.actors.Mario;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * Brick
 */
public class Brick extends MapTileObject {

    private boolean hit;

    private Vector2 originalPosition;
    private Vector2 movablePosition;

    public Brick(PlayScreen playScreen, float x, float y, TextureRegion textureRegion) {
        super(playScreen, x, y, textureRegion);

        originalPosition = new Vector2(x, y);
        movablePosition = new Vector2(x, y + 0.2f);

        hit = false;
    }

    @Override
    protected void defBody() {

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.StaticBody;

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


        if (hit) {
            body.setTransform(movablePosition.x, movablePosition.y, 0);
            hit = false;
        }
        else {
            body.setTransform(originalPosition.x, originalPosition.y, 0);

            if (toBeDestroyed) {
                setBounds(0, 0, 0, 0);
                world.destroyBody(body);
                destroyed = true;
                return;
            }
        }

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);

    }

    @Override
    public void onTrigger(Collider other) {

        if (other.getFilter().categoryBits == GameManager.MARIO_HEAD_BIT) {

            if (((Mario)other.getUserData()).isGrownUp()) {
                hit = true;
                GameManager.instance.getAssetManager().get("audio/sfx/breakblock.wav", Sound.class).play();

                queueDestroy();
            }
            else {
                GameManager.instance.getAssetManager().get("audio/sfx/bump.wav", Sound.class).play();
                hit = true;
            }

        }
    }
}
