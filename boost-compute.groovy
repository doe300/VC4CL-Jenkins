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

    // Run the various boost-compute tests and report test results
    // TODO have list (overrideable via parameter) of all tests (grouped), run all of them
    // TODO similar to OpenCL-CTS, by default discover all tests by checking the available files
    tests = [
        runner.newTest('test_accumulate'),
        runner.newTest('test_adjacent_difference'),
        runner.newTest('test_adjacent_find'),
        runner.newTest('test_amd_cpp_kernel_language'),
        runner.newTest('test_any_all_none_of'),
        runner.newTest('test_array'),
        runner.newTest('test_async_wait'),
        runner.newTest('test_async_wait_guard'),
        runner.newTest('test_bernoulli_distribution'),
        runner.newTest('test_binary_search'),
        runner.newTest('test_buffer'),
        runner.newTest('test_buffer_allocator'),
        runner.newTest('test_buffer_iterator'),
        runner.newTest('test_clamp_range'),
        runner.newTest('test_closure'),
        runner.newTest('test_command_queue'),
        runner.newTest('test_complex'),
        runner.newTest('test_constant_iterator'),
        runner.newTest('test_context'),
        runner.newTest('test_context_error'),
        runner.newTest('test_copy'),
        runner.newTest('test_copy_if'),
        runner.newTest('test_copy_type_mismatch'),
        runner.newTest('test_count'),
        runner.newTest('test_counting_iterator'),
        runner.newTest('test_device'),
        runner.newTest('test_discard_iterator'),
        runner.newTest('test_discrete_distribution'),
        runner.newTest('test_dynamic_bitset'),
        runner.newTest('test_equal'),
        runner.newTest('test_equal_range'),
        runner.newTest('test_event'),
        runner.newTest('test_extents'),
        runner.newTest('test_extrema'),
        runner.newTest('test_fill'),
        runner.newTest('test_find'),
        runner.newTest('test_find_end'),
        runner.newTest('test_flat_map'),
        runner.newTest('test_flat_set'),
        runner.newTest('test_for_each'),
        runner.newTest('test_function'),
        runner.newTest('test_function_input_iterator'),
        runner.newTest('test_functional_as'),
        runner.newTest('test_functional_bind'),
        runner.newTest('test_functional_convert'),
        runner.newTest('test_functional_get'),
        runner.newTest('test_functional_hash'),
        runner.newTest('test_functional_identity'),
        runner.newTest('test_functional_popcount'),
        runner.newTest('test_functional_unpack'),
        runner.newTest('test_gather'),
        runner.newTest('test_generate'),
        runner.newTest('test_image1d'),
        runner.newTest('test_image2d'),
        runner.newTest('test_image3d'),
        runner.newTest('test_image_sampler'),
        runner.newTest('test_includes'),
        runner.newTest('test_inner_product'),
        runner.newTest('test_inplace_merge'),
        runner.newTest('test_inplace_reduce'),
        runner.newTest('test_insertion_sort'),
        runner.newTest('test_invoke'),
        runner.newTest('test_iota'),
        runner.newTest('test_is_permutation'),
        runner.newTest('test_is_sorted'),
        runner.newTest('test_kernel'),
        runner.newTest('test_lambda'),
        runner.newTest('test_lexicographical_compare'),
        runner.newTest('test_linear_congruential_engine'),
        runner.newTest('test_literal_conversion'),
        runner.newTest('test_local_buffer'),
        runner.newTest('test_malloc'),
        runner.newTest('test_mapped_view'),
        runner.newTest('test_merge'),
        runner.newTest('test_merge_sort_gpu'),
        runner.newTest('test_mersenne_twister_engine'),
        runner.newTest('test_mismatch'),
        runner.newTest('test_next_permutation'),
        runner.newTest('test_no_device_found'),
        runner.newTest('test_normal_distribution'),
        runner.newTest('test_nth_element'),
        runner.newTest('test_opencl_error'),
        runner.newTest('test_pair'),
        runner.newTest('test_partial_sum'),
        runner.newTest('test_partition'),
        runner.newTest('test_partition_point'),
        runner.newTest('test_permutation_iterator'),
        runner.newTest('test_pinned_allocator'),
        runner.newTest('test_pipe'),
        runner.newTest('test_platform'),
        runner.newTest('test_prev_permutation'),
        runner.newTest('test_program'),
        runner.newTest('test_program_cache'),
        runner.newTest('test_radix_sort'),
        runner.newTest('test_radix_sort_by_key'),
        runner.newTest('test_random_fill'),
        runner.newTest('test_random_shuffle'),
        runner.newTest('test_reduce'),
        runner.newTest('test_reduce_by_key'),
        runner.newTest('test_remove'),
        runner.newTest('test_replace'),
        runner.newTest('test_result_of'),
        runner.newTest('test_reverse'),
        runner.newTest('test_rotate'),
        runner.newTest('test_rotate_copy'),
        runner.newTest('test_scan'),
        runner.newTest('test_scatter'),
        runner.newTest('test_scatter_if'),
        runner.newTest('test_search'),
        runner.newTest('test_search_n'),
        runner.newTest('test_set_difference'),
        runner.newTest('test_set_intersection'),
        runner.newTest('test_set_symmetric_difference'),
        runner.newTest('test_set_union'),
        runner.newTest('test_sort'),
        runner.newTest('test_sort_by_key'),
        runner.newTest('test_sort_by_transform'),
        runner.newTest('test_stable_partition'),
        runner.newTest('test_stable_sort'),
        runner.newTest('test_stable_sort_by_key'),
        runner.newTest('test_stack'),
        runner.newTest('test_strided_iterator'),
        runner.newTest('test_string'),
        runner.newTest('test_struct'),
        runner.newTest('test_svm_ptr'),
        runner.newTest('test_system'),
        runner.newTest('test_tabulate'),
        runner.newTest('test_threefry_engine'),
        runner.newTest('test_transform'),
        runner.newTest('test_transform_if'),
        runner.newTest('test_transform_iterator'),
        runner.newTest('test_transform_reduce'),
        runner.newTest('test_tuple'),
        runner.newTest('test_type_traits'),
        runner.newTest('test_types'),
        runner.newTest('test_uniform_int_distribution'),
        runner.newTest('test_uniform_real_distribution'),
        runner.newTest('test_unique'),
        runner.newTest('test_unique_copy'),
        runner.newTest('test_unsupported_extension'),
        runner.newTest('test_user_defined_types'),
        runner.newTest('test_user_event'),
        runner.newTest('test_valarray'),
        runner.newTest('test_vector'),
        runner.newTest('test_wait_list'),
        runner.newTest('test_zip_iterator')
    ]

    createTestCommand = {test -> "sudo ${config.boostComputeDir}/${test.commandArg}"}
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
