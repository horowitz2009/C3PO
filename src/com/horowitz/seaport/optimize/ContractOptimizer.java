package com.horowitz.seaport.optimize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.horowitz.commons.Deserializable;
import com.horowitz.commons.Deserializer;
import com.horowitz.seaport.model.Destination;
import com.horowitz.seaport.model.DispatchEntry;
import com.horowitz.seaport.model.Ship;
import com.horowitz.seaport.model.storage.JsonStorage;

public class ContractOptimizer {

	private List<DispatchEntry> shipsLog;
	private List<Destination> destinations;
	private List<Ship> ships;
	private Map<String, Ship> shipMap;

	private int min;
	private int max;
	private int solutionsLimit;
	private int precission = 5;
	private int solutionsLimit2;

	public ContractOptimizer(int min, int max, int solutionsLimit) {
		super();
		this.min = min;
		this.max = max;
		this.solutionsLimit = solutionsLimit;
		this.solutionsLimit2 = solutionsLimit;
	}

	public static void main(String[] args) throws IOException {
		ContractOptimizer co = new ContractOptimizer(100, 450, 500);
		co.init();
		co.loadShipsLog();

		List<Solution> solutions = co.getSolutionForFAST(4314);
		co.printSolutions(solutions);
	}

	public List<Solution> getSolutionFor(int cap) {
		precission = 5;
		solutionsLimit = solutionsLimit2;
		_solutions.clear();
		List<DispatchEntry> solution = new ArrayList<>();
		getFirstShipFor(new ArrayList<>(shipsLog), cap, solution);
		// printLog(solution);
		return _solutions;
		
	}
	
	public List<Solution> getSolutionForFAST(int cap) {
		precission = 0;
		solutionsLimit = 5;
		_solutions.clear();
		List<DispatchEntry> solution = new ArrayList<>();
		getFirstShipFor(new ArrayList<>(shipsLog), cap, solution);
		// printLog(solution);
		return _solutions;

	}

