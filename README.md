# 📚 Multidocument Automatic Summarization for Portuguese

This repository contains the experiments described in the paper:

**Multidocument Automatic Summarization for Portuguese: Comparing a Graph-Based Method with a Large Language Model**

Authors: *\[Your names here]*
Institution: *\[Your institution here]*
Date: August 2025

---

##  Description

This project aims to compare a **graph-based summarization method** with a **large language model (LLM)** for the task of **multidocument automatic summarization in Portuguese**, using the **OpiSums-PT** corpus.

The graph-based method is an updated version of **Opinosis** (Ganesan et al., 2010), incorporating **Universal Dependencies (UD)** and morphosyntactic rules. The LLM used is the open-source model **Mistral-7B-Instruct-v0.3**, executed in quantized form for efficiency.

---

## Experimental Setup

The project includes four main experimental steps. Each step is implemented via dedicated scripts located in the `scripts/` directory.

### Steps:

1. **Summarization with LLM:**

   * Script: `scripts/llm_summarization`
   * Description: Generates automatic summaries for each document group using the Mistral model.

2. **Evaluation with BLANC:**

   * Script: `scripts/blanc_eval`
   * Description: Evaluates summary quality using the **BLANC** metric, which measures utility and coherence.

3. **Sentiment Analysis:**

   * Script: `scripts/SentimentAnalyst`
   * Description: Analyzes sentiment polarity (positive/negative) in the generated summaries, using the **TeenyTinyLlama-460m-IMDB** model.

4. **ROUGE + Sentiment Evaluation:**

   * Script: `scripts/metrics_evaluator`
   * Description: Performs both **ROUGE-1** and **ROUGE-L** evaluation and additional **sentiment analysis**.

---

## 🗂️ Repository Structure

```
├── scripts/
│   ├── llm_summarization        # Summarization using Mistral
│   ├── blanc_eval               # BLANC metric evaluation
│   ├── SentimentAnalyst         # Sentiment polarity analysis
│   └── metrics_evaluator        # ROUGE + sentiment analysis
│
├── OpiSums-PT/
│   ├── resumos_gerados/         # Generated resumes with llms and opinosis
│
├── UD_approach/
│   └── [opinosis algorithm running on a java project]
│
├── utils/
│   └── ud_tagger/            
│
├── README.md                    # Project documentation
└── requirements.txt             # Python dependencies
```

---
Obs: OpiSums-PT/ has 3 more folders, explained better in the readme.md file in it.

## Running the Experiments

Clone the repository and install dependencies with:

```bash
git clone https://github.com/GusSampaio/OpinosisUd.git
cd OpinosisUd
pip install -r requirements.txt
```


## Results Summary

The results reveal a clear **trade-off** between the two methods:

* **Opinosis** achieves **greater fidelity to source content** (higher **BLANC** scores).
* The **LLM** produces summaries **closer to human style** (higher **ROUGE-1**).

Sentiment analysis shows **higher agreement with human-written summaries** for the LLM-generated abstracts and extracts, especially in the abstractive case.

Evaluation plots and detailed data are available in the `figures/` folder.

---

## Resources

* [`mistralai/Mistral-7B-Instruct-v0.3`](https://huggingface.co/mistralai/Mistral-7B-Instruct-v0.3)
* [`TeenyTinyLlama-460m-IMDB`](https://huggingface.co/nkoriyama/teenytinyllama-460m-imdb)
* [Porttinari + UDPipe 2](https://www.sciencedirect.com/science/article/abs/pii/S0957417417300829?via%3Dihub))

---

##  Contact

Questions, suggestions or contributions are welcome!
Please contact: *\[[your-email@example.com](mailto:your-email@example.com)]*
Or open an *Issue* on this repository.

## ⚙️ Useful Java Commands for Compilation and Packaging

### How to create a `.jar` (executable) from compiled `.class` files

```bash
jar cfm app.jar .\META-INF\MANIFEST.MF -C com . -C org .
```

* This command creates a JAR file named `app.jar`, including:

  * A manifest file located at `.\META-INF\MANIFEST.MF`.
  * All files and subdirectories from `com` and `org`, preserving the internal directory structure. These should already be compiled `.class` files.

---

###  How to compile a `.java` file into a `.class` file

```bash
javac MainClass.java
```

* This will generate a `MainClass.class` file in the same directory as the source `.java`.
* If your code depends on other classes or external libraries located in different packages or folders, you should specify a classpath:

```bash
javac -classpath . com/example/MainClass.java
```

* The `.` tells the compiler to look for classes in the current directory and subdirectories.

---

### How to decompile a `.class` file into a `.java` file

* The simplest way is to use a Java decompiler (e.g., [JD-GUI](http://java-decompiler.github.io/), Fernflower, CFR).
* Open the `.class` file in the decompiler and either:

  * Copy the decompiled code into a new `.java` file manually, or
  * Use the export feature (if supported) to save as `.java`.

---

### How to extract `.class` files and resources from a `.jar` file

```bash
jar xf your_archive.jar
```

* After running this command, all `.class` files (and other resources) will be extracted into the current directory or subfolders, preserving the original structure of the JAR.

---

### How to compile multiple `.java` files recursively using PowerShell

```powershell
javac -d out -cp org (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
```

* This command:

  * Recursively finds all `.java` files from the current directory and subdirectories,
  * Compiles them using `javac`,
  * Outputs the `.class` files into the `out` directory,
  * Uses `org` as the classpath (can be replaced by any relevant path or JAR).

> ⚠️ This command is intended for use in **PowerShell (Windows)**. On Linux/macOS, use `find` and `xargs` instead.

