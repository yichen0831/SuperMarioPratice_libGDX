package com.ychstudio.actors.maptiles;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.actors.Collider;
import com.ychstudio.actors.effects.FlippingCoin;
import com.ychstudio.actors.items.Mushroom;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/12/15.
 *
 * CoinBlock
 */
public class CoinBlock extends MapTileObject {

    private boolean hitable;
    private boolean hit;

    private Vector2 originalPosition;
    private Vector2 movablePosition;
    private Vector2 targetPosition;

    private TextureRegion unhitableTextureRegion;
    private Animation flashingAnimation;

    private float stateTimer;

    public CoinBlock(PlayScreen playScreen, float x, float y, TiledMapTileMapObject mapObject) {
        super(playScreen, x, y, mapObject);

        TiledMap tiledMap = playScreen.getTiledMap();
        unhitableTextureRegion = tiledMap.getTileSets().getTileSet(0).getTile(28).getTextureRegion();

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();

        for (int i = 25; i < 28; i++) {
            keyFrames.add(tiledMap.getTileSets().getTileSet(0).getTile(i).getTextureRegion());
        }
        keyFrames.add(tiledMap.getTileSets().getTileSet(0).getTile(26).getTextureRegion());
        flashingAnimation = new Animation(0.2f, keyFrames);

        originalPosition = new Vector2(x, y);
        movablePosition = new Vector2(x, y + 0.2f);
        targetPosition = originalPosition;

        hitable = true;
        hit = false;

        stateTimer = 0;
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
        stateTimer += delta;
        if (hitable) {
            setRegion(flashingAnimation.getKeyFrame(stateTimer, true));
        }
        else {
            setRegion(unhitableTextureRegion);
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
            if (hitable) {

                GameManager.instance.addScore(200);
                hitable = false;
                hit = true;
                targetPosition = movablePosition;

                if (mapObject.getProperties().containsKey("mushroom")) {
                    playScreen.addSpawnItem(body.getPosition().x, body.getPosition().y + 16 / GameManager.PPM, Mushroom.class);
                    GameManager.instance.getAssetManager().get("audio/sfx/powerup_spawn.wav", Sound.class).play();
                }
                else {
                    playScreen.addSpawnEffect(body.getPosition().x, body.getPosition().y + 1.0f, FlippingCoin.class);
                    GameManager.instance.getAssetManager().get("audio/sfx/coin.wav", Sound.class).play();
                }
            }
            else {
                GameManager.instance.getAssetManager().get("audio/sfx/bump.wav", Sound.class).play();
            }
        }
    }
}
