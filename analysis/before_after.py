import pandas as pd
import researchpy as rp
import numpy as np
import matplotlib.pyplot as plt
import scipy.stats as stats
import warnings
warnings.filterwarnings("ignore")

plt.rc('font', size=17)
plt.clf()

def shapiro_test(group1, group2):
    shapiro1 = stats.shapiro(group1)
    shapiro2 = stats.shapiro(group2)

    if (shapiro1.pvalue < 0.05 or shapiro2.pvalue < 0.05): 
        #print("G1:", shapiro1.pvalue)
        return False, shapiro1.pvalue, shapiro2.pvalue

    # If all values are significant
    return True, shapiro1.pvalue, shapiro2.pvalue

# Create dataframe
df = pd.read_csv("./example_ans.csv")

# Update values to match regular scales (reverse the reversing of questioning)
for column in ['conscious', 'lively', 'responsive', 'nice', 'sensible', 'calm_b']:
    df[column] = 6 - df[column]

# Get types we want to compare
removed = [str(column) for column in df][2:]

# For each measurement we are gonna measure significance
for i in range(23,26):
    # Groups for natural
    g1_b = df.iloc[:,i][df['group'] == 'no-memory']
    g2_b = df.iloc[:,i][df['group'] == 'memory']

    # Groups for natural
    g1_a = df.iloc[:,i+2][df['group'] == 'no-memory']
    g2_a = df.iloc[:,i+2][df['group'] == 'memory']

    # Test statistic
    print("----", g1_b.name, "----")

    # Test normality of no memory group
    test_1, p1_1, p2_1 = shapiro_test(g1_b, g1_a)
    print("--> NoMemory")
    if test_1:
        stat_1, p_value_1 = stats.ttest_rel(g1_b, g1_a)
        print(" + Shapiro:",round(p1_1,4),round(p2_1,4))
        print(" = T-test:", round(p_value_1,4))
    else:
        stat_1, p_value_1 = stats.wilcoxon(g1_b, g1_a)
        print(" - Shapiro:",round(p1_1,4),round(p2_1,4))
        print(" = Wilcoxon:", round(p_value_1,4))

    # Reject null hypothesis or not
    print(" - REJECT" if p_value_1 < 0.05 else " + ACCEPT")

    # Concatenate results for boxplot
    g1_b.reset_index(drop=True, inplace=True)
    g1_a.reset_index(drop=True, inplace=True)
    frame_1 = pd.concat([g1_b.rename("Before"), g1_a.rename("After")], axis=1)

    # Safe plot
    plt.figure()
    frame_1.boxplot()
    plt.savefig('./before-after/no-memory-' + g1_b.name + '.png', bbox_inches='tight')

    # Test normality of memory group
    test_2, p1_2, p2_2 = shapiro_test(g2_b, g2_a)
    print("--> Memory")
    if test_2:
        stat_2, p_value_2 = stats.ttest_rel(g2_b, g2_a)
        print(" + Shapiro:",round(p1_2,4),round(p2_2,4))
        print(" = T-test:", round(p_value_2,4))
    else:
        stat_2, p_value_2 = stats.wilcoxon(g2_b, g2_a)
        print(" - Shapiro:",round(p1_2,4),round(p2_2,4))
        print(" = Wilcoxon:", round(p_value_2,4))

    # Reject null hypothesis or not
    print(" - REJECT" if p_value_2 < 0.05 else " + ACCEPT")

    # Concatenate results for boxplot
    g2_b.reset_index(drop=True, inplace=True)
    g2_a.reset_index(drop=True, inplace=True)
    frame_2 = pd.concat([g2_b.rename("Before"), g2_a.rename("After")], axis=1)

    # Safe plot
    plt.figure()
    frame_2.boxplot()
    plt.savefig('./before-after/memory-' + g1_b.name + '.png', bbox_inches='tight')

    # Check if difference is significant
    diff_1 = (g1_b - g1_a).abs()
    diff_2 = (g2_b - g2_a).abs()

    # Test normality of difference
    test_3, p1_3, p2_3 = shapiro_test(diff_1, diff_2)
    print("--> Difference")
    if test_3:
        stat_3, p_value_3 = stats.ttest_rel(diff_1, diff_2)
        print(" + Shapiro:",round(p1_3,4),round(p2_3,4))
        print(" = T-test:", round(p_value_3,4))
    else:
        stat_3, p_value_3 = stats.wilcoxon(diff_1, diff_2)
        print(" - Shapiro:",round(p1_3,4),round(p2_3,4))
        print(" = Wilcoxon:", round(p_value_3,4))

    # Reject null hypothesis or not
    print(" - REJECT" if p_value_3 < 0.05 else " + ACCEPT")

    # Concatenate results for boxplot
    diff_1.reset_index(drop=True, inplace=True)
    diff_2.reset_index(drop=True, inplace=True)
    frame_3 = pd.concat([diff_1.rename("No-memory"), diff_2.rename("Memory")], axis=1)

    # Safe plot
    plt.figure()
    frame_3.boxplot()
    plt.savefig('./before-after/difference-' + g1_b.name + '.png', bbox_inches='tight')

