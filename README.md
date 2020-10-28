# MultiverseJ 

Multiverse aims to find what aspects of a dataset constitute its main characteristics. By adding poisoning (label poisoning as a first approach), we modify the dataset and learn random forest models for increasing poisoning levels $p_0,\ldots,p_n$ where $p_0=0$. Corresponding dataset instantiation is denoted as $D=\{\mathcal{D}_0,\ldots,\mathcal{D}_i\}$.

We learn a random forest model $\mathcal{F}_i$ for each posioned dataset $D_i\in \mathcal{D}$ with $t$ decision trees. The other parameters of the random forest are number of data points in a tree leaf $k$, ...

Multiverse considers each random forest model a new data and extracts features from the learned decision trees. Formally, we define a set of features $f_0,\ldots,f_{|F|}$ defined on a learned decision tree $T_j \in \mathcal{F}_i$ as follows:

1- avDegree: average degree of $G_j$ vertices
2- diameter: diameter of $G_j$
3- medDegre: median degree of $G_j$ vertices
4- numWeakCluster: number of weakly connected components on $G_j$
5- avgWeakCompSize: average size of weakly connected components on $G_j$
6- numWeakCluster: number of strongly connected components on $G_j$
7- avgStrCompSize: average size of strong connected components on $G_j$
8- meanDist: mean distance between node pairs on the directed graph $G_j$
9- medHub: mean hub scores of nodes on the undirected graph $G_j$
10- medAuth: median hub scores of nodes on the undirected graph $G_j$
  
