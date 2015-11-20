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

public class ExtendedShipProtocol extends BaseShipProtocol {

	private ShipProtocol _shipProtocol;

	public ExtendedShipProtocol(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager) throws IOException {
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
			List<ProtocolEntry> entries = _shipProtocol.getEntries();
			for (ProtocolEntry protocolEntry : entries) {
  			protocolEntry.deserialize(new ProtocolEntryDeserializer(_mapManager));
			}
			
			if (ship != null) {
				boolean found = false;
				for (ProtocolEntry protocolEntry : entries) {
					if (protocolEntry.getShip().getName().equals(ship.getName())) {
						_destChain = protocolEntry.getChain();
						found = true;
					}
				}
				if (!found) {
					//find rest
					for (ProtocolEntry protocolEntry : entries) {
						if (protocolEntry.getShip().getName().equals("<Rest>")) {
							_destChain = protocolEntry.getChain();
						}
					}
				}
				sendShip(new LinkedList<Destination>(_destChain));
			} else {
				//find unknown
				for (ProtocolEntry protocolEntry : entries) {
					if (protocolEntry.getShip().getName().equals("<Unknown>")) {
						_destChain = protocolEntry.getChain();
					}
				}
				sendShip(new LinkedList<Destination>(_destChain));
			}
		}
	}

	public ShipProtocol getShipProtocol() {
		return _shipProtocol;
	}

	public void setShipProtocol(ShipProtocol shipProtocol) {
		_shipProtocol = shipProtocol;
	}

}
