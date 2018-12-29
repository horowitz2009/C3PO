package com.horowitz.seaport.optimize;

import java.util.ArrayList;
import java.util.List;

import com.horowitz.seaport.model.DispatchEntry;

public class Solution {
	public List<DispatchEntry> ships = new ArrayList<>();
	public long latest = 0;
	public int goal;
	public String destination;

	@Override
	public String toString() {
		return "Solution [destination=" + destination + ", goal=" + goal + ", ships=" + ships + "]";
	}
	
	public void combine() {
		
		List<DispatchEntry> newList = new ArrayList<>();
		for (DispatchEntry de : ships) {
			boolean found = false;
			for (DispatchEntry newDE : newList) {
				if (newDE.getShip().equals(de.getShip())) {
					found = true;
					newDE.setTimes(newDE.getTimes() + 1);
				}
			}
			if (!found) {
				DispatchEntry newDE = de.copy();
				newDE.setTimes(de.getTimes());
				newList.add(newDE);
			}
			
		}
		ships = newList;
	}

}