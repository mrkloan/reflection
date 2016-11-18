package io.fries.bogger.ws.log;

import io.fries.bogger.ws.Application;
import io.fries.bogger.ws.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.annotations.SparkComponent;
import spark.annotations.SparkController;
import spark.annotations.SparkInject;
import spark.annotations.SparkRoute;

@SparkComponent
@SparkController
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
