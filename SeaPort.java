/* File: SeaPort.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Sub-class of thing. This class holds the ArrayLists of all docks, ships and persons associated
 * 			with the instance of port. Most search information is gathered from this class.
 */

package seaPortProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SeaPort extends Thing{
	//instance variables
	protected ArrayList<Dock> docks = new ArrayList<Dock>();
	protected Queue<Ship> queQueue = new ConcurrentLinkedQueue<Ship>();
	protected ArrayList<Ship> que = new ArrayList<Ship>();
	protected ArrayList<Ship> ships = new ArrayList<Ship>();
	protected ArrayList<Person> persons =  new ArrayList<Person>();
	protected ResourcePool pool;
	private List<String> portSkills = Collections.synchronizedList(new ArrayList<String>());
	protected List<Ship> impossibleQue = Collections.synchronizedList(new ArrayList<Ship>());
	private List<String> missingSkills = Collections.synchronizedList(new ArrayList<String>());
	protected Map<String, Integer> portSkillMap = new HashMap<String, Integer>();
	protected int newHireCount = 0;
	
	//constructors
	protected SeaPort(Scanner sc, World world) {
		super(sc, world);
	}
	
	protected void addDock(Dock dock) {
		this.docks.add(dock);
	}
	
	protected void addShipQue(Ship ship) {
		this.ships.add(ship);
		this.que.add(ship);
		this.queQueue.add(ship);
	}
	
	protected void addShipNoQue(Ship ship) {
		this.ships.add(ship);
	}
	
	protected void addPerson(Person person) {
		this.persons.add(person);
		this.portSkills.add(person.getSkill());
	}
	
	protected void addImpossibleShip(Ship ship) {
		impossibleQue.add(ship);
	}
	
	protected void buildPool() {
		/*
		 * Builds the resource pool and calls the methods to create the portskillsmap
		 * and to see if the port is missing any skills or doesn't have enough of a specific skill.
		 */
		pool = new ResourcePool(persons, this, thisWorld);
		for(Ship ship: ships) {
			ship.startJobs();
		}
		setupSkillsMap();
		for(Ship ship: ships) {
			for(Job job:ship.jobs) {
				job.portAndJobMapDif(portSkillMap);
			}
		}
	}
	
	protected void tryMapAgain(Job job) {
		job.portAndJobMapDif(portSkillMap);
	}
	
	protected boolean isPoolLive() {
		if(pool == null) {
			return false;
		} else {
			return true;
		}
	}
	
	protected void setupSkillsMap() {
		/*
		 * This is a complicated method for setting up the portsskillsmap.
		 * This map is used to determine if there are insufficent quantities of 
		 * a specific skill. Doesn't work right. Ran out of time.
		 */
		ArrayList<String> tempList = new ArrayList<String>(portSkills);
		for(String skill: tempList) {
			if(portSkillMap.containsKey(skill)) {
				portSkillMap.computeIfPresent(skill, (key, val) -> val + 1);
			} else {
				portSkillMap.putIfAbsent(skill, 1);
			}
		}
	}
	
	public void addToMissingSkillsList(String missSkill) {
		this.missingSkills.add(missSkill);
	}
	
	
	protected void newHire(Person newHire) {
		/*
		 * Handles the necessary tasks involved in hiring a new worker
		 * to the port. Once new hire has been added to the port and the
		 * pool the method calls the method to see if the new hire allows
		 * any previously impossible jobs to be compeleted.
		 */
		addPerson(newHire);
		String newSkill = newHire.getSkill();
		if(missingSkills.contains(newSkill)) {
			missingSkills.remove(newSkill);
		}
		pool.addNewHire(newHire);
		checkMoreJobsPossible(newSkill);
	}
		
	protected synchronized void checkMoreJobsPossible(String skill)	{		
		/*
		 * This method determines if any jobs with impossible jobs are now possible
		 * that a new person was hired to the port. If this is the case the ship with the 
		 * job will be taken out of the impossible queue and added to the regular queue if it
		 * is in the impossible queue. If the ship is still docked the updateMissingSkills()
		 * method will be called to ensure currently docked ship's jobs will change status appropriately.
		 */
		for(Iterator<Ship> iterator = impossibleQue.iterator(); iterator.hasNext();) {
			Ship tempShip = iterator.next();
			for(Job job: tempShip.jobs) {
				if(job.getStatus().equalsIgnoreCase("impossible")){
					if(job.portHasSkills()) {
						queQueue.add(tempShip);
						job.ship.location = "Queued";
						try {
							iterator.remove();
						}catch (IllegalStateException e) {}
					}
				}
			}
		}
		for(Ship ship: ships) {
			ship.updateMissingSkills();
		}
	}
	
	protected void ifOpenDockPlzDockMe(Ship ship) {
		/*
		 * Method to ensure the program continues running if dockNextShip() isn't being
		 * called anymore due to all possible jobs having been finished.
		 */
		for(Dock dock: docks) {
			if(!dock.hasShip()) {
				dockNextShip(dock);
			}
		}
	}
	
	protected void incrementNewHireCount() {
		this.newHireCount += 1;
	}
	
	protected int getNewHireCount() {
		return newHireCount;
	}
	
	protected void dockNextShip(Dock dock) {
		/*
		 * This method goes through the que ignoring ships without jobs and
		 * adds the first ship with jobs to the now empty dock calling it.
		 */
		if(queQueue.isEmpty()) {
			return;
		}else {
			Ship tempShip = queQueue.poll();
			tempShip.dockShip(dock);
		}
	}
	
	//tostring
	public String toString () {
		 String st = "\n\nSeaPort: " + super.toString() + "\n";
		 for (Dock dock: docks) st += "\n" + dock;
		 st += "\n\n --- List of all ships in que:";
		 for (Ship queued: que ) st += "\n > " + queued;
		 st += "\n\n --- List of all ships:";
		 for (Ship ship: ships) st += "\n > " + ship;
		 st += "\n\n --- List of all persons:";
		 for (Person person: persons) st += "\n > " + person;
		 return st;
	} 
	
	public String toString(String sortBy) {
		String st = "\n\nSeaPort: " + super.toString() + "\n";
		st += "\n  Docks:";
		for(Dock dock: docks) st += "\n    " + dock.toString(sortBy);
		st += "\n\n  Ships:";
		for(Ship ship: ships) st += "\n    " + ship.toString();
		st += "\n\n  People:";
		for(Person person: persons) st += "\n    " + person.toString();
		return st;
	}
	
	//Search methods 
	//Very specialized search methods
	public String findShipByIndex(int index) {
		for(Ship ship: ships) {
			if(ship.getIndex() == index) {
				return ship.toString();
			}
		}
		return null;
	}
	
	public String findPersonByIndex(int index) {
		for(Person pers: persons) {
			if(pers.getIndex() == index) {
				return pers.toString();
			}
		}
		return null;
	}

	public String findShipByName(String name) {
		for(Ship ship: ships) {
			if(ship.getName().equalsIgnoreCase(name)) {
				return ship.toString();
			}
		}
		return null;
	}
	
	public String findPersonByName(String name) {
		for(Person pers: persons) {
			if(pers.getName().equalsIgnoreCase(name)) {
				return pers.toString();
			}
		}
		return null;
	}
	
	public String findPersonBySkill(String skill) {
		for(Person pers: persons) {
			if(pers.getSkill().equalsIgnoreCase(skill)) {
				return pers.toString();
			}
		}
		return null;
	}
	
	public String getPeeps() {
		/*
		 * Returns a string of all people at the called port.
		 */
		String st = "";
		for(Person peep: persons) {
			st += "    " + peep.toString() + "\n";
		}
		return st;
	}
	
	//Attempt at a more general search method helper method
	public ArrayList<Thing> getListCopy(String list){
		switch(list) {
			case "ships": return new ArrayList<Thing>(ships);
				
			case "docks": return new ArrayList<Thing>(docks);
			
			default: return null;	
		}
	}
	
	protected synchronized List<String> getPortSkills(){
		return new ArrayList<String>(portSkills);
	}
	
	protected synchronized List<String> getMissingSkillsList(){
		return new ArrayList<String>(missingSkills);
	}

}

/*
 * Comparator classes. Used for sorting by various descriptors
 */

class ShipComparatorByWidth implements Comparator<Ship>{
	@Override
	public int compare(Ship s1, Ship s2) {
		double result = s1.getWidth() - s2.getWidth();
		return (int) result;
	}
}

class ShipComparatorByDraft implements Comparator<Ship>{
	@Override
	public int compare(Ship s1, Ship s2) {
		double result = s1.getDraft() - s2.getDraft();
		return (int) result;
	}
}

class ShipComparatorByLength implements Comparator<Ship>{
	@Override
	public int compare(Ship s1, Ship s2) {
		double result = s1.getLength() - s2.getLength();
		return (int) result;
	}
}

class ShipComparatorByWeight implements Comparator<Ship>{
	@Override
	public int compare(Ship s1, Ship s2) {
		double result = s1.getWeight() - s2.getWeight();
		return (int) result;
	}
}
