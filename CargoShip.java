/* File: CargoShip.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Sub-class of Ship. Has cargo weight, volume and value.
 */

package seaPortProject;

import java.util.Scanner;

public class CargoShip extends Ship {
	//instance variables
	private double cargoValue, cargoVolume, cargoWeight;
	
	//constructors
	protected CargoShip(Scanner sc) {
		super(sc);
		if(sc.hasNextDouble()) this.cargoWeight = sc.nextDouble();
		if(sc.hasNextDouble()) this.cargoVolume = sc.nextDouble();
		if(sc.hasNextDouble()) this.cargoValue = sc.nextDouble();
	}
	
	//tostring override
	public String toString() {
		String st = "Cargo Ship: " + super.toString();
		return st;
	}
	
	public String statsToString() {
		String st = "Cargo Ship: " + super.toString() + " Cargo Weight: " + cargoWeight + " Cargo Volume: " + cargoVolume + " Cargo Value: $" + cargoValue;
		return st;
	}

}
