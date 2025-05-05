import argparse
import os
import logging
from taggers import mac_morpho_tagger, ud_tagger #, penn_treebank_tagger

logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Rodar os taggers de ud (ud), stanza (ptb), roque (mac_morpho)")
    parser.add_argument("input_folder", help="Pasta com arquivos de entrada (.txt)")
    args = parser.parse_args()
    
    print(args.input_folder)
    
    for filename in os.listdir(args.input_folder):
        if filename.endswith(".txt"):
            input_path = os.path.join(args.input_folder, filename)
            
            try:
                #penn_treebank_tagger.perform_ptb_tagging([input_path])
                mac_morpho_tagger.perform_stanza_tagging([input_path])
                ud_tagger.perform_ud_tagging([input_path])
                
            except Exception as e:
                logging.error(f"Error in processing {input_path}: {e}")