# MultiverseJ 

Multiverse aims to find what aspects of a dataset constitute its main characteristics. By adding poisoning (label poisoning as a first approach), we modify the dataset and learn random forest for increasing poisoning levels $p_0,\ldots,p_n$ where $p_0=0$. Corresponding dataset instantiation is denoted as $D=\{\mathcal{D}_0,\ldots,\mathcal{D}_i\}$.

We learn a random forest model for each posioned dataset $D\in \mathcal{D}$ with $t$ decision trees. The other parameters of the random forest are number of data points in a tree leaf $k$, ...
