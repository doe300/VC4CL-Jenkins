/**
 * Configures and runs the piglit OpenCL tests
 *
 * Required parameters:
 * - scriptDir - the path to the directory where these scripts are located
 * - piglitPath - the path to the piglit executable
 *
 * Optional parameters:
 * - setupConfig - if set, this object will be passed to the #installPackages() function (see setup.groovy) to customize the package installation
 */
def runTests(Map config) {

    // Load required libraries
    runner = load "${config.scriptDir}/runner.groovy"

    def resultsFolder = "junit"

    // Run the various piglit OpenCL tests and report test results as well as profiling information
    tests = [
        // can use 'glslparser' as fast skip-all test to just test the Jenkins scripts
        // runner.newTest('skip', 'glslparser'),
        runner.newTest('all', 'cl'),
    ]
    
    createTestCommand = {test -> "${config.piglitPath} run --sync --no-concurrency --backend junit --junit-subtests --verbose --overwrite ${test.commandArg} ${resultsFolder}"}
    // we do not need this, since piglit already generates JUnit results
    generateDummyResult = {}
    // simply point to the piglit output directory
    getReportFolder = {test_result -> "${resultsFolder}/results.xml"}
    runner.runTests(scriptDir: config.scriptDir, setupConfig: config.setupConfig, tests: tests, createCommand: createTestCommand, generateTestResults: generateDummyResult, extractProfile: null, generateTestReport: getReportFolder)

    // Reset all local variables to prevent our serialization stack overflow...
    createTestCommand = null
    generateDummyResult = null
    getReportFolder = null
    tests = null
    runner = null

    return
}

// Required so the functions/variables in here are accessible from the caller
return this
