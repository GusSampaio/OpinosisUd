import requests
import os
import logging

def load_text(path):
    text = ""
    with open(path, "r", encoding="utf-8") as f:
        text = f.read()
    return text

def tag_text(text):
    
    url = "https://lindat.mff.cuni.cz/services/udpipe/api/process"
    
    params = {
        "tokenizer":"",
        "tagger":"",
        "parser":"",
        "data":text,
        "model":"portuguese-bosque-ud-2.15-241121"
    }
    
    response = requests.post(url, data= params)
    
    if response.status_code == 200:
        output = response.json()
        return output["result"]
    else:
        print("Request Error: ", response.status_code)
        return None

def save_result(filename, text):
    with open(filename, "w", encoding="utf-8") as f:
        f.write(text)

def process_ud_files(file_list, output_file):
    """
    Processes UD-formatted files to convert sentences into a single line format
    like 'token/tag token/tag ...' and saves the result to an output file.

    :param file_list: List of paths to UD-formatted files.
    :param output_file: Path to the output file.
    """
    with open(output_file, 'w', encoding='utf-8') as out:
        for file in file_list:
            with open(file, 'r', encoding='utf-8') as inp:
                tokens = []
                for line in inp:
                    line = line.strip()
                    if not line:  # End of a sentence
                        if tokens:
                            out.write(" ".join(tokens) + '\n')
                            tokens = []
                    elif not line.startswith('#'):  # Skip comments
                        parts = line.split('\t')
                        if len(parts) >= 4:  # Valid UD format
                            token = parts[1]      # FORM (word/token)
                            tag = parts[3]        # UPOS (universal POS tag)
                            tokens.append(f"{token}/{tag}")
                if tokens:  # Write any remaining tokens
                    out.write(" ".join(tokens) + '\n')

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

def perform_ud_tagging(file_paths):
    for file_path in file_paths:
        try:
            logging.info(f"Processing file: {file_path}")

            # Load the text from the file
            text = load_text(file_path)

            # Tag the text using UD tagging
            tagged_text = tag_text(text)

            # Save the intermediate tagged result
            aux_filename = os.path.splitext(file_path)[0] + "_aux_ud.txt"
            save_result(aux_filename, tagged_text)
            logging.info(f"Intermediate file saved: {aux_filename}")

            # Generate final UD-tagged file
            final_filename = os.path.splitext(file_path)[0] + "_ud.txt"
            process_ud_files([aux_filename], final_filename)
            logging.info(f"Final UD file saved: {final_filename}")
            
            # Delete the auxiliary file
            os.remove(aux_filename)
            logging.info(f"Auxiliary file deleted: {aux_filename}")

        except Exception as e:
            logging.error(f"Error processing '{file_path}': {e}")

# Example usage
if __name__ == "__main__":
    input_files = ["dummy.txt", "example.txt"]  # Add more file names as needed
    perform_ud_tagging(input_files)