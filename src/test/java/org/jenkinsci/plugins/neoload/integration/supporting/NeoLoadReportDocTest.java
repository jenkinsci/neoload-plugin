/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jenkinsci.plugins.neoload.integration.supporting;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author ajohnson
 *
 */
public class NeoLoadReportDocTest  {

	@Test
	public void getXPathForCustomGraph(){
		assertEquals("/report/virtual-users/statistic-item[@name=\"UserPath\"]/statistic-item[@name=\"Actions\"]/statistic-item[@name=\"toto\"]/@error_rate",NeoLoadReportDoc.getXPathForCustomGraph("UserPath>Actions>toto","error"));
		assertEquals("/report/virtual-users/statistic-item[@name=\"UserPath\"]/statistic-item[@name=\"Actions\"]/statistic-item[@name=\"toto\"]/@percentile2",NeoLoadReportDoc.getXPathForCustomGraph("UserPath>Actions>toto","percentile"));
		assertEquals("/report/virtual-users/statistic-item[@name=\"UserPath\"]/statistic-item[@name=\"Actions\"]/statistic-item[@name=\"toto\"]/@avg",NeoLoadReportDoc.getXPathForCustomGraph("UserPath>Actions>toto","average"));
	}


	@Test
	public void getXPathForCustomMonitorOrLGGraphTest(){
		assertEquals("/report/monitors/monitored-host[@name=\"LG localhost:7100\"]/monitor/counters/statistic-item[@name=\"LG localhost:7100/CPU Load\"]/@error_rate",NeoLoadReportDoc.getXPathForCustomMonitorOrLGGraph("LG localhost:7100/CPU Load","error"));
		assertEquals("/report/monitors/monitored-host[@name=\"LG localhost:7100\"]/monitor/counters/statistic-item[@name=\"LG localhost:7100/CPU Load\"]/@percentile2",NeoLoadReportDoc.getXPathForCustomMonitorOrLGGraph("LG localhost:7100/CPU Load","percentile"));
		assertEquals("/report/monitors/monitored-host[@name=\"Controller\"]/monitor/counters/statistic-item[@name=\"Controller/CPU Load\"]/@avg",NeoLoadReportDoc.getXPathForCustomMonitorOrLGGraph("Controller/CPU Load","average"));
	}

}
