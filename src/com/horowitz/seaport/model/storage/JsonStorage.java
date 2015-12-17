package com.horowitz.seaport.model.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.horowitz.seaport.model.Building;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.DispatchEntry;
import com.horowitz.seaport.model.Ship;
import com.horowitz.seaport.model.ShipProtocol;

public class JsonStorage {
	private Gson _gson = new GsonBuilder().setPrettyPrinting().create();

	public List<Building> loadBuildings() throws IOException {
		String json = FileUtils.readFileToString(new File("data/buildings.json"));

		Building[] buildings = _gson.fromJson(json, Building[].class);

		return new ArrayList<Building>(Arrays.asList(buildings));
	}

	public void saveBuildings(List<Building> buildings) throws IOException {

		String json = _gson.toJson(buildings);

		FileUtils.writeStringToFile(new File("data/Building.json"), json);
	}

	public List<Destination> loadDestinations() throws IOException {
		String json = FileUtils.readFileToString(new File("data/destinations.json"));

		Destination[] destinations = _gson.fromJson(json, Destination[].class);

		return new ArrayList<Destination>(Arrays.asList(destinations));
	}

	public List<Destination> loadDestinationsNEW() throws IOException {
		String json = FileUtils.readFileToString(new File("data/destinationsNEW.json"));
		
		Destination[] destinations = _gson.fromJson(json, Destination[].class);
		
		return new ArrayList<Destination>(Arrays.asList(destinations));
	}
	
	public void saveDestinations(List<Destination> destinations) throws IOException {

		String json = _gson.toJson(destinations);

		FileUtils.writeStringToFile(new File("data/destinations.json"), json);
	}

	public List<Ship> loadShips() throws IOException {
		String json = FileUtils.readFileToString(new File("data/ships.json"));

		Ship[] array = _gson.fromJson(json, Ship[].class);

		return new ArrayList<Ship>(Arrays.asList(array));
	}

	public void saveShips(List<Ship> ships) throws IOException {

		String json = _gson.toJson(ships);

		FileUtils.writeStringToFile(new File("data/ships.json"), json);
	}

	public void saveShipProtocols(List<ShipProtocol> shipProtocols) throws IOException {

		String json = _gson.toJson(shipProtocols);

		FileUtils.writeStringToFile(new File("data/shipProtocols.json"), json);
	}

	public List<ShipProtocol> loadShipProtocols() throws IOException {
		String json = FileUtils.readFileToString(new File("data/shipProtocols.json"));

		ShipProtocol[] array = _gson.fromJson(json, ShipProtocol[].class);

		return new ArrayList<ShipProtocol>(Arrays.asList(array));
	}

	public List<DispatchEntry> loadDispatchEntries() throws IOException {
		File f = new File("data/dispatchEntries.json");
		if (!f.exists())
			saveDispatchEntries(new ArrayList<DispatchEntry>());
		String json = FileUtils.readFileToString(f);

		DispatchEntry[] dispatchEntries = _gson.fromJson(json, DispatchEntry[].class);

		return new ArrayList<DispatchEntry>(Arrays.asList(dispatchEntries));
	}

	public void saveDispatchEntries(List<DispatchEntry> dispatchEntries) throws IOException {

		String json = _gson.toJson(dispatchEntries);

		FileUtils.writeStringToFile(new File("data/dispatchEntries.json"), json);
	}

}
