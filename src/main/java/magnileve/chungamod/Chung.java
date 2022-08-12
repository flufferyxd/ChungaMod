package magnileve.chungamod;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONTokener;

import magnileve.chungamod.events.EventManager;
import magnileve.chungamod.modules.Factory;
import magnileve.chungamod.modules.GetLogger;
import magnileve.chungamod.modules.Module;
import magnileve.chungamod.modules.ModuleConstructionException;
import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.modules.ModuleLoader;
import magnileve.chungamod.modules.ModuleManager;
import magnileve.chungamod.settings.ConfigMap;
import magnileve.chungamod.settings.DirectorySettingManager;
import magnileve.chungamod.settings.ConfigBuffer;
import magnileve.chungamod.settings.GetSetting;
import magnileve.chungamod.settings.InvalidSettingPathException;
import magnileve.chungamod.settings.Setting;
import magnileve.chungamod.settings.SettingListener;
import magnileve.chungamod.settings.SettingManager;
import magnileve.chungamod.settings.SettingMapFactory;
import magnileve.chungamod.settings.SettingInfo;
import magnileve.chungamod.settings.SettingInfoMap;
import magnileve.chungamod.settings.SettingTraverser;
import magnileve.chungamod.settings.SettingUtil;
import magnileve.chungamod.settings.UnsetSettingException;
import magnileve.chungamod.settings.SettingMapper;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.ClassHashMap;
import magnileve.chungamod.util.DoubleKeyHashMap;
import magnileve.chungamod.util.MCUtil;
import magnileve.chungamod.util.Util;
import magnileve.chungamod.util.function.TriConsumer;
import magnileve.chungamod.util.json.JSONManager;
import net.minecraft.client.Minecraft;

/**
 * <p>
 * Loads and manages the default module system of Chungamod.  For a module to be loaded, it must meet the following requirements:
 * </p>
 * <p style="margin-left:40px">
 * - be in a jar in the plugins directory<br>
 * - implement {@link Module}<br>
 * - be annotated with {@link magnileve.chungamod.modules.ModuleInfo ModuleInfo}<br>
 * - contain a factory annotated with {@link Factory}
 * </p>
 * <h2>Interface implementations</h2>
 * <h3>SettingMapper</h3>
 * <p>
 * Forwards calls to an encapsulated instance of {@link DirectorySettingManager}
 * unless the target config is {@value SettingManager#TEMPORARY_CONFIG}, in which case,
 * calls are forwarded to implemented {@link ConfigMap} methods.
 * </p>
 * <h3>ConfigMap</h3>
 * <p>
 * Interacts with the current active settings.  Any changes to settings are saved to {@value SettingManager#TEMPORARY_CONFIG}.
 * </p>
 * @author Magnileve
 * @see #US
 */
