import os
import logging
import nltk

# Ensure required NLTK resources are downloaded
nltk.download('punkt')
nltk.download('averaged_perceptron_tagger')

from nltk import word_tokenize, pos_tag

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

def load_text(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

def tag_penn_nltk(text):
    tokens = word_tokenize(text)
    return pos_tag(tokens)  # Returns list of (word, tag) tuples

def write_penn_file(filename, tagged_tokens):
    with open(filename, "w", encoding="utf-8") as f:
        for word, tag in tagged_tokens:
            f.write(f" {word}/{tag}")
    logging.info(f"Tagged file written: {filename}")

def perform_ptb_tagging(file_paths, delete_original=False):
    for file_path in file_paths:
        try:
            logging.info(f"Tagging file: {file_path}")

            # Load and tag the text
            text = load_text(file_path)
            tagged_tokens = tag_penn_nltk(text)

            # Write tagged result
            output_file = os.path.splitext(file_path)[0] + "_ptb.txt"
            write_penn_file(output_file, tagged_tokens)

            # Optionally delete original
            if delete_original:
                os.remove(file_path)
                logging.info(f"Deleted original file: {file_path}")

        except Exception as e:
            logging.error(f"Error processing {file_path}: {e}")

# Example usage
if __name__ == "__main__":
    input_files = ["dummy.txt", "example.txt"]  # Replace with your file paths
    perform_ptb_tagging(input_files, delete_original=False)