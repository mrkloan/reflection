package spark.annotations;

class SparkRunnerException extends Exception {

	SparkRunnerException(String msg) {
		super(msg);
	}

	SparkRunnerException(Throwable t) {
		super(t);
	}

	SparkRunnerException(String msg, Throwable t) {
		super(msg, t);
	}
}