public class Chung extends ModuleLoader implements SettingMapper, ConfigMap {

public static final String
		MODID = "chungamod",
		NAME = "Chungamod",
		VERSION = "0.3",
		ACCEPTED_MINCERFAT_VERSIONS = "[1.12]",
		CHUNGAMOD_DIRECTORY = "chungamod",
		CONFIGURATIONS_PROPERTY = "chungamod.configs",
		CONFIGURATIONS_DIRECTORY = "configs",
		LOGS_DIRECTORY = "logs",
		PLUGINS_DIRECTORY = "plugins";

static final String[] SETTING_PATH_ON = new String[] {Setting.ON};

public static final SettingMapFactory SETTING_MAP_FACTORY = HashMap::new;

/**
 * Singleton instance of {@link Chung}
 */
public static final Chung US = new Chung(Minecraft.getMinecraft(), LogManager.getLogger(MODID), new JSONManager());
public static final Logger debug = US.log;
/**
 * Singleton instance of {@link ChungamodSystem}
 */
public static final ChungamodSystem SYSTEM = new ChungamodSystem(US.log);

DateTimeFormatter timeFormatter;

private final Minecraft mc;
private final Logger log;
private final JSONManager json;
private final EventManager eventManager;
private final Map<Class<?>, Collection<SettingListener>> settingListeners;

private Set<ModuleManager<?>> enabledModules;
private Map<ModuleID<?>, Map<String, Object>> savedSettingBuffer;

private Chung(Minecraft mc, Logger log, JSONManager json) {
	super(log, new DoubleKeyHashMap<>(ModuleLoader.MODULE_MAP_KEY_CONVERTER), new DirectorySettingManager(log, json, Collections.emptySet(),
			SETTING_MAP_FACTORY, Paths.get(CHUNGAMOD_DIRECTORY, CONFIGURATIONS_DIRECTORY), (name, pluginID) -> US.getModule(name)));
	this.mc = mc;
	this.log = log;
	this.json = json;
	eventManager = new EventManager(log);
	settingListeners = new ClassHashMap<>();
}

@Override
protected <T extends Module> void initialize(ModuleManager<T> m) {
	boolean on = (boolean) m.getSettings().get(Setting.ON);
	if(on) m.setEnabled(true);
	if(m.isSingleton() && (on || m.alwaysInstantiate())) {
		T instance;
		try {
			instance = initEventListener(m.getFactory(), m.getSubscribedEvents());
		} catch (Exception e) {
			log.fatal("Unable to create instance of module " + m,
					e instanceof InvocationTargetException ? e.getCause() : e);
			throw new RuntimeException(e);
		}
		m.setInstance(instance);
		ModuleID<T> moduleID = m.getModuleID();
		addSettingListener(moduleID.getModuleType(), moduleID.getInstance());
	}
}

@Override
protected void initialize(String scope, Class<?> initClass, Class<?>[] events) {
	Callable<?> factory;
	try {
		factory = getCallableFactory(initClass, null);
	} catch(IllegalArgumentException | AnnotationFormatError e) {
		throw new AnnotationFormatError("Unable to create factory for event listener " + initClass, e);
	}
	switch(scope) {
	case SCOPE_SINGLETON:
		try {
			initEventListener(factory, events);
		} catch (Exception e) {
			log.fatal("Unable to create instance of event listener " + initClass,
					e instanceof InvocationTargetException ? e.getCause() : e);
			throw new RuntimeException(e);
		}
		break;
	case SCOPE_SESSION:
		getModule(ChungamodModule.class).getInstance().addEventListenerForSession(factory, events);
		break;
	}
}

/**
 * Creates and registers a new instance of an event listener.  If the listener implements {@link Runnable}, calls {@link Runnable#run()}.
 * @param <T> type of the event listener
 * @param factory factory of the event listener
 * @param eventClasses event classes the listener listens for
 * @return a new instance of the event listener
 * @throws Exception if the call to the factory throws an exception
 * @throws RuntimeException if the listener implements {@link Runnable}, and {@link Runnable#run()} throws a runtime exception
 */
<T> T initEventListener(Callable<T> factory, Class<?>[] eventClasses) throws Exception {
	T instance = factory.call();
	if(instance instanceof Runnable) ((Runnable) instance).run();
	for(Class<?> eventClass:eventClasses) eventManager.addListener(instance, eventClass);
	return instance;
}

@Override
protected void initialize(ModuleManager<?> m, Method method, String name) {
	log.debug("- {}", name);
	try {
		method.invoke(null, Chung.US.getParameterInstances(m, method.getParameters()));
	} catch (ReflectiveOperationException | IOException e) {
		throw new RuntimeException("Unable to initialize " + name, e);
	}
}

/**
 * Initializes Chungamod and loads modules from plugin jars.
 */
void init() {
	long startTime = System.currentTimeMillis();
	log.info("Loading Chungamod");
	
	//initialize core
	JSONManager.addDefaultProcessors(json);
	SYSTEM.init();
	SYSTEM.addProperty(CONFIGURATIONS_PROPERTY, "", value -> {
		Set<String> configs = settings.getActiveConfigs();
		configs.clear();
		configs.addAll(Arrays.asList(value.split(",")));
		configs.remove("");
		configs.remove(SettingManager.TEMPORARY_CONFIG);
	});
	magnileve.chungamod.Tick.init(log);
	magnileve.chungamod.packets.PacketListener.init(log);
	ChungamodModule.init(log);
	log.info(() -> log.getMessageFactory().newMessage("Chungamod core initialized in {}",
			Util.formatSeconds(System.currentTimeMillis() - startTime)));
	
	//load core modules
	startLoading("core modules");
	load(Arrays.asList(JSONManager.class, ChungamodForge.class, ChungamodModule.class, Commands.class,
			magnileve.chungamod.gui.ClickGUIModule.class,
			
			magnileve.chungamod.util.MCUtil.class,
			magnileve.chungamod.Tick.class,
			magnileve.chungamod.gui.values.BlockPosButton.class,
			magnileve.chungamod.settings.AbstractSettingManager.class,
			
			magnileve.chungamod.Hotkeys.class
			));
	
	//load plugin jars
	Path pluginsDirectory = Paths.get(CHUNGAMOD_DIRECTORY, PLUGINS_DIRECTORY);
	try {
		if(!Files.exists(pluginsDirectory)) Files.createDirectories(pluginsDirectory);
		List<File> jarFiles = Files.list(pluginsDirectory)
				.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".jar"))
				.map(path -> new File(path.toString()))
				.collect(Collectors.toList());
		URL[] jarURLs = new URL[jarFiles.size()];
		int i = 0;
		for(File jar:jarFiles) jarURLs[i++] = jar.toURI().toURL();
		
		try(URLClassLoader pluginClassLoader = new URLClassLoader(jarURLs, Chung.class.getClassLoader())) {for(File jar:jarFiles) {
			switchGroup(jar.getName().substring(0, jar.getName().length() - 4));
			try(JarFile jarFile = new JarFile(jar)) {
				load(() -> new Iterator<Class<?>>() {
					private Enumeration<JarEntry> iter = jarFile.entries();
					private Class<?> next;
					{if(iter.hasMoreElements()) next();}
					
					@Override
					public boolean hasNext() {
						return next != null;
					}
					
					@Override
					public Class<?> next() {
						Class<?> current = next;
						while(iter.hasMoreElements()) {
							JarEntry e = iter.nextElement();
							String name = e.getName();
						    if(!e.isDirectory() && name.endsWith(".class")) try {
								next = pluginClassLoader.loadClass(name.substring(0, name.length() - 6).replace('/', '.'));
								return current;
							} catch (ClassNotFoundException e1) {
								throw new RuntimeException(e1);
							}
						}
						next = null;
						return current;
					}
				});
			} catch (IOException | RuntimeException e) {
				log.fatal("Error reading plugin jar " + jar, e);
				throw new RuntimeException(e);
			}
		}}
	} catch (IOException e) {
		throw new UncheckedIOException("Unable to open plugins directory: " + pluginsDirectory, e);
	}
	
	initializeAndFinishLoading();
	log.info(() -> log.getMessageFactory().newMessage("Chungamod loaded in {}",
			Util.formatSeconds(System.currentTimeMillis() - startTime)));
	log.info("Modules loaded:");
}