	public void printSolutions(List<Solution> solutions) {

		Collections.sort(solutions, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				return s1.goal - s2.goal;
			}
		});

		System.out.println("================================== BY PRECISION =====================================");
		for (Solution solution : solutions.stream().limit(5).collect(Collectors.toList())) {
			List<DispatchEntry> ships = new ArrayList<>(solution.ships);
			sortLogByArrival(ships);
			printLog(ships);
		}
		
		
		System.out.println("==================================== BY TIME =====================================");
		Collections.sort(solutions, new Comparator<Solution>() {
			@Override
			public int compare(Solution s1, Solution s2) {
				return new Date(s1.latest).compareTo(new Date(s2.latest));
			}
		});

		for (Solution solution : solutions.stream().limit(5).collect(Collectors.toList())) {
			List<DispatchEntry> ships = new ArrayList<>(solution.ships);
			sortLogByArrival(ships);
			printLog(ships);
		}

	}

	private void printLog(List<DispatchEntry> log) {
		System.out.println("LOG:");
		for (DispatchEntry de : log) {
			System.out.println(de.getShipObj().getCapacity() + "    " + de);
		}
		System.out.println("SUM: " + calcSum(log));
	}

	private int calcSum(List<DispatchEntry> solution) {
		int sum = 0;
		for (DispatchEntry de : solution) {
			sum += de.getShipObj().getCapacity();
		}
		return sum;
	}

	private void getFirstShipFor(List<DispatchEntry> log, int goal, List<DispatchEntry> solution) {
		int sum = calcSum(solution);
		if (sum >= goal) {
			// we're done
			if (sum - goal <= precission)
				registerSolution(solution);
		} else {
			for (DispatchEntry de : log) {

				if (_solutions.size() > solutionsLimit) {
					// enough
					break;
				}

				// if (sum + de.getShipObj().getCapacity() + 5 > goal) {
				// //skip this ship
				// continue;
				// }

				List<DispatchEntry> newLog = new ArrayList<>(log);
				List<DispatchEntry> newSolution = new ArrayList<>(solution);
				newLog.remove(de);
				newSolution.add(de);

				// add the ship at the end of the log with new time
				// DispatchEntry newDE = de.copy();
				// newDE.setTime(newDE.getTime() + 121 * 60000); //TODO use the dest this solution is and use its time
				// newLog.add(newDE);

				getFirstShipFor(newLog, goal, newSolution);
				// solution.remove(de);
			}
		}
	}

	private int getFirstShipForOLD(List<DispatchEntry> log, int goal, List<DispatchEntry> solution) {
		int sum = calcSum(solution);

		int need = goal - sum;
		if (need < 100) {
			// one ship and it can be the lowest available

			// if strict -> only 100 ship
			// else could be 100, 102 or 105

		} else if (need <= 300) {
			// could be done by one ship

			// if strict - find exact one
			// else find the one that has up to 5 cap more than needed
			boolean found = false;
			for (DispatchEntry de : log) {

				int diff = de.getShipObj().getCapacity() - need;
				if (diff >= 0 && diff <= 5) {
					// this ship will do
					solution.add(de);
					// found = true;
					// the end
					registerSolution(solution);
					return diff;
				}
			}

			if (!found) {
				// try with more than one ship
				for (DispatchEntry de : log) {
					List<DispatchEntry> newLog = new ArrayList<>(log);
					newLog.remove(de);
					solution.add(de);
					// add the ship at the end of the log with new time
					DispatchEntry newDE = de.copy();
					newDE.setTime(newDE.getTime() + 121 * 60000); // TODO use the dest this solution is and use its time
					newLog.add(newDE);

					getFirstShipFor(newLog, goal, solution);

				}
			}

		} else if (need > 300) {
			// more than one ship required
			int diff = 300 - need;
			// way too more
			// take first
			// and move on
			DispatchEntry de = log.get(0);
			List<DispatchEntry> newLog = new ArrayList<>(log);
			newLog.remove(de);
			solution.add(de);
			// add the ship at the end of the log with new time
			DispatchEntry newDE = de.copy();
			newDE.setTime(newDE.getTime() + 121 * 60000); // TODO use the dest this solution is and use its time
			newLog.add(newDE);

			return getFirstShipForOLD(newLog, goal, solution);

		} // end if

		DispatchEntry de = null;
		if (need < 100 && need > 95) {
			// get first lowest cap ship
			// make sure log stays full

			List<DispatchEntry> l = new ArrayList<>(log); // clone the log
			sortLogByCap(l);
			de = l.get(0);
			solution.add(de);
			// this is the last ship in solution. The solution is GOOD!!!
			sum += de.getShipObj().getCapacity();
			return sum;

		} else if (need >= 100) {
			for (DispatchEntry dee : log) {
				if (dee.getShipObj().getCapacity() <= need) {
					de = dee;

					int newCap = goal - sum - de.getShipObj().getCapacity();
					if (newCap >= 3 && newCap <= 95) {
						System.err.println("not good: " + newCap);
						continue;
					}
					List<DispatchEntry> newLog = new ArrayList<>(log);
					newLog.remove(de);
					solution.add(de);

					// add the ship at the end of the log with new time
					DispatchEntry newDE = de.copy();
					newDE.setTime(newDE.getTime() + 122 * 60000);
					newLog.add(newDE);

					return getFirstShipForOLD(newLog, goal, solution);
				}
			}
		} else {
			// cap should be <=95
			System.err.println(need);
			// this is not good
		}
		return need;
	}

	private List<Solution> _solutions = new ArrayList<>();

	private long getLatest(List<DispatchEntry> solution) {
		return solution.get(solution.size() - 1).willArriveAt();
	}
	
	private long getLatestOLD(List<DispatchEntry> solution) {
		long latest = 0;
		for (DispatchEntry de : solution) {
			long t = de.willArriveAt();
			if (t >= latest)
				latest = t;
		}
		return latest;
	}

	private void registerSolution(List<DispatchEntry> shipLog) {
		boolean found = false;
		sortLogByArrival(shipLog);
		for (Solution s : _solutions) {
			if (s.ships.containsAll(shipLog)) {
				// solution already registered
				long newLatest = getLatest(shipLog);
				if (s.latest < newLatest) {
					s.latest = newLatest;
					s.ships.clear();
					s.ships.addAll(shipLog);
				}
				found = true;
				break;
			}
		}

		if (!found) {
			Solution sol = new Solution();
			sol.ships.addAll(shipLog);
			sortLogByArrival(sol.ships);
			sol.latest = getLatest(shipLog);
			sol.goal = calcSum(shipLog);
			// Date d = new Date(sol.latest);
			// System.err.println(d);
			_solutions.add(sol);
			// printLog(solution);
		}

	}

	public void init() {
		try {
			JsonStorage storage = new JsonStorage();
			destinations = storage.loadDestinationsNEW();
			ships = storage.loadShips();
			shipMap = new HashMap<>();
			for (Ship ship : ships) {
				shipMap.put(ship.getName(), ship);
			}

			// System.err.println(destinations);
			System.err.println(ships);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void loadShipsLog() throws IOException {

		JsonStorage storage = new JsonStorage();
		shipsLog = storage.loadDispatchEntries();
		// shipsLog = new ArrayList<>();
		//
		// shipsLog.add(createDE("AURANIA", "G", -32));
		// shipsLog.add(createDE("HMS Blen", "G", -27));
		// shipsLog.add(createDE("SS Polar", "G", -27));
		// shipsLog.add(createDE("SS Verbon", "S", -4));
		// shipsLog.add(createDE("HMS Cambdridge", "G", -5));
		// shipsLog.add(createDE("SS Great West", "G", -49));
		// shipsLog.add(createDE("HMS Cull", "G", -58));
		// shipsLog.add(createDE("HMS Merlin", "G", -45));
		// shipsLog.add(createDE("HMS Valeur", "G", -37));
		// shipsLog.add(createDE("Rhodian", "M1C", -3));
		// shipsLog.add(createDE("NEWCASTLE", "G", -27));
		// shipsLog.add(createDE("Seleucus", "G", -15));
		// shipsLog.add(createDE("USS MAR", "G", -4));
		// shipsLog.add(createDE("TITANIC", "G", -5));
		// shipsLog.add(createDE("Yarmouth", "G", -15));

		Deserializer customDeserializer = new Deserializer() {

			@Override
			public void deserialize(Deserializable deserializable) throws IOException {
				if (deserializable instanceof DispatchEntry) {
					DispatchEntry de = (DispatchEntry) deserializable;
					de.setShipObj(shipMap.get(de.getShip()));
					de.setDestObj(getDestinationByAbbr(de.getDest()));

					if (de.getLastTime() != null && !de.getLastTime().isEmpty())
						de.setLastTime(de.getLastTime());
					else
						de.setTime(System.currentTimeMillis());
				}
			}
		};

		// add the rest of fleet
		List<DispatchEntry> toAdd = new ArrayList<>();
		for (Ship ship : ships) {
			if (ship.isActive() && ship.isFavorite() && !shipInLog(ship.getName())) {
				toAdd.add(createDE(ship.getName(), "E", 0));
			}
		}
		shipsLog.addAll(toAdd);

		for (DispatchEntry de : shipsLog) {
			try {
				de.deserialize(customDeserializer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Map<String, DispatchEntry> map = new HashMap<>();
		for (DispatchEntry de : shipsLog) {
			if (System.currentTimeMillis() - de.willArriveAt() > 5 * 60000) {
				// too old
				continue;
			}
			if (map.containsKey(de.getShip())) {
				DispatchEntry deOld = map.get(de.getShip());
				if (de.getTime() > deOld.getTime())
					map.put(de.getShip(), de);
			} else {
				map.put(de.getShip(), de);
			}
		}
		

		shipsLog = new ArrayList<>(map.values());

		//remove not-favorite ones
		List<DispatchEntry> toRemove = new ArrayList<>();
		for (DispatchEntry de : shipsLog) {
			if (!de.getShipObj().isFavorite()) {
				toRemove.add(de);
			}
		}
		shipsLog.removeAll(toRemove);
		
		// sort by arrival
		sortLogByArrival(shipsLog);

		// List<DispatchEntry> shipsLog2 = new ArrayList<>();

		shipsLog = shipsLog.stream()
		    .filter(de -> (de.getShipObj().getCapacity() >= min && de.getShipObj().getCapacity() <= max))
		    .collect(Collectors.toList());

		// for (DispatchEntry de : shipsLog) {
		// if (de.getShipObj() == null) {
		// System.out.println("UHOH " + de);
		// } else if (de.getShipObj().getCapacity() > 100 && de.getShipObj().getCapacity() <= 300)
		// shipsLog2.add(de);
		//
		// }

		// try {
		// Date d = DateUtils.parseFromISO(de.getLastTime());
		// System.err.println(d.getTime());
		//
		//
		// } catch (ParseException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		printLog(shipsLog);
	}

	private boolean shipInLog(String shipName) {
		for (DispatchEntry de : shipsLog) {
			if (de.getShip().equals(shipName)) {
				return true;
			}
		}
		return false;
	}

	private DispatchEntry createDE(String ship, String dest, int time) {
		DispatchEntry de = new DispatchEntry();
		de.setShip(ship);
		de.setDest(dest);
		de.setTime(System.currentTimeMillis() + time * 60000);
		return de;
	}

	private Destination getDestinationByAbbr(String abbr) {
		for (Destination destination : destinations) {
			for (String ds : destination.getAbbrs().split(",")) {
				if (ds.equalsIgnoreCase(abbr)) {
					return destination;
				}
			}
		}
		return null;
	}

	private void sortLogByCap(List<DispatchEntry> log) {
		Collections.sort(log, new Comparator<DispatchEntry>() {
			@Override
			public int compare(DispatchEntry de1, DispatchEntry de2) {
				return de1.getShipObj().getCapacity() - de2.getShipObj().getCapacity();
			}
		});
	}

	private void sortLogByArrival(List<DispatchEntry> log) {
		Collections.sort(log, new Comparator<DispatchEntry>() {
			@Override
			public int compare(DispatchEntry de1, DispatchEntry de2) {
				return (int) (de1.willArriveAt() - de2.willArriveAt());
			}
		});
	}

}
