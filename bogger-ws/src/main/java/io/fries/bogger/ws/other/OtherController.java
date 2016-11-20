package io.fries.bogger.ws.other;

import io.fries.bogger.ws.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkInject;
import spark.runner.annotations.SparkRoute;

@SparkController(path = "/other")
public class OtherController {

	@SparkInject
	private OtherService otherService;

	@SparkRoute(path = "")
	private ApiResponse getOtherResponse(Request req, Response res) {
		String msg = otherService.other();
		return new ApiResponse.Builder(req, res).data(msg).build();
	}
}
