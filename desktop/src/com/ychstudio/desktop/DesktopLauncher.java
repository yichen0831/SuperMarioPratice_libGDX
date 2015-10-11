package com.ychstudio.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.ychstudio.SuperMario;
import com.ychstudio.gamesys.GameManager;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = GameManager.WINDOW_WIDTH;
		config.height = GameManager.WINDOW_HEIGHT;
		new LwjglApplication(new SuperMario(), config);
	}
}
