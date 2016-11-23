package io.fries.bogger.ws;

import spark.runner.SparkRunner;
import spark.runner.SparkRunnerException;
import spark.runner.annotations.SparkApplication;

@SparkApplication
public class Application {

	public static void main(String[] args) throws SparkRunnerException {
		SparkRunner.startApplication(Application.class);
	}
}
