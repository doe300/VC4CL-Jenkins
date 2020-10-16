/**
 * Container type for the information required to run a single test-suite
 */
class Test {
    // general fields
    String name
    String commandArg

    // profiling fields
    String unit
    String xPathSelector

    Test(String name, String commandArg, String unit, xPathSelector) {
        this.name = name;
        this.commandArg = commandArg

        this.unit = unit
        this.xPathSelector = xPathSelector
    }
}

/**
 * Creates a new Test using the given command string as both test-suite name as well as command
 *
 * Returns a newly created Test object
 */
def newTest(String command) {
    return new Test(command, command, '', '')
}

/**
 * Creates a new Test using the given name and command strings
 *
 * Returns a newly created Test object
 */
def newTest(String name, String commandArg) {
    return new Test(name, commandArg, '', '')
}

/**
 * Create a new Test using the full signature
 *
 * The additional parameters "unit" and "xPathSelector" are used for extracting plot information (e.g. for profiling)
 *
 * Returns a newly created Test object
 */
def newTest(String name, String commandArg, String unit, xPathSelector) {
    return new Test(name, commandArg, unit, xPathSelector)
}

/**
 * Container type for storing information about the result of a test suite
 */
@groovy.transform.TupleConstructor
class TestResult {
    String name
    int returnValue
    String stdoutFile
    String stderrFile
}

/**
 * Runs all the given tests, records the results and any plot data
 *
 * This is the main function doing all the heavy lifting (or at least the distribution of that).
 *
 * Required parameters:
 * - scriptDir - the path to the directory where these scripts are located
 * - tests - the list of Test objects to be executed sequentially
 * - createCommand - a function taking the Test object and returning a String which will then be actually executed
 *
 * Optional parameters:
 * - skipSetup - if set, the setup will be skipped, i.e. the packages for VC4C, VC4CL and VC4CLStdLib will not be installed
 * - setupConfig - if set, this object will be passed to the #installPackages() function (see setup.groovy) to customize the package installation
 * - generateTestResults - if set, this function taking a TestResult and a long (test duration) will be used to create a list of TestCase objects (see junit-report.groovy) for this given test
 * - checkTestPassed - if set (and "generateTestResults" is not given), this function taking an int (the test process return value) and two Strings (the paths to the files containing the test process standard output and error)
 *                     will be used to determine whether the test has passed (returns true) or failed (returns false). If not set, a test is assumed to have passed when it's return value is zero (0).
 * - extractProfile - if set, this function takes a TestResult object and returns a String to the file used to extract profiling information from
 * - skipCleanup - if set, the cleanup will be skipped, i.e. the packages for VC4C, VC4CL and VC4CLStdLib will not be uninstalled after the tests have run
 */
