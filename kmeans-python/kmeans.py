"""
A PSCO based implementation of the K-Means algorithm.
2018
Author: Sergio Rodriguez Guasch < sergio dot rodriguez at bsc dot es >
Revision (dataClay, objects): Alex Barcelo < alex dot barcelo at bsc dot es >
"""
from dataclay.api import init, finish


def kmeans_frag(fragments, dimensions, num_centres=10, iterations=20, seed=0., epsilon=1e-9, norm='l2'):
    """
    A fragment-based K-Means algorithm.
    Given a set of fragments (which can be either PSCOs or future objects that
    point to PSCOs), the desired number of clusters and the maximum number of
    iterations, compute the optimal centres and the index of the centre
    for each point.
    PSCO.mat must be a NxD float np.matrix, where D = dimensions
    :param fragments: Number of fragments
    :param dimensions: Number of dimensions
    :param num_centres: Number of centers
    :param iterations: Maximum number of iterations
    :param seed: Random seed
    :param epsilon: Epsilon (convergence distance)
    :param norm: Norm
    :return: Final centers and labels
    """
    import numpy as np
    # Choose the norm among the available ones
    norms = {
        'l1': 1,
        'l2': 2,
    }
    # Set the random seed
    np.random.seed(seed)
    # Centres is usually a very small matrix, so it is affordable to have it in
    # the master.
    centres = np.matrix(
        [np.random.random(dimensions) for _ in range(num_centres)]
    )

    # Note: this implementation treats the centres as files, never as PSCOs.
    for it in range(iterations):
        print("Doing iteration #%d/%d" % (it, iterations))
        partial_results = []
        for frag in fragments:
            # For each fragment compute, for each point, the nearest centre.
            # Return the mean sum of the coordinates assigned to each centre.
            # Note that mean = mean ( sum of sub-means )
            partial_result = frag.cluster_and_partial_sums(centres, norms[norm])
            partial_results.append(partial_result)

        print("Tasks sent")

        # Bring the partial sums to the master, compute new centres when syncing
        new_centres = np.matrix(np.zeros(centres.shape))
        # from pycompss.api.api import compss_wait_on
        for partial in partial_results:
            # partial = compss_wait_on(partial)
            # Mean of means, single step
            new_centres += partial / float(len(fragments))
        if np.linalg.norm(centres - new_centres, norms[norm]) < epsilon:
            # Convergence criterion is met
            break
        # Convergence criterion is not met, update centres
        centres = new_centres

    # If we are here either we have converged or we have run out of iterations
    # In any case, now it is time to update the labels in the master

    # from pycompss.api.api import compss_barrier
    # compss_barrier()

    return centres, None

    ret_labels = []
    for frag in fragments:
        ret_labels.extend(frag.labels)
    return centres, ret_labels


def parse_arguments():
    """
    Parse command line arguments. Make the program generate
    a help message in case of wrong usage.
    :return: Parsed arguments
    """
    import argparse
    parser = argparse.ArgumentParser(description='A COMPSs-Redis Kmeans implementation.')
    parser.add_argument('-s', '--seed', type=int, default=0,
                        help='Pseudo-random seed. Default = 0')
    parser.add_argument('-n', '--numpoints', type=int, default=100,
                        help='Number of points. Default = 100')
    parser.add_argument('-d', '--dimensions', type=int, default=2,
                        help='Number of dimensions. Default = 2')
    parser.add_argument('-c', '--centres', type=int, default=5,
                        help='Number of centres. Default = 2')
    parser.add_argument('-f', '--fragments', type=int, default=10,
                        help='Number of fragments. Default = 10. Condition: fragments < points')
    parser.add_argument('-m', '--mode', type=str, default='uniform',
                        choices=['uniform', 'normal'],
                        help='Distribution of points. Default = uniform')
    parser.add_argument('-i', '--iterations', type=int, default=20,
                        help='Maximum number of iterations')
    parser.add_argument('-e', '--epsilon', type=float, default=1e-9,
                        help='Epsilon. Kmeans will stop when |old - new| < epsilon.')
    parser.add_argument('-l', '--lnorm', type=str, default='l2', choices=['l1', 'l2'],
                        help='Norm for vectors')
    parser.add_argument('--plot_result', action='store_true',
                        help='Plot the resulting clustering (only works if dim = 2).')
    parser.add_argument('--use_storage', action='store_true',
                        help='Use storage?')
    return parser.parse_args()


