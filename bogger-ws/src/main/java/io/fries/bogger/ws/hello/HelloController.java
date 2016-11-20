package io.fries.bogger.ws.hello;

import io.fries.bogger.ws.core.ApiParser;
import io.fries.bogger.ws.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkFilter;
import spark.runner.annotations.SparkInject;
import spark.runner.annotations.SparkRoute;

@SparkController(path = "/hello")
public class HelloController {

	@SparkInject
	private ApiParser apiParser;

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

	@SparkFilter(filter = SparkFilter.Filter.AFTER, path = "/*")
	private void afterCustomHello(Request req, Response res) {
		try {
			ApiResponse initialResponse = apiParser.object(res.body(), ApiResponse.class);
			String data = initialResponse.getData() + " It was nice meeting you. :)";
			ApiResponse newResponse = new ApiResponse.Builder(req, res).data(data).build();

			res.body(apiParser.json(newResponse));
		}
		catch(Exception e) {
			ApiResponse errorResponse = new ApiResponse.Builder(req, res).data(e.getMessage()).build();
			res.body(apiParser.json(errorResponse));
		}
	}
}
