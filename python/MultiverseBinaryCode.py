import pandas as pd
import os
import numpy as np
from sklearn import manifold
from sklearn.preprocessing import MinMaxScaler
import kmapper as km
import sklearn
import csv

abs_dir_path = "C:/Users/win_10/Desktop/MyProject/CuneytRunningProject/Results"
metricFile = "C:/Users/win_10/Desktop/MyProject/CuneytRunningProject/Results/metrics.txt"

dataset = pd.read_csv(metricFile,sep="\t",header=0)
firstPoisonLevel = 0.0
secondPoisonLevel = 45.0

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
cls = len(set(yfilt))

graph = mapper.map(
    lens,
    Xfilt,
    clusterer=sklearn.cluster.KMeans(n_clusters=cls, random_state=1618033),
    cover=km.Cover(n_cubes=10, perc_overlap=0.6)
)

treeFrame = pd.DataFrame(treeID)
treeFrame.insert(0, 'New_ID', range(0, 0 + len(treeFrame)))
treeFrame.to_csv(os.path.join(abs_dir_path, 'clusternodeIDs.csv'), index=False)

with open(os.path.join(abs_dir_path, 'clusterLinks.csv'), 'w') as csv_file:
    writer = csv.writer(csv_file, delimiter='\t')
    for key, value in graph['links'].items():
        writer.writerow([key, value])
with open(os.path.join(abs_dir_path, 'clusterNodes.csv'), 'w') as csv_file:
    writer = csv.writer(csv_file, delimiter='\t')
    for key, value in graph['nodes'].items():
        writer.writerow([key, value])
