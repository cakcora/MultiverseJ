import numpy as np
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
    #  "adult", "Diabetes", "LR" , "Poker"

    for dataset in ["Nursery", "adult", "Diabetes", "LR", "Poker", "C4", "BankNote", "BreastCancer"]:
        datapath = dataset + "TFfinalResults.csv"
        tf_df = pd.read_csv(datapath, sep=',')
        tf_df['Auc'] = pd.to_numeric(tf_df['Auc'], errors='coerce')
        tf_df = tf_df.loc[(tf_df["TreeNo"] <= 300)]
        tf_df_greedy = tf_df.loc[tf_df['Method'] == 'Greedy']
        tf_df_Random = tf_df.loc[tf_df['Method'] == 'Random']
        tf_df_Quality = tf_df.loc[tf_df['Method'] == 'Quality']
        tf_df_Quality_Top_1 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top1']
        tf_df_Quality_Top_2 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top2']
        tf_df_Quality_Top_5 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top5']

        tf_df_greedy = tf_df_greedy.groupby('ID').agg({'Auc': 'mean', 'TreeNo': 'max'})[
            ['Auc', 'TreeNo']].reset_index().sort_values(by=['TreeNo'])
        tf_df_Random = tf_df_Random.groupby('ID').agg({'Auc': 'mean', 'TreeNo': 'max'})[
            ['Auc', 'TreeNo']].reset_index().sort_values(by=['TreeNo'])
        tf_df_Quality = tf_df_Quality.groupby('ID').agg({'Auc': 'mean', 'TreeNo': 'max'})[
            ['Auc', 'TreeNo']].reset_index().sort_values(by=['TreeNo'])
        tf_df_Quality_Top_1 = tf_df_Quality_Top_1.groupby('ID').agg({'Auc': 'mean', 'TreeNo': 'max'})[
            ['Auc', 'TreeNo']].reset_index().sort_values(by=['TreeNo'])
        tf_df_Quality_Top_2 = tf_df_Quality_Top_2.groupby('ID').agg({'Auc': 'mean', 'TreeNo': 'max'})[
            ['Auc', 'TreeNo']].reset_index().sort_values(by=['TreeNo'])
        tf_df_Quality_Top_5 = tf_df_Quality_Top_5.groupby('ID').agg({'Auc': 'mean', 'TreeNo': 'max'})[
            ['Auc', 'TreeNo']].reset_index().sort_values(by=['TreeNo'])

        plt.plot(tf_df_greedy['Auc'], tf_df_greedy['TreeNo'], label="Greedy")
        plt.plot(tf_df_Random['Auc'], tf_df_Random['TreeNo'], label="Random")
        plt.plot(tf_df_Quality['Auc'], tf_df_Quality['TreeNo'], label="Quality")
        plt.plot(tf_df_Quality_Top_1['Auc'], tf_df_Quality_Top_1['TreeNo'], label="Q_Top1")
        plt.plot(tf_df_Quality_Top_2['Auc'], tf_df_Quality_Top_2['TreeNo'], label="Q_Top2")
        plt.plot(tf_df_Quality_Top_5['Auc'], tf_df_Quality_Top_5['TreeNo'], label="Q_Top5")
        plt.xlabel('Auc', fontsize=8)
        ax = plt.gca()

        # getting minimum of AUCs
        min_auc = min(tf_df_greedy['Auc'].min(), tf_df_Random['Auc'].min(), tf_df_Quality['Auc'].min(),
                      tf_df_Quality_Top_1['Auc'].min(), tf_df_Quality_Top_2['Auc'].min(),
                      tf_df_Quality_Top_5['Auc'].min())
        # getting max of AUCs
        max_auc = min(tf_df_greedy['Auc'].max(), tf_df_Random['Auc'].max(), tf_df_Quality['Auc'].max(),
                      tf_df_Quality_Top_1['Auc'].max(), tf_df_Quality_Top_2['Auc'].max(),
                      tf_df_Quality_Top_5['Auc'].max())

        # getting minimum of TreeNos
        min_tree_no = min(tf_df_greedy['TreeNo'].min(), tf_df_Random['TreeNo'].min(), tf_df_Quality['TreeNo'].min(),
                          tf_df_Quality_Top_1['TreeNo'].min(), tf_df_Quality_Top_2['TreeNo'].min(),
                          tf_df_Quality_Top_5['TreeNo'].min())
        # getting max of TreeNos
        max_tree_no = max(tf_df_greedy['TreeNo'].max(), tf_df_Random['TreeNo'].max(), tf_df_Quality['TreeNo'].max(),
                          tf_df_Quality_Top_1['TreeNo'].max(), tf_df_Quality_Top_2['TreeNo'].max(),
                          tf_df_Quality_Top_5['TreeNo'].max())

        plt.ylim([min_tree_no, max_tree_no])
        # plt.xlim([min_auc, max_auc])
        plt.xlim(0.56, 0.76)
        plt.title(dataset, loc='left')
        # plt.axvline(x=0.9, linestyle=":", color='blue')
        # plt.aniline(y=300, linestyle=":", color='blue')
        ax.xaxis.set_major_locator(MaxNLocator(integer=True))
        # plt.title("Tree Numbers")
        plt.ylabel('Number of Trees', fontsize=8)
        plt.legend(bbox_to_anchor=(0.5, 1.05),
                   ncol=2, fancybox=True, shadow=True, loc="upper center", fontsize=7)
        output = "graphs/" + dataset + "_zoom_8.jpg"
        plt.savefig(output, dpi=300)
        plt.show(figsize=(20, 10))


