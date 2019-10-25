from storage.api import StorageObject
from dataclay import dclayMethod

from dataclay.contrib.dummy_pycompss import task

import numpy as np


class Fragment(StorageObject):
    """
    @ClassField points numpy.matrix
    @ClassField labels numpy.array

    @dclayImport numpy as np
    """
    @dclayMethod()
    def __init__(self):
        self.labels = None
        self.points = None

    @task(returns=object, isModifier=False)
    @dclayMethod(centres='numpy.matrix', norm='anything', return_='numpy.matrix')
    def cluster_and_partial_sums(self, centres, norm):
        """
        Given self (fragment == set of points), declare a CxD matrix A and, for each point p:
        1) Compute the nearest centre c of p
        2) Add p / num_points_in_fragment to A[index(c)]
        3) Set label[index(p)] = c
        :param centres: Centers
        :param norm: Norm for normalization
        :return: Distances to centers of each point
        """
        mat = self.points
        ret = np.matrix(np.zeros(centres.shape))
        n = mat.shape[0]
        c = centres.shape[0]
        labels = np.zeros(n, dtype=int)
        self.labels = labels  # Store, as master may retrieve this (last iteration)

        # Compute the big stuff
        associates = np.zeros(c, dtype=int)
        # Get the labels for each point
        for (i, point) in enumerate(mat):
            distances = np.zeros(c)
            for (j, centre) in enumerate(centres):
                distances[j] = np.linalg.norm(point - centre, norm)
            labels[i] = np.argmin(distances)
            associates[labels[i]] += 1

        # Add each point to its associate centre
        for (i, point) in enumerate(mat):
            ret[labels[i]] += point / associates[labels[i]]
        return ret

    @task(isModifier=False)
    @dclayMethod(num_points='int', dim='int', mode='str', seed='int')
    def generate_points(self, num_points, dim, mode, seed):
        """
        Generate a random fragment of the specified number of points using the
        specified mode and the specified seed. Note that the generation is
        distributed (the master will never see the actual points).
        :param num_points: Number of points
        :param dim: Number of dimensions
        :param mode: Dataset generation mode
        :param seed: Random seed
        :return: Dataset fragment
        """
        # Random generation distributions
        rand = {
            'normal': lambda k: np.random.normal(0, 1, k),
            'uniform': lambda k: np.random.random(k),
        }
        r = rand[mode]
        np.random.seed(seed)
        mat = np.matrix(
            [r(dim) for __ in range(num_points)]
        )
        # Normalize all points between 0 and 1
        mat -= np.min(mat)
        mx = np.max(mat)
        if mx > 0.0:
            mat /= mx

        self.points = mat
