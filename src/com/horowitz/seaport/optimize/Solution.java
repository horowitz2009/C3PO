package com.horowitz.seaport.optimize;

import java.util.HashSet;
import java.util.Set;

import com.horowitz.seaport.model.DispatchEntry;

public class Solution {
	public Set<DispatchEntry> ships = new HashSet<>();
	public long latest = 0;
	public int goal;
}