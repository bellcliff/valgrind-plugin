package com.baidu.ibase.valgrind;

import hudson.FilePath;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.ibase.valgrind.util.IOHelper;

public class ValgrindReport extends
		AggregatedReport<ValgrindReport, ValgrindReport, FileReport> {
	private ValgrindBuildAction action;

	public ValgrindReport() {
		this.setName("valgrind");
	}

	void setAction(ValgrindBuildAction action) {
		this.action = action;
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

	public static ValgrindReport parse(FilePath[] files) throws IOException {
		ValgrindReport report = new ValgrindReport();
		for (FilePath f : files) {
			report.add(parseFileReport(f));			
		}
		logger.info(report.toString());
		for(FileReport fp : report.getChildren().values())
			fp.setParent(report);
		return report;
	}

	private static FileReport parseFileReport(FilePath file) throws IOException {
		FileReport report = new FileReport();
		report.setName(file.getBaseName());
		StringBuilder sb = IOHelper.read(file.read());
		Matcher m = Pattern.compile(PATTERN).matcher(sb);
		int end = 0;
		String last = null;
		while (m.find()) {			
			if(last != null){
				//set detail info for loss record
				report.getChildren().get(last).recordDetail = sb.substring(end, m.start());
			}
			LossRecordReport lrr = parseLossRecord(m);
			report.add(lrr);
			last = lrr.getName();
			end = m.end();
		}
		report.getChildren().get(last).recordDetail = sb.substring(end);
		for(LossRecordReport lrr : report.getChildren().values())
			lrr.setParent(report);
		return report;
	}

	private static LossRecordReport parseLossRecord(Matcher m) {
		String type = m.group(3);
		LossRecordReport report = new LossRecordReport();
		report.recordIndex = Integer.parseInt(m.group(4));
		report.recordCount = Integer.parseInt(m.group(5));
		report.recordBrief = m.group(0);
		report.setName(report.recordCount + " - " + report.recordIndex);
		Ratio ratio = new Ratio(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
		Ratio[] rs = new Ratio[4];
		for (int i = 0; i < VALGRIND_TYPE.length; i++)
			if (VALGRIND_TYPE[i].equals(type)) {
				rs[i] = ratio;
				break;
			}
		report.setRatios(rs);
		return report;
	}

	private static final Logger logger = Logger.getLogger(ValgrindReport.class
			.getName());

	private static final String PATTERN = "(\\d+) [^b]{0,50}bytes in (\\d+) blocks are ([a-z ]{10,50}) in loss record (\\d+) of (\\d+)";

	public static String[] VALGRIND_TYPE = new String[] { "definitely lost",
			"indirectly lost", "still reachable", "possibly lost" };

	public static void main(String[] args) {
		try {
			ValgrindReport.parse(new FilePath(new File("C:\\valgrind"))
					.list("*.xml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