/**
 * Starts modules when connected to a server.
 */
void connectModules() {
	enabledModules = new HashSet<>();
	Collection<Bucket<ModuleManager<?>, Module>> runModules = new ArrayBuildList<>(Bucket.class);
	for(ModuleManager<?> m:modules.values()) if(m.alwaysInstantiate() || (boolean) m.getSettings().get(Setting.ON))
		startModule(m, runModules);
	for(Bucket<ModuleManager<?>, Module> enabledModule:runModules) {
		Module instance = enabledModule.getE2();
		try {
			((Runnable) instance).run();
		} catch(ModuleConstructionException e) {
			ModuleManager<?> m = enabledModule.getE1();
			MCUtil.sendMessage(m + " - " + e.getMessage());
			log.debug(() -> log.getMessageFactory().newMessage("Caught exception while starting module " + m), e);
			m.getSettings().put(Setting.ON, false);
			continue;
		}
		onEnabledModule(enabledModule.getE1(), instance);
	}
}

private <T extends Module> void startModule(ModuleManager<T> m, Collection<Bucket<ModuleManager<?>, Module>> runs) {
	log.debug("Starting module {}", m);
	try {
		T instance;
		T prevInstance = m.getModuleID().getInstance();
		if(m.alwaysInstantiate()) {
			if(m.isSingleton()) instance = prevInstance;
			else instance = prevInstance == null ? m.getFactory().call() : prevInstance;
		} else instance = m.isSingleton() && prevInstance != null ? prevInstance : m.getFactory().call();
		if(instance instanceof Runnable) {
			if(runs == null) ((Runnable) instance).run();
			else {
				runs.add(Bucket.of(m, instance));
				return;
			}
		}
		onEnabledModule(m, instance);
	} catch (Exception e) {
		if(e instanceof InvocationTargetException && e.getCause() instanceof ModuleConstructionException ||
				e instanceof ModuleConstructionException) {
			Throwable e1 = e instanceof InvocationTargetException ? e.getCause() : e;
			MCUtil.sendMessage(m + " - " + e1.getMessage());
			log.debug(() -> log.getMessageFactory().newMessage("Caught exception while starting module " + m), e);
			m.getSettings().put(Setting.ON, false);
			return;
		}
		if(e instanceof IOException) {
			MCUtil.sendMessage("Exception creating FileWriter for logger of " + m + ":\n" + e.getMessage());
			log.error("Exception creating FileWriter for logger of " + m, e);
			return;
		}
		log.fatal("Unable to create instance of module " + m, e);
		throw new RuntimeException(e);
	}
}

@SuppressWarnings("unchecked")
private <T extends Module> void onEnabledModule(ModuleManager<T> m, Object instance) {
	m.setEnabled(true);
	if(m.getInstance() == null) {
		m.setInstance((T) instance);
		Class<?>[] eventClasses = m.getSubscribedEvents();
		if(eventClasses.length > 0) for(int i = 0; i < eventClasses.length; i++) eventManager.addListener(instance, eventClasses[i]);
		if(enabledModules != null && enabledModules.add(m)) addSettingListener(m.getModuleID().getModuleType(), (T) instance);
	}
}

/**
 * Shuts down modules when disconnected from a server.
 */
void disconnectModules() {
	for(ModuleManager<?> m:enabledModules) {
		Module instance = m.getInstance();
		if(!m.isSingleton()) {
			m.setInstance(null);
			removeSettingListener(m.getModuleID().getModuleType(), instance);
			Class<?>[] eventClasses = m.getSubscribedEvents();
			if(eventClasses.length > 0) for(int i = 0; i < eventClasses.length; i++) eventManager.removeListener(instance, eventClasses[i]);
		}
		instance.softDisable();
	}
	enabledModules = null;
}

/**
 * Disables a module.
 * @param m the module to be disabled
 */
private void stopModule(ModuleManager<?> m) {
	Module instance = m.getInstance();
	if(instance != null) {
		log.debug("Disabling module {}", m);
		m.setEnabled(false);
		if(enabledModules != null) enabledModules.remove(m);
		if(!m.alwaysInstantiate()) {
			m.setInstance(null);
			Class<?>[] eventClasses = m.getSubscribedEvents();
			if(eventClasses.length > 0) for(int i = 0; i < eventClasses.length; i++) eventManager.removeListener(instance, eventClasses[i]);
			removeSettingListener(m.getModuleID().getModuleType(), instance);
		}
		instance.disable();
	}
}

/**
 * Gets the instance of {@link DateTimeFormatter} used by Chungamod.
 * This instance can be changed using the module {@code Chungamod} setting {@code TimeFormat}.
 * @return Chungamod's {@link DateTimeFormatter}
 */
public DateTimeFormatter getTimeFormatter() {
	return timeFormatter;
}

/**
 * Adds a listener for changes in settings of a module.  Modules are automatically added to listen to themselves when enabled.
 * @param moduleClass class of module
 * @param listener listener for changes in settings
 * @see #removeSettingListener(Class, SettingListener)
 */
public void addSettingListener(Class<?> moduleClass, SettingListener listener) {
	log.trace("Adding setting listener {} for {}", listener, moduleClass);
	settingListeners.computeIfAbsent(moduleClass, m -> new ArrayBuildList<>(new SettingListener[1])).add(listener);
}

/**
 * Removes a listener for changes in settings of a module.  Modules are automatically removed from listening to themselves when disabled.
 * @param moduleClass class of module
 * @param listener listener for changes in settings
 * @see #addSettingListener(Class, SettingListener)
 */
