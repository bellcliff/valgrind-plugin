package com.baidu.ibase.valgrind;

import hudson.FilePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.baidu.ibase.valgrind.util.IOHelper;

public class Ratio implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int bytes = 0;
	int blocks = 0;
	boolean initialized = false;

	@Override
	public String toString() {
		return bytes + " bytes in " + blocks + " blocks";
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void addValue(int bytes, int blocks) {
		this.bytes = bytes;
		this.blocks = blocks;
		initialized = true;
	}

	public static Ratio parse(int bytes, int blocks) {
		Ratio r = new Ratio();
		r.bytes = bytes;
		r.blocks = blocks;
		return r;
	}

	public static Ratio[] parseRatio(InputStream in, Ratio[] r)
			throws IOException {
		StringBuilder sb = IOHelper.read(in);
		if (r == null || r.length < 4)
			r = new Ratio[4];
		Matcher m = Pattern.compile(PATTERN).matcher(sb);
		while (m.find()) {
			String type = m.group(3);
			int blocks = Integer.parseInt(m.group(2));
			int bytes = Integer.parseInt(m.group(1));
			int index = -1;
			for (int i = 0; i < VALGRIND_TYPE.length; i++)
				if (VALGRIND_TYPE[i].equals(type)) {
					index = i;
					break;
				}
			if (index != -1) {
				if (r[index] == null)
					r[index] = parse(bytes, blocks);
				else
					r[index].addValue(bytes, blocks);
			} else
				logger.warning("new type found in parse valgrind report : "
						+ m.group());
		}

		return r;
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException, InterruptedException {
		FilePath path = new FilePath(new File("c:\\valgrind"));
		for (FilePath file : path.list("*.xml")) {
			System.out.println(file.getBaseName());			
			Ratio[] rs = parseRatio(file.read(), null);
			for (Ratio r : rs)
				System.out.println(r);
		}
	}

	private static final Logger logger = Logger.getLogger(ValgrindReport.class
			.getName());

	private static final String PATTERN = "(\\d+) [^b]{0,50}bytes in (\\d+) blocks are ([a-z ]{10,50}) in loss record \\d+ of \\d+";

	private static String[] VALGRIND_TYPE = new String[] { "definitely lost",
			"indirectly lost", "still reachable", "possibly lost" };
}
