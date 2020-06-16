/* File:	Job.java
 * Date:	11/30/2019
 * Author:	Zachary Finnegan
 * Purpose: This class implements the runnable class allowing for multiple jobs
 * 			to run at the same time as different threads. Jobs are basically a 
 * 			collection of JSwing components, a timer and status enumerations. 
 * 			Zero or more jobs will be assigned to a ship and will only progress
 * 			when the parent ship is docked.
 */

package seaPortProject;


import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Job extends Thing implements Runnable{
	//instance variables
	protected double duration;
	protected ArrayList<String> requirements = new ArrayList<String>();
	protected Object[] jobRow;
	private JProgressBar progressBar = new JProgressBar();
	private boolean suspendedFlag = true, canceledFlag = false;
	private JButton suspendB = new JButton("Suspend");
	private JButton cancelB = new JButton("Cancel");
	protected String missingSkills = "";
	protected JLabel skillsRequired = new JLabel();
	private Status status = Status.WAITING;
	private enum Status {RUNNING, SUSPENDED, WAITING, DONE, IMPOSSIBLE};
	private Thread jobThread;
	protected Ship ship;
	protected boolean isDone = false;
	protected ArrayList<Person> workers = new ArrayList<Person>();
	protected SeaPort port; 
	protected Map<String, Integer> jobSkillMap = new HashMap<String, Integer>();
	protected int rowNum;

	//constructor
	protected Job(Scanner sc) {
		super(sc);
		if(sc.hasNextDouble()) {duration = sc.nextDouble();}
		while(sc.hasNext()) {
			requirements.add(sc.next());
		}
		setupMap();
		jobThread = new Thread(this);

	}
	
	public void setupMap() {
		/*
		 * Sets up the map of skills and the number of each needed to complete the job.
		 * Used to determine if the port is short people with a given skill.
		 */
		for(String skill: requirements) {
			if(jobSkillMap.containsKey(skill)) {
				jobSkillMap.computeIfPresent(skill, (key, val) -> val + 1);
			} else {
				jobSkillMap.putIfAbsent(skill, 1);
			}
		}

	}
	
	public void portAndJobMapDif(Map<String, Integer> portSkillsMap) {
		/*
		 * This method compares the number of people in the port that have a specific skill
		 * to how many people with that skill the job needs. If the job requires more people with a 
		 * given skill than the port currently has the needed skill is added to the ports missing skill
		 * list and will be added to the skill combobox in the hire people panel.
		 */
		jobSkillMap.forEach((key, val) ->{
			if(portSkillsMap.containsKey(key)) {
				if(portSkillsMap.get(key) < val) {
					int temp = portSkillsMap.get(key);
					for(int i =0; i < (val - temp); i++) {
						port.addToMissingSkillsList(key);
					}
				}
			}
		});
	}
	
	public void setupJob(Ship ship) {
		/*
		 * Method is called by World to add parent ship, call the isDocked method which 
		 * will determine if the job should start or wait and calls teh addJobRow method.
		 */

		this.ship = ship;
		this.port = ship.port;
		addJobRow();
	}
	
	public void addJobRow() {
		/*
		 * Creates the the data that goes into the JTable row.
		 */
		progressBar.setStringPainted(true);
		progressBar.setFont(new Font("Serif", Font.PLAIN, 12));
		progressBar.setBackground(Color.white);
		progressBar.setForeground(Color.blue);
		suspendB.setFont(new Font("Serif", Font.PLAIN, 12));
		cancelB.setFont(new Font("Serif", Font.PLAIN, 12));
		missingSkills = (getMissingSkills());
		skillsRequired.setText(jobSkillMap.toString());
		skillsRequired.setFont(new Font("Serif", Font.PLAIN, 12));
		jobRow = new Object[7];
		jobRow[0] = ship.getPortName();
		jobRow[1] = ship.getName();
		jobRow[2] = getName();
		jobRow[3] = skillsRequired;
		jobRow[4] = progressBar;
		jobRow[5] = suspendB;
		jobRow[6] = cancelB;
		
		suspendB.addActionListener((ActionEvent e) -> {
			if(e.getSource() == suspendB) {toggleSuspendFlag();}});
		cancelB.addActionListener((ActionEvent e) -> {
			if(e.getSource() == cancelB) {raiseCanceledFlag();}});
	}

	public void updateMissingSkills() {
		/*
		 * This method is called after a new person is hired at the port
		 * to see if the ships/jobs that were impossible before are now possible.
		 * If they are possible they will be changed from impossible to suspended or waiting
		 * depending if the ship is docked or not.
		 */
		String tempString = getMissingSkills();
		if(!(missingSkills.equalsIgnoreCase("None")) && tempString.equalsIgnoreCase("None") && status == Status.IMPOSSIBLE){
			if(ship.getLocation().equalsIgnoreCase("Dock") && !jobThread.isAlive()) {
				jobThread.start();
			}else if(ship.getLocation().equalsIgnoreCase("Dock") && jobThread.isAlive() && status == Status.SUSPENDED) {
				toggleSuspendFlag();
			}else {
				showStatus(Status.WAITING);
				port.ifOpenDockPlzDockMe(ship);
			}
		}
		this.missingSkills = (tempString);
	}
	
	public Object[] getJobRow() {
		return jobRow;
	}
	
	public void setRowNum(int row) {
		this.rowNum = row;
	}
	
	public int getRowNum() {
		return rowNum;
	}
	
	public void startAsDocked() {
		/*
		 * This method activates the job if the ship is docked.
		 * Starts the thread.
		 */
		try {
			if(portHasSkills()) {
				if(!jobThread.isAlive()) {
					if(requirements.isEmpty()) {toggleSuspendFlag();}
					jobThread.start();
				}
			} else {
				showStatus(Status.IMPOSSIBLE);
				ship.impossibleJob();
			}
		}catch (IllegalThreadStateException e) {
			e.getMessage();
		}
	}
	
	public void startAsNotDocked() {
		/*
		 * When the jobs are initial instantiated if the ship is not docked this method
		 * is called to set the jobs status to waiting.
		 */
		showStatus(Status.WAITING);
	}

	public void toggleSuspendFlag() {
		suspendedFlag = !suspendedFlag;
	}
	
	public void raiseCanceledFlag() {
		//If raised the job will be canceled and marked done.
		canceledFlag = true;
		cancelB.setBackground(Color.RED);
	}
	
	public void showStatus(Status st) {
		status = st;
		switch (status) {
	    case RUNNING:
	    	suspendB.setBackground (Color.green);
	    	suspendB.setText ("Running");
	   		break;
	    case SUSPENDED:
	    	suspendB.setBackground (Color.yellow);
	    	suspendB.setText ("Suspended");
	        break;
	    case WAITING:
	    	suspendB.setBackground (Color.orange);
	    	suspendB.setText ("Waiting turn");
	        break;
	    case DONE:
	    	suspendB.setBackground (Color.GRAY);
	    	suspendB.setText ("Done");
	        break;
	    case IMPOSSIBLE:
	    	suspendB.setBackground (Color.RED);
	    	suspendB.setText ("Impossible");
		} 
	}
	
	@Override
	synchronized public void run() {
		/*
		 * This is the run method of the thread. It updates the progress bar based on a timer,
		 * changes the status as needed and lets the parent ship know when the job is complete.
		 * It also requests for the necessary workers and if the request is granted the job runs. 
		 * If the request is denied it will continue asking until the necessary workers are available.
		 * Once the job is complete the workers are returned.
		 */
		long time = System.currentTimeMillis();
		long startTime = time;
		long stopTime = time + 1000 * (long) duration;
		double timeNeeded = stopTime - time;
		boolean firstRequestDenied = false;
		while(time < stopTime && !canceledFlag) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			} catch (IndexOutOfBoundsException e1) {}
			if(workers.isEmpty() && !requirements.isEmpty()) {
				if(port.pool.assignWorkers(this, requirements)) {
					toggleSuspendFlag();
				}else {
					if(!firstRequestDenied) {
						SeaPortProgram.updateResourceRequestLog("Request made by " + ship.getName() + "'s " + getName() + " for workers with skills: " + requirements.toString() + "\n  DENIED\n");
						firstRequestDenied = true;
					}
				}
			}
			if(!suspendedFlag) {
				showStatus(Status.RUNNING);
				time += 100;
				int currentValue = (int)(((time-startTime)/timeNeeded)*100);
				progressBar.setValue(currentValue);
			} else {
				showStatus(Status.SUSPENDED);
				if(ship.location.equalsIgnoreCase("Queued")) {
					port.ifOpenDockPlzDockMe(ship);
				}
			}
		}
		progressBar.setValue(100);
		showStatus(Status.DONE);
		if(!workers.isEmpty()) {
			port.pool.unassignWorkers(workers, this);
		}
		isDone = true;
		ship.jobComplete();
	}
	
	public String toString() {
		return "Job: " + super.toString() + " Duration: " + duration + " Requirements: " + requirements.toString();
	}
	
	public String getStatus() {
		return status.toString();
	}
	
	public boolean portHasSkills() {
		/*
		 * Method checks to see if the port has the necessary people/skills to 
		 * complete the job. Used to determine if the job is impossible or not.
		 */
		for(String skill: requirements) {
			if(!(port.getPortSkills().contains(skill))) {
				return false;
			}
		}
		return true;
	}
	
	public String getMissingSkills() {
		/*
		 * This method is used to determine of the port has the skills needed.
		 * If it does not, then the method adds those skills to the ports missingSkills 
		 * list so they can be added to the hire panel skill combobox for hiring.
		 */
		String skills = "";
		int count = 0;
		if(portHasSkills()) {
			return "None";
		} else {
			
			for(String skill: requirements) {
				if(!(port.getPortSkills().contains(skill))) {
					if(!port.getMissingSkillsList().contains(skill)) {port.addToMissingSkillsList(skill);}
					if(count > 0) {
						skills += ", ";
					}
					skills += skill;
					count++;
				}
			}
			port.tryMapAgain(this);
			return skills;
		}
	}

	public void addWorkers(ArrayList<Person> tempPersonList) {
		this.workers = tempPersonList;
		
	}
	
}