public boolean removeSettingListener(Class<?> moduleClass, SettingListener listener) {
	Collection<SettingListener> listeners = settingListeners.get(moduleClass);
	if(listeners != null) {
		Iterator<SettingListener> iter = listeners.iterator();
		while(iter.hasNext()) if(listener.equals(iter.next())) {
			iter.remove();
			if(listeners.isEmpty()) settingListeners.remove(moduleClass);
			log.trace("Removed setting listener {} for {}", listener, moduleClass);
			return true;
		}
	}
	log.trace("Unable to remove setting listener {} for {}", listener, moduleClass);
	return false;
}

/**
 * Notifies any setting listeners of a change in the value of a setting.
 * @param moduleClass the class of the module of the setting
 * @param settingPath the path of the setting
 * @param value the new value of the setting
 */
private void notifySettingListeners(Class<?> moduleClass, String[] settingPath, Object value) {
	Collection<SettingListener> listeners = settingListeners.get(moduleClass);
	if(listeners != null) for(SettingListener listener:listeners.toArray(new SettingListener[listeners.size()]))
		listener.onNewSetting(settingPath, value);
}

/**
 * Enables a module if it is not enabled, or disables it if it is enabled.
 * The module is not guaranteed to change between on and off by the end of this call.
 * One situation resulting in no change would be if the module throws {@link ModuleConstructionException} while being enabled.
 * @param m the module
 * @return the updated value of the the module's setting {@code On}.
 */
public boolean toggleModule(ModuleID<?> m) {
	log.trace("Toggling module {}", m);
	ModuleManager<?> manager = getModuleManager(m);
	set(SettingManager.TEMPORARY_CONFIG, m, manager, !(boolean) get(m, manager, SETTING_PATH_ON), SETTING_PATH_ON);
	return (boolean) get(m, manager, SETTING_PATH_ON);
}

/**
 * Disables and enables a module.
 * @param m the module
 */
public void restartModule(ModuleID<?> m) {
	log.trace("Restarting module {}", m);
	ModuleManager<?> manager = getModuleManager(m);
	set(SettingManager.TEMPORARY_CONFIG, m, manager, false, SETTING_PATH_ON);
	set(SettingManager.TEMPORARY_CONFIG, m, manager, true, SETTING_PATH_ON);
}

@Override
public Object get(ModuleID<?> m, String... settingPath) {
	return get(m, getModuleManager(m), settingPath);
}

/**
 * Gets a setting for a module.
 * @param m the module
 * @param manager manager of module
 * @param settingPath path of setting
 * @return value of setting
 * @throws UnsetSettingException if the setting does not have a value
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
private Object get(ModuleID<?> m, ModuleManager<?> manager, String... settingPath) {
	return SettingUtil.getValue(manager.getSettings(), settingPath, SettingInfo.getTree(m));
}

@Override
public Object get(String config, ModuleID<?> m, String... settingPath) {
	return SettingManager.TEMPORARY_CONFIG.equals(config) ? get(m, settingPath) : settings.get(config, m, settingPath);
}

@Override
public void loadConfig(String config, Map<ModuleID<?>, Map<String, Object>> configSettings,
		TriConsumer<ModuleID<?>, String[], Object> onNewSetting) {
	settings.loadConfig(config, configSettings, onNewSetting);
}

@Override
public ConfigBuffer loadConfig(String config) {
	if(SettingManager.TEMPORARY_CONFIG.equals(config)) return new ConfigBuffer(new HashMap<>(), settings) {
		@Override
		public void save() {
			Chung.this.importSettings(settings);
		}
	};
	return settings.loadConfig(config);
}

/**
 * Sets a setting for a module.
 * @param m record of module
 * @param value new value of setting
 * @param settingPath path of setting
 * @return old value of setting
 */
@Override
public void set(ModuleID<?> m, Object value, String... settingPath) {
	set(SettingManager.TEMPORARY_CONFIG, m, null, value, settingPath);
}

@Override
public Object set(String config, ModuleID<?> m, Object value, String... settingPath) {
	return set(config, m, null, value, settingPath);
}

/**
 * Sets a value of a setting for a module.
 * @param config name of config
 * @param m the module
 * @param manager optional manager of module
 * @param value the new value
 * @param settingPath path of setting
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
private Object set(String config, ModuleID<?> m, ModuleManager<?> manager, Object value, String... settingPath) {
	log.trace(() -> {
		StringBuilder logMessage = new StringBuilder("Module ").append(m.toString()).append(" setting ").append(settingPath[0]);
		for(int i = 1; i < settingPath.length; i++) logMessage.append('/').append(settingPath[i]);
		return log.getMessageFactory().newMessage(logMessage.append(" in config ").append(config)
				.append(" set to ").append(Util.toString(value)).toString());
	});
	Object oldValue = savedSettingBuffer == null ?
			settings.set(config, m, Util.recursiveArrayClone(value), settingPath) :
			SettingUtil.setValue(savedSettingBuffer, m, settingPath, Util.recursiveArrayClone(value), settings);
	if(SettingManager.TEMPORARY_CONFIG.equals(config)) {
		if(manager == null) manager = getModuleManager(m);
		oldValue = SettingUtil.setValue(manager.getSettings(), settingPath, value, SettingInfo.getTree(m), settings);
		if(!Util.equals(value, oldValue)) {
			boolean toggleModule = settingPath[0].equalsIgnoreCase(Setting.ON) && settingPath.length == 1;
			if(toggleModule && !(boolean) value) stopModule(manager);
			notifySettingListeners(m.getModuleType(), settingPath, value);
			if(toggleModule && (boolean) value && (oldValue == null ? false : !(boolean) oldValue)) startModule(manager, null);
		}
	}
	return oldValue;
}

/**
 * Gets a setting for a module, clears its temporary config value, then reloads it.
 * @return previous value of setting from temporary config
 * @throws UnsetSettingException if the setting does not have a temporary config value to be removed
 * @throws InvalidSettingPathException {@inheritDoc}
 */
