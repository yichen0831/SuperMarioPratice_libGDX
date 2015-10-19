package com.ychstudio.actors.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/18/15.
 * 
 * Flower
 */
public class Flower extends Item {
    
    private Animation anim;
    private float stateTime;
    
    public Flower(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        name = "flower";

        Array<TextureRegion> keyFrames = new Array<TextureRegion>();
        for (int i = 0; i < 4; i++) {
            keyFrames.add(new TextureRegion(playScreen.getTextureAtlas().findRegion("Flower"), 16 * i, 0, 16, 16));
        }

        anim = new Animation(0.2f, keyFrames);
        setRegion(anim.getKeyFrame(0, true));
        setBounds(getX(), getY(), 16 / GameManager.PPM, 16 / GameManager.PPM);
        stateTime = 0;
    }

    @Override
    public void use() {
        GameManager.instance.addScore(1000);
        playScreen.getScoreIndicator().addScoreItem(getX(), getY(), 1000);
        queueDestroy();
    }

    @Override
    protected void defBody() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(bodyDef);

        CircleShape shape = new CircleShape();
        shape.setRadius(8.0f / GameManager.PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameManager.ITEM_BIT;
        fixtureDef.filter.maskBits = GameManager.GROUND_BIT | GameManager.MARIO_BIT;
        
        body.createFixture(fixtureDef).setUserData(this);
        
        shape.dispose();
    }

    @Override
    public void update(float delta) {
        if (destroyed) {
            return;
        }
        
        if (toBeDestroyed) {
            world.destroyBody(body);
            body = null;
            destroyed = true;
            setSize(0, 0);
            return;
        }
        
        stateTime += delta;

        setRegion(anim.getKeyFrame(stateTime, true));
        setPosition(body.getPosition().x - 8 / GameManager.PPM, body.getPosition().y - 8 / GameManager.PPM);
    }
}
