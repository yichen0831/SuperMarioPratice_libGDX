package com.ychstudio.actors.enemies;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.ychstudio.actors.Mario;
import com.ychstudio.actors.RigidBody;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/13/15.
 *
 * Enemy
 */
public abstract class Enemy extends RigidBody {

    protected TextureAtlas textureAtlas;

    protected boolean active = false;

    public Enemy(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
        this.textureAtlas = playScreen.getTextureAtlas();
    }

    public abstract void getDamage(int damage);

    public void interact(Mario mario) {

    }

}
