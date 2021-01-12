# neoload-plugin
https://www.neotys.com

This plugin allows you to monitor load tests performed by
[NeoLoad](http://www.neotys.com/product/overview-neoload.html)
from Jenkins. NeoLoad test results are combined with the other
integration jobs in the Jenkins dashboard. Performance regression issues
are raised quickly to make Jenkins integration projects more relevant.
For regular jobs or pipeline jobs.

After every build of a job configured with the NeoLoad plugin, Jenkins
can display:

-   the HTML summary of the NeoLoad test result per build available with
    the **Performance Result** command
-   the JUnit details of every passed or failed test available with the
    **Test Result** command---also used in the test result trend graph

See <https://www.neotys.com/documents/doc/neoload/latest/en/html/#5769.htm> for
detailed documentation.

## Prerequisites

The NeoLoad plugin, from version 2.1.0 requires:

-   Jenkins 1.609.1 or later, 
-   Java 7 or later.

## Collaboration Configuration

The plugin allows to configure a collaboration server like Neotys Team
Server (NTS) with the **Manage Jenkins \> Configure System** command.
![](/docs/images/Nl-Jenkins-NTSServer.PNG)

## Build Step

The plugin adds a new build step: **Execute a NeoLoad Scenario**.

The following steps are required:

-   Type the path of the **NeoLoad** executable.
-   Select **Local Project** or **Shared Project** and configure it.
-   Type the name of the NeoLoad scenario in the **Scenario Name**
    field.
-   Select **Default Report File Names** or **Custom Report File
    Names** and configure it.

![](/docs/images/NL-Jenkins-ExecuteNLScenario.PNG)

-   Select **Existing License** or **Shared License** and configure it.

![](/docs/images/NL-Jenkins-ExecuteNLScenario2.PNG)


-   Select **Display Trend Graph: Average Response Time (all pages)** to
    include the **Avg. Resp. Time** trend graph in Jenkins.
-   Select **Display Trend Graph: Error Rate %** to include the **Error
    Rate** trend graph in Jenkins.
-   *(Recommended)* Add as many user-defined graphs as wanted with
    several curves on each graph.  
    ![](/docs/images/configuration.png)}

## Post-Build Action

The plugin needs the **Archive the artifacts** post-build action. The
regeneration of trends could be triggered. Please archive the artifact
before Refresh trends.

![](/docs/images/post-build-actions.png)

**Example Trend Graphs**

**![](/docs/images/graphs.png)}**

## Pipelines Steps

The "neoloadRun" step in the Jenkins Snippet Generator makes it possible
to run a NeoLoad scenario from Jenkins. It also archives the reports and
refreshes the graphs.

-   Warning: To use the Snippet Generator, the Jenkins project including
    the job to configure must be compliant with Pipeline as code. For
    more information, see [Pipeline as
    code](https://jenkins.io/doc/book/pipeline-as-code/#introduction).

Once the Jenkins project is selected, the **Snippet Generator** is
accessible with a click on the **Pipeline Syntax** link.

This plugin provides two steps: 

-   neoloadRun: to run NeoLoad scenario, archive report and refresh the
    trends.

-   neoloadRefreshTrends: to refresh or change the trends only.

##### Execute NeoLoad

```groovy
neoloadRun executable: '/opt/neoload/bin/NeoLoadCmd', project: 'test.nlp', scenario: 'Test for CD', trendGraphs: ['AvgResponseTime', 'ErrorRate']
neoloadRun executable: '/opt/neoload/bin/NeoloadCmd', project: [server: 'NTS', name: 'MyProject', publishTestResult: false], scenario: 'Test for CD', trendGraphs: ['AvgResponseTime', 'ErrorRate']
neoloadRun executable: '/opt/neoload/bin/NeoloadCmd', project: 'test.nlp', scenario: 'Test for CD', sharedLicense: [server: 'NTS', duration: 2, vuCount: 50], trendGraphs: ['AvgResponseTime', 'ErrorRate']
```

#####  Refresh graph

```groovy
neoloadRefreshTrends(trendGraphs: ['AvgResponseTime', 'ErrorRate'])
neoloadRefreshTrends(trendGraphs: [[name: 'Cpu vs User Load', curve: ['Controller/User Load', 'LG localhost:7100/CPU Load'], statistic: 'error'], 'AvgResponseTime', 'ErrorRate'])
```

## FAQ

Why don't I see any trend graphs?

In order to see trend graphs, please verify:

-   The **Archive the artifacts** post-build action has been added.
-   Either **Default Report File Names** or **Custom Report File
    Names** is selected and an xml report is defined.
-   At least two executions were run.
-   Date and time is synchronized between the Jenkins machine and the
    build machine.

## Known Issues

1. The NeoLoad report file (via artifacts and the Performance Result
link) displays a blank page. This affects versions released before
NeoLoad 5.2.

#### Workaround

Use the Jenkins Script Console to disable the sandboxing security by
executing the following script. The Script Console is under Jenkins -\>
Manage Jenkins -\> Script Console.

    System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")

Clear the cache afterwards (hold shift and reload the page).

See
<https://wiki.jenkins-ci.org/display/JENKINS/Configuring+Content+Security+Policy>
for more information.

2. The NeoLoad Graphs aren't displayed in the main page of my job.

#### Workaround

Make sure you used a "Freestyle project" for your job. If you use (for
example) the Maven Plugin for your job, create a "Freestyle project"
then add Maven configuration build step.
