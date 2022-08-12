package magnileve.chungamod;

import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.Logger;

import magnileve.chungamod.events.ConnectionEvent;
import magnileve.chungamod.events.EventListener;
import magnileve.chungamod.events.EventManager;
import magnileve.chungamod.events.OnEvent;
import magnileve.chungamod.modules.ChungamodPlugin;
import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Factory;
import magnileve.chungamod.modules.ModuleInfo;
import magnileve.chungamod.modules.Init;
import magnileve.chungamod.modules.Module;
import magnileve.chungamod.settings.GetSetting;
import magnileve.chungamod.settings.Setting;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.json.ValueLimiterSyntaxException;

/**
 * A Chungamod module containing settings for parts of Chungamod functionality.
 * Event listeners with scope {@value magnileve.chungamod.modules.ModuleLoader#SCOPE_SESSION} are also initialized here.
 * @author Magnileve
 */
@ChungamodPlugin(id = "CoreModules", level = ChungamodPlugin.Level.PACKAGE)
@ModuleInfo(name = "Chungamod", category = "Client", description = "Chungamod configurations", alwaysInstantiate = true)
@EventListener(Chung.SCOPE_SINGLETON)
@ContainsInit
@Setting(name = Setting.ON, 		type = Boolean.class, 	value = "true")
//Deprecated
@Setting(name = "TickDelay", 		type = Byte.class, 		value = "1", 		limits = Setting.POSITIVE_INT_NONZERO,
	description = "(Deprecated) Universal tick delay setting to be used for automation modules")
@Setting(name = "LoggingFormat", 	type = String.class, 	value = "\"[\\\\module] \\\\message\"")
@Setting(name = "LoggingChatFormat", type = String.class, 	value = "\"\\\\message\"")
@Setting(name = "LoggingFileFormat", type = String.class, 	value = "\"[\\\\time] \\\\message\"")
@Setting(name = "TimeFormat", 		type = String.class, 	value = "HH:mm:ss", limits = "datetimeformat",
	description = "Time format for logging.  Please don't use 12-hour time.")
public class ChungamodModule implements Module {

private static Logger log;

private final EventManager eventManager;
private final ArrayBuildList<Bucket<Callable<?>, Class<?>[]>> eventListeners;

private Object[] eventListenerInstances;

static void init(Logger logIn) {
	log = logIn;
}

@Init.PreInit1
static void init1() {
	Chung.US.getJSONManager().addLimiter(String.class, "datetimeformat", (value, limits) -> {
		try {
			DateTimeFormatter.ofPattern(value);
		} catch(IllegalArgumentException e) {
			throw new ValueLimiterSyntaxException("Invalid date-time pattern: " + value, e);
		}
	});
}

@Init.PreInit2
static void init2(@GetSetting("TimeFormat") String timeFormat) {
	Chung.US.timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
}

@Factory
ChungamodModule(EventManager eventManager, @GetSetting(Setting.ON) Boolean on) {
	this.eventManager = eventManager;
	this.eventListeners = new ArrayBuildList<>(Bucket.class, 1);
	if(!on) set(true, Chung.SETTING_PATH_ON);
}

@OnEvent
public void onConnection(ConnectionEvent event) {
	Bucket<Callable<?>, Class<?>[]>[] initializers = eventListeners.getArray();
	int l = eventListeners.size();
	if(event.isJoin()) {
		eventListenerInstances = new Object[l];
		for(int i = 0; i < l; i++) try {
			eventListenerInstances[i] = Chung.US.initEventListener(initializers[i].getE1(), initializers[i].getE2());
		} catch (Exception e) {
			log.fatal("Unable to create instance of event listener ",
					e instanceof InvocationTargetException ? e.getCause() : e);
			throw new RuntimeException(e);
		}
	} else {
		for(int i = 0; i < l; i++) {
			Class<?>[] eventClasses = initializers[i].getE2();
			for(int h = 0; h < eventClasses.length; h++) eventManager.removeListener(eventListenerInstances[i], eventClasses[h]);
			eventListenerInstances = null;
		}
	}
}

void addEventListenerForSession(Callable<?> factory, Class<?>[] eventClasses) {
	eventListeners.add(Bucket.of(factory, eventClasses));
}

@Override
public void disable() {
	set(true, Chung.SETTING_PATH_ON);
}

@Override
public void onNewSetting(String[] settingPath, Object value) {
	switch(settingPath[0]) {
	case "TimeFormat":
		Chung.US.timeFormatter = DateTimeFormatter.ofPattern((String) value);
		break;
	}
}

}