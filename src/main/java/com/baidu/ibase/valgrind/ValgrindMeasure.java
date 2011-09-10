package com.baidu.ibase.valgrind;

public class ValgrindMeasure {
	int bytes = 0;
	int blocks = 0;
	String brief;
	String detail;

	String getBrief() {
		return this.brief;
	}

	String getDetail() {
		return this.detail;
	}
}
