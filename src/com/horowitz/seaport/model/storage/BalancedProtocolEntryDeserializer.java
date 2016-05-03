package com.horowitz.seaport.model.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.DispatchEntry;
import com.horowitz.seaport.model.ProtocolEntry;
import com.horowitz.seaport.model.Ship;

public class BalancedProtocolEntryDeserializer implements Deserializer {

	private MapManager _mapManager;
	private Ship _ship;

	public BalancedProtocolEntryDeserializer(MapManager mapManager, Ship ship) {
		super();
		_mapManager = mapManager;
		_ship = ship;
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
			de.setShip(_ship.getName());

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
		
		
		try {
			entry.setShip((Ship)_ship.clone());//do I need clone?
//			
//			
//	    if (entry.getShipName().startsWith("<")||entry.getShipName().startsWith("[")) {
//	    	entry.setShip((Ship)_ship.clone());//do I need clone?
//	    } else {
//	    	entry.setShip(_mapManager.getShip(entry.getShipName()));
//	    }
    } catch (CloneNotSupportedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
    }

	}

}
