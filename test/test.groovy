// TODO document required, optional entries and default values
def runTests(Map config) {

    // Load required libraries
    junit_scripts = load "${config.scriptDir}/junit-report.groovy"

    // Test out test reports
    reportFile = junit_scripts.generateReport(name: "testReport", tests: [
        junit_scripts.createTimeout(name: 'SuperTest', timeInSeconds: 15000, stderr: 'Some error'),
        junit_scripts.createPassed(name: 'DuperTest', timeInSeconds: 150),
        junit_scripts.createSkipped(name: 'MegaTest', timeInSeconds: 17, stdout: 'Some error'),
        junit_scripts.createFailed(name: 'GigaTest', timeInSeconds: 42, message: 'Too bad', stderr: 'Some error')
    ])
    echo "${reportFile}"
    junit "${reportFile}"

    reportFile = junit_scripts.generateReport(name: "secondReport", tests: [
        junit_scripts.createTimeout(name: 'SuperTest', timeInSeconds: 15000, stderr: 'Some error'),
        junit_scripts.createPassed(name: 'DuperTest', timeInSeconds: 150),
        junit_scripts.createSkipped(name: 'MegaTest', timeInSeconds: 17, stdout: 'Some error'),
        junit_scripts.createFailed(name: 'GigaTest', timeInSeconds: 42, message: 'Too bad', stderr: 'Some error')
    ])
    echo "${reportFile}"
    junit "${reportFile}"

    // Test plot/profiling data
    sh "/usr/bin/time -o ./time.csv --format='wall clock,system,user\n%e,%S,%U' sleep \$(shuf -i 1-3 -n 1)> /dev/null"
    plot csvFileName: 'plot-test-time.csv', csvSeries: [[displayTableFlag: false, exclusionValues: '', file: 'time.csv', inclusionFlag: 'OFF', url: '']], group: 'Test Plot', style: 'line', title: 'Some sleep Time', yaxis: 'seconds'
    sh "cp ${config.scriptDir}/test/clpeak-log.sp.xml ./clpeak-log.sp.xml"
    plot csvFileName: 'plot-test-performance.csv', group: 'Test Plot', style: 'line', title: 'Performance', yaxis: 'GFLOP/s', xmlSeries: [[file: 'clpeak-log.sp.xml', nodeType: 'NODESET', url: '', xpath: '/clpeak/platform/device/single_precision_compute/*']]
}

// Required so the functions/variables in here are accessible from the caller
return this
