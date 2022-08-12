package magnileve.chungamod.modules;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import magnileve.chungamod.Chung;
import magnileve.chungamod.Commands;
import magnileve.chungamod.Commands.Command;
import magnileve.chungamod.Commands.CommandFactory;
import magnileve.chungamod.Commands.ContainsCommand;
import magnileve.chungamod.Commands.MultiCommandFactory;
import magnileve.chungamod.events.EventListener;
import magnileve.chungamod.events.EventManager;
import magnileve.chungamod.settings.SettingManager;
import magnileve.chungamod.settings.SettingInfoMap;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.DoubleKeyHashMap;
import magnileve.chungamod.util.DoubleKeyMap;
import magnileve.chungamod.util.Util;

/**
 * Loads and initializes groups of modules.
 * @author Magnileve
 * @see magnileve.chungamod.Chung Chung
 */
public abstract class ModuleLoader {

/**
 * Indicates that a module should have only one instance until it is disabled.
 */
public static final String SCOPE_SINGLETON = "singleton";
/**
 * Indicates that a module should have a new instance each time the client disconnects and joins a server.
 */
public static final String SCOPE_SESSION = "session";

/**
 * Returns the name of the input module in lowercase.
 */
public static final BiFunction<String, ModuleManager<?>, String> MODULE_MAP_KEY_CONVERTER =
		(className, module) -> module.getModuleID().getName().toLowerCase();

/**
 * Returns a new {@link DoubleKeyMap} that uses the lowercase module name as its secondary key.
 */
public static final Supplier<DoubleKeyMap<String, String, ModuleManager<?>>> MODULE_MAP_SUPPLIER =
		() -> new DoubleKeyHashMap<>(MODULE_MAP_KEY_CONVERTER);

private static final String[] SUPPORTED_LISTENER_SCOPES = {SCOPE_SINGLETON, SCOPE_SESSION};

/**
 * This module loader's logger.
 */
protected final Logger log;
/**
 * Map of loaded modules.
 * This map's primary key should be the name of the module type returned by {@link Class#getName()},
 * and this map's secondary key should be the lowercase module name.
 */
protected final DoubleKeyMap<String, String, ModuleManager<?>> modules;
/**
 * Loads settings for modules.
 */
protected final SettingManager settings;
/**
 * Collection of modules currently being loaded.
 * After initialization, all contents of this collection are moved to {@link #modules}.
 */
protected final Queue<ModuleManager<?>> loadingModules;
/**
 * Collects methods to be invoked during initialization.
 * @see #initialize(ModuleManager, Method, String)
 */
protected final Collection<Bucket<ModuleManager<?>, Method>> initMethods;
/**
 * Collects event listeners to be initialized.
 * @see #initialize(String, Class, Class[])
 */
protected final Collection<Bucket<String, Bucket<Class<?>, Class<?>[]>>> eventListeners;

/**
 * The name of the current loading group.
 */
protected String groupName;
/**
 * The default plugin ID of the current loading group.
 */
protected String groupPluginID;

private long startTime;

/**
 * Creates a new {@code ModuleLoader}.
 * @param log this module loader's logger
 * @param modules map of modules with primary key as module type name and secondary key as module name
 * @param settings loads settings for modules
 */
public ModuleLoader(Logger log, DoubleKeyMap<String, String, ModuleManager<?>> modules, SettingManager settings) {
	this.log = log;
	this.modules = modules;
	this.settings = settings;
	loadingModules = new ArrayDeque<>();
	initMethods = new ArrayList<>();
	eventListeners = new ArrayList<>();
}

/**
 * Called to invoke a static initialization method.
 * @param m module manager, or {@code null} if for a non-module type
 * @param method static initialization method
 * @param name name of module or declaring type
 * @see Init
 */
protected abstract void initialize(ModuleManager<?> m, Method method, String name);

/**
 * Called for all newly loaded modules during initialization.
 * @param <T> module type
 * @param m a module
 */
protected abstract <T extends Module> void initialize(ModuleManager<T> m);

/**
 * Called for all newly loaded non-module event listeners during initialization.
 * @param scope scope of listener
 * @param initType declaring type of listener
 * @param events events to be listened for
 */
protected abstract void initialize(String scope, Class<?> initType, Class<?>[] events);

public String[] getSupportedListenerScopes() {
	return SUPPORTED_LISTENER_SCOPES.clone();
}

/**
 * Gets the module ID of a declaring type.
 * @param <T> declaring type
 * @param moduleType declaring type
 * @return a {@code ModuleID} for this declaring type, or {@code null} if one is not registered to this {@code ModuleLoader}
 */
@SuppressWarnings("unchecked")
public <T extends Module> ModuleID<T> getModule(Class<T> moduleType) {
	ModuleManager<?> m = modules.get(moduleType.getName());
	return m == null ? null : (ModuleID<T>) m.getModuleID();
}

/**
 * Gets the module ID from a module name.
 * @param moduleName name of module, not case sensitive
 * @return a {@code ModuleID} with this name, or {@code null} if one is not registered to this {@code ModuleLoader}
 */
public ModuleID<?> getModule(String moduleName) {
	ModuleManager<?> m = modules.getFromSecondaryKey(moduleName.toLowerCase());
	return m == null ? null : m.getModuleID();
}

/**
 * Gets a list of all of the modules by this module loader.
 * @return a new list of all modules loaded by this loader at the time of this method call
 */
public List<ModuleID<?>> getAllModules() {
	return modules.values().stream().map(m -> m.getModuleID()).collect(Collectors.toList());
}

/**
 * Gets the module manager of a declaring type.
 * @param <T> declaring type
 * @param moduleType declaring type
 * @return a {@code ModuleManager} for this declaring type, or {@code null} if one is not registered to this {@code ModuleLoader}
 */
@SuppressWarnings("unchecked")
protected <T extends Module> ModuleManager<T> getModuleManager(Class<T> moduleType) {
	return (ModuleManager<T>) modules.get(moduleType.getName());
}

/**
 * Gets the module manager from a module ID.
 * @param <T> the module type
 * @param m a module ID
 * @return a {@code ModuleManager} that manages the given module
 * @throws IllegalArgumentException if the given module is not registered to this {@code ModuleLoader}
 */
protected <T extends Module> ModuleManager<T> getModuleManager(ModuleID<T> m) {
	@SuppressWarnings("unchecked")
	ModuleManager<T> manager = (ModuleManager<T>) modules.get(m.getModuleType().getName());
	if(manager == null) throw new IllegalArgumentException("ModuleID " + String.valueOf(m) + " is not registered");
	return manager;
}

/**
 * Gets the module manager from a module name.
 * @param moduleName name of module, not case sensitive
 * @return a {@code ModuleManager} with this name, or {@code null} if one is not registered to this {@code ModuleLoader}
 */
protected ModuleManager<?> getModuleManager(String moduleName) {
	return modules.getFromSecondaryKey(moduleName.toLowerCase());
}

/**
 * Starts loading modules from a group.
 * @param groupName name of loading group
 * @throws IllegalStateException if this {@code ModuleLoader} is already loading a group
 * @see #switchGroup(String)
 */
public void startLoading(String groupName) {
	if(this.groupName != null) throw new IllegalStateException("Already loading modules for " + groupName);
	startTime = System.currentTimeMillis();
	log.debug("Loading {}", groupName);
	this.groupName = groupName;
}

/**
 * Switches to a new group for loading modules.
 * @param nextGroupName name of next loading group
 * @throws IllegalStateException if this {@code ModuleLoader} is not loading a group
 */
public void switchGroup(String nextGroupName) {
	finishGroup();
	startLoading(nextGroupName);
}

/**
 * Finishes loading the current group and initializes all loaded classes since the last call to initialization.
 * @throws IllegalStateException if this {@code ModuleLoader} is not loading a group
 */
public void initializeAndFinishLoading() {
	finishGroup();
	initializeModules();
}

/**
 * Finishes loading the current group.
 * @throws IllegalStateException if this {@code ModuleLoader} is not loading a group
 */
private void finishGroup() {
	if(groupName == null) throw new IllegalStateException("Not loading modules");
	log.info(() -> log.getMessageFactory().newMessage("Loaded " + groupName +
			" in " + Util.formatSeconds(System.currentTimeMillis() - startTime)));
	groupName = null;
	groupPluginID = null;
}

/**
 * Loads classes for the current loading group.
 * @param classes iterates through classes to be loaded
 * @throws AnnotationFormatError if the loading classes are improperly annotated
 */
public void load(Iterable<Class<?>> classes) {
	List<Bucket<Class<? extends Module>, Bucket<Bucket<ModuleInfo, EventListener>,
			Bucket<Method, Method>>>> loadingModules = new ArrayList<>();
	Map<Package, String> packagePluginIDs = new HashMap<>(8);
	List<Bucket<String, Method>> commandFactories = new ArrayList<>();
	List<Bucket<ModuleManager<?>, Method>> preInitMethods = new ArrayList<>();
	
	log.debug("PreInitialization1");
	for(Class<?> loadingClass:classes) try {
		boolean containsInit = false;
		boolean containsCommand = false;
		Bucket<Class<? extends Module>, ModuleInfo> unloadedModule = null;
		ModuleInfo moduleInfo = null;
		EventListener eventListener = null;
		Method preInit2 = null;
		Method init = null;
		
		for(Annotation a:loadingClass.getAnnotations()) {
			if(a instanceof ModuleInfo) {
				if(!Module.class.isAssignableFrom(loadingClass))
					throw new AnnotationFormatError(loadingClass + " must implement Module interface");
				@SuppressWarnings("unchecked")
				Class<? extends Module> moduleClass = (Class<? extends Module>) loadingClass;
				unloadedModule = Bucket.of(moduleClass, (ModuleInfo) a);
				moduleInfo = (ModuleInfo) a;
			} else if(a instanceof ContainsInit) containsInit = true;
			else if(a instanceof ContainsCommand) containsCommand = true;
			else if(a instanceof EventListener) eventListener = (EventListener) a;
			else if(a instanceof ChungamodPlugin) {
				ChungamodPlugin a1 = (ChungamodPlugin) a;
				switch(a1.level()) {
				case TYPE:
					break;
				case PACKAGE:
					packagePluginIDs.merge(loadingClass.getPackage(), a1.id(), (k1, k2) -> {
						throw new AnnotationFormatError("Found two plugin IDs for " + loadingClass.getPackage() + ": " + k1 + ", " + k2);
					});
					break;
				case LOADING_GROUP:
					if(groupPluginID != null) throw new AnnotationFormatError("Found two loading group plugin IDs " +
							groupPluginID + ", " + a1.id());
					groupPluginID = a1.id();
					break;
				}
			}
		}
		
		if(containsCommand || containsInit) for(Method method:loadingClass.getDeclaredMethods()) {
			if(containsInit) {
				if(method.isAnnotationPresent(Init.class)) {
					staticAccessible(method);
					if(moduleInfo != null) init = method;
					else initMethods.add(Bucket.of(null, method));
					continue;
				}
				if(method.isAnnotationPresent(Init.PreInit1.class)) {
					staticAccessible(method);
					initialize(null, method, moduleInfo == null || moduleInfo.name().isEmpty() ?
							loadingClass.getSimpleName() : moduleInfo.name());
					continue;
				}
				if(method.isAnnotationPresent(Init.PreInit2.class)) {
					staticAccessible(method);
					if(moduleInfo != null) preInit2 = method;
					else preInitMethods.add(Bucket.of(null, method));
					continue;
				}
			}
			if(containsCommand) {
				if(method.isAnnotationPresent(CommandFactory.class)) {
					staticAccessible(method);
					if(!method.getReturnType().equals(Command.class))
						throw new AnnotationFormatError("CommandFactory method must return type Commands.Command");
					commandFactories.add(Bucket.of(moduleInfo == null || moduleInfo.name().isEmpty() ?
							loadingClass.getSimpleName() : moduleInfo.name(), method));
					continue;
				}
				if(method.isAnnotationPresent(MultiCommandFactory.class)) {
					staticAccessible(method);
					if(!method.getReturnType().equals(Command[].class))
						throw new AnnotationFormatError("MultiCommandFactory method must return array of type Commands.Command");
					commandFactories.add(Bucket.of(moduleInfo == null || moduleInfo.name().isEmpty() ?
							loadingClass.getSimpleName() : moduleInfo.name(), method));
					continue;
				}
			}
		}
		
		if(moduleInfo == null) {
			if(eventListener != null) {
				String[] types = eventListener.value();
				int i = Util.indexOfAny(types, SUPPORTED_LISTENER_SCOPES);
				String type = i < 0 ? SCOPE_SINGLETON : types[i];
				switch(type) {
				case SCOPE_SINGLETON:
					eventListeners.add(Bucket.of(type, Bucket.of(loadingClass, EventManager.getEventTypes(loadingClass))));
					break;
				case SCOPE_SESSION:
					eventListeners.add(Bucket.of(type, Bucket.of(loadingClass, EventManager.getEventTypes(loadingClass))));
					break;
				}
			}
		} else loadingModules.add(
				Bucket.of(unloadedModule.getE1(), Bucket.of(Bucket.of(unloadedModule.getE2(), eventListener),
						Bucket.of(preInit2, init))));
	} catch(AnnotationFormatError e) {
		throw new AnnotationFormatError("Unable to load " + loadingClass, e);
	}
	
	log.debug("Loading modules");
	List<ModuleManager<?>> getModules = new ArrayList<>(loadingModules.size());
	
	for(Bucket<Class<? extends Module>, Bucket<Bucket<ModuleInfo, EventListener>,
			Bucket<Method, Method>>> loadingModule:loadingModules) {
		String pluginID = null;
		ChungamodPlugin a1 = loadingModule.getE1().getAnnotation(ChungamodPlugin.class);
		if(a1 != null && a1.level().equals(ChungamodPlugin.Level.TYPE)) pluginID = a1.id();
		if(pluginID == null) pluginID = packagePluginIDs.get(loadingModule.getE1().getPackage());
		if(pluginID == null) {
			if(groupPluginID == null) throw new AnnotationFormatError("No plugin ID found for " + loadingModule.getE1());
			else pluginID = groupPluginID;
		}
		Bucket<ModuleInfo, EventListener> buildRecordBucket = loadingModule.getE2().getE1();
		ModuleManager<?> m = buildModuleManager(loadingModule.getE1(),
				buildRecordBucket.getE1(), buildRecordBucket.getE2(), pluginID, log);
		getModules.add(m);
		Bucket<Method, Method> methods = loadingModule.getE2().getE2();
		if(methods.getE1() != null) preInitMethods.add(Bucket.of(m, methods.getE1()));
		if(methods.getE2() != null) initMethods.add(Bucket.of(m, methods.getE2()));
	}
	this.loadingModules.addAll(getModules);
	
	log.debug("Loading settings");
	settings.loadSettings(getModules.stream()
			.map(m -> Bucket.<ModuleID<?>, Bucket<SettingInfoMap, Map<String, Object>>>
					of(m.getModuleID(), Bucket.of(new SettingInfoMap(m.getModuleID()), m.getSettings())))
			::iterator);
	log.debug("PreInitialization2");
	for(Bucket<ModuleManager<?>, Method> init:preInitMethods) initialize(init.getE1(), init.getE2(),
			init.getE1() == null ? init.getE2().getDeclaringClass().getSimpleName() : init.getE1().getModuleID().getName());
	log.debug("Loading commands");
	Commands.loadCommands(commandFactories, log);
}

/**
 * Initializes all loaded classes since the last call to this method.
 */
protected void initializeModules() {
	long startInitTime = System.currentTimeMillis();
	log.debug("Initializing loaded classes");
	for(Bucket<ModuleManager<?>, Method> init:initMethods) initialize(init.getE1(), init.getE2(),
			init.getE1() == null ? init.getE2().getDeclaringClass().getSimpleName() : init.getE1().getModuleID().getName());
	log.debug("Instantiating singletons and initializing event listeners");
	for(ModuleManager<?> m = loadingModules.poll(); m != null; m = loadingModules.poll()) {
		initialize(m);
		modules.put(m.getModuleID().getModuleType().getName(), m);
	}
	for(Bucket<String, Bucket<Class<?>, Class<?>[]>> eventListener:eventListeners)
		initialize(eventListener.getE1(), eventListener.getE2().getE1(), eventListener.getE2().getE2());
	initMethods.clear();
	eventListeners.clear();
	long completeTime = System.currentTimeMillis();
	log.info(() -> log.getMessageFactory().newMessage("Initialization completed in " + Util.formatSeconds(completeTime - startInitTime)));
}

/**
 * Creates a new logger for a module.
 * @param m module manager
 * @param chatLevel least specific logging level for chat
 * @param fileLevel least specific logging level for files
 * @param directory directory for log files
 * @param format {@code ModuleLogger} format
 * @param chatFormat {@code ModuleLogger} format for chat
 * @param fileFormat {@code ModuleLogger} format for files
 * @return a new logger for the given module
 * @throws IOException if an I/O error occurs
 * @see ModuleLogger
 */
protected Logger moduleLogger(ModuleManager<?> m, Level chatLevel, Level fileLevel, Path directory,
		String format, String chatFormat, String fileFormat) throws IOException {
	return new ModuleLogger(m.getModuleID(), log, chatLevel, fileLevel, directory, format, chatFormat, fileFormat);
}

/**
 * Adds a module that has already been loaded elsewhere to this {@code ModuleLoader}.
 * Subclasses may reject any or all calls to this method.
 * @param <T> module type
 * @param m module manager
 * @throws IllegalArgumentException if the given module cannot be added
 */
public <T extends Module> void addPreLoadedModule(ModuleManager<T> m) {
	m = new ModuleManager<>(m, Chung.SETTING_MAP_FACTORY);
	initialize(m);
	modules.put(m.getModuleID().getModuleType().getName(), m);
}

/**
 * Adds an event listener that has already been loaded elsewhere to this {@code ModuleLoader}.
 * Subclasses may reject any or all calls to this method.
 * @param scope scope of event listener
 * @param initType declaring type
 * @param events events to be listened for
 * @throws IllegalArgumentException if the given event listener cannot be added
 */
public void addPreLoadedEventListener(String scope, Class<?> initType, Class<?>[] events) {
	initialize(scope, initType, events.clone());
}

/**
 * Creates a new {@code ModuleManager} for a loading module.
 * @param <T> module type
 * @param moduleType module type
 * @param info module info annotation
 * @param event event listener annotation, or {@code null} if one does not exist
 * @param pluginID plugin ID of module
 * @param log debug logger
 * @return a new {@code ModuleManager} with a new {@code ModuleID}
 */
public static <T extends Module> ModuleManager<T> buildModuleManager(Class<T> moduleType,
		ModuleInfo info, EventListener event, String pluginID, Logger log) {
	String name = info.name().isEmpty() ? moduleType.getSimpleName() : info.name();
	log.debug("- Loading module {}", name);
	try {
		int eventTypeIndex = event == null ? -1 : Util.indexOfAny(event.value(), SUPPORTED_LISTENER_SCOPES),
				scopeFlags = eventTypeIndex >= 0 && event.value()[eventTypeIndex].equals(SCOPE_SINGLETON) ?
				ModuleManager.SINGLETON_FLAG : 0;
		if(info.alwaysInstantiate()) scopeFlags |= ModuleManager.ALWAYS_INSTANTIATE_FLAG;
		return new ModuleManager<T>(moduleType, name, new HashMap<>(),
				info.category(), info.description(), pluginID, event == null ? Util.CLASS_ARRAY_0 :
					EventManager.getEventTypes(moduleType), scopeFlags);
	} catch(AnnotationFormatError e) {
		throw new AnnotationFormatError("Unable to load module " + name, e);
	}
}

/**
 * Ensures that a method is static and accessible.
 * @param method a method
 * @throws AnnotationFormatError if the given method is not static and accessible
 */
public static void staticAccessible(Method method) {
	if(!Modifier.isStatic(method.getModifiers())) throw new AnnotationFormatError(method + " must be static");
	accessible(method);
}

/**
 * Ensures that an executable is accessible.
 * @param executable an executable
 * @throws AnnotationFormatError if the given executable is not accessible
 */
public static void accessible(Executable executable) {
	if(!executable.isAccessible()) try {
		executable.setAccessible(true);
	} catch(RuntimeException e) {
		throw new AnnotationFormatError(executable + " must be accessible", e);
	}
}

}