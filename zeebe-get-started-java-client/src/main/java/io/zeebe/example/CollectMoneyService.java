package io.zeebe.example;

import java.util.HashMap;
import java.util.Map;

import io.zeebe.client.ZeebeClient;

public class CollectMoneyService {
	private final static String BROKER_CONTACT = "127.0.0.1:26500";

	public String collectMoney() {
		// do the service logic
		return "Response From CollectMoney";
	}
}
