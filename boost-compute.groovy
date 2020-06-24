/**
 * Configures and runs the boost-compute tests
 *
 * Required parameters:
 * - scriptDir - the path to the directory where these scripts are located
 * - boostComputeDir - the path to the boost-compute build directory containing the test executables (e.g. <boost-compute>/build/test/ for in-tree build)
 *
 * Optional parameters:
 * - setupConfig - if set, this object will be passed to the #installPackages() function (see setup.groovy) to customize the package installation
 */
def runTests(Map config) {

    // Load required libraries
    runner = load "${config.scriptDir}/runner.groovy"

    // Run the various OpenCL-CTS tests and report test results
    // We automatically discover all the tests to not have to update them manually
    echo "Getting list of tests..."
    test_files = sh returnStdout: true, script: "find ${config.boostComputeDir} -name 'test_*' -executable -type f | sort"
    test_files = test_files.split('\n')
    // TODO have list (overrideable via parameter) of all tests (grouped), run all of them. E.g. move the test detection to default of parameters?
    tests = []
    for (test in test_files) {
        name = test.split('/').last()
        tests.add(runner.newTest(name, test))
    }

    createTestCommand = {test -> "sudo ${test.commandArg}"}
    // TODO set config.generateTestResults to extract log/detailed test cases from stdout/stderr
    runner.runTests(scriptDir: config.scriptDir, setupConfig: config.setupConfig, tests: tests, createCommand: createTestCommand, extractProfile: null)

    // Reset all local variables to prevent our serialization stack overflow...
    createTestCommand = null
    tests = null
    runner = null

    return
}

// Required so the functions/variables in here are accessible from the caller
return this
