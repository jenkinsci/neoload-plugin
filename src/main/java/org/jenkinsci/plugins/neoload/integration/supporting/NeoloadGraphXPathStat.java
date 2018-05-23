package org.jenkinsci.plugins.neoload.integration.supporting;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.Size2D;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;


/**
 * The type Neoload graph x path stat.
 */
public class NeoloadGraphXPathStat {




	/**
	 * We use the same size as the default junit trend graph.
	 */
	private static final int IMAGE_HEIGHT = 200;

	/**
	 * We use the same size as the default junit trend graph.
	 */
	private static final int IMAGE_WIDTH = 500;

	/**
	 * We use the same size as the default junit trend legend.
	 */
	private static final int LEGEND_HEIGHT = 20;


	private final String title;
	private final List<NeoloadCurvesXPathStat> curves;
	private final String yLabel;
	private final boolean legend;

	/**
	 * Instantiates a new Neoload graph x path stat.
	 *
	 * @param title  the title
	 * @param yLabel the y label
	 * @param curves the curves
	 */
	public NeoloadGraphXPathStat(final String title, final String yLabel, final NeoloadCurvesXPathStat... curves) {
		this.title = title;
		this.curves = Arrays.asList(curves);
		this.yLabel = yLabel;
		this.legend = false;
	}

	/**
	 * Instantiates a new Neoload graph x path stat.
	 *
	 * @param title  the title
	 * @param yLabel the y label
	 * @param curves the curves
	 */
	public NeoloadGraphXPathStat(final String title, final String yLabel, final List<NeoloadCurvesXPathStat> curves) {
		this.title = title;
		this.curves = curves;
		this.yLabel = yLabel;
		this.legend = true;
	}

	/**
	 * Gets title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets curves.
	 *
	 * @return the curves
	 */
	public List<NeoloadCurvesXPathStat> getCurves() {
		return curves;
	}

	/**
	 * Gets label.
	 *
	 * @return the label
	 */
	public String getyLabel() {
		return yLabel;
	}

	/**
	 * Add build.
	 *
	 * @param buildNumber the build number
	 * @param document    the document
	 */
	public void addBuild(final int buildNumber, final Document document) {
		for (NeoloadCurvesXPathStat neoloadCurvesXPathStat : curves) {
			neoloadCurvesXPathStat.addBuild(buildNumber, document);
		}
	}

	private CategoryDataset getDataSet() {
		final DefaultCategoryDataset ds = new DefaultCategoryDataset();
		for (NeoloadCurvesXPathStat curve : curves) {
			for (Map.Entry<Integer, Float> entry : curve.getBuildToValue().entrySet()) {
				ds.addValue(entry.getValue(), curve.getLegend(), "#" + entry.getKey());
			}
		}
		return ds;
	}

	private JFreeChart createChart() {

		final JFreeChart chart = ChartFactory.createLineChart(null, // chart title
				null, // categoryAxisLabel
				yLabel, // range axis label
				getDataSet(), // data
				PlotOrientation.VERTICAL, // orientation
				legend, // include legend
				true, // tooltips
				false // urls
		);

		chart.setBackgroundPaint(Color.white);

		final TextTitle textTitle;
		if (title != null && !title.trim().equals("")) {
			textTitle = new TextTitle(title, new Font("Helvetica", Font.BOLD, 16));
		} else {
			// Here it's to have a good display, we need a "blank" title.
			textTitle = new TextTitle(" ", new Font("Helvetica", Font.BOLD, 16));
		}
		chart.setTitle(textTitle);
		if (chart.getLegend() != null) {
			chart.getLegend().setBorder(0, 0, 0, 0); // To haven't border for the legend.
		}

		final CategoryPlot plot = chart.getCategoryPlot();

		// turn the y labels sideways
		final CategoryAxis axis = plot.getDomainAxis();
		axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

		final NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();

		yAxis.setStandardTickUnits(createStandardTickUnits());


		int i = 0;
		for (NeoloadCurvesXPathStat curve : curves) {
			plot.getRenderer().setSeriesPaint(i, curve.getColor());
		}

		plot.setBackgroundPaint(Color.white);

		return chart;
	}

	/**
	 * Number of builds int.
	 *
	 * @return the int
	 */
	int numberOfBuilds() {
		int nbOfBuild = 0;
		for (NeoloadCurvesXPathStat curve : curves) {
			nbOfBuild = Math.max(curve.getBuildsNumber(), nbOfBuild);
		}
		return nbOfBuild;
	}

	/**
	 * Compute width int.
	 *
	 * @return the int
	 */
	int computeWidth() {
		return Math.max(IMAGE_WIDTH, numberOfBuilds() * 15);
	}

