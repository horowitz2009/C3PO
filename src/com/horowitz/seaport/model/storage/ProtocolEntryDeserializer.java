package com.horowitz.seaport.model.storage;

import java.io.IOException;
import java.util.LinkedList;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.ProtocolEntry;
import com.horowitz.seaport.model.Ship;

public class ProtocolEntryDeserializer implements Deserializer {

	private MapManager _mapManager;

	public ProtocolEntryDeserializer(MapManager mapManager) {
		super();
		_mapManager = mapManager;
	}

	@Override
	public void deserialize(Deserializable deserializable) throws IOException {
		ProtocolEntry entry = (ProtocolEntry) deserializable;
		String s = entry.getChainStr().toUpperCase();
		String[] ss = s.split(",");
		LinkedList<Destination> chain = new LinkedList<Destination>();
		for (String ds : ss) {
      if (ds.indexOf("-") > 0)
      	ds = ds.split("-")[0];
      
			Destination dest = _mapManager.getDestinationByAbbr(ds.trim());
			if (dest != null) {
				chain.add(dest);
			}
		}
		entry.setChain(chain);
		if(entry.getShipName().startsWith("<")) {
			entry.setShip(new Ship(entry.getShipName()));
		} else {
		  entry.setShip(_mapManager.getShip(entry.getShipName()));
		}

	}

}
