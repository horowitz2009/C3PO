package com.horowitz.seaport.dest;

import java.io.IOException;
import java.util.List;

import com.horowitz.commons.ImageData;
import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.storage.JsonStorage;

public class MapManager {

  private ScreenScanner _scanner;

  private List<Destination> _destinations;

  public MapManager(ScreenScanner scanner) {
    super();
    _scanner = scanner;
  }

  public void loadDestinations() throws IOException {
    _destinations = new JsonStorage().loadDestinations();
  }

  public List<Destination> getDestinations() {
    return _destinations;
  }

  public void update() throws IOException, RobotInterruptedException {
    deserializeDestinations();
    
    _scanner.zoomOut();
    
    //TODO check is map moved
    
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

}
