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
    with open(output_file, 'a', encoding='utf-8') as out:
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
                        if len(parts) >= 4 and not (parts[1] == '''"'''):  # Valid UD format
                            token = parts[1]      # FORM (word/token)
                            tag = parts[3]        # UPOS (universal POS tag)
                            if tag != '_':
                                tokens.append(f"{token}/{tag}")
                if tokens:  # Write any remaining tokens
                    out.write(" ".join(tokens) + '\n\n')

# Configure logging
logging.basicConfig(level=logging.INFO, format="%(levelname)s: %(message)s")

def perform_ud_tagging(file_paths, output_path):
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
            #logging.info(f"Intermediate file saved: {aux_filename}")

            process_ud_files([aux_filename], output_path)
            logging.info(f"Final UD file saved: {output_path}")
            
            # Delete the auxiliary file
            os.remove(aux_filename)
            logging.info(f"Auxiliary file deleted: {aux_filename}")

        except Exception as e:
            logging.error(f"Error processing '{file_path}': {e}")

# Example usage
if __name__ == "__main__":
    subject_paths = []
    input_path = "../../OpiSums-PT/Textos_Fontes/"

    for file in os.listdir(input_path):
        subject_paths.append(os.path.join(input_path, file))

    for subject_path in subject_paths:
        input_files = []
        for file in os.listdir(subject_path):
            input_files.append(os.path.join(subject_path,file))

        output_path = "../../UD_approach/opinosis_sample/input/" + subject_path.split("/")[-1] + ".txt"
        perform_ud_tagging(input_files, output_path)