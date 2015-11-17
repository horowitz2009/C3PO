package com.horowitz.seaport.dest;

import java.io.IOException;
import java.util.List;

import com.horowitz.commons.RobotInterruptedException;
import com.horowitz.seaport.ScreenScanner;
import com.horowitz.seaport.model.Building;
import com.horowitz.seaport.model.storage.BuildingDeserializer;
import com.horowitz.seaport.model.storage.JsonStorage;

public class BuildingManager {

  private ScreenScanner _scanner;

  private List<Building> _buildings;

  public BuildingManager(ScreenScanner scanner) {
    super();
    _scanner = scanner;
  }

  public void loadData() throws IOException {
    loadBuildings();
  }

  public void loadBuildings() throws IOException {
    _buildings = new JsonStorage().loadBuildings();
  }

  public List<Building> getBuildings() {
    return _buildings;
  }

  public void update() throws IOException, RobotInterruptedException {
    deserializeBuildings();
  }

  public void deserializeBuildings() throws IOException {
    BuildingDeserializer deserializer = new BuildingDeserializer(_scanner);

    for (Building b : _buildings) {
      b.deserialize(deserializer);
    }
  }

  public void saveBuildings() {
    try {
      new JsonStorage().saveBuildings(_buildings);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public Building getBuilding(String name) {
    for (Building b : _buildings) {
      if (b.getName().startsWith(name)) {
        return b;
      }
    }
    return null;
  }

}
