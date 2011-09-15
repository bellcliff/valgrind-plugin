package com.baidu.ibase.valgrind;

public class FileReport extends
		AggregatedReport<ValgrindReport, FileReport, LossRecordReport> {

	public String getSimpleName() {
		return getName().length() > 20 ? (getName().substring(0, 20) + "...")
				: getName();
	}
}
