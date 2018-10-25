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

}