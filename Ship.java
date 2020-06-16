/* File: Ship.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Sub-class of thing. An instance of ship is either in a que or at a dock.
 * 			A Ship is either a PassengerShip or a CargoShip.
 */

package seaPortProject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Ship extends Thing{
	//instance variables
	private PortTime arrivalTime, dockTime;
	private double draft, length, weight, width;
	protected ArrayList<Job> jobs = new ArrayList<Job>();
	private int numCompleteJobs = 0;
	protected SeaPort port;
	protected Dock dock;
	protected boolean isDocked;
	protected int numImpossibleJobs = 0;
	protected boolean jobsStarted = false;
	protected String location;
	protected ArrayList<String> missingSkills;
	protected Map<String, Integer> skillsCntMap = new HashMap<String, Integer>();
	
	//constructor
	protected Ship(Scanner sc) {
		super(sc);
		if(sc.hasNextDouble()) this.weight = sc.nextDouble();
		if(sc.hasNextDouble()) this.length = sc.nextDouble();
		if(sc.hasNextDouble()) this.width = sc.nextDouble();
		if(sc.hasNextDouble()) this.draft = sc.nextDouble();
	}
	

	protected void shipSetup(Dock dock, Thing port) {
		/*
		 * sets up the ship properly if it starts the program as docked.
		 */
		this.setParent(dock); 
		this.dock = dock;
		this.setPort((SeaPort) port);
		this.location = "Dock";
		SeaPortProgram.updateArrivalsAndDepartures(getName() + " arrived at port " + port.getName() + " dock " + dock.getName() + ".\n"); 
		this.isDocked = true;
	}
	
	protected void shipSetup(SeaPort port) {
		/*
		 * Sets up the ship properly if it starts the program as queued.
		 */
		this.setParent((SeaPort) port); 
		this.setPort(port);
		this.location = "Queue";
		this.isDocked = false;
	}
	
	protected void startJobs() {
		/*
		 * Method used to activate jobs based on various criteria so the job runs properly.
		 * Criteria include: If ship has jobs, if the ship is docked or not and if the ships jobs are 
		 * done running or not.
		 */
		if(jobs.size() != 0) {
			jobsStarted = true;
			if(isDocked) {
				for(Job job: jobs) {
					if(!job.isDone) {
						job.startAsDocked();
					}
				}
			} else {
				for(Job job: jobs) {
					if(!job.isDone) {
						job.startAsNotDocked();
					}
				}
			}
		}
		
	}
	
	public void updateMissingSkills() {
		for(Job job: jobs) {
			job.updateMissingSkills();
		}
	}
	
	protected boolean jobsStarted() {
		return jobsStarted;
	}
	
	protected void addJob(Job job) {
		jobs.add(job);
	}
	
	public double getDraft() {
		return draft;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getLength() {
		return length;
	}
	
	public double getWeight() {
		return weight;
	}
	
	public ArrayList<Job> getJobs() {
		return jobs;
	}
	
	public boolean getDocked() {
		return isDocked;
	}
	
	protected String getPortName() {
		return port.getName();
	}
	
	public int getNumJobs() {
		return jobs.size();
	}
	
	public void setPort(SeaPort port) {
		this.port = port;
	}
	
	public String getLocation() {
		return location;
	}
	
	//tostring
	public String toString() {
		String st = super.toString();
		if(!jobs.isEmpty()) {
			st += "\n  Jobs:\n";
			for(Job job: jobs) {
				st += "   -" + job.toString() + "\n";
			}
		} else {
			st += "\n  No Jobs.";
		}
		return st;
	}
	
	public String allToString() {
		String st = super.toString() + "\tWeight: " + weight + "\tLength: " + length + "\tWidth: " + width + "\tDraft: " + draft;
		return st;
	}

	//ship methods
	public void jobComplete() {
		/*
		 * This method is called by the job(s) as they are completed.
		 * When the number of times this method is called equals the
		 * number of jobs the ship has the Dock.depart() method is called
		 * to let the dock know that the ship is done and can depart
		 * allowing the next ship in the que to take its place.
		 */
		numCompleteJobs++;
		checkJobsStatus();
	}
	
	protected void impossibleJob() {
		numImpossibleJobs++;
		checkJobsStatus();
	}
	
	private void checkJobsStatus() {
		/*
		 * Each job will send a "Job Complete" or "Job Impossible" signal to it's ship.
		 * This method determines if the ship should leave the dock period and whether after 
		 * it leaves the dock if it should go back to the seas or if it should enter the impossible queue.
		 */
		if(numCompleteJobs == jobs.size()) {
			this.location = "The Seas";
			dock.removeShip();
			SeaPortProgram.updateArrivalsAndDepartures(getName() + " departed from port " + port.getName() + " dock " + dock.getName() + ". Jobs Complete.\n"); 
			port.dockNextShip(dock);
		}else if((numImpossibleJobs + numCompleteJobs) == jobs.size()) {
			SeaPortProgram.updateArrivalsAndDepartures(getName() + " departed dock " + dock.getName() + " due to missing skills. Will wait in port queue for new hires.\n"); 
			this.location = "Need Skill\nQueue";
			dock.removeShip();
			port.dockNextShip(dock);
			port.addImpossibleShip(this);
		}
	}

	protected void dockShip(Dock newDock) {
		/*
		 * This method assigns the ship to the newly available dock
		 * and calls the method that starts the job threads for the ship.
		 */
		this.setParent(newDock);
		this.dock = newDock;
		dock.addShip(this);
		this.location = "Dock";
		SeaPortProgram.updateArrivalsAndDepartures(getName() + " arrived at port " + port.getName() + " dock " + dock.getName() + ".\n"); 
		if(isDocked == false) {
			isDocked = true;
		}
		if(jobs.isEmpty()) {
			this.location = "The Seas";
			SeaPortProgram.updateArrivalsAndDepartures(getName() + " arrived at port " + port.getName() + " dock " + dock.getName() + " and immediately departed. No jobs needed completion.\n"); 
			port.dockNextShip(dock);
			return;
		}
		startJobs();

	}
}
