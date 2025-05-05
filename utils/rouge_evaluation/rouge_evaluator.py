from rouge_score import rouge_scorer
import csv

if __name__ == "__main__":
    
    scorer = rouge_scorer.RougeScorer(['rouge1', 'rouge2', 'rougeL'], use_stemmer=True)
    
    with open("C:/Users/davif/OneDrive/Documentos/github/OpinosisUd/utils/rouge_evaluation/tests/human_summaries/releicao_lula _sumario_humano.txt", "r") as f:
        human_summary = f.readlines()

    with open("C:/Users/davif/OneDrive/Documentos/github/OpinosisUd/UD_approach/opinosis_sample/output/MyTestRun/tag_ud_.MyTestRun.system", "r") as f:
        summary_ud = f.readlines()
    
    scores = scorer.score(human_summary[0], summary_ud[0]) # weird because it should actually compare between the actual summary and the produced
    
    csv_path = "rouge_scores.csv"
    with open(csv_path, mode='w', newline='', encoding='utf-8') as file:
        writer = csv.writer(file)
        writer.writerow(["metric", "precision", "recall", "f1_score"])
        for metric, score in scores.items():
            writer.writerow([metric, score.precision, score.recall, score.fmeasure])