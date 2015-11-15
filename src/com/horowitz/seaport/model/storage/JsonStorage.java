package com.horowitz.seaport.model.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.horowitz.commons.Pixel;
import com.horowitz.seaport.model.Building2;
import com.horowitz.seaport.model.Destination;

public class JsonStorage {
  private Gson _gson = new GsonBuilder().setPrettyPrinting().create();

  public List<Building2> loadBuildings() throws IOException {
    String json = FileUtils.readFileToString(new File("data/buildings2.json"));

    Building2[] buildings = _gson.fromJson(json, Building2[].class);

    return new ArrayList<Building2>(Arrays.asList(buildings));
  }

  public void saveBuildings(List<Building2> buildings) throws IOException {

    String json = _gson.toJson(buildings);

    FileUtils.writeStringToFile(new File("data/buildings2.json"), json);
  }

  public List<Destination> loadDestinations() throws IOException {
    String json = FileUtils.readFileToString(new File("data/destinations.json"));
    
    Destination[] destinations = _gson.fromJson(json, Destination[].class);
    
    return new ArrayList<Destination>(Arrays.asList(destinations));
  }
  
  public void saveDestinations(List<Destination> destinations) throws IOException {
    
    String json = _gson.toJson(destinations);
    
    FileUtils.writeStringToFile(new File("data/destinations.json"), json);
  }
  
}
