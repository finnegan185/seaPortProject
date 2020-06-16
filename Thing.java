/* File: Thing.java
 * Date: 11/30/2019
 * Author: Zachary Finnegan
 * Purpose: Super class of all needed sub classes. Has basic information like name, index, parent
 * 			with constructor and implements comparable. Various get methods.
 * 
 */
package seaPortProject;

import java.util.Comparator;
import java.util.Scanner;

public class Thing implements Comparable<Thing>{
	//instance variables
	protected String name;
	protected int index;
	protected int parentIndex;
	protected Thing parent;
	protected World thisWorld;
	
	//constructor
	protected Thing(Scanner sc) {
		super();
		if(sc.hasNext()) this.name = sc.next();
		if(sc.hasNextInt()) this.index = sc.nextInt();
		if(sc.hasNextInt()) this.parentIndex = sc.nextInt();
	}
	
	protected Thing(Scanner sc, Thing parent) {
		super();
		if(sc.hasNext()) this.name = sc.next();
		if(sc.hasNextInt()) this.index = sc.nextInt();
		if(sc.hasNextInt()) this.parentIndex = sc.nextInt();
		this.parent = parent;
	}
	
	protected Thing() {
		
	}

	protected void setParent(Thing parent) {
		this.parent = parent;
	}
	
	protected Thing getParent() {
		return parent;
	}

	//tostring
	public String toString() {
		String st = this.name + " " + this.index;
		return st;
	}

	public int getIndex() {
		return index;
	}
	
	public int getParentIndex() {
		return parentIndex;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public int compareTo(Thing arg0) {
		return 0;
	}

}

/*
 * Comparator classes for Name and Index. 
 */
class NameComparator implements Comparator<Thing>{
	@Override
	public int compare(Thing t1, Thing t2) {
		return t1.getName().compareTo(t2.getName());
	} 
}

class IndexComparator implements Comparator<Thing>{
	@Override
	public int compare(Thing t1, Thing t2) {
		return t1.getIndex() - (t2.getIndex());
	} 
}
