package com.horowitz.seaport.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShipProtocol implements Serializable, Cloneable {
	private static final long serialVersionUID = 2582381532488603423L;

	private String _name;
	private int _slot;
	private boolean _enabled;
	
	private List<ProtocolEntry> _entries;

	public ShipProtocol(String name) {
		_name = name;
		_slot = -1;
	}

	public ShipProtocol() {
		super();
	}

	public int getSlot() {
		return _slot;
	}

	public void setSlot(int slot) {
		_slot = slot;
	}

	@Override
	public String toString() {
		return getName();
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	public boolean isEnabled() {
		return _enabled;
	}

	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}
	
  @Override
  public Object clone() throws CloneNotSupportedException {
    Object clone = super.clone();
    ShipProtocol other = (ShipProtocol) clone;
    other._entries = new ArrayList<ProtocolEntry>(this._entries);
    for (ProtocolEntry pe : other._entries) {
	    pe = (ProtocolEntry) pe.clone();
    }
    
		return clone;
  }

	public List<ProtocolEntry> getEntries() {
		return _entries;
	}

	public void setEntries(List<ProtocolEntry> entries) {
		_entries = entries;
	}

}
