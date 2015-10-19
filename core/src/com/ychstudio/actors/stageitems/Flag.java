package com.ychstudio.actors.stageitems;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/18/15.
 *
 * Flag
 */
public class Flag extends Sprite {

    public Flag(PlayScreen playScreen, float x, float y) {
        setRegion(new TextureRegion(playScreen.getTextureAtlas().findRegion("Flag"), 0, 0, 16, 16));
        setBounds(x, y, 16 / GameManager.PPM, 16 / GameManager.PPM);
    }

}
