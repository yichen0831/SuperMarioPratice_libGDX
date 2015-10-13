package com.ychstudio.actors.items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/13/15.
 *
 * Mushroom
 */
public class Mushroom extends Item {
    public Mushroom(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);

        setRegion(new TextureRegion(playScreen.getTextureAtlas().findRegion("Mushroom"), 0, 0, 16, 16));
        setBounds(body.getPosition().x - 8 / GameManager.PPM, body.getPosition().y - 8 / GameManager.PPM, 16 / GameManager.PPM, 16 / GameManager.PPM);

        name = "mushroom";
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
