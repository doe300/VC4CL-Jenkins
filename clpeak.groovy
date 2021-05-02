/**
 * Configures and runs the clpeak benchmarks
 *
 * Required parameters:
 * - scriptDir - the path to the directory where these scripts are located
 * - clpeakPath - the path to the clpeak executable
 *
 * Optional parameters:
 * - setupConfig - if set, this object will be passed to the #installPackages() function (see setup.groovy) to customize the package installation
 */
def runTests(Map config) {

    // Load required libraries
    runner = load "${config.scriptDir}/runner.groovy"

    // Run the various clpeak tests and report test results as well as profiling information
    tests = [
        runner.newTest('global-bandwidth', '--global-bandwidth', 'GB/s', '/clpeak/platform/device/global_memory_bandwidth/*'),
        runner.newTest('compute-sp', '--compute-sp', 'GFLOP/s', '/clpeak/platform/device/single_precision_compute/*'),
        runner.newTest('compute-integer', '--compute-integer', 'GIOP/s', '/clpeak/platform/device/integer_compute/*'),
        runner.newTest('compute-intfast', '--compute-intfast', 'GIOP/s', '/clpeak/platform/device/integer_compute_fast/*'),
        // TODO split graphs for enqueue(un)map and rest, since they differ too much, how?
        runner.newTest('transfer-bandwidth', '--transfer-bandwidth', 'GB/s', '/clpeak/platform/device/transfer_bandwidth/*'),
        runner.newTest('kernel-latency', '--kernel-latency', 'us', '/clpeak/platform/device/*')
    ]
    
    createTestCommand = {test -> "${config.clpeakPath} ${test.commandArg} --enable-xml-dump --xml-file ${test.name}.xml"}
    extractTestProfile = {test -> "${test.name}.xml"}
    runner.runTests(scriptDir: config.scriptDir, setupConfig: config.setupConfig, tests: tests, createCommand: createTestCommand, extractProfile: extractTestProfile)

    // Reset all local variables to prevent our serialization stack overflow...
    extractTestProfile = null
    createTestCommand = null
    tests = null
    runner = null

    return
}

// Required so the functions/variables in here are accessible from the caller
return this
