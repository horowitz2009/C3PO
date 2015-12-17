package com.horowitz.seaport.model;

import java.awt.AWTException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;

import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.storage.BalancedProtocolEntryDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;

public class BalancedShipProtocolExecutor extends BaseShipProtocolExecutor {

	private ShipProtocol _shipProtocol;

	public BalancedShipProtocolExecutor(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager)
	    throws IOException {
		super(scanner, mouse, mapManager);

		addPropertyChangeListener(new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				// TODO Auto-generated method stub
				// _support.firePropertyChange("SHIP_SENT", dest, _lastShip);
				if (evt.getPropertyName().equals("SHIP_SENT")) {
					Destination dest = (Destination) evt.getOldValue();
					Ship ship = (Ship) evt.getNewValue();
					try {
						_mapManager.registerTrip(ship, dest);
					} catch (IOException e) {
						LOGGER.info("Failed to save dispatch entries");
					}
				}

			}
		});
	}

	void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException {

		scanShipName(pin);

		_mouse.click(pin);
		_mouse.delay(550);
		_mouse.mouseMove(_scanner.getParkingPoint());
		_mouse.delay(100);

		Pixel anchor = _scanner.scanOneFast("anchor.bmp", null, false);
		if (anchor != null) {
			// MAP IS OPEN
			boolean isOK = _mapManager.ensureMap();

			if (_lastShip == null) {
				_lastShip = new Ship("<Unknown>");
			}
			Ship ship = _lastShip;
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

				if (Math.abs(1 - z) > 0.0000001) {// z > 1

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
				}
				// finally extract the result in form of dest chain
				LinkedList<Destination> chainList = new LinkedList<Destination>();
				for (DispatchEntry de : des) {
					chainList.add(_mapManager.getDestinationByAbbr(de.getDest()));
				}

				// use this chain
				boolean sent = sendShip(new LinkedList<Destination>(chainList));
				if (!sent) {
					_scanner.scanOneFast("anchor.bmp", null, true);
				}

			} else {
				LOGGER.info("ERROR: BADLY DEFINED PROTOCOL!");
				LOGGER.info("COUNDN'T FIND WHERE TO SEND THE SHIP!");
				_mouse.click(anchor);
				_mouse.delay(1000);
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
