package com.baidu.ibase.valgrind;

public enum ValgrindTypes {

	DEFINETY("definety"), INDIRECT("indirectly"), REACHABL("reachable"), POSSIBLE(
			"possible");

	private String type;

	private ValgrindTypes(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return this.type;
	}

}
