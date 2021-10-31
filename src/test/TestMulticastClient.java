package test;

import java.io.IOException;

import network.MulticastClient;

public class TestMulticastClient {
	public static void main(String[] args) throws IOException, InterruptedException {
		MulticastClient mc = new MulticastClient();
		mc.start();
		//for (int i=0; i< 10; i++ ) {
			mc.sendPing();				
		//}
		Thread.sleep(10000);
		mc.interrupt();
		mc.join();
		
		for (MulticastClient.Message msg : mc.getMessages()) {
			System.out.println(msg.getIp() + " " + msg.getContent());
		}
		
		mc.close();
	}
}
