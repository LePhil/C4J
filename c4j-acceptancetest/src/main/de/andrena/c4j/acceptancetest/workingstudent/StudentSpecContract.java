package de.andrena.c4j.acceptancetest.workingstudent;

import static de.andrena.c4j.Condition.ignored;
import static de.andrena.c4j.Condition.pre;

public final class StudentSpecContract implements StudentSpec {

	@Override
	public String getMatriculationNumber() {
		// No contracts identified yet
		return ignored();
	}

	@Override
	public void setAge(int age) {
		if (pre()) {
			assert age > 0 : "age > 0";
			assert age < 100 : "age < 100";
		}
	}
	
}