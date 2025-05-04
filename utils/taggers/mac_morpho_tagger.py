import os
import logging
import stanza

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

# Load Portuguese model (trained on Mac-Morpho)
nlp = stanza.Pipeline(lang='pt', processors='tokenize,pos', tokenize_no_ssplit=True)

def load_text(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def tag_with_stanza(text):
    return nlp(text)  # Returns a Stanza Document

def write_stanza_tags(filename, doc):
    with open(filename, "w", encoding="utf-8") as f:
        for sentence in doc.sentences:
            tagged = [f"{word.text}/{word.upos}" for word in sentence.words]
            f.write(" ".join(tagged) + "\n")
    logging.info(f"Tagged output written to: {filename}")

def perform_stanza_tagging(file_paths, delete_original=False):
    for file_path in file_paths:
        try:
            logging.info(f"Tagging file: {file_path}")
            text = load_text(file_path)
            doc = tag_with_stanza(text)

            output_file = os.path.splitext(file_path)[0] + "_macmorpho.txt"
            write_stanza_tags(output_file, doc)

            if delete_original:
                os.remove(file_path)
                logging.info(f"Deleted original file: {file_path}")

        except Exception as e:
            logging.error(f"Error processing {file_path}: {e}")

# Example usage
if __name__ == "__main__":
    input_files = ["dummy.txt"]  # Replace with your input files
    perform_stanza_tagging(input_files, delete_original=False)