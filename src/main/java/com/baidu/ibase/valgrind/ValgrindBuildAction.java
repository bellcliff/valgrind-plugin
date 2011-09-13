package com.baidu.ibase.valgrind;

import hudson.FilePath;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.util.IOException2;
import hudson.util.NullStream;
import hudson.util.StreamTaskListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kohsuke.stapler.StaplerProxy;
import org.xmlpull.v1.XmlPullParserException;

import com.baidu.ibase.valgrind.util.IOHelper;

public class ValgrindBuildAction extends ValgrindObject<ValgrindBuildAction>
		implements HealthReportingAction, StaplerProxy {
	public final AbstractBuild<?, ?> owner;
	private transient WeakReference<ValgrindReport> report;
	/**
	 * Non-null if the coverage has pass/fail rules.
	 */
	private final Rule rule;

	/**
	 * The thresholds that applied when this build was built.
	 * 
	 * @TODO add ability to trend thresholds on the graph
	 */
	private final ValgrindHealthReportThresholds thresholds;

	public ValgrindBuildAction(AbstractBuild<?, ?> owner, Rule rule,
			Ratio definity, Ratio indirectly, Ratio reachable, Ratio possible,
			ValgrindHealthReportThresholds thresholds) {
		this.owner = owner;
		this.rule = rule;
		this.definity = definity;
		this.indirect = indirectly;
		this.reachable = reachable;
		this.possible = possible;
		this.thresholds = thresholds;
	}

	public String getDisplayName() {
		return "Valgrind";
	}

	public String getIconFileName() {
		return "graph.gif";
	}

	public String getUrlName() {
		return "valgrind";
	}

	public HealthReport getBuildHealth() {
		if (thresholds == null)
			return null;
		// TODO
		return null;
	}

	public Object getTarget() {
		return getResult();
	}

	@Override
	public AbstractBuild<?, ?> getBuild() {
		return owner;
	}

	protected static FilePath[] getValgrindReports(FilePath file)
			throws IOException, InterruptedException {
		return file.list("*xml");
	}

	/**
	 * Obtains the detailed {@link CoverageReport} instance.
	 */
	public synchronized ValgrindReport getResult() {

		if (report != null) {
			final ValgrindReport r = report.get();
			if (r != null)
				return r;
		}

		final FilePath reportFolder = ValgrindPublisher.getValgrindReportPath(owner);

		try {

			// Get the list of report files stored for this build
			FilePath[] reports = getValgrindReports(reportFolder);
			InputStream[] streams = new InputStream[reports.length];
			for (int i = 0; i < reports.length; i++) {
				streams[i] = reports[i].read();
			}

			// Generate the report
			ValgrindReport r = new ValgrindReport(this, streams);

			if (rule != null) {
				// we change the report so that the FAILED flag is set correctly
				logger.info("calculating failed packages based on " + rule);
				rule.enforce(r, new StreamTaskListener(new NullStream()));
			}

			report = new WeakReference<ValgrindReport>(r);
			return r;
		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "Failed to load " + reportFolder, e);
			return null;
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load " + reportFolder, e);
			return null;
		}
	}

	@Override
	public ValgrindBuildAction getPreviousResult() {
		return getPreviousResult(owner);
	}

	/**
	 * Gets the previous {@link EmmaBuildAction} of the given build.
	 */
	/* package */static ValgrindBuildAction getPreviousResult(
			AbstractBuild<?, ?> start) {
		AbstractBuild<?, ?> b = start;
		while (true) {
			b = b.getPreviousBuild();
			if (b == null)
				return null;
			if (b.getResult() == Result.FAILURE)
				continue;
			ValgrindBuildAction r = b.getAction(ValgrindBuildAction.class);
			if (r != null)
				return r;
		}
	}

	/**
	 * Constructs the object from emma XML report files. See <a
	 * href="http://emma.sourceforge.net/coverage_sample_c/coverage.xml">an
	 * example XML file</a>.
	 * 
	 * @throws IOException
	 *             if failed to parse the file.
	 */
	public static ValgrindBuildAction load(AbstractBuild<?, ?> owner,
			Rule rule, ValgrindHealthReportThresholds thresholds,
			FilePath... files) throws IOException {
		Ratio ratios[] = null;
		for (FilePath f : files) {
			InputStream in = f.read();
			try {
				ratios = Ratio.parseRatio(in, ratios);
			} finally {
				in.close();
			}
		}
		return new ValgrindBuildAction(owner, rule, ratios[0], ratios[1],
				ratios[2], ratios[3], thresholds);
	}

	private static final Logger logger = Logger
			.getLogger(ValgrindBuildAction.class.getName());
}
