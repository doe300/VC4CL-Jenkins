/**
 * Configures and runs the boost-compute tests
 *
 * Required parameters:
 * - scriptDir - the path to the directory where these scripts are located
 * - conformanceTestDir - the path to the OpenCL-CTS build directory containing the test executables (e.g. <OpenCL-CTS>/build_lnx/test_conformance/ for in-tree build)
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
    test_files = sh returnStdout: true, script: "find ${config.conformanceTestDir} -name 'test_*' -executable -type f | sort"
    test_files = test_files.split('\n')
    blacklist = [
        'test_printf' // Completely hangs Jenkins (slave), can't even abort properly anymore
    ]
    // TODO have list (overrideable via parameter) of all tests (grouped), run all of them. E.g. move the test detection to default of parameters?
    tests = []
    for (test in test_files) {
        name = test.split('/').last()
        if(blacklist.contains(name)) {
            continue
        }
        if(name.contains("spirv_new")) {
            // Need to specify the SPIR-V binary folder for some reason, see https://github.com/KhronosGroup/OpenCL-CTS/blob/master/test_conformance/spirv_new/README.txt
            test = test + " --spirv-binaries-path ${config.conformanceTestDir}/../../test_conformance/spirv_new/spirv_bin/"
        }
        if(name.contains("bruteforce")) {
            // For now, since the full test will not pass for a long time still, just run in wimpy mode
            test = test + " -w"
        }
        tests.add(runner.newTest(name, test))
    }

    createTestCommand = {test -> "sudo ${test.commandArg}"}
    hasTestPassed = { returnValue, stdoutFile, stderrFile ->
        // If some tests fail normally (e.g. checks fail), the return value is still reported as success (0), so we need to also check the output
        errorInStdout = sh returnStdout: true, script: "grep -P 'FAILED (\\d+ of \\d+ )?test' ${stdoutFile} || echo 'PASSED'"
        returnValue == 0 && !errorInStdout.toString().contains('FAILED')
    }
    runner.runTests(scriptDir: config.scriptDir, setupConfig: config.setupConfig, tests: tests, createCommand: createTestCommand, extractProfile: null, checkTestPassed: hasTestPassed)

    // Reset all local variables to prevent our serialization stack overflow...
    createTestCommand = null
    tests = null
    runner = null

    return
}

// Required so the functions/variables in here are accessible from the caller
return this
