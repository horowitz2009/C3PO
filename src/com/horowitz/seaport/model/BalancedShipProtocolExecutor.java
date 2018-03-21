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
import com.horowitz.commons.Settings;
import com.horowitz.seaport.GameErrorException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.dest.MapManager;
import com.horowitz.seaport.model.storage.BalancedProtocolEntryDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;

public class BalancedShipProtocolExecutor extends BaseShipProtocolExecutor {

	private ShipProtocol _shipProtocol;

	public BalancedShipProtocolExecutor(ScreenScanner scanner, MouseRobot mouse, MapManager mapManager, Settings settings)
	    throws IOException {
		super(scanner, mouse, mapManager, settings);

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

	private int _doShipDelay = 350;
	private int _doShipDelaySlow = 650;

	@Override
	public void update() {
		super.update();
		_doShipDelay = _settings.getInt("shipProtocol.doShipDelay", 350);
		_doShipDelaySlow = _settings.getInt("shipProtocol.doShipDelaySlow", 650);
	}

	void doShip(Pixel pin) throws AWTException, RobotInterruptedException, IOException, GameErrorException {

		scanShipName(pin);
		if (isInterrupted())
			return;
		_mouse.click(pin);
		_mouse.delay(_doShipDelay);
		if (_mouse.getMode() == MouseRobot.SLOW)
			_mouse.delay(_doShipDelaySlow);

		_mouse.mouseMove(_scanner.getParkingPoint());
		_mouse.delay(100);
		if (_mouse.getMode() == MouseRobot.SLOW)
			_mouse.delay(_settings.getInt("slow.delay", 500));

		Pixel anchor = _scanner.scanOne(_scanner.getAnchorButton(), null, false);
		if (anchor == null && _mouse.getMode() == MouseRobot.SLOW) {
			_mouse.delay(_settings.getInt("slow.delay", 500) + 500);
			// try again
			anchor = _scanner.scanOne(_scanner.getAnchorButton(), null, false);
		}

		if (anchor != null && isNotInterrupted()) {
			if (_mapManager.ensureMap()) {
				// MAP IS OPEN and SmallTown visible

				if (_lastShip == null) {
					_lastShip = new Ship("<Unknown>");
				}
				Ship ship = _lastShip;
				List<ProtocolEntry> entries = _shipProtocol.getEntries();

				if (isInterrupted())
					return;

				ProtocolEntry pe = findSuitableProtocolEntry(ship, entries);

				if (pe != null) {

					// do what you gotta do with pe
					// end product: _destChain

					pe.deserialize(new BalancedProtocolEntryDeserializer(_mapManager, ship));

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
						Destination dest = _mapManager.getDestinationByAbbr(de.getDest());
						if (dest != null)
							chainList.add(dest);
					}

					// use this chain
					boolean sent = sendShip(new LinkedList<Destination>(chainList));
					if (!sent && isNotInterrupted()) {
						_scanner.scanOne(_scanner.getAnchorButton(), null, true);
					}

				} else {
					LOGGER.info("ERROR: BADLY DEFINED PROTOCOL!");
					LOGGER.info("COUNDN'T FIND WHERE TO SEND THE SHIP!");
					_mouse.click(anchor);
					_mouse.delay(1000);
				}
			} else {
				LOGGER.info("Can't find Small Town...");
			}
		} else {
			LOGGER.info("anchor not found...");
		}
	}

	public static void main(String[] args) {
		String ss = "[C 27] 1";
		String sss[] = ss.split(" ");
		ss = ss.substring(1, ss.length() - 1);
		String sc = sss[0].substring(1, 2);
		ss = sss[1].replace("]", "");
		if (sc.equalsIgnoreCase("c")) {
			// capacity

		} else {
			// sailors crew
		}

		System.out.println(sc);
		System.out.println(ss);
	}

	private ProtocolEntry findSuitableProtocolEntry(Ship ship, List<ProtocolEntry> entries) {
		ProtocolEntry pe = null;
		assert ship != null;
		// get the corresponfing protocolEntry, including <Unknown>

		// 1
		for (ProtocolEntry protocolEntry : entries) {
			String ss = protocolEntry.getShipName();
			
			if (ss.equalsIgnoreCase(ship.getName())) {
				pe = protocolEntry;
				break;
			}
			
			if (ss.equalsIgnoreCase("<CUSTOM>")) {
				String ch = protocolEntry.getChainStr();
				int index = ch.indexOf("-");
				if (index > 1) {
					// good. take first
					String s = ch.substring(0, index).trim();
					int value = ship.getCapacity();
					if (s.endsWith("s")) {
						// sailors
						s = s.replace("s", "");
						value = ship.getCrew();
					}

					if (s.startsWith("<=")) {
						s = s.substring(2);
						int value2 = Integer.parseInt(s);
						if (value <= value2) {
							pe = protocolEntry;
						}
					} else if (s.startsWith("<")) {
						s = s.substring(1);
						int value2 = Integer.parseInt(s);
						if (value < value2) {
							pe = protocolEntry;
						}
					} else if (s.startsWith(">=")) {
						s = s.substring(2);
						int value2 = Integer.parseInt(s);
						LOGGER.info(value + " >= " + value2);
						if (value >= value2) {
							pe = protocolEntry;
						}
					} else if (s.startsWith(">")) {
						s = s.substring(1);
						int value2 = Integer.parseInt(s);
						if (value > value2) {
							pe = protocolEntry;
						}
					}

					if (pe != null) {
						String newChain = ch.substring(index + 1);
						// copy
						ProtocolEntry pe2 = new ProtocolEntry();
						// pe2.setShip(pe.getShip());
						pe2.setShipName(pe.getShipName());
						pe2.setChainStr(newChain);
						pe = pe2;
						break;
					}
				}
			}

			if (ss.startsWith("[")) {
				String sss[] = ss.split(" ");
				String sc = sss[0].substring(1, 2);
				ss = sss[1].replace("]", "");

				int n = Integer.parseInt(ss);
				if (sc.equalsIgnoreCase("c")) {
					// capacity
					if (ship.getCapacity() == n) {
						pe = protocolEntry;
						break;
					}
				} else if (sc.equalsIgnoreCase("s")) {
					// crew
					if (ship.getCrew() == n) {
						pe = protocolEntry;
						break;
					}
				}
			}
		}

		// the rest
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
