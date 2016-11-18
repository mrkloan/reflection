package spark.runner;

import spark.Spark;
import spark.route.RouteOverview;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

final class SparkConfiguration implements Runnable {

	// WebServer
	private static final String PROPERTY_PORT = "spark.port";

	// Threads
	private static final String PROPERTY_THREADS_MAX = "spark.threads.max";
	private static final String PROPERTY_THREADS_MIN = "spark.threads.min";
	private static final String PROPERTY_THREADS_TIMEOUT = "spark.threads.timeout";

	// SSL
	private static final String PROPERTY_KEYSTORE = "spark.keystore";
	private static final String PROPERTY_KEYSTORE_PWD = "spark.keystore.password";
	private static final String PROPERTY_TRUSTSTORE = "spark.truststore";
	private static final String PROPERTY_TRUSTSTORE_PWD = "spark.truststore.password";

	// Routes
	private static final String PROPERTY_ROUTE_OVERVIEW = "spark.route.overview";

	// Static
	private static final String PROPERTY_STATIC = "spark.static";
	private static final String PROPERTY_STATIC_CACHE = "spark.static.cache";

	private final ResourceBundle bundle;

	SparkConfiguration(ResourceBundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public void run() {
		this.port()
			.threads()
			.ssl()
			.routeOverview()
			.staticFiles();
	}

	private SparkConfiguration port() {
		try {
			Spark.port(Integer.parseInt(bundle.getString(PROPERTY_PORT)));
		}
		catch(MissingResourceException e) { /* Do nothing */ }

		return this;
	}

	private SparkConfiguration threads() {
		try {
			int max = Integer.parseInt(bundle.getString(PROPERTY_THREADS_MAX));

			try {
				int min = Integer.parseInt(bundle.getString(PROPERTY_THREADS_MIN));
				int timeout = Integer.parseInt(bundle.getString(PROPERTY_THREADS_TIMEOUT));

				Spark.threadPool(max, min, timeout);
			}
			catch(MissingResourceException e) {
				Spark.threadPool(max);
			}

		}
		catch(MissingResourceException e) { /* Do nothing */ }

		return this;
	}

	private SparkConfiguration ssl() {
		try {
			String keystore = bundle.getString(PROPERTY_KEYSTORE);
			String keystorePwd = bundle.getString(PROPERTY_KEYSTORE_PWD);

			try {
				String truststore = bundle.getString(PROPERTY_TRUSTSTORE);
				String truststorePwd = bundle.getString(PROPERTY_TRUSTSTORE_PWD);

				Spark.secure(keystore, keystorePwd, truststore, truststorePwd);
			}
			catch(MissingResourceException ex) {
				Spark.secure(keystore, keystorePwd, null, null);
			}
		}
		catch(MissingResourceException e) { /* Do nothing */ }

		return this;
	}

	private SparkConfiguration routeOverview() {
		try {
			RouteOverview.enableRouteOverview(bundle.getString(PROPERTY_ROUTE_OVERVIEW));
		}
		catch(MissingResourceException e) { /* Do nothing */ }

		return this;
	}

	private SparkConfiguration staticFiles() {
		try {
			Spark.staticFiles.location(bundle.getString(PROPERTY_STATIC));
			Spark.staticFiles.expireTime(Integer.parseInt(bundle.getString(PROPERTY_STATIC_CACHE)));
		}
		catch(MissingResourceException e) { /* Do nothing */ }

		return this;
	}
}
