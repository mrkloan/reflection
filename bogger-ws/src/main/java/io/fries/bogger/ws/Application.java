package io.fries.bogger.ws;

import io.fries.bogger.annotations.spark.SparkApplication;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

@SparkApplication
public class Application {

	private static final String APPLICATION_RESOURCE_BUNDLE = "application";

	private final String[] args;
	private final ResourceBundle applicationProperties;

	public Application(String[] args) {
		this.args = args;
		this.applicationProperties = ResourceBundle.getBundle(APPLICATION_RESOURCE_BUNDLE);

		System.out.println(getProperty("app.name") + " v" + getProperty("app.version") + " started successfully!");
	}

	public String getProperty(String key) {
		try {
			return applicationProperties.getString(key);
		}
		catch(MissingResourceException e) {
			return null;
		}
	}

	public static void main(String[] args) {
		new Application(args);
	}
}
