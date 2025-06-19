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

    // Estrutura UD
    public boolean isValidCandidate(String str) {
        boolean isGood = false;
        // Nosso verb pode pegar mais coisas que o da ganesan, que eh soh base form (VB), o mesmo vale para adj
        if (str.matches(".*(/adj)*.*(/noun)+.*(/verb)+.*(/adj)+.*")) {
            isGood = true;
        } else if (str.matches(".*(/det)*.*(/noun)+.*(/aux)+.*(/adj)+.*")){ //nos adicionamos, pq?
            isGood = true;
        } else if (!str.matches(".*(/det).*") && str.matches(".*(/adv)*.*(/adj)+.*(/noun)+.*")) {
            isGood = true;
        } else if (str.matches(".*(/pron|/det)+.*(/verb)+.*(/adv|/adj)+.*(/noun)+.*")) { // regra 1 modificada do roque
            isGood = true;
        } else if (str.matches(".*(/adj)+.*(para/adp)+.*(/verb).*")) {
            isGood = true;
        } else if (str.matches(".*(/adv)+.*(/adp)+.*(/noun)+.*")) {
            isGood = true;
        }

        String last = str.substring(str.lastIndexOf(' '), str.length());
        if (last.matches(".*(/verb|/adp|/cconj|/det|/pron|,/punct)")) {
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

    // VSNs UD
    public boolean isVSN(Node x) {
        String nname = x.getNodeName();
        if (x.getAveragePos() <= 15.0D) {
            if ((nname.contains("/adj")) ||
                    (nname.contains("/adv")) ||
                    (nname.contains("/det")) || // pag 9 manual pos -> entendemos como pronome possesivo
                    (nname.contains("/verb")) ||
                    (nname.contains("/noun")) ||
                    (nname.matches("^(seu/|sua/|nosso/|nossa/|quando/).*")) || //pron. possesivos e conjunção
                    (nname.contains("o/pron")) ||
                    (nname.contains("se/")) ||
                    (nname.contains("para/"))) {
                return true;
            }
        }
        return false;
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
