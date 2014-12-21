package edu.sjsu.cmpe.cache.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class CRDTClient {

	
	private List<CacheServiceInterface> cacheService = new ArrayList<CacheServiceInterface>();
	private final ConcurrentHashMap<String, String> concurrentMap = new ConcurrentHashMap<String, String>();
	private final CountDownLatch countDown = new CountDownLatch(3);

	public CRDTClient(List<String> servers) {
		List<String> node =  servers;
		for (String server_node : node) {
			DistributedCacheService distributeCache = new DistributedCacheService(server_node);
			cacheService.add(distributeCache);
		}
		
	}

	public void put(long key, String value) {

		for (CacheServiceInterface cache : cacheService) {
			cache.write(Long.valueOf(key), value);
		}

		List<CacheServiceInterface> list = new ArrayList<CacheServiceInterface>();
		int temp = 0;
		int response1=0;
		for (CacheServiceInterface cache : cacheService) {
			response1=cache.getResponseCode();
			if (response1 == 200) {
				temp++;
				list.add(cache);
			}
		}

		
		if (temp < 2) {
			for (CacheServiceInterface cache_list : list) {
				cache_list.delete(key);
				
			}
		}

	}

	public String get(long key) {
		String finalValue = null;
		String resValue = null;
		
		for (CacheServiceInterface cache : cacheService) {
			cache.asynchroGet(key, concurrentMap, countDown);
		}

		HashMap<String, Integer> uniqueValueMap = new HashMap<String, Integer>();
		
		try {
			countDown.await();
			System.out.println(" After wait");
			for (String node : concurrentMap.keySet()) {
				resValue = concurrentMap.get(node);
				if (uniqueValueMap.containsKey(resValue)) {

					uniqueValueMap.put(resValue,uniqueValueMap.get(resValue) + 1);
					finalValue = resValue;
				} else {
					uniqueValueMap.put(resValue, 1);
				}
			}
		} catch (Exception e) {
			
		}

		//Read On Repair
		for (String node : concurrentMap.keySet()) {
			
			resValue = concurrentMap.get(node);
			if (!resValue.equals(finalValue)) {
				System.out.println("Read Repair for node "+node + " " + concurrentMap.get(node));
				DistributedCacheService cache = new DistributedCacheService(node);
				cache.put(key, finalValue);
			}
		}

		return finalValue;
	}

}
