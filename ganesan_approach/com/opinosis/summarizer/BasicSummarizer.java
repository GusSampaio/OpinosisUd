//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.opinosis.summarizer;

import com.opinosis.Candidate;
import com.opinosis.Node;
import com.opinosis.OpinosisCore;
import com.opinosis.OpinosisSettings;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.textbug.utility.MathUtil;

public class BasicSummarizer extends OpinosisCore {
    public BasicSummarizer(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g, HashMap<String, Node> wordNodeMap, Writer printer) {
        super(g, wordNodeMap, printer);
    }
    // Estrutura ganesan
    public boolean isValidCandidate(String str) {
        boolean isGood = false;
        if (str.matches(".*(/jj)*.*(/nn)+.*(/vb)+.*(/jj)+.*")) {
            isGood = true;
        } else if (!str.matches(".*(/dt).*") && str.matches(".*(/rb)*.*(/jj)+.*(/nn)+.*")) {
            isGood = true;
        } else if (str.matches(".*(/prp|/dt)+.*(/vb)+.*(/rb|/jj)+.*(/nn)+.*")) {
            isGood = true;
        } else if (str.matches(".*(/jj)+.*(/to)+.*(/vb).*")) {
            System.out.println(str);
            isGood = true;
        } else if (str.matches(".*(/rb)+.*(/in)+.*(/nn)+.*")) {
            isGood = true;
        }

        String last = str.substring(str.lastIndexOf(32), str.length());
        if (last.matches(".*(/to|/vbz|/in|/cc|wdt|/prp|/dt|/,)")) {
            isGood = false;
        }
        return isGood;
    }

    public List<int[]> getNodeOverlap(List<int[]> left, List<int[]> right) {
        List<int[]> l3 = new ArrayList();
        int pointer = 0;

        for(int i = 0; i < left.size(); ++i) {
            int[] eleft = (int[])left.get(i);
            if (pointer > right.size()) {
                break;
            }

            for(int j = pointer; j < right.size(); ++j) {
                int[] eright = (int[])right.get(j);
                if (eright[0] == eleft[0]) {
                    if (eright[1] > eleft[1] && Math.abs(eright[1] - eleft[1]) <= OpinosisSettings.CONFIG_PERMISSABLE_GAP) {
                        l3.add(eright);
                        pointer = j + 1;
                        break;
                    }

                    int var10000 = eright[1];
                    var10000 = eleft[1];
                } else if (eright[0] > eleft[0]) {
                    break;
                }
            }
        }

        return l3;
    }
    // VSNs ganesan
    public boolean isVSN(Node x) {
        String nname = x.getNodeName();
        return x.getAveragePos() <= 15.0 &&
                (nname.contains("/jj") ||
                        nname.contains("/rb") ||
                        nname.contains("/prp$") ||
                        nname.contains("/vbg") ||
                        nname.contains("/nn") ||
                        nname.contains("/dt") ||
                        nname.matches("^(its/|the/|when/|a/|an/|this/|the/|they/|it/|i/|we/|our/).*") ||
                        nname.contains("it/prp")
                        || nname.contains("if/")
                        || nname.contains("for/"));
    }

    public boolean isVEN(Node x, int pathLength, boolean isCollapsedCandidate) {
        if (this.isEndToken(x)) {
            return true;
        } else {
            return this.getGraph().outDegreeOf(x) <= 0;
        }
    }

    public boolean isEndToken(Node x) {
        String token = x.getNodeName();
        return token.matches(".*(/\\.|/,)");
    }

    public double computeCandidateSimScore(Candidate s1, Candidate s2) {
        List<Node> l1 = s1.theNodeList;
        List<Node> l2 = s2.theNodeList;
        HashSet union = new HashSet(l1);
        HashSet intersect = new HashSet(l1);
        union.addAll(l2);
        intersect.retainAll(l2);
        double overlap = (double)intersect.size() / (double)union.size();
        return overlap;
    }

    public double computeScore(double currentScore, List<int[]> currOverlapList, int pathLength) {
        double theGain = 0.0;
        int overlapSize = currOverlapList.size();

        if (CONFIG_SCORING_FUNCTION == GAIN_REDUNDANCY_ONLY) {
            theGain = currentScore + (double)overlapSize;
        }

        if (CONFIG_SCORING_FUNCTION == GAIN_WEIGHTED_REDUNDANCY_BY_LEVEL) {
            theGain = currentScore + (double)(overlapSize * pathLength);
        }

        if (CONFIG_SCORING_FUNCTION == GAIN_WEIGHTED_REDUNDANCY_BY_LOG_LEVEL) {
            if (pathLength > 1) {
                theGain = currentScore + (double)overlapSize * MathUtil.getLog2((double)pathLength);
            } else {
                theGain = currentScore + (double)overlapSize;
            }
        }

        return theGain;
    }

    public double computeAdjustedScore(double score, int level) {
        double oGain = score;
        if (CONFIG_NORMALIZE_OVERALLGAIN) {
            oGain /= (double)level;
        }

        return oGain;
    }

    public boolean shouldContinueTraverse(Node x, List<int[]> overlapSoFar, int pathLength, double score) {
        if (pathLength >= this.P_MAX_SENT_LENGTH) {
            return false;
        } else if (score == Double.NEGATIVE_INFINITY) {
            return false;
        } else {
            return overlapSoFar.size() >= CONFIG_MIN_REDUNDANCY || this.isEndToken(x);
        }
    }
}
