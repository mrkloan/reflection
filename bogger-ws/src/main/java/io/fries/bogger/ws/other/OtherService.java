package io.fries.bogger.ws.other;

import spark.runner.annotations.SparkComponent;

@SparkComponent
public class OtherService {

	public String other() {
		return "Because you know?";
	}
}
