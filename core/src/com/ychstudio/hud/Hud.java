package com.ychstudio.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.ychstudio.gamesys.GameManager;

/**
 * Created by yichen on 10/12/15.
 *
 * Hud
 */
public class Hud implements Disposable {

    private Stage stage;

    private int timeLeft;

    private Label scoreLabel;
    private Label timeLabel;
    private Label levelLabel;

    private boolean showFPS;
    private Label fpsLabel;

    private float fpsTimeAccumulator;
    private float accumulator;

    public Hud(SpriteBatch batch) {

        Viewport viewport = new FitViewport(GameManager.WINDOW_WIDTH / 2, GameManager.WINDOW_HEIGHT / 2, new OrthographicCamera());
        stage = new Stage(viewport, batch);

        timeLeft = 300;

        Label scoreTextLabel;
        Label timeTextLabel;
        Label levelTextLabel;

        scoreTextLabel = new Label("SCORE", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        timeTextLabel = new Label("TIME", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        levelTextLabel = new Label("LEVEL", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        scoreLabel = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        timeLabel = new Label(intToString(timeLeft, 3), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        levelLabel = new Label("1-1", new Label.LabelStyle(new BitmapFont(), Color.WHITE));


        Table table = new Table();
        table.top();
        table.setFillParent(true);

        table.add(scoreTextLabel).expandX().padTop(6.0f);
        table.add(levelTextLabel).expandX().padTop(6.0f);
        table.add(timeTextLabel).expandX().padTop(6.0f);

        table.row();

        table.add(scoreLabel).expandX();
        table.add(levelLabel).expandX();
        table.add(timeLabel).expandX();

        table.row();

        // FPS
        fpsLabel = new Label("FPS:    ", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        Table fpsTable = new Table();
        fpsTable.add(fpsLabel);
        table.add(fpsTable).expand().bottom();

        stage.addActor(table);

        accumulator = 0;
        fpsTimeAccumulator = 0;
        showFPS = false;
    }

    public void setLevel(String level) {
        levelLabel.setText(level);
    }

    public boolean isShowFPS() {
        return showFPS;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setShowFPS(boolean value) {
        showFPS = value;
    }

    public void draw() {
        scoreLabel.setText(intToString(GameManager.instance.getScore(), 6));
        stage.draw();

    }

    public void update(float delta) {
        accumulator += delta;

        fpsLabel.setVisible(showFPS);

        if (showFPS) {
            fpsTimeAccumulator += delta;
            if (fpsTimeAccumulator > 0.2) {
                fpsLabel.setText("FPS: " + intToString((int) (1 / delta * GameManager.timeScale), 3));
                fpsTimeAccumulator = 0;
            }
        }

        if (accumulator > 1.0f) {
            if (timeLeft > 0)
                timeLeft -= 1;
            accumulator -= 1.0f;
            timeLabel.setText(intToString(timeLeft, 3));
        }


    }

    private String intToString(int value, int length) {
        String valueStr = Integer.toString(value);
        StringBuilder result = new StringBuilder();
        if (valueStr.length() < length) {
            for (int i = 0; i < length - valueStr.length(); i++) {
                result.append(0);
            }
        }
        result.append(valueStr);
        return result.toString();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