	/**
	 * Write png.
	 *
	 * @param file the file
	 * @throws IOException the io exception
	 */
	public void writePng(final File file) throws IOException {
		final int width = computeWidth();
		final JFreeChart chart = createChart();
		final int height = computeHeight(chart,width);
		ChartUtilities.saveChartAsPNG(file, chart, width, height);

	}

	private int computeHeight(final JFreeChart chart, final int width) {
		return computeLegendHeight(chart, width) + IMAGE_HEIGHT;
	}

	private int computeLegendHeight(final JFreeChart chart, final int imageWidth) {
		if (chart.getSubtitleCount() == 0) {
			return 0;
		}

		BufferedImage image = new BufferedImage(imageWidth, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = image.createGraphics();
		RectangleConstraint constraint = new RectangleConstraint(
				imageWidth, new Range(0.0, imageWidth), LengthConstraintType.RANGE,
				0.0, null, LengthConstraintType.NONE
		);
		int height = 0;
		for (int i = 0; i < chart.getSubtitleCount(); i++) {
			final Title subtitle = chart.getSubtitle(i);
			final Size2D arrange = subtitle.arrange(g2, constraint);
			height += arrange.getHeight();
		}
		return height;
	}


	/**
	 * Creates the standard tick units.
	 *
	 * If you don't like these defaults, create your own instance of TickUnits
	 * and then pass it to the setStandardTickUnits() method in the
	 * NumberAxis class.
	 *	 see setStandardTickUnits(TickUnitSource)
	 * 	 see createIntegerTickUnits()
	 *
	 *
	 * @return The standard tick units.
	 */
	public static TickUnitSource createStandardTickUnits() {

		TickUnits units = new TickUnits();

		DecimalFormat df5 = new DecimalFormat("0.000");
		DecimalFormat df6 = new DecimalFormat("0.00");
		DecimalFormat df7 = new DecimalFormat("0.0");
		DecimalFormat df8 = new DecimalFormat("#,##0");
		DecimalFormat df9 = new DecimalFormat("#,###,##0");
		DecimalFormat df10 = new DecimalFormat("#,###,###,##0");

		// we can add the units in any order, the TickUnits collection will
		// sort them...

		units.add(new NumberTickUnit(0.001, df5));
		units.add(new NumberTickUnit(0.01, df6));
		units.add(new NumberTickUnit(0.1, df7));
		units.add(new NumberTickUnit(1, df8));
		units.add(new NumberTickUnit(10, df8));
		units.add(new NumberTickUnit(100, df8));
		units.add(new NumberTickUnit(1000, df8));
		units.add(new NumberTickUnit(10000, df8));
		units.add(new NumberTickUnit(100000, df8));
		units.add(new NumberTickUnit(1000000, df9));
		units.add(new NumberTickUnit(10000000, df9));
		units.add(new NumberTickUnit(100000000, df9));
		units.add(new NumberTickUnit(1000000000, df10));
		units.add(new NumberTickUnit(10000000000.0, df10));
		units.add(new NumberTickUnit(100000000000.0, df10));


		units.add(new NumberTickUnit(0.025, df5));
		units.add(new NumberTickUnit(0.25, df6));
		units.add(new NumberTickUnit(2.5, df7));
		units.add(new NumberTickUnit(25, df8));
		units.add(new NumberTickUnit(250, df8));
		units.add(new NumberTickUnit(2500, df8));
		units.add(new NumberTickUnit(25000, df8));
		units.add(new NumberTickUnit(250000, df8));
		units.add(new NumberTickUnit(2500000, df9));
		units.add(new NumberTickUnit(25000000, df9));
		units.add(new NumberTickUnit(250000000, df9));
		units.add(new NumberTickUnit(2500000000.0, df10));
		units.add(new NumberTickUnit(25000000000.0, df10));
		units.add(new NumberTickUnit(250000000000.0, df10));


		units.add(new NumberTickUnit(0.005, df5));
		units.add(new NumberTickUnit(0.05, df6));
		units.add(new NumberTickUnit(0.5, df7));
		units.add(new NumberTickUnit(5L, df8));
		units.add(new NumberTickUnit(50L, df8));
		units.add(new NumberTickUnit(500L, df8));
		units.add(new NumberTickUnit(5000L, df8));
		units.add(new NumberTickUnit(50000L, df8));
		units.add(new NumberTickUnit(500000L, df8));
		units.add(new NumberTickUnit(5000000L, df9));
		units.add(new NumberTickUnit(50000000L, df9));
		units.add(new NumberTickUnit(500000000L, df9));
		units.add(new NumberTickUnit(5000000000L, df10));
		units.add(new NumberTickUnit(50000000000L, df10));
		units.add(new NumberTickUnit(500000000000L, df10));

		return units;

	}


}
