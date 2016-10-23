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
		Pixel br = _scanner.getBottomRight();
		Rectangle anchorZone = new Rectangle(br.x - 127, br.y - 100, 127, 100);
		_forbiddenAreas.add(anchorZone);
		Pixel tl = _scanner.getTopLeft();
		int w = _scanner.getGameWidth() - 268 - 29;
		_mapArea = new Rectangle(tl.x + 278, tl.y + 69, w, _scanner.getGameHeight() - 69);

	}

	public Destination getSmallTown() {
		return getDestination("Small Town");
	}

	public void deserializeDestinations() throws IOException {

		for (Destination destination : _destinations) {
			destination.setImageData(_scanner.getImageData(destination.getImage()));
			destination.setImageDataTitle(_scanner.getImageData(destination.getImageTitle()));

			ImageData id = destination.getImageData();
			//id.set_xOff(id.getImage().getWidth() / 2);
			//id.set_yOff(43);
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
//			id.set_xOff(id.getImage().getWidth() / 2);
//			id.set_yOff(43);
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

	public boolean ensureMap() throws AWTException, RobotInterruptedException, IOException {
		// MAP ZONE

		// first zoom out
		_scanner.zoomOut();

		Destination _smallTown = getSmallTown();
		// _marketPos = null;
		if (_smallTownPos == null) {
			LOGGER.info("Looking for Small Town for the first time");

			int xx = _scanner.getGameWidth() / 3;
			int yy = _scanner.getGameHeight() / 2;
			Rectangle smallerArea = new Rectangle(_scanner.getTopLeft().x + xx, _scanner.getTopLeft().y
			    + yy / 2, xx, yy);
			//Rectangle smallerArea = _mapArea;
			_smallTownPos = _scanner.scanOneFast(_smallTown.getImageData(), smallerArea, false);
			if (_smallTownPos == null) {
				_smallTownPos = _scanner.scanOneFast(_smallTown.getImageData(), null, false);
				LOGGER.info("DAMMMMMMMMMMMN");
			} else {
				LOGGER.info("BINGOOOOOOOOOO");
			}
		} else {
			Rectangle areaSpec = new Rectangle(_smallTownPos.x - 10, _smallTownPos.y - 10, _smallTown.getImageData().getImage()
			    .getWidth() + 20, _smallTown.getImageData().getImage().getHeight() + 20);

			Pixel newSmallTownPos = _scanner.scanOneFast(_smallTown.getImageData(), areaSpec, false);
			if (newSmallTownPos == null) {
				LOGGER.info("COULDN'T FIND Small Town at once. Trying again...");
				newSmallTownPos = _scanner.scanOneFast(_smallTown.getImageData(), null, false);
			}

			if (_smallTownPos.equals(newSmallTownPos)) {
				LOGGER.info("Small Town found in the same place.");
			}
			_smallTownPos = newSmallTownPos;
		}

		boolean isOK = true;
		int xx2 = 0; int yy2 = 0;
		int xx3 = 0; int yy3 = 0;
		for (Destination dest : _destinations) {
	    if (dest.isFavorite()) {
	    	dest.getRelativePosition();
				int x = _smallTownPos.x + dest.getRelativePosition().x;
				int y = _smallTownPos.y + dest.getRelativePosition().y;
				if (x < _mapArea.x) {
					//2
					dest.x = _mapArea.x - x;
					if (dest.x > xx2)
						xx2 = dest.x;
				} else if (x > _mapArea.x + _mapArea.getWidth()) {
					//3
					dest.x = (int)(_mapArea.x + _mapArea.getWidth() - x);
					if (dest.x < xx3)
						xx3 = dest.x;
				}

				if (y < _mapArea.y) {
					//2
					dest.y = _mapArea.y - y;
					if (dest.y > yy2)
						yy2 = dest.y;
				} else if (y > _mapArea.y + _mapArea.getHeight()) {
					//3
					dest.y = (int)(_mapArea.y + _mapArea.getHeight() - y);
					if (dest.y < yy3)
						yy3 = dest.y;
				}
				
	    	
	    }
    }
		
		if ((xx2 != 0 && xx3 != 0) || (yy2 != 0 && yy3 != 0)) {
			isOK = false;
			LOGGER.info("ALL DESTINATIONS CAN'T BE IN THE ZONE!!!");
		} else {
			//can be fixed by dragging
			int xxx = (xx2 != 0) ? xx2 : xx3;
			int yyy = (yy2 != 0) ? yy2 : yy3;
			if (xxx != 0 || yyy != 0) {
				if (xxx > 0)
					xxx += 15;
				else
					xxx -= 15;
				if (yyy > 0)
					yyy += 15;
				else
					yyy -= 15;
				
			  _scanner.getMouse().drag4(_smallTownPos.x + 40, _smallTownPos.y - 36, _smallTownPos.x + xxx, _smallTownPos.y + yyy - 36, true, true);
			  _scanner.getMouse().delay(1200);
			  _smallTownPos.x += xxx;
			  _smallTownPos.y += yyy;
			  Rectangle areaSpec = new Rectangle(_smallTownPos.x - 10, _smallTownPos.y - 10, _smallTown.getImageData().getImage()
			      .getWidth() + 20, _smallTown.getImageData().getImage().getHeight() + 20);

			  Pixel newSmallTownPos = _scanner.scanOneFast(_smallTown.getImageData(), areaSpec, false);
			  if (newSmallTownPos == null) {
				  LOGGER.info("COULDN'T FIND Small Town at once. Trying again...");
				  newSmallTownPos = _scanner.scanOneFast(_smallTown.getImageData(), null, false);
			  }

			  if (_smallTownPos.equals(newSmallTownPos)) {
				  LOGGER.info("Small Town found in the same place.");
			  }
			  _smallTownPos = newSmallTownPos;
			}
		}
		
		return isOK;
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
		jsonStorage.saveDispatchEntries(new ArrayList<DispatchEntry>());
	}

	PropertyChangeSupport _support = new PropertyChangeSupport(this);

	private Rectangle _mapArea;

	public void amendShipProtocol(Ship ship, Destination dest) throws IOException {
	  //TODO	
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

		jsonStorage.saveDispatchEntries(des);
		new TripLogger().log2(ship, dest);
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

}
