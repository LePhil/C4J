package de.vksi.c4j.acceptancetest.lesson111;

public class ADemo {

	public static void main(String[] args) {
		A a = new A();
		a.query(1, 2);
		a.command(4);
		a.query(2, 3);
		a.command(5);
	}

}
