package io.fries.bogger.ws.core;

import com.google.gson.Gson;
import spark.runner.annotations.SparkComponent;

@SparkComponent
public class ApiParser {

	private final Gson gson;

	public ApiParser() {
		this.gson = new Gson();
	}

	public String json(Object obj) {
		return gson.toJson(obj);
	}

	public <T> T object(String json, Class<T> c) {
		return gson.fromJson(json, c);
	}
}
