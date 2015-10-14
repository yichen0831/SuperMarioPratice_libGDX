package com.ychstudio.actors.effects;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.ychstudio.actors.RigidBody;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/14/15.
 *
 * Effect
 */
public abstract class Effect extends RigidBody {

    protected TextureAtlas textureAtlas;

    public Effect(PlayScreen playScreen, float x, float y) {
        super(playScreen, x, y);
        this.textureAtlas = playScreen.getTextureAtlas();

    }

}
