package magnileve.chungamod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.IntConsumer;

import org.apache.logging.log4j.Logger;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.lwjgl.input.Keyboard;

import magnileve.chungamod.Commands.Argument;
import magnileve.chungamod.Commands.Arguments;
import magnileve.chungamod.Commands.Command;
import magnileve.chungamod.Commands.CommandArgumentException;
import magnileve.chungamod.Commands.ContainsCommand;
import magnileve.chungamod.Commands.MultiCommandFactory;
import magnileve.chungamod.modules.ChungamodPlugin;
import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Factory;
import magnileve.chungamod.modules.Init;
import magnileve.chungamod.modules.ModuleInfo;
import magnileve.chungamod.modules.Module;
import magnileve.chungamod.settings.GetSetting;
import magnileve.chungamod.settings.Setting;
import magnileve.chungamod.tasks.CoreTask;
import magnileve.chungamod.tasks.Task;
import magnileve.chungamod.tasks.Tasks;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.MCUtil;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.json.JSONConverter;
import magnileve.chungamod.util.json.JSONManager;
import magnileve.chungamod.util.json.JSONUtil;
import net.minecraft.client.settings.KeyBinding;

/**
 * Manages hotkeys that can be set to execute commands when pressed.
 * @author Magnileve
 */
@ChungamodPlugin(id = "Hotkeys", level = ChungamodPlugin.Level.TYPE)
@ModuleInfo(category = "Client", description = "Hotkey manager")
@Setting(name = Setting.ON, type = Boolean.class, value = "true")
@Setting(name = "Hotkeys", type = Hotkeys.HotkeyRecord[].class, description = "Use hotkeyadd and hotkeyrm commands to add or remove hotkeys",
		value = "[{\"keyCode\":21,\"commands\":[\"toggle ClickGUI\"]}]")
