package magnileve.chungamod.modules;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.MessageFactory2;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.MessageFactory2Adapter;

import magnileve.chungamod.Chung;
import magnileve.chungamod.util.MCUtil;

/**
 * <p>
 * A logger for a module.
 * In addition to application logging, this logger can log to chat and its own files.
 * This logger processes messages using its given format strings as follows:
 * </p>
 * <p style = "margin-left:40px">
 * - Any occurrence of "\message" is replaced with the input log message.<br>
 * - Any occurrence of "\time" is replaced with the current time.<br>
 * - Any occurrence of "\module" is replaced with the module name.
 * </p>
 * @author Magnileve
 */
public class ModuleLogger extends AbstractLogger {

private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
private static final DateTimeFormatter DATE_REGEX_FORMAT = DateTimeFormatter.ofPattern("yyyy\\-MM\\-dd");
private static final Pattern FORMAT_PATTERN = Pattern.compile("(.*?)\\\\(message|time|module)");
private static final long serialVersionUID = 4192275626350901981L;

private final Logger log;
private final ModuleID<?> logModule;
private final Writer stream;
private final Runnable streamShutdown;
private final Level chatLevel;
private Level fileLevel; //should be treated as final except when set to Level.OFF due to an IO error
private final Formatter chatFormatter;
private final Formatter fileFormatter;
private final int intLevel;

/**
 * Creates a new {@code ModuleLogger}.
 * @param logModule module of this logger
 * @param log Log4j Logger
 * @param chatLevel least specific logging level for chat messages
 * @param fileLevel least specific logging level for file messages
 * @param directory directory for log files
 * @param format logging format
 * @param chatFormat chat logging format
 * @param fileFormat file logging format
 * @throws IOException if this logger logs to a file, and the file cannot be created and opened
 */
public ModuleLogger(ModuleID<?> logModule, Logger log, Level chatLevel, Level fileLevel,
		Path directory, String format, String chatFormat, String fileFormat) throws IOException {
	super(logModule.getName(), createDefaultMessageFactory(format, logModule.getName()));
	this.log = log;
	this.logModule = logModule;
	Writer getStream = null;
	if(!fileLevel.equals(Level.OFF)) {
		if(!Files.isDirectory(directory)) Files.createDirectories(directory);
		LocalDate date = LocalDate.now();
		Pattern datePattern = Pattern.compile(DATE_REGEX_FORMAT.format(date) + "(\\-\\d+)?\\.log");
		
		int logNumber = Files.list(directory)
				.map(path -> path.getFileName().toString())
				.filter(name -> datePattern.matcher(name).matches())
				.reduce(-1, (value, name) -> Math.max(value, name.length() == 14 ? 0 :
					Integer.parseInt(name.substring(11, name.length() - 4))), Math::max) + 1;
		getStream = Files.newBufferedWriter(directory.resolve(DATE_FORMAT.format(date) + (logNumber == 0 ? ".log" : "-" + logNumber + ".log")));
	}
	stream = getStream;
	if(stream == null) streamShutdown = null;
	else {
		streamShutdown = () -> {
			try {
				stream.close();
			} catch (IOException e) {}
		};
		Chung.SYSTEM.addShutdownHook(streamShutdown);
	}
	this.chatLevel = chatLevel;
	this.fileLevel = stream == null ? Level.OFF : fileLevel;
	intLevel = Math.min(chatLevel.intLevel(), fileLevel.intLevel());
	chatFormatter = newFormatter(chatFormat, logModule.getName());
	fileFormatter = newFormatter(fileFormat, logModule.getName());
}

@Override
public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
	String messageString = message.getFormattedMessage();
	log.log(level, marker, message, t);
	if(level.isMoreSpecificThan(chatLevel)) MCUtil.trySendMessage(chatFormatter.format(messageString));
	if(level.isMoreSpecificThan(fileLevel)) try {
		stream.write(fileFormatter.format(messageString));
		stream.write('\n');
		stream.flush();
	} catch(IOException e) {
		if(Level.ERROR.isMoreSpecificThan(chatLevel)) MCUtil.trySendMessage("Failed to write to log file for " + logModule.getName());
		log.error("Failed to write to log file for " + logModule.getName(), e);
		fileLevel = Level.OFF;
		if(Chung.SYSTEM.removeShutdownHook(streamShutdown)) streamShutdown.run();
	}
}

@Override
protected void finalize() {
	if(streamShutdown != null && Chung.SYSTEM.removeShutdownHook(streamShutdown)) streamShutdown.run();
}

@FunctionalInterface
private static interface Formatter {
	String format(String message);
}

private enum FormatSegment {
	MESSAGE {
		@Override
		void appendTo(StringBuilder message, Object value) {
			message.append((String) value);
		}
	}, MODULE {
		@Override
		void appendTo(StringBuilder message, Object value) {
			message.append((String) value);
		}
	}, TIME {
		@Override
		void appendTo(StringBuilder message, Object value) {
			message.append(((DateTimeFormatter) value).format(LocalTime.now()));
		}
	};
	
