package util;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Logger4Finger {
	private Logger log = null;
	//private static String filename = "/home/one/log/log-";
	private static String filename = "D:/log/log-";
	
	public Logger4Finger(String file) {
		log = Logger.getLogger("lavasoft");
		log.setLevel(Level.INFO);
		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.ALL);
		try {
			FileHandler fileHandler = new FileHandler(filename + file + ".txt");
			fileHandler.setLevel(Level.INFO);
			fileHandler.setFormatter(new MyLogHandler());
			log.addHandler(fileHandler);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void info(String str) {
		log.info(str);
	}
}

class MyLogHandler extends Formatter { 
    public String format(LogRecord record) { 
            return record.getLevel() + ":" + record.getMessage()+"\n"; 
    } 
}
