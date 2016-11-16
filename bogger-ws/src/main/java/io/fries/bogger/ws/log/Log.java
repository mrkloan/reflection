package io.fries.bogger.ws.log;

import java.io.Serializable;
import java.util.Date;

public class Log implements Serializable {

	public enum Level { INFO, WARN, ERROR, FATAL }

	private String application;
	private Level level;
	private String message;
	private Date timestamp;

	public Log() {
		this.timestamp = new Date();
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
