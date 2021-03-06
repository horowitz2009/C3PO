package com.horowitz.seaport.dest;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.GameErrorException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.DispatchEntry;
import com.horowitz.seaport.model.Ship;
import com.horowitz.seaport.model.ShipProtocol;
import com.horowitz.seaport.model.storage.GameUnitDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;
import com.horowitz.seaport.model.storage.TripLogger;

public class MapManager {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;

	private List<Destination> _destinations;
	private List<Ship> _ships;
	private Pixel _smallTownPos = null;
	private Pixel _blacksmithPos = null;
	private Pixel _tailorPos = null;
	private List<Rectangle> _forbiddenAreas;

	public MapManager(ScreenScanner scanner) {
		super();
		_scanner = scanner;
		_forbiddenAreas = new ArrayList<Rectangle>(1);
	}

	public void loadData() throws IOException {
		loadDestinations();
		loadShips();
	}

	public void loadDestinations() throws IOException {
		_destinations = new JsonStorage().loadDestinationsNEW();
	}

	public List<Destination> getDestinations() {
		return _destinations;
	}

	public void loadShips() throws IOException {
		_ships = new JsonStorage().loadShips();
	}

	public List<ShipProtocol> loadShipProtocols() throws IOException {
		return new JsonStorage().loadShipProtocols();
	}

	public List<Ship> getShips() {
		return _ships;
	}

	public Ship getShip(String name) {
		for (Ship ship : _ships) {
			if (ship.getName().equals(name))
				return ship;
		}
		return null;
	}

	public void update() throws IOException, RobotInterruptedException {
		deserializeDestinations2();
		deserializeShips();

		_forbiddenAreas.clear();
		Pixel tl = _scanner.getTopLeft();
		Pixel br = _scanner.getBottomRight();
		Rectangle anchorZone = new Rectangle(br.x - 127, br.y - 100, 127, 100);
		Rectangle shipZone = new Rectangle(tl.x + 23, tl.y + 62, 261, 374);
		_forbiddenAreas.add(anchorZone);
		_forbiddenAreas.add(shipZone);
		_mapArea = new Rectangle(tl.x, tl.y + 70, _scanner.getGameWidth() - 29, _scanner.getGameHeight() - 70);
	}

	public Destination getSmallTown() {
		return getDestination("Small Town");
	}

	public void deserializeDestinations() throws IOException {

		for (Destination destination : _destinations) {
			destination.setImageData(_scanner.getImageData(destination.getImage()));
			destination.setImageDataTitle(_scanner.getImageData(destination.getImageTitle()));

			ImageData id = destination.getImageData();
			// id.set_xOff(id.getImage().getWidth() / 2);
			// id.set_yOff(43);
			id.set_xOff(0);
			id.set_yOff(0);

			id.setDefaultArea(_scanner.getScanArea());
			id = destination.getImageDataTitle();
			id.setDefaultArea(_scanner.getPopupArea());
			id.set_xOff(0);
			id.set_yOff(0);
		}
	}

	public void deserializeDestinations2() throws IOException {
		GameUnitDeserializer deserializer = new GameUnitDeserializer(_scanner);

		for (Destination destination : _destinations) {
			destination.deserialize(deserializer);

			// destination.setImageData(_scanner.getImageData(destination.getImage()));
			// destination.setImageDataTitle(_scanner.getImageData(destination.getImageTitle()));

			ImageData id = destination.getImageData();
			// id.set_xOff(id.getImage().getWidth() / 2);
			// id.set_yOff(43);
			id.set_xOff(0);
			id.set_yOff(0);
			id.setDefaultArea(_scanner.getScanArea());
			id = destination.getImageDataTitle();
			id.setDefaultArea(_scanner.getPopupArea());
			id.set_xOff(0);
			id.set_yOff(0);
		}
	}

	public void deserializeShips() throws IOException {
		GameUnitDeserializer deserializer = new GameUnitDeserializer(_scanner);

		for (Ship ship : _ships) {
			ship.deserialize(deserializer);
		}

	}

