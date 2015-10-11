package com.ychstudio;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ychstudio.screens.PlayScreen;

public class SuperMario extends Game {
	public SpriteBatch batch;

	@Override
	public void create () {
		batch = new SpriteBatch();

		setScreen(new PlayScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}


	@Override
	public void dispose() {
		super.dispose();
	}

}
