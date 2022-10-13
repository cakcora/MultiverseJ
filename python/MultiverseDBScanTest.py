import os
from collections import Counter

import pandas as pd
import numpy as np
from sklearn import manifold
from sklearn.neighbors import NearestNeighbors
from sklearn.preprocessing import MinMaxScaler
import kmapper as km
import sklearn
import csv
# import matplotlib.pyplot as plt
import sys

# abs_dir_path = "C:/Users/cakcora/IdeaProjects/multiverseJ/results/"
# metricFile = "C:/Users/cakcora/IdeaProjects/multiverseJ/results/metrics.txt"


if __name__ == "__main__":
    abs_dir_path = sys.argv[1]
    metricFile = sys.argv[2]
    datasetName= sys.argv[5]

    # abs_dir_path = "\\Users\\kiara\\Desktop\\MyProject\\Multiverse\\results\\0_0\\"
    # metricFile = "\\Users\\kiara\\Desktop\\MyProject\\Multiverse\\results\\0_0\\Adultmetrics.txt"
    # datasetName = "َAdult"

    try:
        dataset = pd.read_csv(metricFile, sep="\t", header=0)
        firstPoisonLevel = float(0)
        secondPoisonLevel = float(0)

        X = pd.read_csv(metricFile, sep="\t", header=0)

        Xfilt = X[X['label'].isin([firstPoisonLevel, secondPoisonLevel])]
        yfilt = Xfilt.label
        treeID = Xfilt.treeID
        treeIDNew = np.array(range(0, 0 + len(treeID)))

        Xfilt = Xfilt.drop(columns=['label', 'treeID'])
        mapper = km.KeplerMapper()
        scaler = MinMaxScaler(feature_range=(0, 1))

        Xfilt = scaler.fit_transform(Xfilt)
        lens = mapper.fit_transform(Xfilt, projection=sklearn.manifold.TSNE())
        cls = 5  # We use cls=5, but this parameter can be further refined.  Its impact on results seems minimal.

        graph = mapper.map(
            lens,
            Xfilt,
            clusterer=sklearn.cluster.KMeans(n_clusters=cls, random_state=1618033),
            cover=km.Cover(n_cubes=10, perc_overlap=0.2))  # 0.2 0.4

        nbrs = NearestNeighbors(n_neighbors=5).fit(lens)
        # Find the k-neighbors of a point
        neigh_dist, neigh_ind = nbrs.kneighbors(lens)
        # sort the neighbor distances (lengths to points) in ascending order
        # axis = 0 represents sort along first axis i.e. sort along row
        sort_neigh_dist = np.sort(neigh_dist, axis=0)

        k_dist = sort_neigh_dist[:, 4]
        # plt.plot(k_dist)
        # plt.axhline(y=2, linewidth=1, linestyle='dashed', color='k')
        # plt.ylabel("k-NN distance")
        # plt.xlabel("Sorted observations (5th NN)")
        # plt.show()

        clusters = sklearn.cluster.DBSCAN(eps=0.05, min_samples=4).fit(lens)
        set(clusters.labels_)
        Counter(clusters.labels_)
        df = pd.DataFrame(clusters.labels_, columns=["clusterID"])
        df['treeID'] = df.index
        cluster_series = df.groupby("clusterID").treeID.apply(pd.Series.tolist)




        treeFrame = pd.DataFrame(treeIDNew)
        treeFrame.insert(0, 'New_ID', range(0, 0 + len(treeFrame)))
        treeFrame.to_csv(os.path.join(abs_dir_path, datasetName + "clusternodeIDs.csv"), index=False)

        with open(os.path.join(abs_dir_path, datasetName + 'clusterLinks.csv'), 'w') as csv_file:
            writer = csv.writer(csv_file, delimiter='\t')
            for key, value in graph['links'].items():
                writer.writerow([key, value])
        with open(os.path.join(abs_dir_path, datasetName + 'clusterNodes.csv'), 'w') as csv_file:
            writer = csv.writer(csv_file, delimiter='\t')
            for key, value in cluster_series.items():
                writer.writerow([key, value])
        print("TEST FINISHED")
    except Exception as e:
        print(str(e))
