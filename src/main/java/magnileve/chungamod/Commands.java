package magnileve.chungamod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import magnileve.chungamod.Commands.ContainsCommand;
import magnileve.chungamod.events.EventListener;
import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Factory;
import magnileve.chungamod.modules.GetLogger;
import magnileve.chungamod.modules.ModuleInfo;
import magnileve.chungamod.modules.Module;
import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.settings.GetSetting;
import magnileve.chungamod.settings.Setting;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.MCUtil;
import magnileve.chungamod.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.client.event.ClientChatEvent;

/**
 * Manages the Chungamod command system and contains nested types for adding commands.
 * @author Magnileve
 * @see CommandFactory
 */
@Mod.EventBusSubscriber(modid=Chung.MODID)
@ModuleInfo(category = "Client", description = "Send client commands using the chat bar", alwaysInstantiate = true)
@EventListener(Chung.SCOPE_SINGLETON)
@ContainsInit
@ContainsCommand
@Setting(name = Setting.ON, type = Boolean.class, value = "true")
@Setting(name = "Prefix", type = String.class, value = "\",\"", limits = "matches:\\S*")
@Setting(name = "Debug", type = Boolean.class, value = "false")
public class Commands implements Module {

private static final Map<String, CommandRecord> COMMANDS = new HashMap<>();
private static final Map<String, CommandRecord> DEBUG_COMMANDS = new HashMap<>();

private static Minecraft mc;
private static Logger log;
private static Commands instance;

private String prefix;
private boolean debug;

@Factory
static Commands factory(Minecraft mcIn, @GetLogger(chatLevel = StandardLevel.WARN) Logger logIn,
		@GetSetting("Prefix") String prefix, @GetSetting("Debug") Boolean debug, @GetSetting(Setting.ON) Boolean on) {
	mc = mcIn;
	log = logIn;
	instance = new Commands(prefix, debug);
	if(!on) instance.set(true, Chung.SETTING_PATH_ON);
	return instance;
}

private Commands(String prefix, boolean debug) {
	this.prefix = prefix;
	this.debug = debug;
}

/**
 * Loads commands into the command system from command factory methods.
 * @param factoryMethods contains pairs of class names and {@link Method} references to be invoked
 * @param log debug logger
 */
public static void loadCommands(Iterable<Bucket<String, Method>> factoryMethods, Logger log) {
	for(Bucket<String, Method> bucket:factoryMethods) {
		Method method = bucket.getE2();
		try {
			StringBuilder getLogMessage = log.isDebugEnabled() ? new StringBuilder("- For class ").append(bucket.getE1()).append(": ") : null;
			if(method.getReturnType().equals(Command.class)) {
				CommandFactory a = method.getAnnotation(CommandFactory.class);
				if(log.isDebugEnabled()) log.debug(getLogMessage.append(a.name()).toString());
				Argument[] args = method.getAnnotationsByType(Argument.class);
				String[] argNames = new String[args.length];
				String[] argDescriptions = new String[args.length];
				for(Argument arg:args) {
					argNames[arg.index()] = arg.name();
					argDescriptions[arg.index()] = arg.description();
				}
				Map<String, CommandRecord> commandMap = a.debug() ? DEBUG_COMMANDS : COMMANDS;
				commandMap.put(a.name(), new CommandRecord((Command) method.invoke(null), a.description(), argNames, argDescriptions, a.limitArgs()));
			} else {
				MultiCommandFactory a = method.getAnnotation(MultiCommandFactory.class);
				String[] names = a.names();
				if(log.isDebugEnabled()) {
					getLogMessage.append(names[0]);
					for(int i = 1; i < names.length; i++) getLogMessage.append(", ").append(names[i]);
					log.debug(getLogMessage.toString());
				}
				Command[] runs = (Command[]) method.invoke(null);
				if(names.length != runs.length)
					throw new IllegalArgumentException("Command registration method returns a different amount of commands than listed in annotation\nCommands in annotation: "
						+ names.length + "Commands returned:" + runs.length);
				String[] descriptions = a.descriptions();
				boolean[] limitArgsArray = a.limitArgsForEach();
				if(limitArgsArray.length == 0) {
					limitArgsArray = new boolean[names.length];
					for(int i = 0; i < limitArgsArray.length; i++) limitArgsArray[i] = true;
				}
				Arguments[] argsArray = a.argsArray();
				if(names.length != descriptions.length || names.length != argsArray.length || names.length != limitArgsArray.length)
					throw new IllegalArgumentException("Command registration does not register equal amounts of " +
							"names, descriptions, argument arrays, and limit-argument booleans\nNames:" +
							names.length + ", Descriptions: " + descriptions.length + ", Argument arrays: " +
							argsArray.length + ", Limit-argument booleans: " + limitArgsArray.length);
				CommandRecord[] fullCommands = new CommandRecord[argsArray.length];
				for(int i = 0; i < argsArray.length; i++) {
					Argument[] args = argsArray[i].value();
					String[] argNames = new String[args.length];
					String[] argDescriptions = new String[args.length];
					for(Argument arg:args) {
						argNames[arg.index()] = arg.name();
						argDescriptions[arg.index()] = arg.description();
					}
					fullCommands[i] = new CommandRecord(runs[i], descriptions[i], argNames, argDescriptions, limitArgsArray[i]);
				}
				Map<String, CommandRecord> commandMap = a.debug() ? DEBUG_COMMANDS : COMMANDS;
				for(int i = 0; i < names.length; i++) commandMap.put(names[i], fullCommands[i]);
			}
		} catch(Exception e) {
			throw new RuntimeException("Exception creating commands from " + method.getDeclaringClass().getName() + " method " + method.getName(), e);
		}
	}
}

@SubscribeEvent
@SideOnly(value = Side.CLIENT)
public static void onClientChatEvent(ClientChatEvent event) {
	if(event.getMessage().startsWith(instance.prefix)) {
		String message = event.getMessage();
		log.info("Chungamod command called: " + message);
		event.setCanceled(true);
		mc.ingameGUI.getChatGUI().addToSentMessages(message);
		Runnable run;
		try {
			run = onCommand(message, true);
		} catch(CommandArgumentException e) {
			MCUtil.sendMessage(e.getMessage());
			return;
		}
		run.run();
	}
}

/**
 * Parses and prepares a command for execution.
 * @param command The command string.  Each argument is separated by a space.
 * An argument surrounded by double quotes can contain spaces.
 * @param hasPrefix the command prefix is before the command name
 * @return a {@link Runnable} to execute the command.
 * @throws CommandArgumentException if the command is not able to be executed with the given arguments
 */
public static Runnable onCommand(String command, boolean hasPrefix) throws CommandArgumentException {
	int start = hasPrefix ? instance.prefix.length() : 0;
	int end = command.indexOf(' ', start);
	if(end == -1) return instance.onCommand(command.substring(start), Util.STRING_ARRAY_0);
	else {
		String name = command.substring(start, end);
		List<String> getArgs = new ArrayList<>(4);
		for(start = end + 1; (end = command.indexOf(' ', start)) != -1; start = end + 1)
			end = addCommandArgument(getArgs, command, start, end);
		addCommandArgument(getArgs, command, start, command.length());
		String[] args = new String[getArgs.size()];
		for(int i = 0; i < args.length; i++) args[i] = getArgs.get(i);
		return instance.onCommand(name, args);
	}
}

private static int addCommandArgument(List<String> args, String command, int start, int end) throws CommandArgumentException {
	if(start != end) if(command.charAt(start) == '\"') {
		if(end - start == 1) end = command.indexOf(' ', end + 1);
		while(end != -1 && (command.charAt(end - 1) != '\"' || command.charAt(end - 2) == '\\'))
			end = command.indexOf(' ', end + 1);
		if(end == -1) {
			end = command.length();
			if(command.charAt(end - 1) != '\"') throw new CommandArgumentException("Improper quote format");
		}
		StringBuilder str = new StringBuilder(end-- - start++ - 2);
		while(start < end) {
			char c = command.charAt(start);
			if(c == '\\') {
				start++;
				if(start == end) throw new CommandArgumentException("Improper quote format");
				c = command.charAt(start);
				if(!(c == '\\' || c == '\"')) throw new CommandArgumentException("Improper quote format");
			}
			str.append(c);
			start++;
		}
		args.add(str.toString());
	} else args.add(command.substring(start, end));
	return end;
}

/**
 * Prepares a command for execution.
 * @param name name of command
 * @param args arguments for command
 * @return a {@link Runnable} to execute the command.
 * @throws CommandArgumentException if the command is not able to be executed with the given arguments
 */
private Runnable onCommand(String name, String[] args) throws CommandArgumentException {
	CommandRecord command = COMMANDS.get(name);
	try {
		if(command != null) {
			if(command.limitArgs && args.length != command.argNames.length) throw new CommandArgumentException(new StringBuilder("Illegal amount of arguments: ").append(args.length).append(", expected: ").append(command.argNames.length).toString());
			return command.command.getExecute(args);
		} else if(debug) {
			command = DEBUG_COMMANDS.get(name);
			if(command != null) {
				return command.command.getExecute(args);
			}
		}
	} catch(CommandArgumentException e) {
		StringBuilder str = new StringBuilder("Illegal arguments for command ").append(name).append(':').append(' ').append(e.getMessage()).append("\nUsage: ").append(prefix).append(name);
		for(String argument:command.argNames) str.append(' ').append('<').append(argument).append('>');
		throw new CommandArgumentException(str.toString());
	}
	throw new CommandArgumentException("Unknown command.  Try " + prefix + "help for a list of commands");
}

@Override
public void onNewSetting(String[] settingPath, Object value) {
	switch(settingPath[0]) {
	case "prefix":
		prefix = (String) value;
		return;
	case "debug":
		debug = (boolean) value;
		return;
	}
}

@Override
public void disable() {
	set(true, Chung.SETTING_PATH_ON);
}

/**
 * Contains basic Chungamod commands {@code set}, {@code help}, {@code say}, and {@code toggle}.
 * @return {@link Command} instances of these commands
 */
@MultiCommandFactory(names = {"set", "help", "say", "toggle"}, descriptions = {"Sets or reads a value of a setting", "Describes commands", "Sends a message to chat", "Toggles a module"}, argsArray = {
	@Arguments(value = {
		@Argument(index = 0, name = "module", description = "Name of module"),
		@Argument(index = 1, name = "setting", description = "(optional) Name of setting"),
		@Argument(index = 2, name = "value", description = "(optional) New value of setting - rm to remove"),
		@Argument(index = 3, name = "config", description = "(optional) Config of setting")
	}),
	@Arguments({
		@Argument(index = 0, name = "command", description = "(optional) Name of command")
	}),
	@Arguments({
		@Argument(index = 0, name = "message", description = "Message to be sent in chat")
	}),
	@Arguments({
		@Argument(index = 0, name = "module", description = "Name of module")
	})
}, limitArgsForEach = {false, false, true, true})
static Command[] commands() {
	return new Command[] {
		args -> () -> MCUtil.sendMessage(Chung.US.set(args)),
		args -> {
			if(args.length > 1) throw new CommandArgumentException("Too many arguments");
			if(args.length == 1) {
				CommandRecord command = COMMANDS.get(args[0]);
				if(command == null) {
					if(instance.debug) command = DEBUG_COMMANDS.get(args[0]);
					if(command == null) return () -> MCUtil.sendMessage("Unknown command.  Try " + instance.prefix + "help for a list of commands");
				}
				StringBuilder str = new StringBuilder(instance.prefix).append(args[0]);
				for(String argument:command.argNames) str.append(' ').append('<').append(argument).append('>');
				str.append('\n').append(command.description).append("\nArguments:");
				for(int i = 0; i < command.argNames.length; i++) str.append('\n').append('<').append(command.argNames[i]).append('>').append(' ').append('-').append(' ').append(command.argDescriptions[i]);
				String message = str.toString();
				return () -> MCUtil.sendMessage(message);
			}
			StringBuilder str = new StringBuilder("\n----- Commands: -----");
			boolean separator = true;
			for(@SuppressWarnings("unchecked") Map<String, CommandRecord> map: instance.debug ? new Map[] {COMMANDS, DEBUG_COMMANDS} : new Map[] {COMMANDS}) {
				List<Map.Entry<String, CommandRecord>> list = Util.sortEntries(map);
				for(Map.Entry<String, CommandRecord> entry:list) {
					str.append('\n').append(instance.prefix).append(entry.getKey());
					for(String argument:entry.getValue().argNames) str.append(' ').append('<').append(argument).append('>');
					str.append(' ').append('-').append(' ').append(entry.getValue().description);
				}
				if(instance.debug && separator) {
					separator = false;
					str.append("\n----- Debug Commands: -----");
				}
			}
			String message = str.toString();
			return () -> MCUtil.sendMessage(message);
		},
		args -> {
			String chatMessage = args[0];
			return () -> mc.player.connection.sendPacket(new CPacketChatMessage(chatMessage));
		},
		args -> {
			ModuleID<?> m = Chung.US.getModule(args[0]);
			return () -> Chung.US.toggleModule(m);
		}
	};
}

private static class CommandRecord {
	private final Command command;
	private final String description;
	private final String[] argNames;
	private final String[] argDescriptions;
	private final boolean limitArgs;
	
