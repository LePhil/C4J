package de.andrena.c4j.acceptancetest.lesson2;

import de.andrena.c4j.ClassInvariant;

public class AContract extends A {

	@ClassInvariant
	public void classInvariant() {
		System.out.println("ClassInvariant class A");
	}

}