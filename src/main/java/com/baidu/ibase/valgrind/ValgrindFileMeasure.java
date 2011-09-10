package com.baidu.ibase.valgrind;

import hudson.FilePath;

public class ValgrindFileMeasure extends ValgrindMeasure {

	final FilePath file;
	ValgrindSummaryMeasure definety = new ValgrindSummaryMeasure(
			ValgrindTypes.DEFINETY);
	ValgrindSummaryMeasure indirect = new ValgrindSummaryMeasure(
			ValgrindTypes.INDIRECT);
	ValgrindSummaryMeasure reachabl = new ValgrindSummaryMeasure(
			ValgrindTypes.REACHABL);
	ValgrindSummaryMeasure possible = new ValgrindSummaryMeasure(
			ValgrindTypes.POSSIBLE);

	private ValgrindFileMeasure(FilePath file) {
		this.file = file;
	}

	static ValgrindFileMeasure parse(FilePath file) {
		ValgrindFileMeasure fileMeasure = new ValgrindFileMeasure(file);
		return fileMeasure;
	}
}