@Override
public Object remove(ModuleID<?> m, String... settingPath) {
	return remove(SettingManager.TEMPORARY_CONFIG, m, settingPath);
}

@Override
public Object remove(String config, ModuleID<?> m, String... settingPath) {
	return remove(config, m, getModuleManager(m), settingPath);
}

/**
 * Gets and removes a setting for a module.
 * @param config name of config
 * @param m the module
 * @param manager manager of module
 * @param settingPath path of setting
 * @return previous value of setting
 * @throws UnsetSettingException if the setting does not have a value to be removed
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
private Object remove(String config, ModuleID<?> m, ModuleManager<?> manager, String... settingPath) {
	Object oldValue = settings.remove(config, m, settingPath);
	if(SettingManager.TEMPORARY_CONFIG.equals(config)) {
		Object value = settings.loadSetting(m, settingPath);
		if(!Util.equals(value, oldValue)) {
			SettingUtil.setValue(manager.getSettings(), settingPath, value, SettingInfo.getTree(m), settings);
			notifySettingListeners(m.getModuleType(), settingPath, value);
		}
	}
	return oldValue;
}

/**
 * Merges a map of settings into the active settings and saves the values to {@value SettingManager#TEMPORARY_CONFIG}.
 */
@Override
public void importSettings(Map<ModuleID<?>, Map<String, Object>> settings) {
	//copy to new buffer map that will collect recursive set calls
	savedSettingBuffer = new HashMap<>();
	SettingUtil.copySettingsGroup(settings, savedSettingBuffer, () -> new HashMap<>(8));
	//iterate through provided map
	for(Map.Entry<ModuleID<?>, Map<String, Object>> e:settings.entrySet()) {
		ModuleID<?> m = e.getKey();
		set(m, getModuleManager(m), e.getValue(), new String[1], SettingInfo.getTree(m));
	}
	//merge buffer of new values, which includes recursive set values, with previous temporary config
	Map<ModuleID<?>, Map<String, Object>> tmpSettings = new HashMap<>();
	this.settings.loadConfig(SettingManager.TEMPORARY_CONFIG, tmpSettings);
	SettingUtil.copySettingsGroup(savedSettingBuffer, tmpSettings, () -> new HashMap<>(8));
	//map by plugins and save each
	Map<String, Map<ModuleID<?>, Map<String, Object>>> pluginMap = new HashMap<>(4);
	SettingUtil.groupByPlugin(savedSettingBuffer, pluginMap);
	for(Map.Entry<String, Map<ModuleID<?>, Map<String, Object>>> entry:pluginMap.entrySet())
		this.settings.save(entry.getValue(), SettingManager.TEMPORARY_CONFIG, entry.getKey());
	savedSettingBuffer = null;
}

/**
 * Recursively sets settings for a module from a setting map.
 * @param m the module
 * @param manager manager of module
 * @param settings map of settings
 * @param settingPath path of setting
 * @param node node of {@code settingPath}
 * @throws InvalidSettingPathException if the provided setting path is invalid
 */
@SuppressWarnings("unchecked")
private void set(ModuleID<?> m, ModuleManager<?> manager, Map<String, Object> settings, String[] settingPath, SettingInfo node) {
	int l = settingPath.length - 1;
	for(Map.Entry<String, Object> setting:settings.entrySet()) {
		SettingInfo subNode;
		try{
			subNode = node.getNode(setting.getKey());
		} catch(IllegalStateException e) {
			subNode = null;
		}
		if(subNode == null) throw new InvalidSettingPathException(settingPath);
		settingPath[l] = subNode.getName();
		if(subNode.isTree())
			set(m, manager, (Map<String, Object>) setting.getValue(), Arrays.copyOf(settingPath, settingPath.length + 1), subNode);
		else set(SettingManager.TEMPORARY_CONFIG, m, manager, setting.getValue(), settingPath);
	}
}

/**
 * Clears {@value SettingManager#TEMPORARY_CONFIG} and reloads all settings.
 */
