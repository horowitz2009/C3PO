package com.horowitz.seaport.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.junit.Test;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.storage.BalancedProtocolEntryDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;

public class BalancedProtocolTest {

	@Test
	public void test() {
		try {
			ScreenScanner scanner = new ScreenScanner(null);
			MouseRobot mouse = scanner.getMouse();
			MapManager mapManager = new MapManager(scanner);
			mapManager.loadData();
			BalancedShipProtocolExecutor shipProtocolExecutor = new BalancedShipProtocolExecutor(scanner, mouse, mapManager);
			ShipProtocol shipProtocol = new ShipProtocol("TEST");
			List<ProtocolEntry> entries = new ArrayList<ProtocolEntry>();
			ProtocolEntry pe = new ProtocolEntry();

			pe.setChainStr("MX-22.2,S-106.82");
			pe.setShipName("<ALL>");

			pe.deserialize(new BalancedProtocolEntryDeserializer(mapManager, new Ship("[C 27] 1")));
			entries.add(pe);

			List<DispatchEntry> des = pe.getDispatchEntries();
			assertTrue(des != null);
			assertEquals(des.size(), 2);
			
			DispatchEntry de1 = des.get(0);
			
			assertEquals(de1.getDest(), "MX");
			assertTrue(de1.getGoal() - 22.2f <= 0.0000001);
			assertTrue(de1.getTimes() == 0);
			
			DispatchEntry de2 = des.get(1);
			
			assertEquals(de2.getDest(), "S");
			assertTrue(de2.getGoal() - 106.82f <= 0.0000001);
			assertTrue(de2.getTimes() == 0);
			
			shipProtocol.setEntries(entries);
			shipProtocolExecutor.setShipProtocol(shipProtocol);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void test2() {
		try {
			ScreenScanner scanner = new ScreenScanner(null);
			MouseRobot mouse = scanner.getMouse();
			MapManager mapManager = new MapManager(scanner);
			mapManager.loadData();
			BalancedShipProtocolExecutor shipProtocolExecutor = new BalancedShipProtocolExecutor(scanner, mouse, mapManager);
			ShipProtocol shipProtocol = new ShipProtocol("TEST");
			List<ProtocolEntry> entries = new ArrayList<ProtocolEntry>();
			ProtocolEntry pe = new ProtocolEntry();
			
			pe.setChainStr("MX,S");
			pe.setShipName("<ALL>");
			
			pe.deserialize(new BalancedProtocolEntryDeserializer(mapManager, new Ship("[C 27] 1")));
			entries.add(pe);
			
			List<DispatchEntry> des = pe.getDispatchEntries();
			assertTrue(des != null);
			assertEquals(des.size(), 2);
			
			DispatchEntry de1 = des.get(0);
			
			assertEquals(de1.getDest(), "MX");
			assertTrue(de1.getGoal() - 1f <= 0.0000001);
			assertTrue(de1.getTimes() == 0);
			
			DispatchEntry de2 = des.get(1);
			
			assertEquals(de2.getDest(), "S");
			assertTrue(de2.getGoal() - 1f <= 0.0000001);
			assertTrue(de2.getTimes() == 0);
			
			shipProtocol.setEntries(entries);
			shipProtocolExecutor.setShipProtocol(shipProtocol);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void test3() {
		try {
			ScreenScanner scanner = new ScreenScanner(null);
			MouseRobot mouse = scanner.getMouse();
			MapManager mapManager = new MapManager(scanner);
			mapManager.loadData();
			BalancedShipProtocolExecutor shipProtocolExecutor = new BalancedShipProtocolExecutor(scanner, mouse, mapManager);
			ShipProtocol shipProtocol = new ShipProtocol("TEST");
			List<ProtocolEntry> entries = new ArrayList<ProtocolEntry>();
			ProtocolEntry pe = new ProtocolEntry();

			pe.setChainStr("MX-22.2,S-106.82");
			pe.setShipName("<ALL>");

			pe.deserialize(new BalancedProtocolEntryDeserializer(mapManager, new Ship("[C 27] 1")));
			entries.add(pe);

			List<DispatchEntry> des = pe.getDispatchEntries();
			assertTrue(des != null);
			assertEquals(des.size(), 2);
			
			DispatchEntry de1 = des.get(0);
			
			assertEquals(de1.getDest(), "MX");
			assertTrue(de1.getGoal() - 22.2f <= 0.0000001);
			assertTrue(de1.getTimes() == 0);
			
			DispatchEntry de2 = des.get(1);
			
			assertEquals(de2.getDest(), "S");
			assertTrue(de2.getGoal() - 106.82f <= 0.0000001);
			assertTrue(de2.getTimes() == 0);
			
			shipProtocol.setEntries(entries);
			shipProtocolExecutor.setShipProtocol(shipProtocol);
			
			
			///////////////////////////////////////////////////
			///////////////////////////////////////////////////
			Ship ship = new Ship("San Miguel");
			
			float z = 1;
			List<DispatchEntry> allDEs = new JsonStorage().loadDispatchEntries();
			for (DispatchEntry de : des) {
				z *= de.getGoal();
				for (DispatchEntry dep : allDEs) {
					if (dep.getShip().equals(ship.getName()) && dep.getDest().equals(de.getDest())) {
						de.setTimes(dep.getTimes());
						break;
					}
				}
			}

			// now that we have Z and times/ let's calculate the coef
			for (DispatchEntry de : des) {
				de.setCoef(de.getTimes() * z / de.getGoal());
			}

			// next sort by this coef
			Collections.sort(des, new Comparator<DispatchEntry>() {
				@Override
				public int compare(DispatchEntry o1, DispatchEntry o2) {
					return new CompareToBuilder().append(o1.getCoef(), o2.getCoef()).toComparison();
				}
			});

			// finally extract the result in form of dest chain
			LinkedList<Destination> chainList = new LinkedList<Destination>();
			for (DispatchEntry de : des) {
				chainList.add(mapManager.getDestinationByAbbr(de.getDest()));
			}

			System.err.println(chainList);
			//fake send ship to first dest in chain
			mapManager.registerTrip(ship, chainList.get(0));
			

		} catch (Exception e) {
			e.printStackTrace();
    }

	}

	
}
