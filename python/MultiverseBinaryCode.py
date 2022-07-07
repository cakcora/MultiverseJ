
import os
import pandas as pd
import numpy as np
from sklearn import manifold
from sklearn.preprocessing import MinMaxScaler
import kmapper as km
import sklearn
import csv
import sys
#abs_dir_path = "C:/Users/cakcora/IdeaProjects/multiverseJ/results/"
#metricFile = "C:/Users/cakcora/IdeaProjects/multiverseJ/results/metrics.txt"


if __name__ == "__main__":
    # abs_dir_path = sys.argv[1]
    # metricFile = sys.argv[2]
    # datasetName= sys.argv[5]

    abs_dir_path ="\\Users\\kiara\\Desktop\\MyProject\\Multiverse\\results\\0_0\\"
    metricFile = "\\Users\\kiara\\Desktop\\MyProject\\Multiverse\\results\\0_0\\Civilmetrics.txt"
    datasetName= "Civil"

    try:
        dataset = pd.read_csv(metricFile,sep="\t",header=0)
        firstPoisonLevel = float(0)
        secondPoisonLevel = float(0)

        X = pd.read_csv(metricFile, sep="\t", header=0)

        Xfilt = X[X['label'].isin([firstPoisonLevel,secondPoisonLevel])]
        yfilt = Xfilt.label
        treeID = Xfilt.treeID
        treeIDNew = np.array(range(0, 0 + len(treeID)))

        Xfilt = Xfilt.drop(columns=['label', 'treeID'])
        mapper = km.KeplerMapper()
        scaler = MinMaxScaler(feature_range=(0, 1))

        Xfilt = scaler.fit_transform(Xfilt)
        lens = mapper.fit_transform(Xfilt, projection=sklearn.manifold.TSNE())
        cls = 5#We use cls=5, but this parameter can be further refined.  Its impact on results seems minimal.

        graph = mapper.map(
            lens,
            Xfilt,
            clusterer=sklearn.cluster.KMeans(n_clusters=cls, random_state=1618033),
            cover=km.Cover(n_cubes=10, perc_overlap=0.2)) # 0.2 0.4

        treeFrame = pd.DataFrame(treeIDNew)
        treeFrame.insert(0, 'New_ID', range(0, 0 + len(treeFrame)))
        treeFrame.to_csv(os.path.join(abs_dir_path, datasetName+"clusternodeIDs.csv"), index=False)

        with open(os.path.join(abs_dir_path, datasetName+'clusterLinks.csv'), 'w') as csv_file:
            writer = csv.writer(csv_file, delimiter='\t')
            for key, value in graph['links'].items():
                writer.writerow([key, value])
        with open(os.path.join(abs_dir_path, datasetName+'clusterNodes.csv'), 'w') as csv_file:
            writer = csv.writer(csv_file, delimiter='\t')
            for key, value in graph['nodes'].items():
                writer.writerow([key, value])
        print("TEST FINISHED")
    except Exception as e:
        print(str(e))