def main(seed, numpoints, dimensions, centres, fragments, mode, iterations, epsilon, lnorm, plot_result, use_storage):
    """
    This will be executed if called as main script. Look @ kmeans_frag for the KMeans function.
    This code is used for experimental purposes.
    I.e it generates random data from some parameters that determine the size,
    dimensionality and etc and returns the elapsed time.
    :param seed: Random seed
    :param numpoints: Number of points
    :param dimensions: Number of dimensions
    :param centres: Number of centers
    :param fragments: Number of fragments
    :param mode: Dataset generation mode
    :param iterations: Number of iterations
    :param epsilon: Epsilon (convergence distance)
    :param lnorm: Norm to use
    :param plot_result: Boolean to plot result
    :param use_storage: Boolean to use storage
    :return: None
    """
    #from pycompss.api.api import compss_barrier, compss_wait_on
    import time

    start_time = time.time()

    # Generate the data
    fragment_list = []
    # Prevent infinite loops in case of not-so-smart users
    points_per_fragment = max(1, numpoints // fragments)

    if use_storage:
        from dataclay.api import get_backends_info
        from itertools import cycle
        backends = cycle(get_backends_info().keys())
    else:
        backends = []

    for l in range(0, numpoints, points_per_fragment):
        # Note that the seed is different for each fragment.
        # This is done to avoid having repeated data.
        r = min(numpoints, l + points_per_fragment)

        if use_storage:
            from kmeans.fragment import Fragment
            fragment = Fragment()
            #bid = backends.__next__()
            bid = next(backends)
            fragment.make_persistent(backend_id=bid)
            fragment.generate_points(r - l, dimensions, mode, seed + l)

        else:
            from nonpsco.fragment import generate_fragment
            fragment = generate_fragment(r - l, dimensions, mode, seed + l)

        fragment_list.append(fragment)

        #if not use_storage:
         #   fragment_list = compss_wait_on(fragment_list)

    print("Generation done")
    # compss_barrier()
    initialization_time = time.time()
    print("Starting kmeans")

    # Run kmeans
    num_centres = centres
    centres, labels = kmeans_frag(fragments=fragment_list,
                                  dimensions=dimensions,
                                  num_centres=num_centres,
                                  iterations=iterations,
                                  seed=seed,
                                  epsilon=epsilon,
                                  norm=lnorm)
    print("Ending kmeans")
    # compss_barrier()
    kmeans_time = time.time()

    print("Second round of kmeans")
    centres, labels = kmeans_frag(fragments=fragment_list,
                                  dimensions=dimensions,
                                  num_centres=num_centres,
                                  iterations=iterations,
                                  seed=seed,
                                  epsilon=epsilon,
                                  norm=lnorm)

    print("Ending kmeans")
    # compss_barrier()
    kmeans_2nd = time.time()

    print("-----------------------------------------")
    print("-------------- RESULTS ------------------")
    print("-----------------------------------------")
    print("Initialization time: %f" % (initialization_time - start_time))
    print("Kmeans time: %f" % (kmeans_time - initialization_time))
    print("Kmeans 2nd round time: %f" % (kmeans_2nd - kmeans_time))
    print("Total time: %f" % (kmeans_2nd - start_time))
    print("-----------------------------------------")


if __name__ == "__main__":
    init()
    options = parse_arguments()
    main(**vars(options))
    finish()
