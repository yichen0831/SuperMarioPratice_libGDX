package com.ychstudio.actors.maptiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ychstudio.actors.RigidBody;
import com.ychstudio.gamesys.GameManager;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * MapTileObject
 */
public abstract class MapTileObject extends RigidBody {
    public MapTileObject(PlayScreen playScreen, float x, float y, TextureRegion textureRegion) {
        super(playScreen, x, y);

        setRegion(textureRegion);

        float width = 16 / GameManager.PPM;
        float height = 16 / GameManager.PPM;

        setBounds(x - width / 2, y - height / 2, width, height);
    }
}
