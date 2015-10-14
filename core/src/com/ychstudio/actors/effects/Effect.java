package com.ychstudio.actors.effects;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/14/15.
 *
 * Effect
 */
public abstract class Effect extends Sprite {

    protected PlayScreen playScreen;
    protected World world;
    protected Body body;
    protected TextureAtlas textureAtlas;

    protected boolean toBeDestroyed;
    protected boolean destroyed;

    public Effect(PlayScreen playScreen, float x, float y) {
        this.playScreen = playScreen;
        this.world = playScreen.world;
        this.textureAtlas = playScreen.getTextureAtlas();

        setPosition(x, y);

        toBeDestroyed = false;
        destroyed = false;

        defBody();
    }

    protected abstract void defBody();
    public abstract void update(float delta);

    public void queueDestroy() {
        toBeDestroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