public void reloadSettings() {
	settings.clearTemporaryConfig();
	//load all configs to copies of module setting maps
	Map<String, Map<String, Bucket<SettingInfoMap, Map<String, Object>>>> pluginMap = new HashMap<>(4);
	SettingUtil.sortByPlugin(modules.values().stream()
			.map(m -> Bucket.<ModuleID<?>, Bucket<SettingInfoMap, Map<String, Object>>>of(m.getModuleID(),
					Bucket.of(new SettingInfoMap(m.getModuleID()), SettingUtil.copySettings(m.getSettings(), () -> new HashMap<>(8)))))
			::iterator, pluginMap);
	settings.loadSettings(pluginMap, SettingManager.TEMPORARY_CONFIG, null);
	for(String config:settings.getActiveConfigs()) settings.loadSettings(pluginMap, config, null);
	settings.loadDefaultSettings(pluginMap, null);
	//run set calls for each setting
	savedSettingBuffer = new HashMap<>();
	Map<ModuleID<?>, Map<String, Object>> loadedSettings = new HashMap<>();
	for(Map<String, Bucket<SettingInfoMap, Map<String, Object>>> plugin:pluginMap.values()) for(Entry<String,
			Bucket<SettingInfoMap, Map<String, Object>>> entry:plugin.entrySet()) {
		ModuleManager<?> manager = getModuleManager(entry.getKey());
		ModuleID<?> m = manager.getModuleID();
		Map<String, Object> settings = entry.getValue().getE2();
		loadedSettings.put(m, settings);
		set(m, manager, settings, new String[1], SettingInfo.getTree(m));
	}
	//find and collect new settings from recursive set calls
	Map<ModuleID<?>, Map<String, Object>> saveTmp = new HashMap<>();
	for(Entry<ModuleID<?>, Map<String, Object>> entry:savedSettingBuffer.entrySet()) {
		ModuleID<?> m = entry.getKey();
		Map<String, Object> settings = entry.getValue();
		Map<String, Object> loaded = loadedSettings.get(m);
		if(loaded == null) saveTmp.put(m, settings);
		else {
			Map<String, Object> changed = new HashMap<>();
			SettingUtil.copyChanged(loaded, settings, changed);
			if(!changed.isEmpty()) saveTmp.put(m, changed);
		}
	}
	savedSettingBuffer = null;
	//save settings from recursive set calls to temporary config
	if(!saveTmp.isEmpty()) {
		ConfigBuffer tmpConfig = settings.loadConfig(SettingManager.TEMPORARY_CONFIG);
		tmpConfig.importSettings(saveTmp);
		tmpConfig.save();
	}
}

/**
 * Gets the default value of a setting for a module.
 * @param moduleClass class of module
 * @param settingPath path of setting
 * @return the default value as declared by the setting annotation
 * @see Setting
 * @throws InvalidSettingPathException if the given setting path does not have a declared setting
 */
public Object getDefaultSetting(Class<?> moduleClass, String[] settingPath) {
	Setting setting = SettingInfo.getSetting(moduleClass, settingPath);
	Class<?> type = setting.type();
	return json.deserialize(new JSONTokener(SettingUtil.prepare(setting.value(), type)), type, setting.limits());
}

/**
 * Gets a set containing the names of each config saved in the config directory.
 * This set does not include {@value SettingManager#TEMPORARY_CONFIG}.
 * @return a new set of config names
 */
public Set<String> getAvailableConfigs() {
	try {
		Set<String> set = Files.list(Paths.get(CHUNGAMOD_DIRECTORY, CONFIGURATIONS_DIRECTORY))
				.filter(path -> Files.isDirectory(path))
				.map(path -> path.getFileName().toString())
				.collect(Collectors.toSet());
		set.remove(SettingManager.TEMPORARY_CONFIG);
		return set;
	} catch (IOException e) {
		return Collections.emptySet();
	}
}

/**
 * Parses {@link String} arguments to interact with a setting.<br>
 * If no arguments are provided, a list of all available modules is returned.<br>
 * If one argument is provided, a list of settings and subsetting names is returned.<br>
 * If two arguments are provided, the setting value, or a list of settings if the setting path is a subsetting, is returned.<br>
 * If three arguments are provided, an attempt is made to set a new value to the setting, and the result is returned.<br>
 * If four arguments are provided, an attempt is made to set a new value to the setting in the specified config, and the result is returned.
 * @param args index 0: module name,
 * index 1: setting path separated by "/",
 * index 2: new setting value or "rm" to remove setting,
 * index 3: config name
 * @return a description of the result of the call
 */
public String set(String[] args) {
	if(args.length != 0) {
		ModuleManager<?> manager = getModuleManager(args[0]);
		if(manager == null) return "Module not found.";
		ModuleID<?> m = manager.getModuleID();
		if(args.length > 1) {
			String[] settingPath = args[1].split("/");
			SettingInfo node;
			try {
				node = SettingInfo.getTree(m).getNode(settingPath);
			} catch(InvalidSettingPathException e) {
				return e.getMessage();
			}
			if(node.isTree()) {
				Iterator<SettingInfo> iter = node.iterator();
				if(iter.hasNext()) {
					StringBuilder message = new StringBuilder("Settings for ").append(node.getName()).append(": ").append(iter.next().getName());
					while(iter.hasNext()) message.append(", ").append(iter.next().getName());
					return message.toString();
				}
				return "No settings for " + node.getName();
			}
			Setting setting = node.getSetting();
			try {
				if(args.length > 2) {
					String config = args.length > 3 ? args[3] : null;
					if(args[2].equalsIgnoreCase("rm")) {
						remove(config == null ? SettingManager.TEMPORARY_CONFIG : config, m, manager, settingPath);
						return (config == null ? "Module " : "Config " + config + " module ") + args[0] + " setting " + args[1] + " removed";
					}
					Object newValue;
					try {
						newValue = json.deserialize(new JSONTokener(SettingUtil.prepare(args[2], setting.type())), setting.type());
					} catch(JSONException | IllegalArgumentException e) {
						return e.getMessage();
					}
					set(config == null ? SettingManager.TEMPORARY_CONFIG : config, m, manager, newValue, settingPath);
					return (config == null ? "Set module " : "Set config " + config + " module ") + args[0] + " setting " + args[1] +
							" to: " + Util.toString(newValue);
				}
				return "Module " + args[0] + " setting " + args[1] + " has type: " + setting.type().getSimpleName() +
						", value: " + Util.toString(get(m, manager, settingPath));
			} catch(InvalidSettingPathException | UnsetSettingException e) {
				return e.getMessage();
			}
		}
		Iterator<SettingInfo> iter = SettingInfo.getTree(m).iterator();
		StringBuilder message = new StringBuilder("Settings for ").append(args[0]).append(": ").append(iter.next().getName());
		while(iter.hasNext()) message.append(", ").append(iter.next().getName());
		return message.toString();
	}
	Iterator<ModuleID<?>> iter = Util.sort(getAllModules()).iterator();
	StringBuilder message = new StringBuilder("Modules: ").append(iter.next().getName());
	while(iter.hasNext()) message.append(", ").append(iter.next().getName());
	return message.toString();
}