	private CommandRecord(Command command, String description, String[] argNames, String[] argDescriptions, boolean limitArgs) {
		this.command = command;
		this.description = description;
		this.argNames = argNames;
		this.argDescriptions = argDescriptions;
		this.limitArgs = limitArgs;
	}
}

/**
 * A command that can be called from the in-game chat bar.
 * @author Magnileve
 *
 */
@FunctionalInterface
public static interface Command {
	/**
	 * Builds a runnable to execute this command with the given arguments.
	 * @param args arguments for execution
	 * @throws CommandArgumentException if the command is not able to be executed with the given arguments
	 */
	public Runnable getExecute(String[] args) throws CommandArgumentException;
}

/**
 * Types containing a command factory method must be annotated with this for the factory to be detected.
 * @author Magnileve
 * @see CommandFactory
 * @see MultiCommandFactory
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public static @interface ContainsCommand {}

/**
 * Marks a method as a factory for a command.  This method must be static, take no parameters, and return {@link Command}.
 * @author Magnileve
 * @see MultiCommandFactory
 * @see ContainsCommand
 * @see Argument
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public static @interface CommandFactory {
	/**
	 * @return name of command
	 */
	String name();
	/**
	 * @return description of command
	 */
	String description();
	/**
	 * @return {@code true} if this command should only be accessible when
	 * {@code Commands} setting {@code debug} is enabled; {@code false} otherwise
	 */
	boolean debug() default false;
	/**
	 * @return {@code true} if the number of arguments should be checked
	 * to match the number of {@link Argument} annotations; {@code false} otherwise
	 */
	boolean limitArgs() default true;
}

