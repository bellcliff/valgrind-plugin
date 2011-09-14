package com.baidu.ibase.valgrind;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.util.IOException2;

import java.io.File;
import java.io.IOException;

import org.jfree.util.Log;

public class ValgrindReport extends
		AggregatedReport<ValgrindReport, ValgrindReport, FileReport> {
	private final ValgrindBuildAction action;

	private ValgrindReport(ValgrindBuildAction action) {
		this.action = action;
		this.setName("valgrind");
	}

	public ValgrindReport(ValgrindBuildAction action, FilePath[] xmlReport)
			throws IOException {
		this(action);
		try {
			parse(xmlReport);
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

	private void parse(FilePath[] files) throws IOException {
		for (FilePath f : files) {
			FileReport report = new FileReport();
			report.setName(f.getBaseName());
			report.setRatios(Ratio.parseRatio(f.read(), null));
			this.add(report);
		}
		Log.info(this);
	}
	
	public static void main(String[] args) {
		try {
			ValgrindReport r = new ValgrindReport(null);
			r.parse(new FilePath(new File("c:\\valgrind")).list("*.xml"));
			System.out.println(r);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