@SuppressWarnings("unchecked")
public <T> T parseValue(Class<T> toClass, String[] args, int i, Object oldValue, String limits)
		throws JSONException, IllegalArgumentException {
	if(toClass.isArray()) {
		if(oldValue == null) throw new IllegalArgumentException("Old value cannot be null for an array value");
		if(!toClass.isInstance(oldValue)) throw new IllegalArgumentException(
				"Old value class expected: " + toClass.getName() + ", given: " + oldValue.getClass().getName());
		if(args[i].equalsIgnoreCase("add")) {
			if(args.length == i + 1) throw new IllegalArgumentException("Must provide value to add");
			return (T) parseValueUpdateArray(toClass.getComponentType(), args[i + 1], oldValue, limits, true);
		}
		if(args[i].equalsIgnoreCase("rm")) {
			return (T) parseValueUpdateArray(toClass.getComponentType(), null, oldValue, null, false);
		}
	}
	return json.deserialize(new JSONTokener(SettingUtil.prepare(args[i], toClass)), toClass, limits);
}

private <T> T[] parseValueUpdateArray(Class<T> toClass, String newValue, Object oldValue, String limits, boolean add)
		throws JSONException, IllegalArgumentException {
	@SuppressWarnings("unchecked")
	T[] oldValues = (T[]) oldValue;
	if(add) {
		T[] newValues = Arrays.copyOf(oldValues, oldValues.length + 1);
		newValues[oldValues.length] = json.deserialize(new JSONTokener(SettingUtil.prepare(newValue, toClass)), toClass, limits);
		return newValues;
	} else if(oldValues.length == 0) throw new IllegalArgumentException("Cannot remove element from empty array");
	else return Arrays.copyOf(oldValues, oldValues.length - 1);
}

/**
 * Builds a factory for a type.  The requirements to build a factory are as follows:
 * <p style="margin-left:40px">
 * - The given type must have a static method or constructor annotated with {@link Factory} that returns an instance of this type.<br>
 * - Each parameter of this executable must represent a setting path or be of the type
 * {@code net.minecraft.client.Minecraft}, {@code org.apache.logging.log4j.Logger}, or {@code magnileve.chungamod.events.EventManager}<br>
 * - Parameters representing the root path must be of the type {@link SettingTraverser}.<br>
 * - Parameters representing a path to subsettings must be of the type {@link SettingTraverser} and annotated with {@link GetSetting}.<br>
 * - Parameters representing a path to a setting must be of the setting type and annotated with {@link GetSetting}.
 * </p>
 * @param <T> type returned by factory
 * @param forType type returned by factory
 * @param m this type's {@code ModuleManager}, or {@code null} if not applicable
 * @return a {@link Callable} factory for the given type
 * @throws IllegalArgumentException if the given type has no factory method or constructor
 * @throws AnnotationFormatError if this given type's method or constructor annotated with {@code Factory}
 * does not meet the above requirements.
 */
@SuppressWarnings("unchecked")
public <T> Callable<T> getCallableFactory(Class<T> forType, ModuleManager<? extends Module> m) {
	Callable<T> callable = null;
	Parameter[] checkParameters = null;
	for(Constructor<?> constructor:forType.getDeclaredConstructors()) if(constructor.isAnnotationPresent(Factory.class)) {
		accessible(constructor);
		Parameter[] parameters = constructor.getParameters();
		checkParameters = parameters;
		callable = () -> (T) constructor.newInstance(
				getParameterInstances(m, parameters));
	}
	for(Method method:forType.getDeclaredMethods()) if(method.isAnnotationPresent(Factory.class)) {
		staticAccessible(method);
		if(!forType.isAssignableFrom(method.getReturnType()))
			throw new AnnotationFormatError("Factory " + method + " must return instance of " + forType);
		Parameter[] parameters = method.getParameters();
		checkParameters = parameters;
		callable = () -> (T) method.invoke(null,
				getParameterInstances(m, parameters));
	}
	if(callable == null) throw new IllegalArgumentException("No specified constructor or factory method for " + forType);
	for(int i = 0; i < checkParameters.length; i++) {
		if(checkParameters[i].isAnnotationPresent(GetSetting.class)) continue;
		Class<?> type = checkParameters[i].getType();
		if(type.equals(SettingTraverser.class) ||
				type.equals(Minecraft.class) ||
				type.equals(Logger.class) ||
				type.equals(EventManager.class)) continue;
		throw new AnnotationFormatError("Incompatible type in factory parameters for " + forType + ": " + type);
	}
	return callable;
}

