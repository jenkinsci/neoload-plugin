package org.jenkinsci.plugins.neoload.integration.supporting;

import hudson.util.Secret;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.xml.xpath.XPathExpressionException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Secret.class})
public class NeoloadGraphsStatisticsTest {

	private MockObjects mo = null;
	private File report1;
	private File report2;

	@Mock
	private Secret secret;

	@Before
	public void setup() throws IOException, IllegalAccessException {
		PowerMockito.when(secret.getEncryptedValue()).thenReturn("123");
		mo = new MockObjects(secret);
		report1 = new File(NeoLoadReportDocTest.class.getResource("data/myReport.xml").getFile());
		report2 = new File(NeoLoadReportDocTest.class.getResource("data/report-valid.xml").getFile());

	}

	@Test
	public void addReportAvgErr() {

		final NeoloadGraphsStatistics neoloadGraphsStatistics = new NeoloadGraphsStatistics(mo.getNeoBuildAction());
		neoloadGraphsStatistics.addReport(report1,1);
		neoloadGraphsStatistics.addReport(report2,2);
		final List<NeoloadGraphXPathStat> neoloadGraphXPathStats = neoloadGraphsStatistics.getNeoloadGraphXPathStats();
		assertEquals(2,neoloadGraphXPathStats.size());
		assertEquals(new Float(0.0),neoloadGraphXPathStats.get(0).getCurves().get(0).getBuildToValue().get(1));
		assertEquals(new Float(0.038),neoloadGraphXPathStats.get(0).getCurves().get(0).getBuildToValue().get(2));


		assertEquals(new Float(100),neoloadGraphXPathStats.get(1).getCurves().get(0).getBuildToValue().get(1));
		assertEquals(new Float(0.0),neoloadGraphXPathStats.get(1).getCurves().get(0).getBuildToValue().get(2));
	}

}