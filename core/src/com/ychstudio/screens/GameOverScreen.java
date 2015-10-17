package com.ychstudio.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.ychstudio.SuperMario;
import com.ychstudio.gamesys.GameManager;

/**
 * Created by yichen on 10/15/15.
 *
 * GameOverScreen
 */
public class GameOverScreen implements Screen {

    private SuperMario game;
    private Stage stage;

    private float countDown;

    public GameOverScreen(Game game) {
        this.game = (SuperMario) game;
        stage = new Stage(new FitViewport(GameManager.WINDOW_WIDTH / 2, GameManager.WINDOW_HEIGHT /2));

        Label gameOverTextLabel = new Label("Game Over", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(gameOverTextLabel).expand();

        stage.addActor(table);

        countDown = 4.5f;

        GameManager.instance.getAssetManager().finishLoading();
    }

    @Override
    public void show() {
        GameManager.instance.playMusic("game_over.ogg");

    }

    public void update(float delta) {
        countDown -= delta;

        if (countDown < 0.0f) {
            game.setScreen(new PlayScreen(game));
            dispose();
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
        update(delta);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