private Object[] getParameterInstances(ModuleManager<?> m, Parameter[] parameters)
		throws IOException, IllegalArgumentException, UnsetSettingException {
	Object[] instances = new Object[parameters.length];
	String[] defaultFormats = null;
	Map<String, Object> settings = null;
	for(int i = 0; i < parameters.length; i++) {
		Class<?> type = parameters[i].getType();
		GetSetting settingRequest = parameters[i].getAnnotation(GetSetting.class);
		if(type.equals(SettingTraverser.class)) {
			String[] settingPath = settingRequest == null ? Util.STRING_ARRAY_0 : settingRequest.value();
			if(settingRequest == null || settingRequest.moduleType().equals(Module.class)) {
				if(settings == null) {
					if(m == null) throw new IllegalArgumentException("Cannot get settings for class that isn't a module");
					settings = m.getSettings();
				}
				instances[i] = traverser(m, settingPath);
			} else {
				ModuleManager<?> forModule = getModuleManager(settingRequest.moduleType());
				if(forModule == null) throw new IllegalArgumentException(settingRequest.moduleType() + " is not a registered module");
				instances[i] = traverser(forModule, settingPath);
			}
		} else if(settingRequest != null) {
			ModuleManager<?> forModule;
			Map<String, Object> rootMap;
			if(settingRequest.moduleType().equals(Module.class)) {
				if(settings == null) {
					if(m == null) throw new IllegalArgumentException("Cannot get settings for class that isn't a module");
					settings = m.getSettings();
				}
				forModule = m;
				rootMap = settings;
			} else {
				forModule = getModuleManager(settingRequest.moduleType());
				if(forModule == null) throw new IllegalArgumentException(settingRequest.moduleType() + " is not a registered module");
				rootMap = forModule.getSettings();
			}
			String[] settingPath = settingRequest.value();
			Object value = settingPath.length == 0 ? rootMap :
				SettingUtil.getValue(rootMap, settingPath, SettingInfo.getTree(forModule.getModuleID()));
			if(value instanceof Map) {
				String name = parameters[i].getName();
				name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
				Map<?, ?> map = (Map<?, ?>) value;
				value = map.get(name);
				if(value == null && !map.containsKey(name)) {
					String[] fullPath = Arrays.copyOf(settingPath, settingPath.length + 1);
					fullPath[settingPath.length] = name; 
					throw new UnsetSettingException(fullPath);
				}
			}
			boolean wrapOptional = type.equals(Optional.class);
			Class<?> actualType = wrapOptional ? (Class<?>) ((ParameterizedType) parameters[i].
					getParameterizedType()).getActualTypeArguments()[0] : type;
			if(value == null || value instanceof Object[] && ((Object[]) value).length == 0) {
				if(!(settingRequest.allowNull() || wrapOptional)) throw new UnsetSettingException(settingPath);
				instances[i] = wrapOptional ? Optional.empty() : value;
			} else {
				if(!actualType.isInstance(value)) throw new IllegalArgumentException("For setting " + Util.inverseSplit(settingPath, "/") +
						", expected type: " + actualType + ", received type: " + value.getClass());
				instances[i] = wrapOptional ? Optional.of(Util.recursiveArrayClone(value)) : Util.recursiveArrayClone(value);
			}
		} else if(type.equals(Minecraft.class)) instances[i] = mc;
		else if(type.equals(Logger.class)) {
			if(m == null) throw new IllegalArgumentException("Cannot get logger for class that isn't a module");
			GetLogger a = parameters[i].getAnnotation(GetLogger.class);
			if(a == null) a = GetLogger.DEFAULT_VALUES;
			if(defaultFormats == null) {
				SettingTraverser chungamodSettings = traverser(getModule(ChungamodModule.class));
				defaultFormats = new String[] {
						chungamodSettings.get("LoggingFormat", String.class),
						chungamodSettings.get("LoggingChatFormat", String.class),
						chungamodSettings.get("LoggingFileFormat", String.class)
				};
			}
			ModuleID<?> moduleID = m.getModuleID();
			instances[i] = moduleLogger(m,
					a.customChatLevel() != 0 ?
							Level.forName(moduleID.getName() + '-' + String.valueOf(a.customChatLevel()), a.customChatLevel()) :
							Level.forName(a.chatLevel().name(), a.chatLevel().intLevel()),
					a.customFileLevel() != 0 ?
							Level.forName(moduleID.getName() + '-' + String.valueOf(a.customFileLevel()), a.customFileLevel()) :
							Level.forName(a.fileLevel().name(), a.fileLevel().intLevel()),
					a.directory().isEmpty() ?
							Paths.get(CHUNGAMOD_DIRECTORY, LOGS_DIRECTORY, moduleID.getPluginID() + "-" + moduleID.getName()) :
							Paths.get(a.directory()),
					a.format().isEmpty() ? defaultFormats[0] : a.format(),
					a.chatFormat().isEmpty() ? defaultFormats[1] : a.chatFormat(),
					a.fileFormat().isEmpty() ? defaultFormats[2] : a.fileFormat());
		} else if(type.equals(EventManager.class)) instances[i] = getEventManager(); 
	}
	return instances;
}

/**
 * @throws InvalidSettingPathException {@inheritDoc}
 */
@Override
public SettingTraverser traverser(ModuleID<?> m, String... settingPath) {
	return new SettingTraverser(SettingUtil.getMapOfSettingSafe(getModuleManager(m).getSettings(),
			settingPath, SettingInfo.getTree(m), settingPath.length), settingPath);
}

private SettingTraverser traverser(ModuleManager<?> m, String... settingPath) {
	return new SettingTraverser(SettingUtil.getMapOfSettingSafe(m.getSettings(),
			settingPath, SettingInfo.getTree(m.getModuleID()), settingPath.length), settingPath);
}

public EventManager getEventManager() {
	return eventManager;
}

public JSONManager getJSONManager() {
	return json;
}

public String[] settingPathOn() {
	return SETTING_PATH_ON.clone();
}

}