package model;

import java.util.ArrayList;

public class People {
	private ArrayList<Person> people;

	public People() {
		people = new ArrayList<>();
	}

	public void add(final Person newPerson) {
		people.add(newPerson);
	}

	public String toString() {
		String result = "People: \n";
		for (Person p : people) {
			result += " - Name: " + p.getName();
			result += " Age: " + p.getAge() + "\n";
		}
		return result;
	}
}
