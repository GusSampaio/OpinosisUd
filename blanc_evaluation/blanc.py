import torch
from transformers import AutoTokenizer, AutoModelForMaskedLM
import numpy as np
from scipy.stats import entropy

class BLANCEvaluator:
    def __init__(self, model_name='bert-base-multilingual-cased'):
        """
        Inicializa o modelo BLANC usando um modelo BERT multilíngue
        """
        # Carregar tokenizador e modelo
        self.tokenizer = AutoTokenizer.from_pretrained(model_name)
        self.model = AutoModelForMaskedLM.from_pretrained(model_name)
        self.model.eval()
    
    def calculate_perplexity(self, text):
        """
        Calcula a perplexidade do texto usando o modelo
        """
        # Tokenizar o texto
        inputs = self.tokenizer(text, return_tensors='pt', truncation=True, max_length=512)
        
        # Calcular probabilidades
        with torch.no_grad():
            outputs = self.model(**inputs, labels=inputs['input_ids'])
        
        # Calcular perplexidade
        loss = outputs.loss
        perplexity = torch.exp(loss)
        
        return perplexity.item()
    
    def calculate_cross_lingual_entropy(self, source_text, target_text):
        """
        Calcula a entropia cruzada entre textos em diferentes línguas
        """
        # Tokenizar ambos os textos
        source_tokens = self.tokenizer.encode(source_text, return_tensors='pt', truncation=True)
        target_tokens = self.tokenizer.encode(target_text, return_tensors='pt', truncation=True)
        
        # Calcular distribuições de probabilidade
        with torch.no_grad():
            source_outputs = self.model(source_tokens)
            target_outputs = self.model(target_tokens)
        
        # Calcular entropia
        source_probs = torch.softmax(source_outputs.logits, dim=-1)
        target_probs = torch.softmax(target_outputs.logits, dim=-1)
        
        cross_entropy_value = entropy(source_probs.mean(dim=0).numpy(), 
                                      target_probs.mean(dim=0).numpy())
        
        return cross_entropy_value
    
    def evaluate_translation(self, source_text, translations):
        """
        Avalia múltiplas traduções usando princípios do BLANC
        """
        results = []
        
        for translation in translations:
            # Cálculo de perplexidade da tradução
            perplexity = self.calculate_perplexity(translation)
            
            # Cálculo de entropia cruzada
            cross_lingual_entropy = self.calculate_cross_lingual_entropy(source_text, translation)
            
            # Métrica composta (quanto menor, melhor)
            blanc_score = 1 / (perplexity * (1 + cross_lingual_entropy))
            
            results.append({
                'translation': translation,
                'perplexity': perplexity,
                'cross_lingual_entropy': cross_lingual_entropy,
                'blanc_score': blanc_score
            })
        
        # Ordenar por BLANC score (maior é melhor)
        return sorted(results, key=lambda x: x['blanc_score'], reverse=True)

# Exemplo de uso
def main():
    # Inicializar o avaliador BLANC
    evaluator = BLANCEvaluator()
    
    # Texto original
    source_text = "O sol brilhava intensamente sobre as montanhas verdes."
    
    # Traduções para avaliar
    translations = [
        "The sun was shining intensely over the green mountains.",
        "Sunlight cascaded brilliantly across verdant peaks.",
        "Sun bright mountains green light shadow make."
    ]
    
    # Realizar avaliação
    results = evaluator.evaluate_translation(source_text, translations)
    
    # Imprimir resultados
    print("Resultados da Avaliação BLANC:")
    for result in results:
        print("\nTradução:", result['translation'])
        print("Perplexidade:", result['perplexity'])
        print("Entropia Cruzada:", result['cross_lingual_entropy'])
        print("Pontuação BLANC:", result['blanc_score'])

if __name__ == "__main__":
    main()