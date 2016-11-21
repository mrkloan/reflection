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
	private ApiParser parser;

	@SparkInject
	private HelloService helloService;

	@SparkRoute(path = "")
	private ApiResponse getHelloMessage(Request req, Response res) {
		return new ApiResponse.Builder(req, res).data(helloService.hello()).build();
	}

	@SparkRoute(path = "/:name")
	private ApiResponse getCustomMessage(Request req, Response res) {
		return new ApiResponse.Builder(req, res).data(helloService.hello(req.params("name"))).build();
	}

	@SparkFilter(filter = SparkFilter.Filter.AFTER, path = "/*")
	private void afterCustomHello(Request req, Response res) {
		ApiResponse initialResponse = parser.object(res.body(), ApiResponse.class);
		String data = initialResponse.getData() + " It was nice meeting you. :)";
		ApiResponse newResponse = new ApiResponse.Builder(req, res).data(data).build();

		res.body(parser.json(newResponse));
	}
}
