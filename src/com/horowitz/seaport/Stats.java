package com.horowitz.seaport;

import java.util.Hashtable;
import java.util.Map;

import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.Ship;

public class Stats {

	private Map<String, Integer> _map = new Hashtable<>();

	// private int _totalShipsSent;
	// private int _buildingsStarted;
	// private int _sawMillStarted;
	// private int _quarryStarted;
	// private int _foundryStarted;

	public void register(String counterName) {
		Integer cnt = 0;
		if (_map.containsKey(counterName))
			cnt = _map.get(counterName);
		_map.put(counterName, cnt + 1);
	}

	public void registerShip(Ship ship) {
		// TODO in future you can extract more data from that ship
		register(ship != null? ship.getName() : "unknown ship");
		register("SHIPS");
	}

	public void registerDestination(Destination dest) {
		// TODO in future you can extract more data from that dest
		register(dest != null? dest.getName():"unknown destination");
		register("DESTINATIONS");
	}

	public int getTotalShipsSent() {
		return getCount("SHIPS");
	}

	public int getCount(String key) {
		int cnt = 0;
		if (_map.containsKey(key))
			cnt = _map.get(key);
		return cnt;
	}

}