def draw_bar_eval_graph():
    greedy_total_eval = pd.DataFrame()
    random_total_eval = pd.DataFrame()
    quality_total_eval = pd.DataFrame()
    quality_top_1_total_eval = pd.DataFrame()
    quality_top_2_total_eval = pd.DataFrame()
    quality_top_5_total_eval = pd.DataFrame()
    overall_total_eval = pd.DataFrame()
    eval_by_dataset = pd.DataFrame()
    total_tree_number = 300
    treeEvalNumber = [30, 60, 150]
    Datasets_names = ["Diabetes", "Connect-4", "Adult", "Poker", "Letter Recognition", "Nursery", ]
    Datasets = ["Diabetes", "C4", "adult", "Poker", "LR", "Nursery"]
    for treeNumber in treeEvalNumber:
        for dataset in Datasets:
            datapath = dataset + "TFfinalResults.csv"
            tf_df = pd.read_csv(datapath, sep=',')
            tf_df['Auc'] = pd.to_numeric(tf_df['Auc'], errors='coerce')
            tf_df = tf_df.loc[(tf_df["TreeNo"] <= 300)]
            tf_df_greedy = tf_df.loc[tf_df['Method'] == 'Greedy']
            tf_df_greedy = \
                tf_df_greedy.groupby('ID').agg(AucMin=('Auc', 'min'), AucMax=('Auc', 'max'), Auc=('Auc', 'mean'),
                                               TreeNo=('TreeNo', 'mean'))[
                    ['Auc', 'TreeNo', 'AucMin', 'AucMax']].reset_index().sort_values(by=['TreeNo'])
            tf_df_greedy['TreeNo'] = tf_df_greedy['TreeNo'].astype(int)

            tf_df_Random = tf_df.loc[tf_df['Method'] == 'Random']
            tf_df_Random = \
                tf_df_Random.groupby('ID').agg(AucMin=('Auc', 'min'), AucMax=('Auc', 'max'), Auc=('Auc', 'mean'),
                                               TreeNo=('TreeNo', 'mean'))[
                    ['Auc', 'TreeNo', 'AucMin', 'AucMax']].reset_index().sort_values(by=['TreeNo'])
            tf_df_Random['TreeNo'] = tf_df_Random['TreeNo'].astype(int)

            tf_df_Quality = tf_df.loc[tf_df['Method'] == 'Quality']
            tf_df_Quality = \
                tf_df_Quality.groupby('ID').agg(AucMin=('Auc', 'min'), AucMax=('Auc', 'max'), Auc=('Auc', 'mean'),
                                                TreeNo=('TreeNo', 'mean'))[
                    ['Auc', 'TreeNo', 'AucMin', 'AucMax']].reset_index().sort_values(by=['TreeNo'])
            tf_df_Quality['TreeNo'] = tf_df_Quality['TreeNo'].astype(int)

            tf_df_Quality_Top_1 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top1']
            tf_df_Quality_Top_1 = \
                tf_df_Quality_Top_1.groupby('ID').agg(AucMin=('Auc', 'min'), AucMax=('Auc', 'max'), Auc=('Auc', 'mean'),
                                                      TreeNo=('TreeNo', 'mean'))[
                    ['Auc', 'TreeNo', 'AucMin', 'AucMax']].reset_index().sort_values(by=['TreeNo'])
            tf_df_Quality_Top_1['TreeNo'] = tf_df_Quality_Top_1['TreeNo'].astype(int)

            tf_df_Quality_Top_2 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top2']
            tf_df_Quality_Top_2 = \
                tf_df_Quality_Top_2.groupby('ID').agg(AucMin=('Auc', 'min'), AucMax=('Auc', 'max'), Auc=('Auc', 'mean'),
                                                      TreeNo=('TreeNo', 'mean'))[
                    ['Auc', 'TreeNo', 'AucMin', 'AucMax']].reset_index().sort_values(by=['TreeNo'])
            tf_df_Quality_Top_2['TreeNo'] = tf_df_Quality_Top_2['TreeNo'].astype(int)

            tf_df_Quality_Top_5 = tf_df.loc[tf_df['Method'] == 'QualityWithTreeSelection_Top5']
            tf_df_Quality_Top_5 = \
                tf_df_Quality_Top_5.groupby('ID').agg(AucMin=('Auc', 'min'), AucMax=('Auc', 'max'), Auc=('Auc', 'mean'),
                                                      TreeNo=('TreeNo', 'mean'))[
                    ['Auc', 'TreeNo', 'AucMin', 'AucMax']].reset_index().sort_values(by=['TreeNo'])
            tf_df_Quality_Top_5['TreeNo'] = tf_df_Quality_Top_5['TreeNo'].astype(int)
            greedy_closest = tf_df_greedy.loc[[tf_df_greedy['TreeNo'].sub(treeNumber).abs().idxmin()]]
            greedy_closest["TreeEvalNo"] = treeNumber
            greedy_closest["Dataset"] = dataset
            greedy_total_eval = greedy_total_eval.append(greedy_closest)
            overall_total_eval = overall_total_eval.append(greedy_closest)
            random_closest = tf_df_Random.loc[[tf_df_Random['TreeNo'].sub(treeNumber).abs().idxmin()]]
            random_closest["TreeEvalNo"] = treeNumber
            random_closest["Dataset"] = dataset
            random_total_eval = random_total_eval.append(random_closest)
            overall_total_eval = overall_total_eval.append(random_closest)
            quality_closest = tf_df_Quality.loc[[tf_df_Quality['TreeNo'].sub(treeNumber).abs().idxmin()]]
            quality_closest["TreeEvalNo"] = treeNumber
            quality_closest["Dataset"] = dataset
            quality_total_eval = quality_total_eval.append(quality_closest)
            overall_total_eval = overall_total_eval.append(quality_closest)
            quality_top_1_closest = tf_df_Quality_Top_1.loc[
                [tf_df_Quality_Top_1['TreeNo'].sub(treeNumber).abs().idxmin()]]
            quality_top_1_closest["TreeEvalNo"] = treeNumber
            quality_top_1_closest["Dataset"] = dataset
            quality_top_1_total_eval = quality_top_1_total_eval.append(quality_top_1_closest)
            overall_total_eval = overall_total_eval.append(quality_top_1_closest)
            quality_top_2_closest = tf_df_Quality_Top_2.loc[
                [tf_df_Quality_Top_2['TreeNo'].sub(treeNumber).abs().idxmin()]]
            quality_top_2_closest["TreeEvalNo"] = treeNumber
            quality_top_2_closest["Dataset"] = dataset
            quality_top_2_total_eval = quality_top_2_total_eval.append(quality_top_2_closest)
            overall_total_eval = overall_total_eval.append(quality_top_2_closest)
            quality_top_5_closest = tf_df_Quality_Top_5.loc[
                [tf_df_Quality_Top_5['TreeNo'].sub(treeNumber).abs().idxmin()]]
            quality_top_5_closest["TreeEvalNo"] = treeNumber
            quality_top_5_closest["Dataset"] = dataset
            quality_top_5_total_eval = quality_top_5_total_eval.append(quality_top_5_closest)
            overall_total_eval = overall_total_eval.append(quality_top_5_closest)

            eval_by_dataset = eval_by_dataset.append(
                pd.DataFrame([(dataset, treeNumber, greedy_closest.iloc[0]['Auc'], greedy_closest.iloc[0]['AucMin'],
                               greedy_closest.iloc[0]['AucMax'], random_closest.iloc[0]['Auc'],
                               random_closest.iloc[0]['AucMin'],
                               random_closest.iloc[0]['AucMax'],
                               quality_closest.iloc[0]['Auc'], quality_closest.iloc[0]['AucMin'],
                               quality_closest.iloc[0]['AucMax'],
                               quality_top_1_closest.iloc[0]['Auc'], quality_top_1_closest.iloc[0]['AucMin'],
                               quality_top_1_closest.iloc[0]['AucMax'], quality_top_2_closest.iloc[0]['Auc'],
                               quality_top_2_closest.iloc[0]['AucMin'],
                               quality_top_2_closest.iloc[0]['AucMax'],
                               quality_top_5_closest.iloc[0]['Auc'], quality_top_5_closest.iloc[0]['AucMin'],
                               quality_top_5_closest.iloc[0]['AucMax'])],
                             columns=['Dataset', 'TreeEvalNo', 'Greedy', 'GreedyMinAuc', 'GreedyMaxAuc', 'Random',
                                      'RandomMinAuc', 'RandomMaxAuc',
                                      'Quality', 'QualityMinAuc', 'QualityMaxAuc', 'Top1', 'Top1MinAuc', 'Top1MaxAuc',
                                      'Top2', 'Top2MinAuc', 'Top2MaxAuc',
                                      'Top5', 'Top5MinAuc', 'Top5MaxAuc'], ))

        # draw group bar chart for this tree numbers
        width = 0.4  # the width of the bars

    # draw group bar chart for each
    eval_by_dataset['Best'] = eval_by_dataset[['Greedy', 'Random', 'Quality', 'Top1', 'Top2',
                                               'Top5']].idxmax(axis=1)

    eval_by_dataset.to_csv("Dataset_Evaluation.csv", sep=',', encoding='utf-8', index=False)
    for treeNo in treeEvalNumber:
        cond = (eval_by_dataset['TreeEvalNo'] == treeNo)
        dataset_list = eval_by_dataset[cond].Dataset.values[:]
        greedy_list = eval_by_dataset[cond].Greedy.values[:]
        greedy_max_list = eval_by_dataset[cond].GreedyMaxAuc.values[:]
        greedy_min_list = eval_by_dataset[cond].GreedyMinAuc.values[:]
        random_list = eval_by_dataset[cond].Random.values[:]
        random_max_list = eval_by_dataset[cond].RandomMaxAuc.values[:]
        random_min_list = eval_by_dataset[cond].RandomMinAuc.values[:]
        quality_list = eval_by_dataset[cond].Quality.values[:]
        quality_max_list = eval_by_dataset[cond].QualityMaxAuc.values[:]
        quality_min_list = eval_by_dataset[cond].QualityMinAuc.values[:]

        top1_list = eval_by_dataset[cond].Top1.values[:]
        top1_max_list = eval_by_dataset[cond].Top1MaxAuc.values[:]
        top1_min_list = eval_by_dataset[cond].Top1MinAuc.values[:]

        top2_list = eval_by_dataset[cond].Top2.values[:]
        top2_max_list = eval_by_dataset[cond].Top2MaxAuc.values[:]
        top2_min_list = eval_by_dataset[cond].Top2MinAuc.values[:]

        top5_list = eval_by_dataset[cond].Top5.values[:]
        top5_max_list = eval_by_dataset[cond].Top5MaxAuc.values[:]
        top5_min_list = eval_by_dataset[cond].Top5MinAuc.values[:]

        x = np.arange(len(dataset_list))  # the label locations
        width = 0.13  # the width of the bars

        r1 = np.arange(len(dataset_list))
        r2 = [x + width for x in r1]
        r3 = [x + width for x in r2]
        r4 = [x + width for x in r3]
        r5 = [x + width for x in r4]
        r6 = [x + width for x in r5]

        fig, ax = plt.subplots()
        rects1 = ax.bar(r1, greedy_list, width, label='Greedy', color="#0D47A1",
                        yerr=[np.subtract(greedy_max_list, greedy_list), np.subtract(greedy_list, greedy_min_list)])
        rects2 = ax.bar(r2, random_list, width, label='Random', color="#1976D2",
                        yerr=[np.subtract(random_max_list, random_list), np.subtract(random_list, random_min_list)])
        rects3 = ax.bar(r3, quality_list, width, label='Quality', color="#4DD0E1", yerr=[np.subtract(quality_max_list, quality_list), np.subtract(quality_list, quality_min_list)] )
        rects4 = ax.bar(r4, top1_list, width, label='Top1', color="#00695C" , yerr=[np.subtract(top1_max_list, top1_list), np.subtract(top1_list, top1_min_list)])
        rects5 = ax.bar(r5, top2_list, width, label='Top2', color="#8BC34A" , yerr=[np.subtract(top2_max_list, top2_list), np.subtract(top2_list, top2_min_list)])
        rects6 = ax.bar(r6, top5_list, width, label='Top5', color="#AFB42B" , yerr=[np.subtract(top5_max_list, top5_list), np.subtract(top5_list, top5_min_list)])

        # Add some text for labels, title and custom x-axis tick labels, etc.
        ax.set_ylabel('Auc')
        # ax.set_title('Auc in Different Datasets (Tree selection {}% )'.format((treeNo / total_tree_number) * 100))
        plt.xticks([r + (2 * width) for r in range(len(dataset_list))], Datasets_names)
        ax.legend(loc="upper left", prop={'size': 6})

        # ax.bar_label(rects1, padding=6)
        # ax.bar_label(rects2, padding=6)
        # ax.bar_label(rects3, padding=6)
        # ax.bar_label(rects4, padding=6)
        # ax.bar_label(rects5, padding=6)
        # ax.bar_label(rects6, padding=6)
        plt.tick_params(labelsize=7)
        fig.tight_layout()
        # fig.set_size_inches(18.5, 10.5, forward=True)
        output = "graphs/" + 'Auc in Different Datasets (Tree selection {}% )'.format(
            (treeNo / total_tree_number)) + ".jpg"
        plt.savefig(output, dpi=300)
        plt.show()

        print(overall_total_eval.head())


#  tf_df ['Quality' 1 nan 0.8865688023634346 0.0180300261420996, 0.3414957630495794 3]
# vf_df ['Random' 1 nan 0.8510975301168158 0.0606594975954072 0.3503614547442671]

if __name__ == '__main__':
    draw_bar_eval_graph()
