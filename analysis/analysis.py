import pandas as pd
import researchpy as rp
import numpy as np
import matplotlib.pyplot as plt
import scipy.stats as stats
import warnings
warnings.filterwarnings("ignore")

def shapiro_test(group1, group2):
    shapiro1 = stats.shapiro(group1)
    if (shapiro1.pvalue < 0.05): 
        #print("G1:", shapiro1.pvalue)
        return False

    shapiro2 = stats.shapiro(group2)
    if (shapiro2.pvalue < 0.05): 
        #print("G2:", shapiro2.pvalue)
        return False

    # If all values are significant
    return True

def f_test(x, y):
    x = np.array(x)
    y = np.array(y)
    
    # calculate F test statistic 
    f = np.var(x, ddof=1)/np.var(y, ddof=1)

    # define degrees of freedom numerator 
    dfn = x.size-1

    # define degrees of freedom denominator
    dfd = y.size-1  

    # find p-value of F test statistic 
    p = 1-stats.f.cdf(f, dfn, dfd)
    return f, p

# Test shapiro test with normal variables
rng1 = np.random.default_rng()
x1 = stats.norm.rvs(loc=5, scale=3, size=100, random_state=rng1)

rng2 = np.random.default_rng()
x2 = stats.norm.rvs(loc=5, scale=3, size=100, random_state=rng2)

# Check if method still works
assert shapiro_test(x1, x2)

# Create dataframe
df = pd.read_csv("./example_ans.csv")

# Update values to match regular scales (reverse the reversing of questioning)
for column in ['conscious', 'lively', 'responsive', 'nice', 'sensible', 'calm_b']:
    df[column] = 6 - df[column]

# Get types we want to compare
headers = [str(column) for column in df][2:]

# For each measurement we are gonna measure significance
for i, header in enumerate(headers):
    # Groups for natural
    g1 = df[header][df['group'] == 'blank']
    g2 = df[header][df['group'] == 'memory']

    # Check if both are normally distributed
    if shapiro_test(g1, g2):
        # Do f test to check for significance in difference in variance
        statistic, p_value = f_test(g1, g2)
        if (p_value > 0.05):
            # If value above use the T-test
            statistic, p_value = stats.ttest_ind(g1, g2, equal_var=True)
        else:
            # If value above use the Weltch T-test
            statistic, p_value = stats.ttest_ind(g1, g2, equal_var=False)
    else:
        # If not do Wilcoxon rank test
        statistic, p_value = stats.wilcoxon(g1, g2)

    # Concatenate results for boxplot
    g1.reset_index(drop=True, inplace=True)
    g2.reset_index(drop=True, inplace=True)
    frame = pd.concat([g1.rename("No-memory"), g2.rename("Memory")], axis=1)

    # Safe plot
    plt.figure()
    frame.boxplot()
    plt.title(header)
    plt.savefig('./analysis/' + str(i) + "-" + header + '.png')

    # Reject null hypothesis or not
    print("- REJECT:" if p_value < 0.05 else "+ ACCEPT:", i, header, p_value)