def runTests(Map config) {

    // Load required libraries
    junit_scripts = load "${config.scriptDir}/junit-report.groovy"
    setup_scripts = load "${config.scriptDir}/setup.groovy"

    def timeoutInMinutes = 20
    def rebootTimeoutInMinutes = 2

    if(!config.containsKey('skipSetup')) {
        script {
            stage('Setup') {
                setup_scripts.installPackages(config.scriptDir, config.containsKey('setupConfig') ? config['setupConfig'] : null)
            }
        }
    }

    hasPassed = config.containsKey('checkTestPassed') && config.checkTestPassed != null ? config.checkTestPassed : {returnValue, stdoutFile, stderrFile -> returnValue == 0}

    for (test in config.tests) {
        script {
            stage (test.name) {
                test_result = null
                long duration = 0
                TestResult result = null

                time_start = (new Date()).getTime()
                try {
                    timeout(activity: true /* when no output */, time: timeoutInMinutes /* minutes */) {
                        result = runSingleTest(test.name, config.createCommand(test))
                    }
                    time_end = (new Date()).getTime()
                    duration = (time_end - time_start) / 1000
                    if (config.generateTestResults != null) {
                        test_result = config.generateTestResults(result, duration)
                    } else {
                        if (hasPassed(result.returnValue, result.stdoutFile, result.stderrFile)) {
                            test_result = [junit_scripts.createPassed(name: result.name, timeInSeconds: duration, stdoutFile: result.stdoutFile, stderrFile: result.stderrFile)]
                        } else {
                            test_result = [junit_scripts.createFailed(name: result.name, timeInSeconds: duration, stdoutFile: result.stdoutFile, stderrFile: result.stderrFile)]
                        }
                    }
                    // This jumps into the catch block on error to restart the host
                    sh "exit ${result.returnValue}"
                } catch (e) {
                    if (test_result == null) {
                        // we also come here for FAILED tests, so don't overwrite the actual FAILED test result
                        time_end = (new Date()).getTime()
                        duration = (time_end - time_start) / 1000
                        test_result = [junit_scripts.createTimeout(name: test.name, timeInSeconds: duration)]
                    }

                    echo "Error occurred: ${e}"
                    try {
                        // Timeout needed, seems the reboot command seems to hang the SSH connection.
                        timeout(time: rebootTimeoutInMinutes /* minutes */) {
                            sh 'echo "Rebooting slave..."'
                            sh 'sudo reboot --force'
                        }
                    } catch (ex) {
                        // It is completely expected that we go in here, since we break the SSH connection
                        // Set the stage status to SUCCESS instead of the default of ABORTED to allow the post-build steps (JUnit graph, plots) to run
                        // Since we always report the JUnit results for all steps, the whole build will in the end still be marked UNSTABLE on test failures
                        currentBuild.result = 'SUCCESS'
                    }
                }

                try {
                    // If we fail above to reconnect to the slave in the given timeout after rebooting, any following code will fail.
                    // So we try to do something on the slave and then wait a little bit more to give more time to reconnect, if the slave is not available.
                    sh "echo 'Test run complete in ${duration} seconds, collecting results...'"
                } catch (ex) {
                    echo 'Slave not yet back up, waiting some more...'
                    sleep time: rebootTimeoutInMinutes, unit: 'MINUTES'
                    // Don't fail the build due to this, see comment in above catch-clause
                    currentBuild.result = 'SUCCESS'
                }

                try {
                    // If we fail above to reconnect to the slave in the extended timeout after rebooting, any following code will fail.
                    // At this point, the slave is not answering at all any more, so properly abort the test run
                    sh 'echo ""'
                } catch (ex) {
                    echo 'Slave unresponsive, aborting test run...'
                    // Completely abort the loop
                    break
                }

                // run the reports
                echo 'Generating test report...'
                def report_file = junit_scripts.generateReport(name: test.name, tests: test_result)
                junit report_file

                //write the CSV file for the time result
                def timeFile = "${test.name}-time.csv"
                writeFile file: timeFile, text: "total\n${duration}"
                plot csvFileName: "plot-$JOB_NAME-${test.name}-time.csv", csvSeries: [
                        [displayTableFlag: false, exclusionValues: '', file: timeFile, inclusionFlag: 'OFF', url: '']
                    ],
                    group: test.name, style: 'line', title: 'Total duration', yaxis: 'seconds', keepRecords: true

                // write the profiling result
                if (result != null && config.extractProfile != null) {
                    // TODO support for non-XML format and none NODESET nodes (e.g. clpeak/kernel_latency)
                    def profileFile = config.extractProfile(result)
                    plot csvFileName: "plot-$JOB_NAME-${test.name}-performance.csv", xmlSeries: [
                            [file: profileFile, nodeType: 'NODESET', url: '', xpath: test.xPathSelector]
                        ],
                        group: test.name, style: 'line', title: 'Performance', yaxis: test.unit, keepRecords: true
                }
                // Reset local variables to prevent our serialization stack overflow...
                test_result = null
                report_file = null
                result = null                
            }
        }
    }

    if(!config.containsKey('skipCleanup')) {
        script {
            stage('Cleanup') {
                setup_scripts.uninstallPackages()
            }
        }
    }

    // Reset loaded script to prevent our serialization stack overflow...
    hasPassed = null
    junit_scripts = null
    setup_scripts = null

    return
}


/**
 * Helper function to run a single test command and return a TestResult object containing the captured retrun value (exit code),
 * standard output and standard error streams.
 */
def runSingleTest(name, command) {
    // Configures to return the last non-zero (if any) exit code instead of the exit code of the last command, requires bash!
    outFile = "${name}-stdout.log"
    errFile = "${name}-stderr.log"
    sh 'set -o pipefail'
    full_command = "${command} 1> >(tee ${outFile}) 2> >(tee ${errFile} >&2)"
    code = sh returnStatus: true, script: full_command
    // TODO there could be a race, since the tee might not yet have finished when the main command is done
    return new TestResult(name, code, outFile, errFile)
}

// Required so the functions/variables in here are accessible from the caller
return this