/**
 * Marks a method as a factory for multiple commands.  This method must be static, take no parameters, and return a {@link Command} array.
 * @author Magnileve
 * @see CommandFactory
 * @see ContainsCommand
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public static @interface MultiCommandFactory {
	/**
	 * @return names of each command
	 */
	String[] names();
	/**
	 * @return descriptions of each command
	 */
	String[] descriptions();
	/**
	 * @return {@link Arguments} for each command
	 */
	Arguments[] argsArray();
	/**
	 * @return {@code true} if the number of arguments for any command should be checked
	 * to match the number of its {@link Argument} annotations; {@code false} otherwise
	 */
	boolean debug() default false;
	/**
	 * @return values of {@link CommandFactory#limitArgs()} for each command
	 */
	boolean[] limitArgsForEach() default {};
}

/**
 * Array of {@link Argument} annotations
 * @author Magnileve
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public static @interface Arguments {
	Argument[] value();
}

/**
 * Represents an argument for a command.
 * @author Magnileve
 */
@Repeatable(Arguments.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public static @interface Argument {
	/**
	 * @return index of argument
	 */
	int index();
	/**
	 * @return name of argument
	 */
	String name();
	/**
	 * @return description of argument
	 */
	String description();
}

/**
 * Thrown to indicate that a command cannot be executed with the given arguments.
 * Typically, the message should be displayed to the caller of the command.
 * @author Magnileve
 */
public static class CommandArgumentException extends IllegalArgumentException {
	private static final long serialVersionUID = -4285478815301671291L;
	
	public CommandArgumentException(String s) {
        super(s);
    }
	
	public CommandArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}

}