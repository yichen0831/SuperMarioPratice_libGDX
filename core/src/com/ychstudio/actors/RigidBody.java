package com.ychstudio.actors;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.ychstudio.screens.PlayScreen;

/**
 * Created by yichen on 10/11/15.
 *
 * RigidBody
 */
public abstract class RigidBody extends Sprite {
    protected World world;
    protected Body body;

    public RigidBody(PlayScreen playScreen, float x, float y) {
        this.world = playScreen.world;

        setPosition(x, y);
        defBody();
    }

    protected abstract void defBody();

    public void update(float delta) {

    }

    public void onCollide(Collider other) {

    }

    public void onTrigger(Collider other) {

    }


}
