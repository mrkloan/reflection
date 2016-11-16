package io.fries.bogger.ws.log;

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
	private LogService logService;

	@SparkRoute(path = "/hello")
	private ApiResponse getHelloMessage(Request req, Response res) {
		try {
			return new ApiResponse.Builder(req, res).data(logService.hello()).build();
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ApiResponse.Builder(req, res).data(e.getMessage()).build();
		}
	}
}
