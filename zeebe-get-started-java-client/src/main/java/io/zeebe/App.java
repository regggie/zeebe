package io.zeebe;

import java.util.HashMap;
import java.util.Map;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.clients.JobClient;
import io.zeebe.client.api.events.DeploymentEvent;
import io.zeebe.client.api.events.WorkflowInstanceEvent;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.subscription.JobHandler;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		// 1. build client
		final ZeebeClient client = ZeebeClient.newClientBuilder()
				// change the contact point if needed
				.brokerContactPoint("127.0.0.1:26500").build();
		System.out.println("Connected");
		
		// 2. Deploy WorkFlow
		/*final DeploymentEvent deployment = client.newDeployCommand().addResourceFromClasspath("io/zeebe/workflow/order-process.bpmn").send()
				.join();
		final int version = deployment.getWorkflows().get(0).getVersion();
		System.out.println("Workflow deployed. Version: " + version);*/
		
//		 3. Start WorkFlow
		final WorkflowInstanceEvent wfInstance = client.newCreateInstanceCommand()
	            .bpmnProcessId("Process_042pn0g")
	            .latestVersion()
	            .send()
	            .join();
				
		final long workflowInstanceKey = wfInstance.getWorkflowInstanceKey();
		System.out.println("Workflow instance created. Key: " + workflowInstanceKey);
		
		
		client.newWorker().jobType("").handler((jobclient, activatedJob) -> {
			//get the headers from previous job
			final Map<String, Object> headers = activatedJob.getCustomHeaders();
			//get the payload from previous job
			final Map<String, Object> payload = activatedJob.getPayloadAsMap();
			
			//Call to the Microservice
			System.out.println("Calling Service A get the results");
			boolean result = true;
			
			//if you want to pass something to next job add it to headers and Payload 
				//add header
			final Map<String, Object> headerFromA = new HashMap<>();
			headerFromA.put("serviceA.response.header", "headerfromA");
				//add payload
			final Map<String,Object> payLoadFromA = new HashMap<>();
			payload.put("serviceA.response.payloadA", result);
			
			// send to the next workflow task
			
			//.join is like .get() in future 
			
			jobclient.newCompleteCommand(activatedJob.getKey()).payload(payLoadFromA).send().join();
			
			//here goes my Payload from Service A!!!! fly fly fly
		}).open();
		
		client.close();
		System.out.println("Disconnected");

	}
}
