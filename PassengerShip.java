/* File: PassengerShip.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Sub-class of Ship. Has passengers and rooms.
 */

package seaPortProject;

import java.util.Scanner;

public class PassengerShip extends Ship {
	//instance variables
	private int numOfOccupiedRooms;
	private int numOfPassengers;
	private int numOfRooms;
	
	//constructors
	protected PassengerShip(Scanner sc) {
		super(sc);
		if(sc.hasNextInt()) this.numOfPassengers = sc.nextInt();
		if(sc.hasNextInt()) this.numOfRooms = sc.nextInt();
		if(sc.hasNextInt()) this.numOfOccupiedRooms = sc.nextInt();
	}
	
	//tostring
	public String toString() {
		String st = "Passenger Ship: " + super.toString();
		return st;
	}
	
	public String statsToString() {
		String st = "Passenger Ship: " + super.toString() + " Passenger Count: " + numOfPassengers;
		st += " Room Count: " + numOfRooms + " Occupied Room Count: " + numOfOccupiedRooms;
		return st;
	}

}
