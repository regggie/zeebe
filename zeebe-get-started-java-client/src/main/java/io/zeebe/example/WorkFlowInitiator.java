package io.zeebe.example;

import java.util.HashMap;
import java.util.Map;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.ZeebeClientBuilder;
import io.zeebe.client.api.events.DeploymentEvent;
import io.zeebe.client.api.events.WorkflowInstanceEvent;

public class WorkFlowInitiator {

	private final static String BROKER_CONTACT = "127.0.0.1:26500";

	// may be get name and version from deployWrokFlow api response
	public void startWorkFlow(String name, int version) {
		// Build client
		final ZeebeClient client = ZeebeClient.newClientBuilder().brokerContactPoint(BROKER_CONTACT).build();
		System.out.println("Connected");
		
		//one level before opening worker from collect-money
		client.newWorker().jobType("payment-service").handler((jobclient, activatedJob) -> {
			// get the headers from previous job
			final Map<String, Object> headers = activatedJob.getCustomHeaders();
			// get the payload from previous job
			final Map<String, Object> payload = activatedJob.getPayloadAsMap();

			// Call to the Microservice
			System.out.println("Calling payment-service to get the results");
			
			CollectMoneyService collectMoneyService = new CollectMoneyService();
			String result = collectMoneyService.collectMoney();

			// if you want to pass something to next job add it to Payload			
			// add payload
			final Map<String, Object> collectMoneyResponse = new HashMap<>();
			payload.put("collect-money-reponse", result);

			// send to the next workflow task
			// .join is like .get() in future
			jobclient.newCompleteCommand(activatedJob.getKey()).payload(collectMoneyResponse).send().join();

			// here goes my Payload from Service A!!!! fly fly fly
		}).open();
		
		client.newWorker().jobType("inventory-service").handler((jobclient, activatedJob) -> {
			// get the headers from previous job
			final Map<String, Object> headers = activatedJob.getCustomHeaders();
			// get the payload from previous job
			final Map<String, Object> payload = activatedJob.getPayloadAsMap();
			String responseFromCollectMoney = (String) payload.get("collect-money-reponse");
			
			// Call to the Microservice
			System.out.println("Calling Service inventory-service get the results");
			
			FetchItemsService fetchService = new FetchItemsService();
			String result = responseFromCollectMoney.concat("--->").concat(fetchService.fetchItems());
			System.out.println("------------->result "+ result);
			// add payload
			final Map<String, Object> fetchItemsResponse = new HashMap<>();
			payload.put("fetch-items-reponse", result);

			// send to the next workflow task
			// .join is like .get() in future
			
			jobclient.newCompleteCommand(activatedJob.getKey()).payload(fetchItemsResponse).send().join();

			// here goes my Payload from Service A!!!! fly fly fly
		}).open();
		
		client.newWorker().jobType("shipment-service").handler((jobclient, activatedJob) -> {
			// get the payload from previous job
			final Map<String, Object> payload = activatedJob.getPayloadAsMap();
			String responseFromFetchItems = (String) payload.get("fetch-items-reponse");

			// Call to the Microservice
			System.out.println("Calling shipment-service to get the results");

			ShipmentService shipmentService = new ShipmentService();
			String result = responseFromFetchItems.concat("--->").concat(shipmentService.shipItems());

			// add payload
			final Map<String, Object> collectMoneyResponse = new HashMap<>();
			payload.put("fetch-items-reponse", result);

			// send to the next workflow task
			// .join is like .get() in future
			System.out.println("Printing out final result:" + result);
			jobclient.newCompleteCommand(activatedJob.getKey()).payload(collectMoneyResponse).send().join();

			// here goes my Payload from Service A!!!! fly fly fly
		}).open();

		final WorkflowInstanceEvent createEvent = client.newCreateInstanceCommand().bpmnProcessId(name).latestVersion()
				.send().join();
		createEvent.getWorkflowInstanceKey();
		// this is per version deployed
		createEvent.getWorkflowKey();
		createEvent.getVersion();
		
		System.out.printf("WorkFlow of key: %d Instance ID : %d Version: %d ",
				createEvent.getWorkflowKey(), createEvent.getWorkflowInstanceKey(), createEvent.getVersion());
		//client.close();
	}

	// may be return BPMN process ID and VERSION used for starting the workflow
	public void deployWorkFlow(String name) {
		// Build client
		final ZeebeClient client = ZeebeClient.newClientBuilder().brokerContactPoint(BROKER_CONTACT).build();
		System.out.println("Connected");
		final DeploymentEvent deployment = client.newDeployCommand().addResourceFromClasspath(name).send().join();
		// log deployed version and
		int version = deployment.getWorkflows().get(0).getVersion();
		String bpnmProcessId = deployment.getWorkflows().get(0).getBpmnProcessId();
		long workFlowKey = deployment.getWorkflows().get(0).getWorkflowKey();
		// Return version and processID
		client.close();
	}
	
	
	
}
