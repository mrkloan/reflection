package io.fries.bogger.ws;

import spark.annotations.SparkApplication;
import spark.annotations.SparkComponentStore;
import spark.annotations.SparkRunner;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

@SparkApplication
public class Application {

	public static void main(String[] args) {
		SparkRunner.startApplication(Application.class);

		ResourceBundle bundle = SparkComponentStore.get(PropertyResourceBundle.class);

		if(bundle != null) {
			System.out.println(bundle.getString("app.name"));
		}
		else
			System.err.println("Bundle is NULL !!!!! ::(((((");
	}
}
