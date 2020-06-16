/* File: Person.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Sub-class of Thing. Has a skill and is attached to a port.
 */

package seaPortProject;

import java.util.Comparator;
import java.util.Scanner;

public class Person extends Thing {
	//instance variables
	private String skill;	
	//constructor
	protected Person(Scanner sc) {
		super(sc);
		if(sc.hasNext()) this.skill = sc.next();
	}

	protected Person(String name, String skill, SeaPort parent) {
		this.name = name;
		this.skill = skill;
		this.parent = parent;
	}

	//tostring
	public String toString() {
		String st = "Person: " + super.toString() + " " + skill;
		return st;
	}
	
	public String getSkill() {
		return skill;
	}

}

/*
 * Person skill comparator
 */
class SkillComparator implements Comparator<Person>{
	@Override
	public int compare(Person o1, Person o2) {
		return o1.getSkill().compareTo(o2.getSkill());
	}
}
