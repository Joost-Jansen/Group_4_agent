import pandas as pd
import researchpy as rp
import numpy as np
import matplotlib.pyplot as plt
import scipy.stats as stats
import warnings
warnings.filterwarnings("ignore")

# Box plots of results
df = pd.read_csv("./example_ans.csv")

# Update values to match regular scales (reverse the reversing of questioning)
for column in ['conscious', 'lively', 'responsive', 'nice', 'sensible', 'calm_b']:
    df[column] = 6 - df[column]

# Dataframes of memory and no memory group
df_mem = df.loc[df['group'] == "memory"]
df_non = df.loc[df['group'] == "no-memory"]

# Get headers we want to check for
headers = [str(column) for column in df][2:]

# Create plots
for type, cols in [("anthropomorphism", 5), ("animacy", 6), ("likeability", 5), ("perceived_intelligence", 5), ("perceived_safety", 6)]:
    for name, frame in [("mem", df_mem), ("non", df_non)]:
        plt.figure()
        frame.boxplot(column=headers[:cols])
        plt.savefig('./boxplots/' + type + '_' + name + '.png')
    headers = headers[cols:]
assert len(headers) == 0