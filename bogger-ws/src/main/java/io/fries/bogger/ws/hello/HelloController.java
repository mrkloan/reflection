package io.fries.bogger.ws.hello;

import io.fries.bogger.ws.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkInject;
import spark.runner.annotations.SparkRoute;

@SparkController(path = "/hello")
public class HelloController {

	@SparkInject
	private HelloService helloService;

	@SparkRoute(path = "")
	private ApiResponse getHelloMessage(Request req, Response res) {
		try {
			return new ApiResponse.Builder(req, res).data(helloService.hello()).build();
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ApiResponse.Builder(req, res).data(e.getMessage()).build();
		}
	}

	@SparkRoute(path = "/:name")
	private ApiResponse getCustomMessage(Request req, Response res) {
		try {
			return new ApiResponse.Builder(req, res).data(helloService.hello(req.params("name"))).build();
		}
		catch(Exception e) {
			e.printStackTrace();
			return new ApiResponse.Builder(req, res).data(e.getMessage()).build();
		}
	}
}
