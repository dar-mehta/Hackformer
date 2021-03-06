package game.world.level;

import game.entity.Camera;
import game.entity.Console;
import game.entity.ConsoleField;
import game.entity.ConsoleObject;
import game.entity.Monster;
import game.entity.Player;

import java.util.ArrayList;

public class TestLevel extends Level {

	public TestLevel() {
		super("map3.tmx");
	}
	
	public void init() {
		super.init();
		
		Console console;
		
		manager.addEntity(console = new Console(camera));
		
		console.addObject(Player.consoleObject);
		console.addObject(Camera.consoleObject);
		console.addObject(Monster.consoleObject);
		
		ArrayList<ConsoleField<?>> wallFields = new ArrayList<ConsoleField<?>>();
		wallFields.add(ConsoleField.createBooleanField("visible", true));
		wallFields.add(ConsoleField.createBooleanField("solid", true));
		console.addObject(new ConsoleObject("obj_walls", wallFields));
		console.addObject(new ConsoleObject("obj_1", wallFields));
		console.addObject(new ConsoleObject("obj_2", wallFields));
		console.addObject(new ConsoleObject("obj_3", wallFields));
		console.addObject(new ConsoleObject("obj_4", wallFields));
		console.addObject(new ConsoleObject("obj_5", wallFields));
		console.addObject(new ConsoleObject("obj_6", wallFields));
	}

}
