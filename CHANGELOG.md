## Changelog

#### Version 2.2.6 (released October 03, 2019)

FIXED: Passwords storage has been changed for security reasons.

#### Version 2.2.5 (released May 20, 2019)

-   IMPROVED: Allow usage of YAML or JSON local project.

#### Version 2.2.4 (released July 26, 2018)

-   FIXED: Graph with ' character in name was not available.

#### Version 2.2.3 (released July 25, 2018)

-   IMPROVED: The log on NeoLoad error.

#### Version 2.2.2 (released July 12, 2018)

-   FIXED: Confusion of launching method when Master and Slave are
    running in different operating system.

#### Version 2.2.1 (released July 10, 2018)

-   FIXED: Process is interrupted on Windows Slave.

#### Version 2.2.0 (released June 26, 2018)

-   ADD:  Pipeline as code support

-   ADD: SAP license capability

#### Version 2.1.1 (released May 23, 2018)

-   FIXED: Runtime Exception "Issue executing password scrambler" was
    thrown when NeoLoad path executable could not be found. (\#11841)

#### Version 2.1.0 (released February 1, 2018)

-   IMPROVED: The trends are now generated at end of Neoload Job with
    new post-build Action 

#### Version 2.0.2 (released March 17, 2017)

-   FIXED: Randomly, the password scrambler could not be executed, so
    usage of shared project of shared license could fail. (\#10743)
-   FIXED: Avoid scanning result files if project does not have NeoLoad
    configuration.

#### Version 2.0.1 (released September 1, 2016)

-   FIXED: Job could not be executed if Jenkins master and slave were on
    different OS. (\#10588)
-   FIXED: Job configuration warned about missing executable and nlp
    file on master even though it could be executed on slave. (\#10635)
-   FIXED: Graphs trend did not work when job was executed on Windows.
    (\#10629)
-   IMPROVED: To install the plugin, Java version 8 was required. Now
    Java version 7 and higher are supported. (\#10610)

#### Version 2.0.0 (released June 20, 2016)

-   FIXED: When creating a new Neoload job there is no choice selected
    by default for Report File Details. (\#10272)
-   FIXED: PDF report option is not kept in Project details after a save
    or apply. (\#10253)
-   FIXED: Exception: Fail to convert sharedProject. (\#10278)
-   FIXED: When using NTS server, job failed due to NTS password
    "PASSWORD" . Input length must be multiple of 16 when decrypting.
    (\#10248)
-   FIXED: Label have to be reviewed for Neoload servers. (\#10246)
-   FIXED: Project details field ordering need to be review for better
    understanding. (\#10244)
-   FIXED: Help mention VU ( User path) when field label mention User
    Path (VU). (\#10243)
-   FIXED: Even Customize Report ... settings are not set they are
    filled in executed command. (\#10236)
-   FIXED: Jenkins 2.0: Deleting a server from the main config page
    makes the job fail. (\#10260)
-   FIXED: Job can't be run if there is no Test description: Invalid
    argument '', Expected value of type 'STRING'. (\#10235)
-   FIXED: Use new NL logo for link to Performance result. (\#10249)

#### Version 1.0.2 (released February 9, 2016)

-   FIXED: HTML report files sometimes didn't display. (\#9257)

#### Version 1.0.1 (released February 6, 2014)

-   FIXED: Performance results links were lost when a job was renamed.
    (\#5308)
-   FIXED: Performance results linked to a report artifact that was not
    related to the current build after renaming a job. (\#5309)
-   FIXED: Displayed graph values did not match displayed html report
    values. (\#5813)

#### Version 1.0 (released November 5, 2013)

-   Initial release.
