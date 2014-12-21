package edu.sjsu.cmpe.cache.client;


import java.util.ArrayList;
import java.util.List;


public class Client {

    public static void main(String[] args) throws Exception {
                       
        List<String> list= new ArrayList<String>();
    	list.add("http://localhost:3000");
    	list.add("http://localhost:3001");
    	list.add("http://localhost:3002");
    	
    	
    	CRDTClient crdtClient= new CRDTClient(list);
    	
    	System.out.println("Writing value 1==>a");
    	crdtClient.put(1, "a");	
    	System.out.println("Writing value 1==>b");
		Thread.sleep(30000);                
    	CRDTClient crdtClient1= new CRDTClient(list);
    	crdtClient1.put(1, "b");
    	System.out.println("@@@@ Get Vale 1 @@@@ Sep 3");
    	Thread.sleep(30000);                
    	CRDTClient crdt2= new CRDTClient(list);
    	String var=crdt2.get(1);
    	System.out.println("Value is "+var);
    }

}
