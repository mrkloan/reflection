package io.fries.bogger.ws.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkException;
import spark.runner.annotations.SparkFilter;
import spark.runner.annotations.SparkInject;

@SparkController
public class CoreController {

	private static final Logger logger = LoggerFactory.getLogger(CoreController.class);

	@SparkInject
	private ApiParser parser;

	@SparkFilter(filter = SparkFilter.Filter.AFTER)
	private void gzipFilter(Request req, Response res) {
		res.header("Content-Encoding", "gzip");
	}

	@SparkException(Exception.class)
	private void genericExceptionHandler(Exception ex, Request req, Response res) {
		ApiResponse response = new ApiResponse.Builder(req, res).status(500).data("An unexpected error occured... :(").build();
		res.body(parser.json(response));

		logger.error(ex.getMessage(), ex);
	}
}
