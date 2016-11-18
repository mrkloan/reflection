package io.fries.bogger.ws.log;

import spark.runner.annotations.SparkComponent;

@SparkComponent
public class LogService {

	public String hello() {
		return "Hello World!";
	}
}