@ContainsInit
@ContainsCommand
public class Hotkeys implements Module, Runnable, TickListener {

private static final ArrayBuildList<KeyBinding> KEY_BINDINGS = new ArrayBuildList<>(new KeyBinding[0]);

private static Hotkeys instance;

private final Logger log;
private final List<Hotkey> hotkeys;

private Task setKeybindListener;
private boolean newSettingDetection = true;

@Init.PreInit1
static void init() {
	JSONManager m = Chung.US.getJSONManager();
	m.addConverter(HotkeyRecord.class, new JSONConverter<HotkeyRecord>() {
		@Override
		public void serialize(JSONWriter w, HotkeyRecord obj) {
			w.object().key("keyCode").value(obj.keyCode).key("commands");
			m.serialize(w, String[].class, obj.commands);
			w.endObject();
		}
		
		@Override
		public HotkeyRecord deserialize(JSONTokener p) {
			int keyCode = -1;
			String[] commands = null;
			for(String key:JSONUtil.iterateJSONObject(p)) switch(key) {
			case "keyCode":
				keyCode = JSONUtil.nextValue(p, Integer.class);
				break;
			case "commands":
				commands = m.deserialize(p, String[].class);
				break;
			default:
				p.nextValue();
			}
			if(keyCode == -1 || commands == null) throw p.syntaxError("Unable to find key code and commands");
			return new HotkeyRecord(keyCode, commands);
		}
	}, new HotkeyRecord(0, Util.STRING_ARRAY_0));
}

@Factory
Hotkeys(Logger log, @GetSetting("Hotkeys") Optional<HotkeyRecord[]> savedHotkeys) {
	this.log = log;
	if(savedHotkeys.isPresent()) {
		HotkeyRecord[] array = savedHotkeys.get();
		hotkeys = new ArrayList<>(array.length);
		for(HotkeyRecord hotkeyRecord:array) if(hotkeyRecord.commands.length != 0)
			hotkeys.add(new UninitializedHotkey(hotkeyRecord, KEY_BINDINGS));
		KEY_BINDINGS.trim();
	} else hotkeys = new ArrayList<>(0);
}

@Override
public void run() {
	if(hotkeys.size() != 0 && hotkeys.get(0) instanceof UninitializedHotkey) {
		ListIterator<Hotkey> iter = hotkeys.listIterator();
		while(iter.hasNext()) iter.set(((UninitializedHotkey) iter.next()).initialize());
	}
	Tick.MAIN.add(this, 0);
	instance = this;
}

@Override
public void disable() {
	if(setKeybindListener != null) setKeybindListener.cancel();
	Tick.MAIN.remove(this);
	instance = null;
}

@Override
public void softDisable() {
	if(setKeybindListener != null) setKeybindListener.softCancel();
	Tick.MAIN.remove(this);
	instance = null;
}

@Override
public int onTick() {
	for(Hotkey hotkey:hotkeys) if(hotkey.keyBinding.isKeyDown()) {
		if(!hotkey.pressed) {
			hotkey.pressed = true;
			for(Runnable run:hotkey.runs) run.run();
		}
	} else if(hotkey.pressed) hotkey.pressed = false;
	return 1;
}

/**
 * Notifies an {@link IntConsumer} with the key code of the next pressed key.
 * @param run the consumer
 */
private void listen(IntConsumer run) {
	log.info("Listening for key");
	if(setKeybindListener != null) setKeybindListener.cancel();
	setKeybindListener = Tasks.newTask(CoreTask.EMPTY, () -> {
		while(Keyboard.next()) if(Keyboard.getEventKeyState()) {
			int keyCode = Keyboard.getEventKey();
			Tick.MAIN.add(() -> {
				run.accept(keyCode);
				return -1;
			});
			return -1;
		}
		return 1;
	}, TickListener.CURRENT, null, null);
	setKeybindListener.run();
}

/**
 * Binds a command to the next pressed key.
 * @param command the command
 */
private void addHotkey(String command) {
	listen(keyCode -> {
		addRunnable(keyCode, command);
		HotkeyRecord[] settings = (HotkeyRecord[]) get("Hotkeys");
		int i = 0;
		for(; i < settings.length && keyCode != settings[i].keyCode; i++);
		if(i == settings.length) {
			settings = Arrays.copyOf(settings, i + 1);
			settings[i] = new HotkeyRecord(keyCode, new String[] {command});
		} else {
			String[] commands = Arrays.copyOf(settings[i].commands, settings[i].commands.length + 1);
			commands[commands.length - 1] = command;
			settings[i] = new HotkeyRecord(keyCode, commands);
		}
		newSettingDetection = false;
		set(settings, "Hotkeys");
		newSettingDetection = true;
		log.info("Hotkey set to " + Keyboard.getKeyName(keyCode));
	});
}

/**
 * Removes a command from the next pressed key.
 * @param index the index of the command bound to the next pressed key
 */
private void removeHotkey(int index) {
	listen(keyCode -> {
		try {
			removeRunnable(keyCode, index);
		} catch(IllegalArgumentException | IndexOutOfBoundsException e) {
			log.info(e.getMessage());
			return;
		}
		HotkeyRecord[] settings = (HotkeyRecord[]) get("Hotkeys");
		int i = 0;
		for(; keyCode != settings[i].keyCode; i++);
		if(settings[i].commands.length == 1) {
			HotkeyRecord[] newSettings = new HotkeyRecord[settings.length - 1];
			for(int h = 0; h < i; h++) newSettings[h] = settings[h];
			for(int h = i + 1; h < newSettings.length; h++) newSettings[h] = settings[h + 1];
			settings = newSettings;
		} else {
			String[] newCommands = new String[settings[i].commands.length - 1];
			for(int h = 0; h < i; h++) newCommands[h] = settings[i].commands[h];
			for(int h = i + 1; h < newCommands.length; h++) newCommands[h] = settings[i].commands[h + 1];
			settings[i] = new HotkeyRecord(keyCode, newCommands);
		}
		newSettingDetection = false;
		set(settings, "Hotkeys");
		newSettingDetection = true;
		log.info("Hotkey removed");
	});
}

/**
 * Lists commands bound to the next pressed key.
 */
private void listHotkey() {
	listen(keyCode -> {
		if(!hotkeys.isEmpty()) for(ListIterator<Hotkey> iter = hotkeys.listIterator(hotkeys.size()); iter.hasPrevious();) {
			Hotkey h = iter.previous();
			if(h.keyBinding.getKeyCode() == keyCode) {
				StringBuilder str = new StringBuilder("----- Commands for hotkey ").append(Keyboard.getKeyName(keyCode)).append(" -----");
				for(int i = 0; i < h.runs.length; i++) str.append('\n').append(i).append(": ").append(h.runs[i]);
				log.info(str.toString());
				return;
			}
		}
		log.info("No commands for hotkey " + Keyboard.getKeyName(keyCode));
	});
}

@Override
public void onNewSetting(String[] settingPath, Object value) {
	if(newSettingDetection) restart();
}

/**
 * If enabled, runs the {@code Runnable}; otherwise, displays a message in chat saying that hotkeys are disabled.
 * @param run
 */
private static void ifEnabled(Runnable run) {
	if(instance == null) MCUtil.sendMessage("Hotkeys disabled");
	else run.run();
}

/**
 * Generates a {@link Runnable} to execute a command and binds it to a key code;
 * @param keyCode the key code
 * @param command the command
 * @throws CommandArgumentException if the command is not able to be executed with the given arguments
 */
private void addRunnable(int keyCode, String command) throws CommandArgumentException {
	Hotkey h = null;
	for(int i = hotkeys.size() - 1; i >= 0; i--) {
		Hotkey h1 = hotkeys.get(i);
		if(h1.keyBinding.getKeyCode() == keyCode) {
			h = h1;
			break;
		}
	}
	if(h == null) {
		h = new Hotkey(Hotkey.getKeyBinding(keyCode, KEY_BINDINGS), new Runnable[] {Commands.onCommand(command, false)});
		KEY_BINDINGS.trim();
		hotkeys.add(h);
	} else {
		Runnable[] prevCommands = h.runs;
		h.runs = Arrays.copyOf(h.runs, h.runs.length + 1);
		h.runs[prevCommands.length] = Commands.onCommand(command, false);
	}
}

/**
 * Removes a command {@link Runnable} from a key code.
 * @param keyCode the key code
 * @param index the index of the command
 * @throws IllegalArgumentException if no commands are bound to {@code keyCode}
 * @throws IndexOutOfBoundsException if no command exists at {@code index} for {@code keyCode}
 */
private void removeRunnable(int keyCode, int index) throws IllegalArgumentException, IndexOutOfBoundsException {
	Hotkey h = null;
	for(int i = hotkeys.size() - 1; i >= 0; i--) {
		Hotkey h1 = hotkeys.get(i);
		if(h1.keyBinding.getKeyCode() == keyCode) {
			h = h1;
			break;
		}
	}
	if(h == null)
		throw new IllegalArgumentException("Hotkey not registered");
	if(index < 0 || index >= h.runs.length)
		throw new IndexOutOfBoundsException("Index must be from 0 to " + (h.runs.length - 1));
	Runnable[] getCommands = new Runnable[h.runs.length - 1];
	int i = 0;
	for(; i < index; i++) getCommands[i] = h.runs[i];
	for(; i < getCommands.length; i++) getCommands[i] = h.runs[i + 1];
	h.runs = getCommands;
}

/**
 * Gets all key bindings that a command is bound to.  This method is not case sensitive.
 * @param command the command
 * @return the list of key bindings
 */
public static ArrayBuildList<KeyBinding> getKeyBindingsForCommand(String command) {
	HotkeyRecord[] records = (HotkeyRecord[]) Chung.US.get(Chung.US.getModule(Hotkeys.class), "Hotkeys");
	ArrayBuildList<KeyBinding> keyBindings = new ArrayBuildList<>(new KeyBinding[1]);
	for(HotkeyRecord key:records) for(String checkCommand:key.commands) if(command.equalsIgnoreCase(checkCommand)) {
		keyBindings.add(Hotkey.getKeyBinding(key.keyCode, KEY_BINDINGS));
		break;
	}
	return keyBindings;
}

@MultiCommandFactory(names = {"hotkeyadd", "hotkeyrm", "hotkeylist"},
	descriptions = {"Set a hotkey for a command.", "Remove a hotkey.", "List commands for a hotkey."},
	argsArray = {
		@Arguments({@Argument(index = 0, name = "Command", description = "Command, not including prefix.")}),
		@Arguments({@Argument(index = 0, name = "Index", description = "Index of registered command")}),
		@Arguments({})
	})
static Command[] hotkeyCommands() {
	return new Command[] {
		args -> {
			String hotkeyCommand = args[0];
			return () -> ifEnabled(() -> instance.addHotkey(hotkeyCommand));
		},
		args -> {
			int index;
			try {
				index = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				throw new CommandArgumentException("Index argument must be an integer");
			}
			return () -> ifEnabled(() -> instance.removeHotkey(index));
		},
		args -> () -> ifEnabled(() -> instance.listHotkey())
	};
}

/**
 * Represents a key binding with command Runnables to be executed.
 * @author Magnileve
 */
private static class Hotkey {
	private final KeyBinding keyBinding;
	
