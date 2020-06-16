/* File: ResourcePool.java
 * Date: 12/10/2019
 * Author: Zachary Finnegan
 * Purpose: This class is used to keep track of each ports resources/workers
 * 			It is used to manage the threads accesses to resources. It also
 * 			is used to set up resource table rows.
 */

package seaPortProject;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JLabel;

public class ResourcePool {
	private ReentrantLock lock = new ReentrantLock();
	protected List<Person> availableWorkers;
	protected SeaPort port;
	protected int totPossWorkers;
	protected JLabel totalWorkLabel = new JLabel();
	protected JLabel currentWorkLabel = new JLabel();
	protected static World world;

	public ResourcePool(List<Person> portPeople, SeaPort port, World world) {
		this.availableWorkers = Collections.synchronizedList(new ArrayList<Person>(portPeople));
		this.port = port;
		this.totPossWorkers = portPeople.size();
		ResourcePool.world = world;
	}

	public boolean assignWorkers(Job job, ArrayList<String> skills){
		/*
		 * A job thread requests the necessary people to complete the job.
		 * If all of the people with the necessary skills are available they are
		 * assigned to that job until it is finished.
		 */
		try {
			lock.lock();
			ArrayList<Person> tempPersonList = new ArrayList<Person>();
			ArrayList<String> skillsAcquired = new ArrayList<String>();
			ArrayList<String> tempHelperSkills = new ArrayList<String>(skills);
			for(Person person: availableWorkers) {
				String skill = person.getSkill();
				if(tempHelperSkills.contains(skill)) {
					tempPersonList.add(person);
					skillsAcquired.add(skill);
					tempHelperSkills.remove(skill);
				}
			}
			if(skills.size() == skillsAcquired.size()) {
				for(Person person: tempPersonList) {
					availableWorkers.remove(person);
				}
				updateAvailableWorkers();
				job.addWorkers(tempPersonList);
				SeaPortProgram.updateResourceRequestLog("Request made by " + job.ship.getName() + "'s " + job.getName() + " for workers with skills: " + skillsAcquired.toString() + "\n  GRANTED\n");
				return true;
			} else {
				return false;
			}
		} finally {
			lock.unlock();
		}
	}
	
	public void unassignWorkers(ArrayList<Person> workers, Job job) {
		/*
		 * When the job has completed it calls this method to have the people
		 * that were assigned to it returned to the resource pool.
		 */
		try {
			lock.lock();
			String tempSkills = "[";
			int count = 0;
			for(Person worker: workers) {
				if(count < workers.size() - 1) {
					tempSkills += worker.getSkill() + ", ";
					count++;
				}else {
					tempSkills += worker.getSkill() + "]";
				}
			}
			availableWorkers.addAll(workers);
			SeaPortProgram.updateResourceReturnLog(job.ship.getName() + " returned workers with skills:\t" + tempSkills + "\n");
			updateAvailableWorkers();
		} finally {
			lock.unlock();
		}
	}
	
	public Object[] getResourceRow() {
		/*
		 * Creates the necessary components to make up the table model
		 * and sends them to the createResourceTableModel method.
		 */
		
		Object[] resoRow = new Object[3];
		currentWorkLabel.setText(Integer.toString(availableWorkers.size()));
		totalWorkLabel.setText(Integer.toString(totPossWorkers));
		currentWorkLabel.setFont(new Font("Serif", Font.BOLD, 15));
		totalWorkLabel.setFont(new Font("Serif", Font.BOLD, 15));
		resoRow[0] = port.getName();
		resoRow[1] = totalWorkLabel;
		resoRow[2] = currentWorkLabel;
		return resoRow;
	}
	
	private void updateAvailableWorkers() {
		/*
		 * This method updates the JLabel used in the resource table  to ensure
		 * the available resource stay current.
		 */
		try {
			lock.lock();
			String curWorkCnt = Integer.toString(this.availableWorkers.size());
			this.currentWorkLabel.setText(curWorkCnt);
		}finally {
			lock.unlock();
		}
	}
	
	protected void addNewHire(Person newHire) {
		/*
		 * This method adds newly "hired" employees to the pool. It also
		 * sends a message to the updateResourceReturnLog that says a new employee was hired for
		 * a given port and what that skill is.
		 */
		try {
			lock.lock();
			this.availableWorkers.add(newHire);
			this.totPossWorkers += 1;
			this.totalWorkLabel.setText(Integer.toString(totPossWorkers));
			SeaPortProgram.updateResourceReturnLog(port.getName() + " hired an additional worker with skill: " + newHire.getSkill() + "\n");
			updateAvailableWorkers();
		} finally {
			lock.unlock();
		}
	}
}
