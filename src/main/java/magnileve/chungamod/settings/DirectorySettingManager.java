package magnileve.chungamod.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import magnileve.chungamod.modules.ModuleID;
import magnileve.chungamod.util.json.JSONManager;

/**
 * A {@code SettingManager} that stores settings in JSON files.
 * The root directory of this setting manager contains directories of each config,
 * and each directory of a config contains JSON files named with plugin IDs.
 * @author Magnileve
 */
public class DirectorySettingManager extends AbstractSettingManager {

private final Path directory;
private final BiFunction<String, String, ModuleID<?>> getModule;

/**
 * Creates a new {@code DirectorySettingManager}.
 * @param log logger
 * @param json converts settings between JSON and Java objects
 * @param configs initial enabled configs
 * @param mapFactory creates setting maps
 * @param directory directory of configs
 * @param getModule gets a module from a name and plugin ID
 */
public DirectorySettingManager(Logger log, JSONManager json, Collection<String> configs, SettingMapFactory mapFactory,
		Path directory, BiFunction<String, String, ModuleID<?>> getModule) {
	super(log, json, configs, mapFactory);
	this.directory = directory;
	this.getModule = getModule;
}

@Override
protected Reader getReader(String config, String pluginID) throws IOException {
	Path path = directory.resolve(Paths.get(config, pluginID + ".json"));
	if(!Files.isReadable(path)) {
		if(Files.exists(path)) throw new IOException("Unable to read file " + path);
		return new StringReader("{}");
	}
	return Files.newBufferedReader(path);
}

@Override
protected Writer getWriter(String config, String pluginID) throws IOException {
	Path dir = directory.resolve(config);
	if(!Files.isDirectory(dir)) Files.createDirectories(dir);
	return Files.newBufferedWriter(dir.resolve(pluginID + ".json"));
}

@Override
protected Set<String> getPluginsInConfig(String config) {
	Stream<Path> paths;
	try {
		paths = Files.list(directory.resolve(config));
	} catch(IOException e) {
		paths = Collections.<Path>emptyList().stream();
	}
	return paths.filter(path -> Files.isRegularFile(path))
			.map(path -> path.getFileName().toString())
			.filter(name -> name.endsWith(".json"))
			.map(name -> name.substring(0, name.length() - 5))
			.collect(Collectors.toSet());
}

@Override
protected void saveEmpty(String config, String pluginID) throws IOException {
	Path dir = directory.resolve(config);
	Path file = dir.resolve(pluginID + ".json");
	Files.deleteIfExists(file);
	if(!Files.list(dir).findAny().isPresent()) Files.delete(dir);
}

@Override
protected ModuleID<?> getModule(String name, String pluginID) {
	return getModule.apply(name, pluginID);
}

}