package com.horowitz.bigbusiness.model.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.horowitz.mickey.Pixel;

public class JsonStorage {
  private Gson _gson = new GsonBuilder().setPrettyPrinting().create();

  public List<Pixel> loadBuildings() throws IOException {
    String json = FileUtils.readFileToString(new File("data/buildings.json"));

    Pixel[] buildings = _gson.fromJson(json, Pixel[].class);

    return new ArrayList<Pixel>(Arrays.asList(buildings));
  }

  public void saveBuildings(List<Pixel> buildings) throws IOException {

    String json = _gson.toJson(buildings);

    FileUtils.writeStringToFile(new File("data/buildings.json"), json);
  }

}