	private Runnable[] runs;
	private boolean pressed;
	
	private Hotkey(KeyBinding keyBinding, Runnable[] runs) {
		this.keyBinding = keyBinding;
		this.runs = runs;
	}
	
	private static KeyBinding getKeyBinding(int keyCode, ArrayBuildList<KeyBinding> keyBindings) {
		KeyBinding[] array = keyBindings.getArray();
		for(int i = 0, l = keyBindings.size(); i < l; i++) {
			KeyBinding keyBinding = array[i];
			if(keyBinding.getKeyCode() == keyCode) return keyBinding;
		}
		KeyBinding keyBinding = new KeyBinding("Chungamod hotkey", keyCode, "key.categories.misc");
		keyBindings.add(keyBinding);
		return keyBinding;
	}
}

/**
 * Transfer class from {@link HotkeyRecord} to {@link Hotkey} used during initialization.
 * @author Magnileve
 */
private static class UninitializedHotkey extends Hotkey {
	private final HotkeyRecord hotkeyRecord;
	
	private UninitializedHotkey(HotkeyRecord hotkeyRecord, ArrayBuildList<KeyBinding> keyBindings) {
		super(Hotkey.getKeyBinding(hotkeyRecord.keyCode, keyBindings), null);
		this.hotkeyRecord = hotkeyRecord;
	}
	
	private Hotkey initialize() throws CommandArgumentException {
		Runnable[] runs = new Runnable[hotkeyRecord.commands.length];
		for(int i = 0; i < runs.length; i++) runs[i] = Commands.onCommand(hotkeyRecord.commands[i], false);
		return new Hotkey(super.keyBinding, runs);
	}
}

/**
 * Maps a key code to commands intended to be executed when the key is pressed.
 * @author Magnileve
 */
static class HotkeyRecord {
	private final int keyCode;
	private final String[] commands;
	
	private HotkeyRecord(int keyCode, String[] commands) {
		this.commands = commands;
		this.keyCode = keyCode;
	}
	
	@Override
	public String toString() {
		return "keyCode " + keyCode + ": " + Arrays.toString(commands);
	}
}

}