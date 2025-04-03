from blanc import BlancHelp, BlancTune

document = "Jack drove his minivan to the bazaar to purchase milk and honey for his large family."
summary = "Jack bought milk and honey."

blanc_help = BlancHelp(device='cuda')
blanc_tune = BlancTune(finetune_mask_evenly=False, show_progress_bar=False)

print(blanc_help.eval_once(document, summary))
print(blanc_tune.eval_once(document, summary))