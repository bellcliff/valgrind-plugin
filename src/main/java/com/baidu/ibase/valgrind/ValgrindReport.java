package com.baidu.ibase.valgrind;

import hudson.model.AbstractBuild;
import hudson.util.IOException2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ValgrindReport extends
		AggregatedReport<ValgrindReport, ValgrindReport, FileReport> {
	private final ValgrindBuildAction action;

	private ValgrindReport(ValgrindBuildAction action) {
		this.action = action;
		this.setName("valgrind");
	}

	public ValgrindReport(ValgrindBuildAction action, InputStream... xmlReports)
			throws IOException {
		this(action);
		for (InputStream is : xmlReports) {
			try {
				Ratio.parseRatio(is, null);
			} catch (IOException e) {
				throw new IOException2("Failed to parse XML", e);
			}
		}
		setParent(null);
	}

	public ValgrindReport(ValgrindBuildAction action, File xmlReport)
			throws IOException {
		this(action);
		try {
			Ratio.parseRatio(new FileInputStream(xmlReport), null);
		} catch (IOException e) {
			throw new IOException2("Failed to parse " + xmlReport, e);
		}
		setParent(null);
	}

	@Override
	public ValgrindReport getPreviousResult() {
		ValgrindBuildAction prev = action.getPreviousResult();
		if (prev != null)
			return prev.getResult();
		else
			return null;
	}

	@Override
	public AbstractBuild<?, ?> getBuild() {
		return action.owner;
	}
}
