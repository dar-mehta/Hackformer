package game.entity;

import game.world.KeyHandler;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class Console extends Entity {

	private static final float HEIGHT = 394;
	private static final float TEXT_X_OFFS = 200;
	private static final float TEXT_Y_OFFS = 75;
	private static final float LINE_X_OFFS = 76;
	private static final float LINE_WIDTH_OFFS = 152;
	private static final float VALUE_X_OFFS = 125;
	private static final int SCISSOR_Y_OFFS = 99;
	private static final int SCISSOR_HEIGHT_OFFS = 200;

	private static final float CONSOLE_MOVE_ACCELERATION = 1f;

	private static final Color DEFAULT_TEXT_COLOR = new Color(1, 1, 1, 1);
	private static final Color HACKED_OBJECT_COLOR = new Color(1, 1, 0, 1);

	private static final ShapeRenderer sr = new ShapeRenderer();

	private static Texture console = new Texture("textures/terminal.png");

	private ArrayList<ConsoleObject> objects = new ArrayList<ConsoleObject>();
	private ConsoleObject selectedObject = null;

	private SpriteBatch batch = new SpriteBatch();
	private BitmapFont font = new BitmapFont(Gdx.files.internal("fonts/console font.fnt"), Gdx.files.internal("fonts/console font.png"), false);

	private boolean active = false;
	private int selectedLine = 0;

	private int numHacks = 0, maxHacks = 1;
	private int lineOffs = 0;
	private float yOffs = 0, maxYOffs, yOffsMoveSpeed = 0;

	public Console(Camera camera) {
		super(new Rectangle(0, camera.bounds.height - HEIGHT, camera.bounds.width, HEIGHT));
		maxYOffs = camera.bounds.height - bounds.y;
		yOffs = maxYOffs;
	}

	public void update(Camera camera, float dt) {
		super.update(camera, dt);

		toggleActive();
		moveYOffs(camera);

		if (!active)
			return;

		moveSelectedLine();
		selectItem();
		moveLineOffs();
		updateHacksCount();
	}

	private void moveYOffs(Camera camera) {
		yOffsMoveSpeed += CONSOLE_MOVE_ACCELERATION;

		if (!active)
			yOffs += yOffsMoveSpeed;
		else
			yOffs -= yOffsMoveSpeed;

		if (yOffs < 0)
			yOffs = 0;

		if (yOffs > maxYOffs)
			yOffs = maxYOffs;

		bounds.y = camera.bounds.height - HEIGHT + yOffs;
	}

	private void moveLineOffs() {
		if (selectedObject != null)
			lineOffs = Math.max(0, Math.min(selectedLine, selectedObject.fields.size() - 3) - 3);
		else
			lineOffs = Math.max(0, Math.min(selectedLine, objects.size() - 4) - 3);
	}

	private void toggleActive() {
		if (KeyHandler.keyClicked(Input.Keys.Q) || KeyHandler.keyClicked(Input.Keys.NUMPAD_0)) {
			active = !active;
			yOffsMoveSpeed = 0;
			
			if (active) {
				selectedObject = null;
				selectedLine = 0;
			}
		}
	}

	private void moveSelectedLine() {
		if (KeyHandler.keyClicked(Input.Keys.W) || KeyHandler.keyClicked(Input.Keys.UP))
			moveSelectedLine(true);
		if (KeyHandler.keyClicked(Input.Keys.S) || KeyHandler.keyClicked(Input.Keys.DOWN))
			moveSelectedLine(false);
	}

	private void moveSelectedLine(boolean up) {
		if (up)
			selectedLine--;
		else
			selectedLine++;

		if (selectedObject != null) {
			if (selectedLine < 0)
				selectedLine = selectedObject.fields.size();
			else if (selectedLine > selectedObject.fields.size())
				selectedLine = 0;
		} else {
			if (selectedLine < 0)
				selectedLine = objects.size() - 1;
			else if (selectedLine >= objects.size())
				selectedLine = 0;
		}
	}

	private void selectItem() {
		if (selectedObject == null && (KeyHandler.keyClicked(Input.Keys.D) || KeyHandler.keyClicked(Input.Keys.RIGHT) || KeyHandler.keyClicked(Input.Keys.A) || KeyHandler.keyClicked(Input.Keys.LEFT))) {
			selectedObject = objects.get(selectedLine);
			selectedLine = lineOffs = 0;
			return;
		}

		if (selectedObject == null)
			return;

		if (KeyHandler.keyClicked(Input.Keys.A) || KeyHandler.keyClicked(Input.Keys.LEFT)) {
			if (selectedLine == 0) {
				selectedLine = objects.indexOf(selectedObject);
				selectedObject = null;
				return;
			}

			if (numHacks < maxHacks || selectedObject.fields.get(selectedLine - 1).isChanged())
				selectedObject.fields.get(selectedLine - 1).moveLeft();
		}
		if (selectedLine != 0 && (KeyHandler.keyClicked(Input.Keys.D) || KeyHandler.keyClicked(Input.Keys.RIGHT)))
			if (numHacks < maxHacks || selectedObject.fields.get(selectedLine - 1).isChanged())
				selectedObject.fields.get(selectedLine - 1).moveRight();
	}

	private void updateHacksCount() {
		numHacks = 0;

		for (int i = 0; i < objects.size(); i++)
			for (int j = 0; j < objects.get(i).fields.size(); j++)
				if (objects.get(i).fields.get(j).isChanged())
					numHacks++;
	}

	public void render(Camera camera, SpriteBatch batch) {
		super.render(camera, batch);

		if (!isActive())
			return;

		drawBackground(camera);

		drawSelectedLine(camera);

		if (selectedObject == null)
			drawObjectsList(camera);
		else
			drawObjectFields(selectedObject, camera);

		drawHacksCount(camera);

	}

	private void drawBackground(Camera camera) {
		batch.begin();
		batch.draw(console, 0, bounds.y, bounds.width, bounds.height);
		batch.end();
	}

	private void drawSelectedLine(Camera camera) {
		sr.begin(ShapeType.Filled);
		sr.setColor(.1f, .8f, .1f, 1);

		sr.rect(bounds.x + LINE_X_OFFS, bounds.y + bounds.height - (selectedLine + 1 - lineOffs) * font.getLineHeight() - TEXT_Y_OFFS - font.getLineHeight() + 2, bounds.width - LINE_WIDTH_OFFS, font.getLineHeight());
		sr.end();
	}

	private void drawObjectsList(Camera camera) {
		batch.begin();
		font.setColor(DEFAULT_TEXT_COLOR);

		font.draw(batch, "<objects list>", bounds.x + TEXT_X_OFFS / 2, bounds.y - TEXT_Y_OFFS + bounds.height);

		batch.end();
		batch.begin();

		Gdx.gl20.glEnable(GL20.GL_SCISSOR_TEST);
		Gdx.gl20.glScissor((int) bounds.x, (int) (bounds.y + SCISSOR_Y_OFFS), (int) bounds.width, (int) bounds.height - SCISSOR_HEIGHT_OFFS);

		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).isChanged())
				font.setColor(HACKED_OBJECT_COLOR);
			else
				font.setColor(DEFAULT_TEXT_COLOR);

			font.draw(batch, objects.get(i).name, bounds.x + TEXT_X_OFFS, bounds.y + bounds.height - (i + 1 - lineOffs) * font.getLineHeight() - TEXT_Y_OFFS);
		}

		batch.end();

		Gdx.gl20.glDisable(GL20.GL_SCISSOR_TEST);
	}

	private void drawObjectFields(ConsoleObject selectedObject, Camera camera) {
		batch.begin();

		font.setColor(DEFAULT_TEXT_COLOR);
		font.draw(batch, "<variables of " + selectedObject.name + ">", bounds.x + TEXT_X_OFFS / 2, bounds.y + bounds.height - TEXT_Y_OFFS);
		batch.end();
		batch.begin();

		Gdx.gl20.glEnable(GL20.GL_SCISSOR_TEST);
		Gdx.gl20.glScissor((int) bounds.x, (int) (bounds.y + SCISSOR_Y_OFFS), (int) bounds.width, (int) bounds.height - SCISSOR_HEIGHT_OFFS);

		if (lineOffs == 0)
			font.draw(batch, "<..", bounds.x + TEXT_X_OFFS, bounds.y + bounds.height - TEXT_Y_OFFS - font.getLineHeight());

		for (int i = 0; i < selectedObject.fields.size(); i++) {
			String name = selectedObject.fields.get(i).getName();
			String value = selectedObject.fields.get(i).getSelectedValue().toString();

			if (selectedObject.fields.get(i).isChanged())
				font.setColor(HACKED_OBJECT_COLOR);
			else
				font.setColor(DEFAULT_TEXT_COLOR);

			float x = bounds.x + TEXT_X_OFFS;
			float y = bounds.y + bounds.height - (i + 2 - lineOffs) * font.getLineHeight() - TEXT_Y_OFFS;

			font.draw(batch, name, x, y);
			font.draw(batch, value, x + VALUE_X_OFFS, y);
		}

		batch.end();

		Gdx.gl20.glDisable(GL20.GL_SCISSOR_TEST);
	}

	private void drawHacksCount(Camera camera) {
		batch.begin();

		if (numHacks >= maxHacks)
			font.setColor(HACKED_OBJECT_COLOR);
		else
			font.setColor(DEFAULT_TEXT_COLOR);

		font.draw(batch, "hacks (" + numHacks + " / " + maxHacks + ")", camera.bounds.width - 200, bounds.y + bounds.height - TEXT_Y_OFFS);
		batch.end();
	}

	public void addObject(ConsoleObject object) {
		objects.add(object);
	}

	public boolean isActive() {
		return active || yOffs < maxYOffs;
	}
}
