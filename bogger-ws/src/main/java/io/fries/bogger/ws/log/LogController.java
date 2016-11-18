package io.fries.bogger.ws.log;

import io.fries.bogger.ws.Application;
import io.fries.bogger.ws.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkComponent;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkInject;
import spark.runner.annotations.SparkRoute;

@SparkController(path = "/log")
public class LogController {

	@SparkInject
	private Application app;

	@SparkInject
	private LogService logService;

	@SparkRoute(path = "/")
	private String getHelloMessage(Request req, Response res) {
		try {
			return app.json(new ApiResponse.Builder(req, res).data(logService.hello()).build());
		}
		catch(Exception e) {
			e.printStackTrace();
			return app.json(new ApiResponse.Builder(req, res).data(e.getMessage()).build());
		}
	}
}
