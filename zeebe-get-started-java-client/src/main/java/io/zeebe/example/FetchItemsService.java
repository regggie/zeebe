package io.zeebe.example;

import java.util.HashMap;
import java.util.Map;

import io.zeebe.client.ZeebeClient;

public class FetchItemsService {
	private final static String BROKER_CONTACT = "127.0.0.1:26500";

	public String fetchItems() {
		return "Response From FetchItems";
	}
}
