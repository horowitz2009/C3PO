package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.storage.BalancedProtocolEntryDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;

public class BalancedShipProtocol extends BaseShipProtocol {

	private ShipProtocol _shipProtocol;

	public BalancedShipProtocol(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
		super(scanner, mouse, mapManager);
	}

	void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException {

		scanShipName(pin);

		_mouse.click(pin);
		_mouse.delay(50);
		_mouse.mouseMove(_scanner.getParkingPoint());
		_mouse.delay(500);

		Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
		if (anchor != null) {
			// MAP IS OPEN
			_mapManager.ensureMap();

			Ship ship = _lastShip;
			if (ship == null) {
				ship = new Ship("<Unknown>");
			}
			List<ProtocolEntry> entries = _shipProtocol.getEntries();

			ProtocolEntry pe = findSuitableProtocolEntry(ship, entries);

			if (pe != null) {

				// do what you gotta do with pe
				// end product: _destChain

				pe.deserialize(new BalancedProtocolEntryDeserializer(_mapManager));

				// TODO NOT GOOD _destChain = pe.getChain();
				List<DispatchEntry> des = pe.getDispatchEntries();
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

				/*
				 * 
				 */
				sendShip(new LinkedList<Destination>(_destChain));

			} else {
				LOGGER.info("ERROR: BADLY DEFINED PROTOCOL!");
				LOGGER.info("COUNDN'T FIND WHERE TO SEND THE SHIP!");
			}
		}
	}

	private ProtocolEntry findSuitableProtocolEntry(Ship ship, List<ProtocolEntry> entries) {
		ProtocolEntry pe = null;
		assert ship != null;
		// get the corresponfing protocolEntry, including <Unknown>
		for (ProtocolEntry protocolEntry : entries) {
			if (protocolEntry.getShipName().equals(ship.getName()) || protocolEntry.getShipName().equals("<ALL>")) {
				pe = protocolEntry;
				break;
			}
		}

		if (pe == null) {
			// find rest
			for (ProtocolEntry protocolEntry : entries) {
				if (protocolEntry.getShipName().equals("<Rest>")) {
					pe = protocolEntry;
					break;
				}
			}
		}

		return pe;
	}

	public ShipProtocol getShipProtocol() {
		return _shipProtocol;
	}

	public void setShipProtocol(ShipProtocol shipProtocol) {
		_shipProtocol = shipProtocol;
	}

}
