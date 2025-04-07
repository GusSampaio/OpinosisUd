from blanc import BlancHelp, BlancTune
import pandas as pd

def clean_ud(ud_summaries):
    new_ud_summaries = []
    for summary in ud_summaries:
        summary = summary.strip()
        x = summary.split()
        new_x = []
        skip = 0

        for i in range(len(x)):
            if skip > 0:
                skip -= 1
                continue

            token = x[i]
            if token.endswith('/_') and i + 2 < len(x):
                new_x.append(token.split('/_')[0])
                skip = 2 
            else:
                new_x.append(token)
        new_ud_summaries.append(' '.join(new_x)) 

    return new_ud_summaries

blanc_help = BlancHelp(device='cuda', show_progress_bar=False)
blanc_tune = BlancTune(device='cuda', finetune_mask_evenly=False, show_progress_bar=False)

results = {}

original_doc = open("../ud_approach/exemplos/original.txt", "r").read()

with open("../ganesam_approach/opinosis_sample/output/MyTestRun/tag_ganesam.MyTestRun.system", "r") as f:
    summaries_ganesam = f.readlines()

with open("../ud_approach/opinosis_sample/output/MyTestRun/tag_ud.MyTestRun.system", "r") as f:
    summaries_ud = f.readlines()
summaries_ud = clean_ud(summaries_ud)

results = [] 
for i in range(len(summaries_ganesam)):  
    results.append({ 
        "id": i,
        "ganesam_blanc_help": blanc_help.eval_once(original_doc, summaries_ganesam[i]),
        "ganesam_blanc_tune": blanc_tune.eval_once(original_doc, summaries_ganesam[i]),
        "UD_blanc_help": blanc_help.eval_once(original_doc, summaries_ud[i]),
        "UD_blanc_tune": blanc_tune.eval_once(original_doc, summaries_ud[i]),
    })

df = pd.DataFrame(results)
df.to_csv("results.csv", index=False) 