from blanc import BlancHelp, BlancTune
import pandas as pd

blanc_help = BlancHelp(device='cuda', show_progress_bar=False)
blanc_tune = BlancTune(device='cuda', finetune_mask_evenly=False, show_progress_bar=False)

results = {}

original_doc = open("../ud_approach/exemplos/original.txt", "r").read()

summaries_ganesam = open("../ganesam_approach/opinosis_sample/output/MyTestRun/tag_ganesam.MyTestRun.system", "r").readlines()
summaries_ud = open("../ud_approach/opinosis_sample/output/MyTestRun/tag_ud.MyTestRun.system", "r").readlines()


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