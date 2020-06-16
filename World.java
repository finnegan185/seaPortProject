/* File: World.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Sub-class of thing. The main parser of the program as a whole. Takes the file and uses a scanner to read 
 * 			the file and create appropriate classes and connecting parent instances to child instances.
 * 			Also runs the main search functions, calling appropriate sub search functions in SeaPort class.
 * 			Beginning of the toString calls. Also performs the main sorting operations and returning sorted strings.
 */
package seaPortProject;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class World extends Thing{
	//instance variables
	protected ArrayList<SeaPort> ports = new ArrayList<SeaPort>();
	protected PortTime time;
	protected Object[][] jTableArray;
	protected ArrayList<Object[]> jobsArrayList = new ArrayList<>();
	protected int numJobs, numJobsProcessed;
	protected CustomTableModel jobTableModel, resourceTableModel;
	protected JTextArea resourceLog = new JTextArea(350, 200);
	
	protected World(File file) throws FileNotFoundException, NoSuchFieldException {
		/*
		 * Constructs the world from a file input and calls the file parser.
		 */
		super();
		parse(file);
	}
	
	private void parse(File file) throws FileNotFoundException, NoSuchFieldException {
		/*
		 * Opens the file, parses the file. Disregards blank lines and lines starting with 
		 * a leading '/'. The rest of the lines are then sent to a line processor.
		 */
		/*
		 * Uses the string line input and calls a scanner object on it
		 * which then goes through each word or number in the line and
		 * calls the appropriate add method based on the first word in
		 * the sentence. If the first word in the sentence is not 
		 * recognized an error is thrown and eventually caught in the 
		 * SeaPortProgram class.
		 */
		HashMap<Integer, SeaPort> portMap = new HashMap<>();
		HashMap<Integer, Dock> dockMap = new HashMap<>();
		HashMap<Integer, Ship> shipMap = new HashMap<>();
		HashMap<Integer, Person> personMap = new HashMap<>();
		HashMap<Integer, Job> jobMap = new HashMap<>();
		numJobs = 0;
		
		Scanner sc = new Scanner(file);
		while(sc.hasNext()) {
			String temp = sc.nextLine();
			if(temp.startsWith("/")) {
//				System.out.println("Skipping >" + temp + "<");
			} else if(temp.trim().isEmpty()) {
//				System.out.println("Skipping >" + temp + "<");
			} else {
//				System.out.println("Processing >" + temp + "<");
				Scanner line = new Scanner(temp);
				switch(line.next()) {
					case "port":
						SeaPort port = new SeaPort(line, this);
						portMap.put(port.getIndex(), port);
						ports.add(port);
						break;
					case "dock": 
						Dock dock = new Dock(line);
						addDockToPort(portMap, dockMap, dock);
						break;
					case "pship":
						PassengerShip pShip = new PassengerShip(line);
						addShip(portMap, dockMap, shipMap, pShip);
						break;
					case "cship":
						CargoShip cShip = new CargoShip(line);
						addShip(portMap, dockMap, shipMap, cShip);
						break;
					case "person":
						Person person = new Person(line);
						addPersonToPort(portMap, personMap, person);
						break;
					case "job":
						Job job = new Job(line);
						addJobsToShip(shipMap, jobMap, job);
						break;
					default: line.close();
						throw new NoSuchFieldException("Input was not a port, dock, pship, cship or person.");
				}
				line.close();
			}
		}
		sc.close();
		activatePools();
		
	}
	
	private void activatePools() {
		System.out.println("Building pools.");		
		for(SeaPort port: ports) {
			port.buildPool();
		}
	}
	
	private void addJobsToShip(HashMap<Integer, Ship> shipMap, HashMap<Integer, Job> jobMap, Job job) throws NoSuchFieldException {
		jobMap.put(job.getIndex(), job);
		if(shipMap.get(job.getParentIndex()) != null) {
			Ship tempShip = shipMap.get(job.getParentIndex());
			tempShip.addJob(job);
			job.setupJob(tempShip);
			numJobs++;
		} else {
			throw new NoSuchFieldException("Parent index: " + job.getParentIndex() + " not processed before child index: " + job.getIndex());
		}
	}

	private void addPersonToPort(HashMap<Integer, SeaPort> portMap, HashMap<Integer, Person> personMap, Person person) throws NoSuchFieldException {

		personMap.put(person.getIndex(), person);
		if(portMap.get(person.getParentIndex()) != null) {
			SeaPort tempPort = portMap.get(person.getParentIndex());
			tempPort.addPerson(person);
			person.setParent(tempPort);
		} else {
			throw new NoSuchFieldException("Parent index: " + person.getParentIndex() + " not processed before child index: " + person.getIndex());
		}
	}

	private void addShip(HashMap<Integer, SeaPort> portMap, HashMap<Integer, Dock> dockMap,
			HashMap<Integer, Ship> shipMap, Ship ship) throws NoSuchFieldException {
		/*
		 * This method is used to add new ships to the Hashmap, determine if the ship 
		 * is currently at a dock or in a port queue. If the ship is at a dock, the ship is added
		 * to that dock, the docks port all ship list and the ships parent and port are set.
		 * If the ship is not at a dock it is added to the port queue it has been assigned to in the data file,
		 * and the parent and port of the ship are set.
		 */
		shipMap.put(ship.getIndex(), ship);
		if(dockMap.get(ship.getParentIndex()) != null) {
			Dock tempDock = dockMap.get(ship.getParentIndex());
			tempDock.addShip(ship);
			portMap.get(tempDock.getParentIndex()).addShipNoQue(ship);
			ship.shipSetup(tempDock, tempDock.getParent());
		} else if(portMap.get(ship.getParentIndex()) != null) {
			SeaPort tempPort = portMap.get(ship.getParentIndex());
			tempPort.addShipQue(ship);
			ship.shipSetup(tempPort);
		} else {
			throw new NoSuchFieldException("Parent index: " + ship.getParentIndex() + " not processed before child index: " + ship.getIndex());
		}
		
	}

	private void addDockToPort(HashMap<Integer, SeaPort> portMap, HashMap<Integer, Dock> dockMap, Dock dock) throws NoSuchFieldException {
		SeaPort tempPort;
		dockMap.put(dock.getIndex(), dock);
		if(portMap.get(dock.getParentIndex()) != null) {
			tempPort = portMap.get(dock.getParentIndex());
			tempPort.addDock(dock);
			dock.setParent(tempPort);
		} else {
			throw new NoSuchFieldException("Parent index: " + dock.getParentIndex() + " not processed before child index: " + dock.getIndex());
		}
		
	}
	
	public void hirePerson(String portName, String skill) {
		String newHireName = "";
		for(SeaPort port: ports) {
			if(port.getName().equalsIgnoreCase(portName)) {
				newHireName += port.getName() + "newEmp" + port.getNewHireCount();
				port.newHire(new Person(newHireName, skill, port));
			}
		}
	}
	
	public JTextArea getResourceLog() {
		return resourceLog;
	}

	//Very specific search methods. Would like to make a more general method.
	public String[] getPortNames() {
		String[] portNames = new String[ports.size()+1];
		portNames[0] = "         ";
		int i = 1;
		for(SeaPort port: ports) {
			portNames[i] = port.getName();
			i++;
		}
		return portNames;
	}
	
	public String findByIndex(int index) {
		/*
		 * Returns a string of the object in the data that has the input index.
		 * Or returns an error message if index not found.
		 */
		for(SeaPort port: ports) {
			if(port.getIndex() == index) {
				return "Port: " + port.getName() + " " + port.getIndex();
			}
			String shipDeets = port.findShipByIndex(index);
			if(!(shipDeets == null)) {
				return shipDeets;
			}
			String personDeets = port.findPersonByIndex(index);
			if(!(personDeets == null)) {
				return personDeets;
			}
		}
		return "No object was found with an index of " + index + ".";
	}
	
	public String findByName(String name) {
		/*
		 * Returns a string of the port, dock, ship or person with the searched name.
		 * If there is one. Else error message.
		 */
		for(SeaPort port: ports) {
			if(port.getName().equalsIgnoreCase(name)) {
				return "Port: "+ port.getName() + " " + port.getIndex();
			}
			String shipDeets = port.findShipByName(name);
			if(!(shipDeets == null)) {
				return shipDeets;
			}
			String personDeets = port.findPersonByName(name);
			if(!(personDeets == null)) {
				return personDeets;
			}
		}
		return "No object was found with a name of " + name + ".";
	}
	
	public String findBySkill(String skill) {
		/*
		 * Returns a string of the person(s) with the searched skill.
		 * If there is one. Else error message.
		 */
		String st = "";
		for(SeaPort port: ports) {
			String personDeets = port.findPersonBySkill(skill);
			if(!(personDeets == null)) {
				st += personDeets + "\n";
			}
		}
		if(st.isEmpty()) {
			return "No person was found with a skill of " + skill + ".";
		} else {
			return st;
		}
	}
	
	//Attempts at a more general search methods.
	public String getShips(String filter) {
		/*
		 * Search method for use with Ship and sub ship radio buttons.
		 * Returns a string of either all ships, ships at docks or ships in queues per port.
		 */
		String st = "";
		for(SeaPort port: ports) {
			st += filter + " ships at:\n";
			st += "  SeaPort: " + port.getName() + " " + port.getIndex() + "\n";
			ArrayList<Thing> tempList = port.getListCopy("ships");
			for(Thing thing: tempList) {
				if(filter.equals("All")) {
					st += "    " + thing.toString() + "\n";
				} else if(filter.equals("Docked")) {
					if(thing.getParentIndex() != port.getIndex()) {
						st += "    " + thing.toString() + "\n";
					}
				}else {
					if(thing.getParentIndex() == port.getIndex()) {
						st += "    " + thing.toString() + "\n";
					}
				}
			}
		}
		return st;
	}
	
	public String getDocks(String filter) {
		/*
		 * Search method for use with Dock and sub dock radio buttons.
		 * Returns a string of either all docks, occupied docks or vacant docks per port.
		 */
		String st = "";
		for(SeaPort port: ports) {
			st += filter + " docks at:\n";
			st += "  SeaPort: " + port.getName() + " " + port.getIndex() + "\n";
			ArrayList<Thing> tempList = port.getListCopy("docks");
			for(Thing thing: tempList) {
				if(filter.equals("All")) {
					st += "    " + thing.getName() + " " + thing.getIndex() + "\n";
				} else if(filter.equals("Occupied")) {
					if(((Dock) thing).hasShip()) {
						st += "    " + thing.getName() + " " + thing.getIndex() + "\n";
					}
				}else {
					if(!((Dock) thing).hasShip()) {
						st += "    " + thing.getName() + " " + thing.getIndex() + "\n";
					}
				}
			}
		}
		return st;
	}
	
	public void NameSort(Boolean isReverse) {
		/*
		 * Less compact sort method for sorting all ArrayLists of ports, ships,
		 * docks and persons by their names in alphabetical order. Returns nothing.
		 */
		if(isReverse) {
			Collections.sort(ports, new NameComparator().reversed());
			for(SeaPort port: ports) {
				Collections.sort(port.docks, new NameComparator().reversed());
				Collections.sort(port.ships, new NameComparator().reversed());
				Collections.sort(port.persons, new NameComparator().reversed());
			}
		} else {
			Collections.sort(ports, new NameComparator());
			for(SeaPort port: ports) {
				Collections.sort(port.docks, new NameComparator());
				Collections.sort(port.ships, new NameComparator());
				Collections.sort(port.persons, new NameComparator());
			}
		}
	}
	
	public String SkillSort(Boolean isReverse) {
		/*
		 * Less compact way for sorting people based on the alphabetical order of
		 * their skills. Returns a string of the people in whichever order was requested.
		 */
		String st = "";
		if(isReverse) {
			Collections.sort(ports, new NameComparator().reversed());
			for(SeaPort port: ports) {
				st += "Port:\n  " + port.getName() + ":\n    ";
				Collections.sort(port.persons, new SkillComparator().reversed());
				for(Person person: port.persons) {
					st += person.toString() + "\n    ";
				}
				st += "\n";
			}
		}else {
			Collections.sort(ports, new NameComparator());
			for(SeaPort port: ports) {
				st += "Port:\n  " + port.getName() + ":\n    ";
				Collections.sort(port.persons, new SkillComparator());
				for(Person person: port.persons) {
					st += person.toString() + "\n    ";
				}
				st += "\n";
			}
		}
		return st;
	}
	
	public String ShipNameSort(boolean isQue, Comparator<Thing> comparator) {
		/*
		 * Method for sorting all ships or queued ships by their names. The Comparator
		 * parameter will determine in what order the ArrayLists are sorted. Returns a string
		 * of the sorted ships.
		 */
		String st = "";
		ArrayList<Ship>	tempList;
		Collections.sort(ports, comparator);
		for(SeaPort port: ports) {
			if(isQue) {
				tempList = port.que;
			} else {
				tempList = port.ships;
			}
			Collections.sort(tempList, comparator);
			st += "Port:\n  " + port.getName() + ":\n    ";
			for(Ship ship: tempList) {
				st += ship.allToString() + "\n    ";
			}
			st += "\n";
		}
		return st;
	}
	
	public String ShipSortByInput(boolean isQue, Comparator<Ship> comparator) {
		/*
		 * Compact method for sorting ships based on queued or not, weight, length,
		 * draft, width and name.
		 */
		String st = "";
		ArrayList<Ship> tempList;
		for(SeaPort port: ports) {
			if(isQue) {
				tempList = port.que;
			} else {
				tempList = port.ships;
			}
			Collections.sort(tempList, comparator);
			st += "Port:\n  " + port.getName() + ":\n    ";
			for(Ship ship: tempList) {
				st += ship.allToString() + "\n    ";
			}
			st += "\n";
		}
		return st;
	}
	
	public String getPeople() {
		/*
		 * Returns a string of people per port.
		 */
		String st = "";
		for(SeaPort port: ports) {
			st += "People at:\n";
			st += "  SeaPort: " + port.getName() + " " + port.getIndex() + "\n";
			st += port.getPeeps();
		}
		return st;
	}

	public String toString() {
		String st =  ">>>>>The World:";
		for(SeaPort port: ports) {
			st += port.toString();
		}
		return st;
	}

	public String toString(String sortBy) {
		/*
		 * Alternative to string. Returns a more basic and sorted string representation
		 * of the world.
		 */
		String st = ">>>>>The World Sorted: ";
		for(SeaPort port: ports) {
			st += port.toString("Name");
		}
		return st;
	}

	protected CustomTableModel createTable() {
		/*
		 * This method is used to create the the table model that the 
		 * JTable in SeaPortProgram will be populated with. It adds the 
		 * port name, ship name and job name to the table model along with
		 * the table headers. It also calls the getPanel() method from all of the 
		 * instantiated jobs methods to populate the right side of the JTable. 
		 * The right side of the JTable holds the progressbars and buttons.
		 */
		jTableArray = new Object[numJobs][7];
		numJobsProcessed = 0;
		for(SeaPort port: ports) {
			for(Ship ship: port.ships) {
				for(Job job: ship.jobs) {
					jTableArray[numJobsProcessed] = job.getJobRow();
					job.setRowNum(numJobsProcessed);
					numJobsProcessed++;
				}
			}
		}
		
		String [] columns = {"Port", "Ship", "Job", "Needed Skills", "Progress", "Status", "Cancel"};
		jobTableModel = new CustomTableModel(jTableArray, columns);
	    return jobTableModel;

	}
	
	protected CustomTableModel createResourceTableModel(JPanel resourcePanel) {
		/*
		 * Basically the same as the createTable() method except it is for the resource pools.
		 */
		Object[][] resoArray = new Object[ports.size()][3];
		Object[] headers = {"Port", "Total", "Available"};
		int count = 0;
		for(SeaPort port: ports) {
			resoArray[count] = port.pool.getResourceRow();
			count++;
		}
		
		
		resourceTableModel = new CustomTableModel(resoArray, headers);
		return resourceTableModel;
	}

	
}



