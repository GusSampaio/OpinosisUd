package com.opinosis;

import com.opinosis.summarizer.BasicSummarizer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.textbug.utility.FileUtil;

public class OpinosisMain extends OpinosisSettings {
    String strRundId = "";

    Properties properties = new Properties();

    public void loadProps(String propfile) {
        try {
            InputStream stream = new FileInputStream(propfile);
            this.properties.load(stream);
            String property = this.properties.getProperty("collapse", "true");
            CONFIG_TURN_ON_COLLAPSE = Boolean.parseBoolean(property);
            property = this.properties.getProperty("dupelim", "true");
            CONFIG_TURN_ON_DUP_ELIM = Boolean.parseBoolean(property);
            property = this.properties.getProperty("normalized", "true");
            CONFIG_NORMALIZE_OVERALLGAIN = Boolean.parseBoolean(property);
            property = this.properties.getProperty("redundancy", "2");
            CONFIG_MIN_REDUNDANCY = Integer.parseInt(property);
            property = this.properties.getProperty("scoring_function", String.valueOf(GAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL));
            CONFIG_SCORING_FUNCTION = Integer.parseInt(property);
            property = this.properties.getProperty("gap", "3");
            CONFIG_PERMISSABLE_GAP = Integer.parseInt(property);
            if (CONFIG_PERMISSABLE_GAP < 2)
                CONFIG_PERMISSABLE_GAP = 3;
            property = this.properties.getProperty("attach_after", "2");
            CONFIG_ATTACHMENT_AFTER = Integer.parseInt(property);
            property = this.properties.getProperty("duplicate_threshold", "0.35");
            CONFIG_DUPLICATE_THRESHOLD = Double.parseDouble(property);
            property = this.properties.getProperty("max_summary", "2");
            CONFIG_MAX_SUMMARIES = Integer.parseInt(property);
            property = this.properties.getProperty("collapse_duplicate_threshold", "0.5");
            CONFIG_DUPLICATE_COLLAPSE_THRESHOLD = Double.parseDouble(property);
            property = this.properties.getProperty("run_id", "1");
            this.strRundId = property;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        OpinosisMain main = new OpinosisMain();
        main.start(args);
    }

    private void start(String[] args) {
        MyOptions bean = new MyOptions();
        CmdLineParser parser = new CmdLineParser(bean);
        try {
            parser.parseArgument(args);
            if (bean.getDirBase() == null) {
                parser.printUsage(System.err);
                System.exit(-1);
            }
        } catch (CmdLineException e) {
            System.err.println(String.valueOf(e.getMessage()) + "\n\n");
            System.err.println("java -jar opinosis.jar [options...] arguments...");
            parser.printUsage(System.err);
            return;
        }
        long tstart = System.currentTimeMillis();
        String propFile = String.valueOf(bean.getDirBase().getAbsolutePath()) + FILE_SEP + "etc" + FILE_SEP + "opinosis.properties";
        String inputDir = String.valueOf(bean.getDirBase().getAbsolutePath()) + FILE_SEP + "input";
        String outputDir = String.valueOf(bean.getDirBase().getAbsolutePath()) + FILE_SEP + "output";
        loadProps(propFile);
        List<String> filesToSum = new ArrayList<String>();
        if (inputDir.length() > 0 && FileUtil.isFileExists(inputDir)) {
            filesToSum = FileUtil.getFilesInDirectory(inputDir);
        } else {
            System.err.println(String.valueOf(inputDir) + " " + " does not exist..please check your directory structure");
        }
        if (filesToSum.size() > 1000) {
            System.err.println("Too many files to summarize. Please limit to 200 files per run.");
            System.exit(-1);
        }
        int i = 1;
        for (String infile : filesToSum) {
            String outfile = getOutputFileName(outputDir, infile);
            doGenerateSummary(infile, outfile, i++);
        }
        long tend = System.currentTimeMillis();
        System.out.println("Took " + (tend - tstart) + "ms");
    }

    private String getOutputFileName(String dirOut, String file) {
        int idxStart = file.lastIndexOf(FILE_SEP);
        int idxEnd = file.indexOf('.', idxStart);
        if (idxEnd == -1)
            idxEnd = file.length() - 1;
        String theOutFile = "";
        String runOutputPath = String.valueOf(dirOut) + FILE_SEP + this.strRundId + FILE_SEP;
        File f = new File(runOutputPath);
        f.mkdirs();
        theOutFile = String.valueOf(runOutputPath) + file.substring(idxStart, idxEnd) + "." + this.strRundId + ".system";
        try {
            PrintWriter writer = new PrintWriter(String.valueOf(dirOut) + FILE_SEP + "config." + this.strRundId + ".txt");
            this.properties.list(writer);
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("There seems to be some problem with the file names, please contact kganes2@illinois.edu");
        }
        return theOutFile;
    }

    public void doGenerateSummary(String fileName, String outfile, int taskId) {
        SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g = new SimpleDirectedWeightedGraph(DefaultWeightedEdge.class);
        OpinosisGraphBuilder builder = new OpinosisGraphBuilder();
        HashMap<String, Node> wordNodeMap = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String str = "";
            int sentenceid = 0;
            while ((str = reader.readLine()) != null) {
                sentenceid++;
                str = str.toLowerCase();
                wordNodeMap = builder.growGraph(str, 1, sentenceid);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.out.println("----------------TASK:" + taskId + "--------------------------");
        System.out.println("Generating Summaries for: " + fileName);
        System.out.println("Graph materialized...");
        Writer bla = new PrintWriter(System.out);
        g = builder.getGraph();
        try {
            System.out.println("Started summary generation...");
            BufferedWriter printer = FileUtil.getWriter(outfile);
            BasicSummarizer basicSummarizer = new BasicSummarizer(g, wordNodeMap, printer);
            basicSummarizer.start();
            System.out.println("Generated: " + outfile);
            System.gc();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private int getReviewId(String str) {
        int revid = -1;
        if (str.startsWith("#"))
            revid = Integer.parseInt(str.substring(1, str.length()));
        return revid;
    }
}
