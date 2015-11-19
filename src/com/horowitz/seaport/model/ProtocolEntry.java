package com.horowitz.seaport.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;

import com.horowitz.commons.Deserializable;
import com.horowitz.seaport.model.storage.Deserializer;

public class ProtocolEntry implements Serializable, Cloneable, Deserializable {

	private static final long serialVersionUID = 3004805811414644172L;
	private String _shipName;
	private String _chainStr;
	private transient Ship _ship;
	private transient LinkedList<Destination> _chain;

	@Override
	public Object clone() throws CloneNotSupportedException {

		return super.clone();
	}

	@Override
	public void deserialize(Deserializer deserializer) throws IOException {
		deserializer.deserialize(this);
	}

	public String getShipName() {
		return _shipName;
	}

	public void setShipName(String shipName) {
		_shipName = shipName;
	}

	public String getChainStr() {
		return _chainStr;
	}

	public void setChainStr(String chainStr) {
		_chainStr = chainStr;
	}

	public Ship getShip() {
		return _ship;
	}

	public void setShip(Ship ship) {
		_ship = ship;
		_shipName = ship.getName();
	}

	public LinkedList<Destination> getChain() {
		return _chain;
	}

	public void setChain(LinkedList<Destination> chain) {
		_chain = chain;
		/*
		String s = "";
		for (Destination d : chain) {
	    s += d.getAbbr() + ",";
    }
		if (s.length() > 0)
			s = s.substring(0, s.length() - 2);
		_chainStr = s;
		*/
	}
}
