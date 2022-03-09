import pandas as pd
from matplotlib import pyplot as plt
from matplotlib.ticker import MaxNLocator

df = pd.read_csv('ClusterEval.csv', sep=',')
df['Replica'] = pd.to_numeric(df['Replica'], errors='coerce')
df = df[df['Replica']==1]
df['ClusterSize'] = pd.to_numeric(df['ClusterSize'], errors='coerce')
df['TruePredictionHomogeneity'] = pd.to_numeric(df['TruePredictionHomogeneity'], errors='coerce')
df['FalsePredictionHomogeneity'] = pd.to_numeric(df['FalsePredictionHomogeneity'], errors='coerce')
df = df.sort_values(by=['ClusterSize'])

print(df.head())
plt.scatter(df['ClusterSize'], df['TruePredictionHomogeneity'], color='green')
plt.scatter(df['ClusterSize'], df['FalsePredictionHomogeneity'],  color='red')
plt.title("Homogeneity Scatter Plot")
ax = plt.gca()
plt.ylabel('Homogeneity', fontsize=8)
plt.xlabel('Cluster Size', fontsize=8)
ax.xaxis.set_major_locator(MaxNLocator(integer=True))
plt.savefig('Homogeneity.jpg', dpi=300)
plt.show()

