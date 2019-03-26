package io.zeebe.main;

import io.zeebe.example.WorkFlowInitiator;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final WorkFlowInitiator initiator = new WorkFlowInitiator();
		initiator.deployWorkFlow("io/zeebe/workflow/order-process.bpmn");
		initiator.startWorkFlow("order-process", 1);
		
	}
	
}