	public void saveDestinations() {
		try {
			new JsonStorage().saveDestinations(_destinations);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public Destination getDestination(String name) {
		for (Destination destination : _destinations) {
			if (destination.getName().startsWith(name)) {
				return destination;
			}
		}
		return null;
	}

	public Destination getDestinationByAbbr(String abbr) {
		for (Destination destination : _destinations) {
			for (String ds : destination.getAbbrs().split(",")) {
				if (ds.equalsIgnoreCase(abbr)) {
					return destination;
				}
			}
		}
		return null;
	}

	public boolean ensureMap() throws AWTException, RobotInterruptedException, IOException, GameErrorException {
		_scanner.zoomOut();

		findSmallTownAgain();

		if (_smallTownPos == null) {
			if (_scanner.handlePopups()) {
				findSmallTownAgain();
				if (_smallTownPos == null) {
					LOGGER.warning("WHAT TO DO? WHAT TO DO?");
					// throw new GameErrorException(9);
					return false;
				}
			}
		}
		return true;
		//
		// boolean isOK = true;
		// int xx2 = 0;
		// int yy2 = 0;
		// int xx3 = 0;
		// int yy3 = 0;
		// for (Destination dest : _destinations) {
		// if (dest.isFavorite()) {
		// dest.getRelativePosition();
		// int x = _smallTownPos.x + dest.getRelativePosition().x;
		// int y = _smallTownPos.y + dest.getRelativePosition().y;
		// if (x < _mapArea.x) {
		// // 2
		// dest.x = _mapArea.x - x;
		// if (dest.x > xx2)
		// xx2 = dest.x;
		// } else if (x > _mapArea.x + _mapArea.getWidth()) {
		// // 3
		// dest.x = (int) (_mapArea.x + _mapArea.getWidth() - x);
		// if (dest.x < xx3)
		// xx3 = dest.x;
		// }
		//
		// if (y < _mapArea.y) {
		// // 2
		// dest.y = _mapArea.y - y;
		// if (dest.y > yy2)
		// yy2 = dest.y;
		// } else if (y > _mapArea.y + _mapArea.getHeight()) {
		// // 3
		// dest.y = (int) (_mapArea.y + _mapArea.getHeight() - y);
		// if (dest.y < yy3)
		// yy3 = dest.y;
		// }
		//
		// }
		// }
		//
		// if ((xx2 != 0 && xx3 != 0) || (yy2 != 0 && yy3 != 0)) {
		// isOK = false;
		// LOGGER.info("ALL DESTINATIONS CAN'T BE IN THE ZONE!!!");
		// } else {
		// // can be fixed by dragging
		// int xxx = (xx2 != 0) ? xx2 : xx3;
		// int yyy = (yy2 != 0) ? yy2 : yy3;
		// if (xxx != 0 || yyy != 0) {
		// if (xxx > 0)
		// xxx += 35;
		// else
		// xxx -= 35;
		// if (yyy > 0)
		// yyy += 35;
		// else
		// yyy -= 35;
		//
		// _scanner.getMouse().drag4(_smallTownPos.x + 40, _smallTownPos.y - 70, _smallTownPos.x + xxx,
		// _smallTownPos.y + yyy - 70, true, true);
		// _scanner.getMouse().click();
		// _scanner.getMouse().click();
		// _scanner.getMouse().delay(1200);
		// _smallTownPos.x += xxx;
		// _smallTownPos.y += yyy;
		// findSmallTownAgain();
		// }
		// }
		//
		// return isOK;
	}

	private Pixel findSmallTownInt() throws AWTException, RobotInterruptedException, GameErrorException, IOException {
		Pixel smallTownPos = null;
		Destination smallTownDEST = getSmallTown();
		Destination blacksmithDest = getDestination("Blacksmith");
		Destination tailorDest = getDestination("Tailor");

		int xx = _scanner.getGameWidth() / 2;
		Rectangle area = new Rectangle(_scanner._tl.x + xx - 200, _scanner._tl.y + 164, 400, 255);
		smallTownPos = _scanner.scanOneFast(smallTownDEST.getImageData(), area, false);

		if (smallTownPos == null) {
			area = new Rectangle(_scanner.getScanArea());
			smallTownPos = _scanner.scanOneFast(smallTownDEST.getImageData(), area, false);
		}
		if (smallTownPos == null) {
			LOGGER.info("trying to find tailor or blacksmith...");
			area = new Rectangle(_scanner.getScanArea());
			Pixel tailorPos = _scanner.scanOneFast(tailorDest.getImageData(), area, false);
			if (tailorPos != null) {
				LOGGER.info("Tailor found: " + tailorPos);
				smallTownPos = new Pixel(tailorPos.x + 155, tailorPos.y + 165);
				LOGGER.info("Small Town calculated: " + smallTownPos);

				_tailorPos = tailorPos;
				_blacksmithPos = new Pixel(smallTownPos.x - 150, smallTownPos.y + 285);
			} else {
				// try blacksmith
				area = new Rectangle(_scanner.getScanArea());
				// area = new Rectangle(_scanner._tl.x + 340, _scanner.getTopLeft().y + 70, 400, _scanner.getGameHeight() - 140);
				Pixel blacksmithPos = _scanner.scanOneFast(blacksmithDest.getImageData(), area, false);
				if (blacksmithPos != null) {
					LOGGER.info("Blacksmith found: " + blacksmithPos);
					smallTownPos = new Pixel(blacksmithPos.x + 150, blacksmithPos.y - 285);
					LOGGER.info("Small Town calculated: " + smallTownPos);

					_blacksmithPos = blacksmithPos;
					_tailorPos = new Pixel(smallTownPos.x - 155, smallTownPos.y - 165);
				}
			}
		}
		return smallTownPos;
	}

	private Pixel findSmallTown() throws AWTException, RobotInterruptedException, GameErrorException, IOException {
		LOGGER.info("Looking for Small Town for the first time");
		Pixel smallTownPos = findSmallTownInt();
		if (smallTownPos == null) {
			LOGGER.info("dragging down a bit...");
			int startX = _scanner._scanArea.x + 300;
			int startY = _scanner._scanArea.y + 30;
			Pixel safeP = _scanner.scanOneFast("dest/sea.bmp", _scanner._scanArea, false);
			if (safeP != null) {
				startX = safeP.x;
				startY = safeP.y;
			}
			_scanner.getMouse().drag4(startX, startY, startX + 250, startY + 300, true, false);
			_scanner.getMouse().delay(2000);
			smallTownPos = findSmallTownInt();
		}

		if (smallTownPos == null) {
			LOGGER.info("popups perhaps...");
			_scanner.scanOneFast("buildings/x.bmp", null, true);
			_scanner.getMouse().delay(150);
			if (!_scanner.isMap()) {
				_scanner.getMouse().click(_scanner.getBottomRight().x - 80, _scanner.getBottomRight().y - 53);
				_scanner.getMouse().delay(2000);

			}
			smallTownPos = findSmallTownInt();
		}

		if (smallTownPos != null) {
			_tailorPos = new Pixel(smallTownPos.x - 155, smallTownPos.y - 165);
			_blacksmithPos = new Pixel(smallTownPos.x - 150, smallTownPos.y + 285);
			LOGGER.info("found Small Town ");
		}
		return smallTownPos;
	}

	private void findSmallTownAgain() throws AWTException, RobotInterruptedException, GameErrorException, IOException {
		Destination smallTownDEST = getSmallTown();

		if (_smallTownPos != null) {
			Rectangle areaSpec = new Rectangle(_smallTownPos.x - 20, _smallTownPos.y - 20,
			    smallTownDEST.getImageData().getImage().getWidth() + 40,
			    smallTownDEST.getImageData().getImage().getHeight() + 40);

			Pixel newSmallTownPos = _scanner.scanOneFast(smallTownDEST.getImageData(), areaSpec, false);
			if (newSmallTownPos == null) {
				newSmallTownPos = findSmallTown();
			}
			if (newSmallTownPos == null) {
				// still null
				_scanner.scanOneFast("buildings/x.bmp", null, true);
				_scanner.getMouse().delay(150);
				newSmallTownPos = findSmallTown();
				if (newSmallTownPos == null) {
					LOGGER.info("CRITICAL! Still can't find Small Town!");
					return; // try without having it
				}
			}

			if (newSmallTownPos == null)
				throw new GameErrorException(3, "Small Town can't be found", null);

			if (Math.abs(_smallTownPos.x - newSmallTownPos.x) < 7 && Math.abs(_smallTownPos.y - newSmallTownPos.y) < 7) {
				LOGGER.info("Small Town found in the same place.");
			}
			_smallTownPos = newSmallTownPos;

		} else
			_smallTownPos = findSmallTown();

		// TODO optimize it
		_tailorPos = new Pixel(_smallTownPos.x - 155, _smallTownPos.y - 165);
		_blacksmithPos = new Pixel(_smallTownPos.x - 150, _smallTownPos.y + 285);

		LOGGER.info("ST: " + _smallTownPos);
		LOGGER.info("BS: " + _blacksmithPos);
		LOGGER.info("JK: " + _tailorPos);
	}

	public Pixel getSmallTownPos() {
		return _smallTownPos;
	}

	public void setSmallTownPos(Pixel marketPos) {
		_smallTownPos = marketPos;
	}

	public void reset() {
		_smallTownPos = null;
	}

	public void resetDispatchEntries() throws IOException {
		JsonStorage jsonStorage = new JsonStorage();
		// jsonStorage.saveDispatchEntries(new ArrayList<DispatchEntry>());
		List<DispatchEntry> allDEs = jsonStorage.loadDispatchEntries();
		for (DispatchEntry de : allDEs) {
			de.setTimes(0);
		}
		jsonStorage.saveDispatchEntries(allDEs);
	}

	PropertyChangeSupport _support = new PropertyChangeSupport(this);

	private Rectangle _mapArea;

	public void amendShipProtocol(Ship ship, Destination dest) throws IOException {
		// TODO
	}

	public void registerTrip(Ship ship, Destination dest) throws IOException {
		JsonStorage jsonStorage = new JsonStorage();
		List<DispatchEntry> des = jsonStorage.loadDispatchEntries();
		boolean found = false;
		DispatchEntry theDE = null;
		for (DispatchEntry de : des) {
			if (de.getShip().equals(ship.getName()) && de.getDest().equals(dest.getAbbr())) {
				de.setTimes(de.getTimes() + 1);
				theDE = de;
				found = true;
				break;
			}
		}
		if (!found) {
			theDE = new DispatchEntry();
			theDE.setShip(ship.getName());
			theDE.setDest(dest.getAbbr());
			theDE.setTimes(1);
			des.add(theDE);
		}
		assert theDE != null;
		theDE.setTime(System.currentTimeMillis());
		jsonStorage.saveDispatchEntries(des);
		new TripLogger().log2(ship, dest);
		//new TripLogger("triplog2.txt").log3(theDE);
		_support.firePropertyChange("TRIP_REGISTERED", null, theDE);

	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		_support.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		_support.addPropertyChangeListener(propertyName, listener);
	}

	public ShipProtocol getSingleProtocol() {
		try {
			List<ShipProtocol> shipProtocols = loadShipProtocols();
			for (ShipProtocol shipProtocol : shipProtocols) {
				if (shipProtocol.getName().equals("SINGLE"))
					return shipProtocol;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean isPixelInArea(Pixel p, Rectangle area) {
		return (p.x >= area.x && p.x <= (area.x + area.getWidth()) && p.y >= area.y && p.y <= (area.y + area.getHeight()));
	}

	public Pixel ensurePixelInArea(Pixel p) {
		Rectangle area = _scanner._scanArea;
		if (!isPixelInArea(p, area)) {
			if (p.x < area.x)
				p.x = area.x + 1;
			if (p.x > area.x + area.getWidth())
				p.x = (int) (area.x + area.getWidth() - 1);
			if (p.y < area.y)
				p.y = area.y + 1;
			if (p.y > area.y + area.getHeight())
				p.y = (int) (area.y + area.getHeight() - 1);
		}
		// ship area issue
		final int y11 = _scanner._tl.y + 60;
		final int y22 = y11 + 296;
		final int x11 = _scanner._tl.x + 39;
		final int x22 = x11 + 228;
		area = new Rectangle(x11, x22, 228, 296);
		if (isPixelInArea(p, area)) {
			if (p.x > x11 && p.x <= x22) {
				p.x = x11 - 20;
			}

			if (p.y > y11 && p.y <= y22) {
				p.y = y11 - 2;
			}
		}
		return p;
	}

	public Pixel ensureDestination(Pixel relativePosition, boolean findSmallTownAgain)
	    throws RobotInterruptedException, AWTException, GameErrorException, IOException {
		if (_smallTownPos != null) {
			int y = _smallTownPos.y + relativePosition.y;
			final int x1 = _mapArea.x;
			final int y1 = _mapArea.y;
			final int x2 = x1 + _mapArea.width;
			final int y2 = y1 + _mapArea.height;
			Pixel startPos;

			// startPos = new Pixel(_smallTownPos.x + 6, _smallTownPos.y - 12);

			int dragY = 0;
			if (y < y1) {
				dragY = y1 - y + 12;// positive -> drag south
			} else if (y > y2) {
				dragY = y2 - y - 12;// negative -> drag north
			}
			int xoff = getSmallTown().getRelativePosition().x;
			int yoff = getSmallTown().getRelativePosition().y;
			// Y AXIS FIRST
			if (dragY != 0) {
				if (dragY > 0) {
					// must drag south
					if (_smallTownPos.y + yoff - 3 > y1)
						startPos = new Pixel(_smallTownPos.x + xoff, _smallTownPos.y + yoff);
					else
						startPos = new Pixel(_blacksmithPos.x - 10, _blacksmithPos.y);
					startPos = ensurePixelInArea(startPos);
					int dragYPart = y2 - startPos.y;
					if (dragY - dragYPart > 0) {
						LOGGER.info("drag south");
						// will need one more drag
						int startX = startPos.x;
						int startY = startPos.y;
						_scanner.getMouse().drag4(startX, startY, startX, startY + dragYPart, true, false);
						_scanner.getMouse().delay(2000);
						// _scanner.getMouse().click();// to stop inertia
						_scanner.handlePopupsFast();
						dragY -= dragYPart;
						_smallTownPos.y += dragYPart;
						if (findSmallTownAgain)
							findSmallTownAgain();
						startPos = new Pixel(_tailorPos.x + 50, _tailorPos.y);
					}
				} else {
					// drag < 0
					// must drag north
					if (_smallTownPos.y + yoff + 3 < y2)
						startPos = new Pixel(_smallTownPos.x + xoff, _smallTownPos.y + yoff);
					else
						startPos = new Pixel(_tailorPos.x + 50, _tailorPos.y);
					startPos = ensurePixelInArea(startPos);
					int dragYPart = y1 - startPos.y;
					if (dragY - dragYPart < 0) {
						LOGGER.info("drag north");
						// will need one more drag
						int startX = startPos.x;
						int startY = startPos.y;
						_scanner.getMouse().drag4(startX, startY, startX, startY + dragYPart, true, false);
						_scanner.getMouse().delay(2000);
						// _scanner.getMouse().click();// to stop inertia
						_scanner.handlePopupsFast();
						dragY -= dragYPart;
						_smallTownPos.y += dragYPart;
						if (findSmallTownAgain)
							findSmallTownAgain();
						startPos = new Pixel(_blacksmithPos.x - 10, _blacksmithPos.y);
					}
				}
				// drag the rest
				startPos = ensurePixelInArea(startPos);
				LOGGER.info("drag rest");
				int startX = startPos.x;
				int startY = startPos.y;
				_scanner.getMouse().drag4(startX, startY, startX, startY + dragY, true, false);
				// _scanner.getMouse().click();// to stop inertia
				_scanner.getMouse().delay(2000);
				_scanner.handlePopupsFast();
				_smallTownPos.y += dragY;
				if (findSmallTownAgain)
					findSmallTownAgain();
				y = _smallTownPos.y + relativePosition.y;

			} // END OF Y AXIS

			int x = _smallTownPos.x + relativePosition.x;
			int dragX = 0;
			if (x < x1) {
				dragX = x1 - x + 12;// positive -> drag east
			} else if (x > x2) {
				dragX = x2 - x - 12;// negative -> drag west
			}

			// X AXIS
			if (dragX != 0) {
				if (_smallTownPos.y + yoff - 3 > y1) {
					if (_smallTownPos.y + yoff + 3 < y2)
						startPos = new Pixel(_smallTownPos.x + xoff, _smallTownPos.y + yoff);
					else
						startPos = new Pixel(_tailorPos.x + 50, _tailorPos.y);
				} else
					startPos = new Pixel(_blacksmithPos.x - 10, _blacksmithPos.y);
				startPos = ensurePixelInArea(startPos);
				LOGGER.info("drag east/west");
				int startX = startPos.x;
				int startY = startPos.y;
				_scanner.getMouse().drag4(startX, startY, startX + dragX, startY, true, false);
				_scanner.getMouse().delay(2000);
				// _scanner.getMouse().click();// to stop inertia
				_scanner.handlePopupsFast();

				_smallTownPos.x += dragX;
				if (findSmallTownAgain)
					findSmallTownAgain();
				x = _smallTownPos.x + relativePosition.x;
				y = _smallTownPos.y + relativePosition.y;

			} // END OF X AXIS

			// ship area issue
			final int y11 = _scanner._tl.y + 64;
			final int y22 = y11 + 290;
			final int x11 = _scanner._tl.x + 47;
			final int x22 = x11 + 212;

			dragY = 0;
			if (y11 < y && y < y22 && x11 < x && x < x22) {
				LOGGER.info("Ship zone! Dragging south...");
				dragY = y22 - y - 12;
				startPos = new Pixel(_smallTownPos.x + xoff, _smallTownPos.y + yoff);
				int startX = startPos.x;
				int startY = startPos.y;
				_scanner.getMouse().drag4(startX, startY, startX, startY + dragY, true, false);
				_scanner.getMouse().delay(2000);
				// _scanner.getMouse().click();// to stop inertia
				_scanner.handlePopupsFast();

				_smallTownPos.y += dragY;
				if (findSmallTownAgain)
					findSmallTownAgain();
				x = _smallTownPos.x + relativePosition.x;
				y = _smallTownPos.y + relativePosition.y;
			}

			// SHIP BUTTON ISSUE
			final int y111 = _scanner._br.y - 100;
			final int y222 = y111 + 87;
			final int x111 = _scanner._tl.x + 19;
			final int x222 = x111 + 120;

			dragY = 0;
			if (y111 < y && y < y222 && x111 < x && x < x222) {
				LOGGER.info("Ship button zone! Dragging north...");
				dragY = y111 - y - 12;
				startPos = new Pixel(_smallTownPos.x + xoff, _smallTownPos.y + yoff);
				int startX = startPos.x;
				int startY = startPos.y;
				_scanner.getMouse().drag4(startX, startY, startX, startY + dragY, true, false);
				_scanner.getMouse().delay(2000);
				// _scanner.getMouse().click();// to stop inertia
				_scanner.handlePopupsFast();

				_smallTownPos.y += dragY;
				if (findSmallTownAgain)
					findSmallTownAgain();
				x = _smallTownPos.x + relativePosition.x;
				y = _smallTownPos.y + relativePosition.y;
			}

			return new Pixel(x, y);
		}
		return null;
	}
}
