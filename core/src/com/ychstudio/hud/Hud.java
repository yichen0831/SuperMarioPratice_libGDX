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

    private int time;
    private int level;

    private Label scoreTextLabel;
    private Label timeTextLabel;
    private Label levelTextLabel;

    private Label scoreLabel;
    private Label timeLable;
    private Label levelLabel;

    private float accumulator;

    public Hud(SpriteBatch batch) {

        Viewport viewport = new FitViewport(GameManager.WINDOW_WIDTH / 2, GameManager.WINDOW_HEIGHT / 2, new OrthographicCamera());
        stage = new Stage(viewport, batch);

        time = 300;
        level = 1;

        scoreTextLabel = new Label("SCORE", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        timeTextLabel = new Label("TIME", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        levelTextLabel = new Label("LEVEL", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        scoreLabel = new Label("", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        timeLable = new Label(String.format("%03d", time), new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        levelLabel = new Label(String.format("%02d", level), new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        table.add(scoreTextLabel).expandX().padTop(6.0f);
        table.add(levelTextLabel).expandX().padTop(6.0f);
        table.add(timeTextLabel).expandX().padTop(6.0f);

        table.row();

        table.add(scoreLabel).expandX();
        table.add(levelLabel).expandX();
        table.add(timeLable).expandX();

        stage.addActor(table);

        accumulator = 0;
    }

    public void draw() {
        scoreLabel.setText(String.format("%06d", GameManager.instance.getScore()));
        stage.draw();
    }

    public void update(float delta) {
        accumulator += delta;

        if (accumulator > 1.0f) {
            time -= 1;
            accumulator -= 1.0f;
            timeLable.setText(String.format("%03d", time));
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
