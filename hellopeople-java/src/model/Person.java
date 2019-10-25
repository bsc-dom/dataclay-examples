package model;

public class Person {
	String name;
	int age;

	public Person(String newName, int newAge) throws Exception {
		name = newName;
		age = newAge;
	}

	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}
}
