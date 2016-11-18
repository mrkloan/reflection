package io.fries.bogger.ws.hello;

import spark.runner.annotations.SparkComponent;

@SparkComponent
public class HelloService {

	public String hello() {
		return hello("World");
	}

	public String hello(String name) {
		return "Hello " + name + "!";
	}
}
