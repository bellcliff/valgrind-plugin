package com.baidu.ibase.valgrind;

public class LossRecordReport extends
		AbstractReport<FileReport, LossRecordReport> {
	int recordIndex;
	int recordCount;
	String recordBrief;
	String recordDetail;

	public String printRecord() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < 4; i++) {
			Ratio r = getRatios()[i];
			if (r.bytes != 0) {
				buf.append("<tr><td>").append(this.getName())
						.append("</td><td>")
						.append(ValgrindReport.VALGRIND_TYPE[i])
						.append("</td><td>").append(r.bytes)
						.append("</td></tr>");
				buf.append("<tr>")
						.append("<td colSpan='3'><textarea readonly='true' style='width:100%;height:200px;overflow-y:visible'>")
						.append(recordBrief).append(recordDetail)
						.append("</textarea></td></tr>");
				break;
			}
		}
		return buf.toString();
	}
}