	abstract void appendTo(StringBuilder message, Object value);
}

private static Formatter newFormatter(String format, String name) {
	Matcher m = FORMAT_PATTERN.matcher(format);
	ArrayList<String> getText = new ArrayList<>();
	ArrayList<FormatSegment> getFormats = new ArrayList<>();
	ArrayList<Object> getFormatInputs = new ArrayList<>();
	
	int end = 0;
	while(m.find()) {
		getText.add(m.group(1));
		FormatSegment formatSegment = Enum.valueOf(FormatSegment.class, m.group(2).toUpperCase());
		getFormats.add(formatSegment);
		Object value;
		switch(formatSegment) {
		case MESSAGE:
			value = null;
			break;
		case MODULE:
			value = name;
			break;
		case TIME:
			value = Chung.US.getTimeFormatter();
			break;
		default:
			throw new Error();
		}
		getFormatInputs.add(value);
		end = m.end();
	}
	getText.add(format.substring(end, format.length()));
	
	if(getText.size() == 2 && getFormats.get(0).equals(FormatSegment.MESSAGE)) return message -> message;
	
	String[] text = new String[getText.size()];
	FormatSegment[] formats = new FormatSegment[getFormats.size()];
	Object[] formatInputs = new Object[getFormatInputs.size()];
	for(int i = 0; i < text.length; i++) text[i] = getText.get(i);
	for(int i = 0; i < formats.length; i++) formats[i] = getFormats.get(i);
	for(int i = 0; i < formatInputs.length; i++) formatInputs[i] = getFormatInputs.get(i);
	
	return messageIn -> {
		StringBuilder message = new StringBuilder();
		for(int i = 0; i < formats.length; i++) {
			Object value = formatInputs[i];
			formats[i].appendTo(message.append(text[i]), value == null ? messageIn : value);
		}
		return message.append(text[formats.length]).toString();
	};
}

@Override
public Level getLevel() {
	return log.getLevel();
}

@Override
public boolean isEnabled(Level level, Marker marker) {
	return level.intLevel() <= intLevel || log.isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3,
		Object p4) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3,
		Object p4, Object p5) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3,
		Object p4, Object p5, Object p6) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3,
		Object p4, Object p5, Object p6, Object p7) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3,
		Object p4, Object p5, Object p6, Object p7, Object p8) {
	return isEnabled(level, marker);
}

@Override
public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3,
		Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
	return isEnabled(level, marker);
}

/**
 * Extended default message factory from <code>AbstractLogger</code>.
 * @param format this message factory's format
 * @param name module name
 * @return default message factory
 */
private static MessageFactory2 createDefaultMessageFactory(String format, String name) {
	MessageFactory2 result;
    try {
        MessageFactory getResult = DEFAULT_MESSAGE_FACTORY_CLASS.newInstance();
        result = getResult instanceof MessageFactory2 ? (MessageFactory2) getResult : new MessageFactory2Adapter(getResult);
    } catch(InstantiationException | IllegalAccessException e) {
        throw new IllegalStateException(e);
    }
    return new MessageFactory2() {
    	private final Formatter formatter = newFormatter(format, name);
    	
		@Override
		public Message newMessage(Object message) {
			return result.newMessage(formatter.format(message == null ? null : message.toString()));
		}

		@Override
		public Message newMessage(String message) {
			return result.newMessage(formatter.format(message));
		}

		@Override
		public Message newMessage(String message, Object... params) {
			return result.newMessage(formatter.format(message), params);
		}

		@Override
		public Message newMessage(CharSequence charSequence) {
			return result.newMessage(formatter.format(charSequence.toString()));
		}

		@Override
		public Message newMessage(String message, Object p0) {
			return result.newMessage(formatter.format(message), p0);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1) {
			return result.newMessage(formatter.format(message), p0, p1);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2) {
			return result.newMessage(formatter.format(message), p0, p1, p2);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3) {
			return result.newMessage(formatter.format(message), p0, p1, p2, p3);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
			return result.newMessage(formatter.format(message), p0, p1, p2, p3, p4);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
			return result.newMessage(formatter.format(message), p0, p1, p2, p3, p4, p5);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
				Object p6) {
			return result.newMessage(formatter.format(message), p0, p1, p2, p3, p4, p5, p6);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
				Object p6, Object p7) {
			return result.newMessage(formatter.format(message), p0, p1, p2, p3, p4, p5, p6, p7);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
				Object p6, Object p7, Object p8) {
			return result.newMessage(formatter.format(message), p0, p1, p2, p3, p4, p5, p6, p7, p8);
		}

		@Override
		public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5,
				Object p6, Object p7, Object p8, Object p9) {
			return result.newMessage(formatter.format(message), p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
		}
    	
    };
}

}