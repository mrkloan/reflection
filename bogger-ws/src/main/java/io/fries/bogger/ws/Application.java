package io.fries.bogger.ws;

import java.util.ResourceBundle;

public class Application {

	public static void main(String[] args) {
		ResourceBundle bundle = ResourceBundle.getBundle("application");
		System.out.println(bundle.getString("app.name") + " v" + bundle.getString("app.version") + " started successfully!");
	}
}
