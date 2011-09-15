package com.baidu.ibase.valgrind;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.util.ChartUtil;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.ColorPalette;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ShiftedCategoryAxis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Logger;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.Log;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

public abstract class ValgrindObject<SELF extends ValgrindObject<SELF>> {
	Ratio definity = new Ratio();
	Ratio indirect = new Ratio();
	Ratio reachable = new Ratio();
	Ratio possible = new Ratio();
	// Ratio[] ratios = new Ratio[] { definity, indirect, reachable, possible };
	private volatile boolean failed = false;

	public boolean isFailed() {
		return failed;
	}

	public Ratio[] getRatios() {
		return new Ratio[] { definity, indirect, reachable, possible };
	}

	public void setRatios(Ratio[] rs) {
		this.definity.setValue(rs[0]);
		this.indirect.setValue(rs[1]);
		this.reachable.setValue(rs[2]);
		this.possible.setValue(rs[3]);
	}

	/**
	 * Marks this coverage object as failed.
	 * 
	 */
	public void setFailed() {
		failed = true;
	}

	@Exported(inline = true)
	public Ratio getDefinety() {
		return definity;
	}

	@Exported(inline = true)
	public Ratio getIndirect() {
		return indirect;
	}

	@Exported(inline = true)
	public Ratio getReachable() {
		return reachable;
	}

	@Exported(inline = true)
	public Ratio getPossible() {
		return possible;
	}

	/**
	 * Gets the build object that owns the whole coverage report tree.
	 */
	public abstract AbstractBuild<?, ?> getBuild();

	/**
	 * Gets the corresponding coverage report object in the previous run that
	 * has the record.
	 * 
	 * @return null if no earlier record was found.
	 */
	@Exported
	public abstract SELF getPreviousResult();

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "-- \r\n\tdefinity : "
				+ definity + "\r\n\tindirect : " + indirect
				+ "\r\n\tpossible : " + possible + "\r\n\treachable : "
				+ reachable;
	}

	public String printFourMeasureColumns() {
		StringBuilder buf = new StringBuilder();
		printRatioCell(definity, buf,definity.bytes>0);
		printRatioCell(indirect, buf, indirect.bytes>0);
		printRatioCell(reachable, buf, false);
		printRatioCell(possible, buf, false);
		return buf.toString();
	}

	protected static void printRatioCell(Ratio ratio, StringBuilder buf, boolean failed) {
		if (ratio != null && ratio.bytes != 0) {
			String className = "nowrap" + (failed ? " red" : "");
			buf.append("<td class='").append(className).append("'");
			buf.append(">\n");
			printRatioTable(ratio, buf);
			buf.append("</td>\n");
		}else
			buf.append("<td />");
	}

	protected static void printRatioTable(Ratio ratio, StringBuilder buf) {
		buf.append(ratio);
	}

	/**
	 * Generates the graph that shows the coverage trend up to this report.
	 */
	public void doGraph(StaplerRequest req, StaplerResponse rsp)
			throws IOException {
		if (ChartUtil.awtProblemCause != null) {
			// not available. send out error message
			rsp.sendRedirect2(req.getContextPath() + "/images/headless.png");
			return;
		}

		AbstractBuild<?, ?> build = getBuild();
		Log.info("do graph on build : " + build.number);
		Calendar t = build.getTimestamp();

		String w = Util.fixEmptyAndTrim(req.getParameter("width"));
		String h = Util.fixEmptyAndTrim(req.getParameter("height"));
		int width = (w != null) ? Integer.valueOf(w) : 500;
		int height = (h != null) ? Integer.valueOf(h) : 200;

		new GraphImpl(this, t, width, height) {

			@Override
			protected DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet(
					ValgrindObject<SELF> obj) {
				DataSetBuilder<String, NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, NumberOnlyBuildLabel>();

				for (ValgrindObject<SELF> a = obj; a != null; a = a
						.getPreviousResult()) {
					NumberOnlyBuildLabel label = new NumberOnlyBuildLabel(
							a.getBuild());
					dsb.add(a.definity.bytes, "definity", label);
					dsb.add(a.indirect.bytes, "indirectly", label);
					dsb.add(a.reachable.bytes, "reachable", label);
					dsb.add(a.possible.bytes, "possible", label);
					for (Ratio r : a.getRatios()) {
						if (upper < r.bytes)
							upper = (1 + r.bytes / 100) * 100;
						// TODO 单位和大小缩放
						if (r.bytes > 1000 * 1000) {
							matrix = "M bytes";
						} else if (r.bytes > 1000)
							matrix = "k bytes";
					}
				}
				return dsb;
			}
		}.doPng(req, rsp);
	}

	public Api getApi() {
		return new Api(this);
	}

	private abstract class GraphImpl extends Graph {

		private ValgrindObject<SELF> obj;

		public GraphImpl(ValgrindObject<SELF> obj, Calendar timestamp,
				int defaultW, int defaultH) {
			super(timestamp, defaultW, defaultH);
			this.obj = obj;
		}

		protected abstract DataSetBuilder<String, NumberOnlyBuildLabel> createDataSet(
				ValgrindObject<SELF> obj);

		protected JFreeChart createGraph() {
			final CategoryDataset dataset = createDataSet(obj).build();
			final JFreeChart chart = ChartFactory.createLineChart(null, // chart
																		// title
					null, // unused
					"bytes", // range axis label
					dataset, // data
					PlotOrientation.VERTICAL, // orientation
					true, // include legend
					true, // tooltips
					false // urls
					);

			// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

			final LegendTitle legend = chart.getLegend();
			legend.setPosition(RectangleEdge.RIGHT);

			chart.setBackgroundPaint(Color.white);

			final CategoryPlot plot = chart.getCategoryPlot();

			// plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0,
			// 5.0));
			plot.setBackgroundPaint(Color.WHITE);
			plot.setOutlinePaint(null);
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.black);

			CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
			plot.setDomainAxis(domainAxis);
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
			domainAxis.setLowerMargin(0.0);
			domainAxis.setUpperMargin(0.0);
			domainAxis.setCategoryMargin(0.0);

			final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			rangeAxis.setUpperBound(upper);
			rangeAxis.setLowerBound(0);

			final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
					.getRenderer();
			renderer.setBaseStroke(new BasicStroke(4.0f));
			ColorPalette.apply(renderer);

			// crop extra space around the graph
			plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

			return chart;
		}

		int upper = 100;
		String matrix = "bytes";
	}

	private static final Logger logger = Logger.getLogger(ValgrindObject.class
			.getName());
}
