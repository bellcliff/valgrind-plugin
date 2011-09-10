package com.baidu.ibase.valgrind;

public class ValgrindSummaryMeasure extends ValgrindMeasure {
	final ValgrindTypes type;

	public ValgrindSummaryMeasure(ValgrindTypes type) {
		this.type = type;
	}

	void addMeasure(ValgrindMeasure vm) {
		this.bytes += vm.bytes;
		this.blocks += vm.blocks;
	}

}
