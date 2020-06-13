# VC4CL Jenkins CI scripts

This is a collection of Jenkins scripts for running various tests against [VC4CL](https://github.com/doe300/VC4CL).
This is mainly for personal testing, but can be used by anyone who wants to run these tests.

The scripts are written in a way to require minimal Jenkins setup.
The setup steps required are documented in the specific Jenkins files.

## General Notes
Due to the nature of the tests, the actual tests need to be run on an actual Raspberry Pi (**NOT Raspberry Pi 4!**)
as well as on the actual OS. Containerization is not possible due to the low-level hardware access.
Also, all tests have to be run as *root* on the executing machine.

Wrong code running on the Raspberry Pi VideoCore IV GPU as well as wrong values written to hardware registers can completely screw up the runner.
Thus, the scripts will reboot the test runner after every failed test to reset the system into a known good state and rule our impacting successive tests.
Since this reboot is not really supported by Jenkins (AFAIK), the code for that is a little bit hacky and may not always be completely reliable.

## General setup

### Base Jenkins configuration
- Shell executable (Jenkins > configuration) needs to be set to `/bin/bash` or a compatible shell

### Jenkins slave setup
- Scripts are written to use a single Jenkins slave for now
- Needs the labels: `llvm39 run_env slave`
- Launch method should be set to "Launch agents via SSH"
- Number of executors must be set to 1
- Required software:
  * clang-3.9, llvm-3.9 (for the CI packages, other versions might be required for locally build packages)
  * ssh (also properly configured, for the Jenkins slave agent)
  * opencl-c-headers, ocl-icd-opencl-dev
  * grep, cat, tee

### Jenkins Job configuration

TODO

## Test scripts
Note: The values for expected runtime are on a Raspberry Pi 3B+, they may differ drastically e.g. for a Raspberry Pi A!

### Jenkinsfile_CTS
Runs the [official OpenCL CTS](https://github.com/KhronosGroup/OpenCL-CTS/) tests against the VC4CL implementation.

Requires:
- python
- A properly set-up build of the [OpenCL CTS](https://github.com/KhronosGroup/OpenCL-CTS/)
- The `OPENCL_CTS_DIR` environment variable should be set to the folder containing the CTS test executables, e.g. `<OpenCL-CTS>/build_lnx/test_conformance/`

Results:
- Expected result: unstable (test failures)
- Expected runtime: 10 - 12h
- Generates JUnit test results (one per test executable)
- Generates test duration plots (one per test executable)

### Jenkinsfile_boost
Runs the test-cases from [boost-compute](https://github.com/boostorg/compute/) against the VC4CL implementation.

Requires:
- python
- A properly set-up build of [boost-compute](https://github.com/boostorg/compute/)
- The `BOOST_TEST_DIR` environment variable should be set to the folder containing the test executables, e.g. `<boost-compute>/build/test/`

Results:
- Expected result: unstable (test failures)
- Expected runtime: 5-6h
- Generates JUnit test results (one per test executable)
- Generates test duration plots (one per test executable)

### Jenkinsfile_clpeak
Runs the [clpeak](https://github.com/krrishnarraj/clpeak) benchmarks against the VC4CL implementation.

Requires:
- python
- A properly set-up build of [clpeak](https://github.com/krrishnarraj/clpeak)
- The `CLPEAK_PATH` environment variable should be set to the clpeak executable, e.g. `<clpeak>/build/clpeak`

Results:
- Expected result: successive
- Expected runtime: &lt; 10min
- Generates JUnit test results (one per test executable)
- Generates test duration plots (one per test executable)
- Generates performance plots (one per test executable)

### Jenkinsfile_Test
Runs some quick tests, mainly to test the behavior of these scripts themselves.

Results:
- Expected result: unstable (test failures)
- Expected runtime: &lt; 10min
- Generates JUnit test results (one per test executable)

## Required Jenkins plugins

- [Git plugin](https://plugins.jenkins.io/git)
- [JUnit Plugin](https://plugins.jenkins.io/junit)
- [Pipeline](https://plugins.jenkins.io/workflow-aggregator)
- [Plot plugin](https://plugins.jenkins.io/plot)
- [SSH Build Agents plugin](https://plugins.jenkins.io/ssh-slaves)
- [Workspace Cleanup plugin](https://plugins.jenkins.io/ws-cleanup)
