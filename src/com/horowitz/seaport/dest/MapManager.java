package com.horowitz.seaport.dest;

import java.io.IOException;
import java.util.List;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.Ship;
import com.horowitz.seaport.model.storage.GameUnitDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;

public class MapManager {

  private ScreenScanner _scanner;

  private List<Destination> _destinations;
  private List<Ship> _ships;

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

}
