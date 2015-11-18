package com.horowitz.seaport.dest;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.Ship;
import com.horowitz.seaport.model.storage.GameUnitDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;

public class MapManager {

	private final static Logger LOGGER = Logger.getLogger("MAIN");

	private ScreenScanner _scanner;

	private List<Destination> _destinations;
	private List<Ship> _ships;
	private Pixel _marketPos = null;
	private String _marketStrategy = "COINS";

	public MapManager(ScreenScanner scanner) {
		super();
		_scanner = scanner;
	}

	public void loadData() throws IOException {
		loadDestinations();
		loadShips();
	}

	public void loadDestinations() throws IOException {
		_destinations = new JsonStorage().loadDestinations();
	}

	public List<Destination> getDestinations() {
		return _destinations;
	}

	public void loadShips() throws IOException {
		_ships = new JsonStorage().loadShips();
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

		// _scanner.zoomOut();

		// TODO check is map moved

	}

	public Destination getMarket() {
		return getDestination("Market");
	}

	public void deserializeDestinations() throws IOException {

		for (Destination destination : _destinations) {
			destination.setImageData(_scanner.getImageData(destination.getImage()));
			destination.setImageDataTitle(_scanner.getImageData(destination.getImageTitle()));

			ImageData id = destination.getImageData();
			id.set_xOff(id.getImage().getWidth() / 2);
			id.set_yOff(43);
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
			id.set_xOff(id.getImage().getWidth() / 2);
			id.set_yOff(43);
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

	public Pixel ensureMap() throws AWTException, RobotInterruptedException, IOException {
		// MAP ZONE

		// first zoom out
		_scanner.zoomOut();

		Destination _market = getMarket();
		// _marketPos = null;
		if (_marketPos == null) {
			LOGGER.info("Looking for market for the first time");

			Rectangle smallerArea = new Rectangle(_scanner.getTopLeft().x + 50, _scanner.getTopLeft().y
			    + _scanner.getGameHeight() / 2, _scanner.getGameWidth() - 100, _scanner.getGameHeight() / 2);
			_marketPos = _scanner.scanOneFast(_market.getImageData(), smallerArea, false);
			if (_marketPos == null) {
				_marketPos = _scanner.scanOneFast(_market.getImageData(), null, false);
				LOGGER.info("DAMMMMMMMMMMMN");
			} else {
				LOGGER.info("BINGOOOOOOOOOO");
			}
		} else {
			Rectangle areaSpec = new Rectangle(_marketPos.x - 20, _marketPos.y - 20, _market.getImageData().getImage()
			    .getWidth() + 40, _market.getImageData().getImage().getHeight() + 40);

			Pixel newMarketPos = _scanner.scanOneFast(_market.getImageData(), areaSpec, false);
			if (newMarketPos == null)
				newMarketPos = _scanner.scanOneFast(_market.getImageData(), null, false);

			if (_marketPos.equals(newMarketPos)) {
				LOGGER.info("Market found in the same place.");
			}
			_marketPos = newMarketPos;
		}
		
	  Pixel idealP = new Pixel(_scanner.getTopLeft().x + _scanner.getGameWidth()/2, _scanner.getBottomRight().y - 164);

		if (Math.abs(_marketPos.x - idealP.x) > 5 && Math.abs(_marketPos.x - idealP.y) > 5) {
			// need adjusting
			_scanner.getMouse().drag2(_marketPos.x, _marketPos.y-50, idealP.x, idealP.y-50);
			_scanner.getMouse().delay(1200);

			////
			Rectangle areaSpec = new Rectangle(_marketPos.x - 20, _marketPos.y - 20, _market.getImageData().getImage()
			    .getWidth() + 40, _market.getImageData().getImage().getHeight() + 40);

			Pixel newMarketPos = _scanner.scanOneFast(_market.getImageData(), areaSpec, false);
			if (newMarketPos == null)
				newMarketPos = _scanner.scanOneFast(_market.getImageData(), null, false);

			if (_marketPos.equals(newMarketPos)) {
				LOGGER.info("Market found in the same place.");
			}
			_marketPos = newMarketPos;
			////
		}
		
		
		return _marketPos;
	}

	public Pixel getMarketPos() {
		return _marketPos;
	}

	public void setMarketPos(Pixel marketPos) {
		_marketPos = marketPos;
	}

	public String getMarketStrategy() {
		return _marketStrategy;
	}

	public void setMarketStrategy(String marketStrategy) {
		_marketStrategy = marketStrategy;
	}

}
