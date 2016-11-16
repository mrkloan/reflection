package io.fries.bogger.ws.core;

import spark.Request;
import spark.Response;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse implements Serializable {

	private final Object data;
	private final Map<String, String> links;
	private final Date timestamp;

	private ApiResponse(Builder builder) {
		this.data = builder.data;
		this.links = builder.links;
		this.timestamp = new Date();
	}

	public Object getData() {
		return data;
	}

	public Map<String, String> getLinks() {
		return links;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public static final class Builder {

		private final Request req;
		private final Response res;

		private Object data;
		private final Map<String, String> links;

		public Builder(Request req, Response res) {
			this.req = req;
			this.res = res;

			this.links = new HashMap<>();
		}

		public Builder status(int status) {
			this.res.status(status);
			return this;
		}

		public Builder header(String header, String value) {
			if(header == null || header.isEmpty() || value == null || value.isEmpty())
				throw new IllegalArgumentException("Header name or value cannot be null or empty.");

			this.res.header(header, value);
			return this;
		}

		public Builder data(Object data) {
			this.data = data;
			return this;
		}

		public Builder link(String name, String href) {
			this.links.put(name, href);
			return this;
		}

		public ApiResponse build() {
			return new ApiResponse(this);
		}
	}
}
