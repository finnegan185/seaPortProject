/* File: Dock.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Sub-class of thing. An instance of class dock can have a ship or not.
 * 			It is connected to a parent port.
 */

package seaPortProject;

import java.util.Scanner;

public class Dock extends Thing{
	//instance variables
	private Ship ship;
	private SeaPort port;
	
	
	//constructor
	protected Dock(Scanner sc, Thing parent) {
		super(sc, parent);
		if(sc.hasNextInt()) {
			sc.nextInt();
		}
	}
	protected Dock(Scanner sc) {
		super(sc);
		if(sc.hasNextInt()) {
			sc.nextInt();
		}
	}
	
	//Getters and Setters
	protected void addShip(Ship ship) {
		this.ship = ship;
		
	}
	
	public Ship getShip() {
		return ship;
	}
	
	protected String getPortName() {
		return port.getName();
	}
	
	public boolean hasShip() {
		if(ship == null) {
			return false;
		}
		return true;
	}
	
	protected void removeShip() {
		this.ship = null;
	}
	
	//tostring
	public String toString() {
		String st = "Dock: " + super.toString();
		if(hasShip()) {
			st += "\n Ship: " + ship.toString();
		} else {
			st += "\n Vacant";
		}
		return st;
	}
	
	public String toString(String sortBy) {
		return super.toString();

	}
	

}
