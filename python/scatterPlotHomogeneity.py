import pandas as pd
from matplotlib import pyplot as plt

df = pd.read_csv('ClusterEval.csv', sep=',')
df = df[df['Replica']=='1']
df['ClusterSize'] = pd.to_numeric(df['ClusterSize'], errors='coerce')
df['TruePredictionHomogeneity'] = pd.to_numeric(df['TruePredictionHomogeneity'], errors='coerce')
df['FalsePredictionHomogeneity'] = pd.to_numeric(df['FalsePredictionHomogeneity'], errors='coerce')
df = df.sort_values(by=['ClusterSize'])

print(df.head())
plt.scatter(df['TruePredictionHomogeneity'], df['ClusterSize'], color='green')
plt.scatter(df['FalsePredictionHomogeneity'], df['ClusterSize'], color='red')
plt.title("Homogeneity Scatter Plot")
ax = plt.gca()
plt.xlabel('Homogeneity', fontsize=8)
plt.ylabel('Cluster Size', fontsize=8)
plt.savefig('Homogeneity.jpg', dpi=300)
plt.show()

