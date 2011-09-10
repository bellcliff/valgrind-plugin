package com.baidu.ibase.valgrind;

import java.util.ArrayList;

public class ValgrindSummaryMeasure extends ValgrindMeasure {
	final ValgrindTypes type;
	ArrayList<ValgrindMeasure> list = new ArrayList<ValgrindMeasure>();

	public ValgrindSummaryMeasure(ValgrindTypes type) {
		this.type = type;
	}

	void addMeasure(ValgrindMeasure vm) {
		list.add(vm);
	}

}
