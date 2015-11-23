package com.horowitz.seaport.model.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.horowitz.commons.Deserializable;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.DispatchEntry;
import com.horowitz.seaport.model.ProtocolEntry;
import com.horowitz.seaport.model.Ship;

public class BalancedProtocolEntryDeserializer implements Deserializer {

	private MapManager _mapManager;

	public BalancedProtocolEntryDeserializer(MapManager mapManager) {
		super();
		_mapManager = mapManager;
	}

	@Override
	public void deserialize(Deserializable deserializable) throws IOException {
		ProtocolEntry entry = (ProtocolEntry) deserializable;
		String s = entry.getChainStr().toUpperCase();
		String[] ss = s.split(",");

		List<DispatchEntry> des = new ArrayList<DispatchEntry>();
		for (String ds : ss) {
			DispatchEntry de = new DispatchEntry();

			String[] sss = ds.split("-");
			de.setDest(sss[0]);
			float goal = 1.0f;
			if (sss.length > 1) {
				try {
					goal = Float.parseFloat(sss[1]);
				} catch (NumberFormatException e) {
					// TODO report for this error
					e.printStackTrace();
				}
			}
			de.setGoal(goal);
			de.setDest(ds);
			de.setShip(entry.getShipName());

			des.add(de);
		}
		entry.setDispatchEntries(des);
		
		
		//OLD
		
		LinkedList<Destination> chain = new LinkedList<Destination>();
		for (String ds : ss) {
			if (ds.indexOf("-") > 0) {
				ds = ds.split("-")[0];
			}

			Destination dest = _mapManager.getDestinationByAbbr(ds.trim());
			if (dest != null) {
				chain.add(dest);
			}
		}
		entry.setChain(chain);
		
		
		if (entry.getShipName().startsWith("<")) {
			entry.setShip(new Ship(entry.getShipName()));
		} else {
			entry.setShip(_mapManager.getShip(entry.getShipName()));
		}

	}

}
