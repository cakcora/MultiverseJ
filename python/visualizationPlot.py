import pandas as pd
from matplotlib import pyplot as plt
from matplotlib.ticker import MaxNLocator
from scipy.signal import savgol_filter

def draw_scatter_plot():
    df = pd.read_csv('ClusterEval.csv', sep=',')
    df['Replica'] = pd.to_numeric(df['Replica'], errors='coerce')
    df = df[df['Replica'] == 1]
    df['ClusterSize'] = pd.to_numeric(df['ClusterSize'], errors='coerce')
    df['TruePredictionHomogeneity'] = pd.to_numeric(df['TruePredictionHomogeneity'], errors='coerce')
    df['FalsePredictionHomogeneity'] = pd.to_numeric(df['FalsePredictionHomogeneity'], errors='coerce')
    df = df.sort_values(by=['ClusterSize'])

    print(df.head())
    tp = plt.scatter(df['ClusterSize'], df['TruePredictionHomogeneity'], color='green')
    fp = plt.scatter(df['ClusterSize'], df['FalsePredictionHomogeneity'], color='red')
    plt.title("Homogeneity Scatter Plot")
    ax = plt.gca()
    plt.ylabel('Homogeneity', fontsize=8)
    plt.xlabel('Cluster Size', fontsize=8)
    ax.xaxis.set_major_locator(MaxNLocator(integer=True))
    ax.legend((tp, fp), ("True Prediction", "False Prediction"), loc="upper right", fontsize=7)
    plt.savefig('Homogeneity.jpg', dpi=300)
    plt.show()


def draw_evaluation_graph():
    tf_df = pd.read_csv('adultTFfinalResults.csv', sep=',')
    tf_df['TreeNo'] = pd.to_numeric(tf_df['TreeNo'], errors='coerce')
    tf_df = tf_df.loc[(tf_df["TreeNo"] <= 300)]
    tf_df_greedy = tf_df.loc[tf_df['Method'] == 'Greedy']
    tf_df_Random = tf_df.loc[tf_df['Method'] == 'Random']
    tf_df_Quality = tf_df.loc[tf_df['Method'] == 'Quality']
    tf_df_Quality_Top_1 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top1']
    tf_df_Quality_Top_2 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top2']
    tf_df_Quality_Top_5 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top5']

    plt.plot(tf_df_greedy['Auc'], tf_df_greedy['TreeNo'], label="Greedy")
    plt.plot(tf_df_Random['Auc'], tf_df_Random['TreeNo'], label="Random")
    plt.plot(tf_df_Quality['Auc'], tf_df_Quality['TreeNo'], label="Quality")
    plt.plot(tf_df_Quality_Top_1['Auc'], tf_df_Quality_Top_1['TreeNo'], label="Q_Top1")
    plt.plot(tf_df_Quality_Top_2['Auc'], tf_df_Quality_Top_2['TreeNo'], label="Q_Top2")
    plt.plot(tf_df_Quality_Top_5['Auc'], tf_df_Quality_Top_5['TreeNo'], label="Q_Top5")
    plt.xlabel('Auc', fontsize=8)
    ax = plt.gca()
    plt.ylim([0, 8])
    plt.xlim([0.84, 0.89])
    plt.axvline(x=0.9, linestyle=":", color='blue')
    plt.axhline(y=300, linestyle=":", color='blue')
    ax.xaxis.set_major_locator(MaxNLocator(integer=True))
    # plt.title("Tree Numbers")
    plt.ylabel('Number of Trees', fontsize=8)
    plt.legend(bbox_to_anchor=(0.5, 1.05),
               ncol=2, fancybox=True, shadow=True, loc="upper center", fontsize=7)
    plt.savefig('Trees-zoom5.jpg', dpi=300)
    plt.show(figsize=(20, 10))



if __name__ == '__main__':
    draw_evaluation_graph()
