package io.fries.bogger.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.runner.SparkRunner;
import spark.runner.SparkRunnerException;
import spark.runner.annotations.SparkApplication;
import spark.runner.annotations.SparkFilter;

@SparkApplication
public class Application {

	private static final Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		try {
			SparkRunner.startApplication(Application.class);
		}
		catch(SparkRunnerException e) {
			logger.error(e.getMessage(), e);
		}
	}
}
