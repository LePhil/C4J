package de.vksi.c4j.acceptancetest.workingstudent;

import de.vksi.c4j.ContractReference;

@ContractReference(YoungWorkingStudentContract.class)
public class YoungWorkingStudent implements StudentSpec, EmployeeSpec {

	private int age;
	
	@Override
	public String getEmployerName() {
		return "andrena objects ag";
	}

	@Override
	public String getMatriculationNumber() {
		return "123456789";
	}

	@Override
	public void setAge(int age) {
		this.age = age;			
	}
	
	public int getAge() {
		return age;
	}
	
}
