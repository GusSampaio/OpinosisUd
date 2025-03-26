//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.opinosis;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

public abstract class OpinosisCore extends OpinosisSettings {
    SimpleDirectedGraph<Node, DefaultWeightedEdge> mGraph;
    Writer mWriter;
    HashMap<String, Node> mWordNodeMap = null;
    protected static final boolean DEBUG = false;
    String mAnchor = "";
    double beforeAttachGain = 0.0;
    double mAnchorPathScore = 0.0;
    private int mAnchorPathLen = 0;
    HashSet<Candidate> shortlisted = new HashSet();
    HashMap<String, Candidate> ccList = new HashMap();

    private void print() {
        System.out.print("");
    }

    private void print(String str) {
        System.out.print(str + " ");
    }

    private void println(String str) {
        System.out.println(str + " ");
    }

    public OpinosisCore(SimpleDirectedWeightedGraph<Node, DefaultWeightedEdge> g, HashMap<String, Node> wordNodeMap, Writer printer) {
        this.mGraph = g;
        this.mWriter = printer;
        this.mWordNodeMap = wordNodeMap;
    }

    private boolean doCollapse(Node x, List<int[]> YintersectX, double pathscore, double prevPathScore, String str, List<int[]> overlapList, int level, boolean concatOn) {
        this.mAnchor = str;
        this.mAnchorPathScore = prevPathScore;
        this.mAnchorPathLen = level;
        Set<DefaultWeightedEdge> edges = this.mGraph.outgoingEdgesOf(x);
        if (edges != null && edges.size() > 1) {
            Iterator<DefaultWeightedEdge> cIter = edges.iterator();

            while(cIter.hasNext()) {
                DefaultWeightedEdge cEdgeOfX = (DefaultWeightedEdge)cIter.next();
                Node cY = (Node)this.mGraph.getEdgeTarget(cEdgeOfX);
                String cYNodeName = cY.getNodeName();
                List<int[]> cYintersectX = this.getNodeOverlap(overlapList, cY.getSentenceIds());
                int newLevel = level + 1;
                double newPathScore = this.computeScore(pathscore, cYintersectX, newLevel);
                if (cYintersectX.size() >= CONFIG_MIN_REDUNDANCY) {
                    this.traverse(cY, cYintersectX, "xx " + cYNodeName, newPathScore, newLevel, true, false);
                }
            }
        }

        concatOn = false;
        return this.processFound();
    }

    public void start() throws IOException {
        long startTime = 0L;
        long endTime = 0L;
        Set<Node> nodeList = this.mGraph.vertexSet();
        Iterator<Node> nodes = nodeList.iterator();

        while(nodes.hasNext()) {
            Node x = (Node)nodes.next();
            double score = 0.0;
            if (this.isVSN(x)) {
                this.traverse(x, x.getSentenceIds(), x.getNodeName(), score, 1, false, false);
            }
        }

        List<Candidate> theSentenceInfos = this.getFinalSentences();
        Iterator var9 = theSentenceInfos.iterator();

        while(var9.hasNext()) {
            Candidate info = (Candidate)var9.next();
            info.sent = info.sent.replaceAll("(/[a-z,.;$]+(\\s+|$))", " ");
            info.sent = info.sent.replaceAll("xx", "");
            info.sent = info.sent + " .";
            info.sent = info.sent.replaceAll("\\s+", " ");
            this.mWriter.append(info.sent);
            this.mWriter.append("\n");
        }

        this.mWriter.close();
    }

    private List<Candidate> getFinalSentences() {
        List<Candidate> temp = new ArrayList();
        List<Candidate> shortlistedFinal = new ArrayList();
        if (this.shortlisted.size() <= 0) {
            return shortlistedFinal;
        } else {
            temp.addAll(this.removeDuplicates(this.shortlisted, false));
            Collections.sort(temp, new SummarySorter());
            if ((double)temp.size() > CONFIG_MAX_SUMMARIES) {
                shortlistedFinal.add((Candidate)temp.get(0));

                for(int i = 1; i < temp.size() && (double)shortlistedFinal.size() < CONFIG_MAX_SUMMARIES; ++i) {
                    Candidate a = (Candidate)temp.get(i - 1);
                    Candidate b = (Candidate)temp.get(i);
                    shortlistedFinal.add(b);
                }
            } else {
                shortlistedFinal.addAll(temp);
            }

            return shortlistedFinal;
        }
    }

    private List<Node> getNodeList(String sent) {
        String[] tokens = sent.split("\\s+");
        ArrayList<Node> l = new ArrayList();
        String[] var7 = tokens;
        int var6 = tokens.length;

        for(int var5 = 0; var5 < var6; ++var5) {
            String token = var7[var5];
            if (token.matches(".*(/nn|/jj|/vb[a-s]).*")) {
                Node n = (Node)this.mWordNodeMap.get(token);
                if (n != null) {
                    l.add(n);
                }
            }
        }

        return l;
    }

    private boolean processFound() {
        boolean success = false;
        Collection<Candidate> temp = this.ccList.values();
        HashSet<Candidate> collapsed = new HashSet(temp);
        collapsed = this.removeDuplicates(collapsed, true);
        int i = 0;
        if (collapsed.size() > 1) {
            double overallgains = 0.0;
            double allscores = this.mAnchorPathScore;
            double allgains = this.beforeAttachGain;
            int alllevels = this.mAnchorPathLen;
            StringBuffer buffer = new StringBuffer(this.mAnchor);
            List<int[]> sentList = new ArrayList();

            for(Iterator var15 = collapsed.iterator(); var15.hasNext(); ++i) {
                Candidate theInfo = (Candidate)var15.next();
                overallgains += theInfo.gain;
                allgains += theInfo.localgain;
                allscores += theInfo.rawscore;
                alllevels += theInfo.level;
                sentList.addAll(theInfo.sentList);
                if (i > 0 && i == collapsed.size() - 1) {
                    buffer.append(" and ");
                } else if (i > 0) {
                    buffer.append(" , ");
                } else {
                    buffer.append(" ");
                }

                buffer.append(theInfo.sent);
            }

            if (this.ccList.size() > 1) {
                double overallGain = overallgains / (double)this.ccList.size();
                this.shortlisted.add(new Candidate(overallGain, buffer.toString(), sentList, alllevels));
                success = true;
            }
        }

        this.ccList.clear();
        this.mAnchor = "";
        this.beforeAttachGain = 0.0;
        this.mAnchorPathScore = 0.0;
        this.mAnchorPathLen = 0;
        return success;
    }

    private void processNext(Node x, String str, List<int[]> overlapList, double currentPathScore, int pathLen, boolean isCollapsedPath) {
        Set<DefaultWeightedEdge> outgoing = this.mGraph.outgoingEdgesOf(x);
        if (outgoing != null && outgoing.size() > 0) {
            Iterator<DefaultWeightedEdge> xEdges = outgoing.iterator();
            boolean doMore = true;

            while(true) {
                while(true) {
                    Node y;
                    String yNodeName;
                    List currOverlapList;
                    do {
                        if (!xEdges.hasNext() || !doMore) {
                            return;
                        }

                        DefaultWeightedEdge xEdge = (DefaultWeightedEdge)xEdges.next();
                        y = (Node)this.mGraph.getEdgeTarget(xEdge);
                        yNodeName = y.getNodeName();
                        currOverlapList = this.getNodeOverlap(overlapList, y.getSentenceIds());
                    } while(currOverlapList.size() <= 0);

                    int newPathLen = pathLen + 1;
                    double newPathScore = this.computeScore(currentPathScore, currOverlapList, newPathLen);
                    if (CONFIG_TURN_ON_COLLAPSE && pathLen >= CONFIG_ATTACHMENT_AFTER && !isCollapsedPath && currOverlapList.size() <= overlapList.size() && x.getNodeName().matches(".*(/vb[a-z]|/in)")) {
                        boolean success = this.doCollapse(x, currOverlapList, newPathScore, currentPathScore, str, overlapList, pathLen, isCollapsedPath);
                        if (!success) {
                            String strTemp = str + " " + y.getNodeName();
                            doMore = this.traverse(y, currOverlapList, strTemp, newPathScore, newPathLen, isCollapsedPath, false);
                        }
                    } else {
                        String strTemp = str + " " + yNodeName;
                        doMore = this.traverse(y, currOverlapList, strTemp, newPathScore, pathLen + 1, isCollapsedPath, false);
                    }
                }
            }
        }
    }

    private Candidate remove(Candidate currSentence, Candidate best) {
        double temp = currSentence.gain;
        if (best.gain < currSentence.gain && best.level <= currSentence.level) {
            best.discard = true;
            best = currSentence;
        } else {
            currSentence.discard = true;
        }

        return best;
    }

    private HashSet<Candidate> removeDuplicates(HashSet<Candidate> set, boolean isIntermediate) {
        HashSet<Candidate> finalSentences = new HashSet();
        if (CONFIG_TURN_ON_DUP_ELIM) {
            List<Candidate> list = new ArrayList(set);

            for(int i = 0; i < list.size(); ++i) {
                Candidate info = (Candidate)list.get(i);
                info.discard = false;
                List<Node> nl = this.getNodeList(info.sent);
                info.theNodeList = nl;
            }

            boolean startFrom = false;

            for(int a = 0; a < list.size(); ++a) {
                if (!((Candidate)list.get(a)).discard) {
                    Candidate prevSentence = (Candidate)list.get(a);
                    Candidate best = (Candidate)list.get(a);

                    for(int b = 0; b < list.size(); ++b) {
                        if (!((Candidate)list.get(b)).discard && a != b) {
                            Candidate currSentence = (Candidate)list.get(b);
                            double overlap = this.computeCandidateSimScore(currSentence, best);
                            if (isIntermediate) {
                                if (overlap > CONFIG_DUPLICATE_COLLAPSE_THRESHOLD) {
                                    best = this.remove(currSentence, best);
                                }
                            } else if (overlap > CONFIG_DUPLICATE_THRESHOLD) {
                                best = this.remove(currSentence, best);
                            }
                        }
                    }

                    finalSentences.add(best);
                    best.discard = true;
                }
            }
        } else {
            finalSentences = set;
        }

        return finalSentences;
    }

    private boolean traverse(Node x, List<int[]> overlapList, String str, double pathScore, int pathLength, boolean isCollapsedCandidate, boolean overlapSame) {
        if (!this.shouldContinueTraverse(x, overlapList, pathLength, pathScore)) {
            return true;
        } else if (this.isVEN(x, pathLength, isCollapsedCandidate) && this.processVEN(x, pathLength, overlapList, isCollapsedCandidate, str, pathScore)) {
            return true;
        } else {
            this.processNext(x, str, overlapList, pathScore, pathLength, isCollapsedCandidate);
            return true;
        }
    }

    private boolean processVEN(Node x, int pathLength, List<int[]> theNodeList, boolean isCollapsedCandidate, String str, double pathScore) {
        String theCandidateStr = str;
        int thePathLen = pathLength;
        double theScore = pathScore;
        if (this.isEndToken(x)) {
            theCandidateStr = theCandidateStr.substring(0, theCandidateStr.lastIndexOf(" "));
            thePathLen = pathLength - 1;
        }

        double theAdjustedScore = this.computeAdjustedScore(theScore, thePathLen);

        // TODO: Adicionar if de 'algo' aqui
        if (this.isValidCandidate(this.mAnchor + " " + theCandidateStr)) {
            if (isCollapsedCandidate) {
                Candidate cc = (Candidate)this.ccList.get(theCandidateStr);
                int ccPathLength = thePathLen - this.mAnchorPathLen;
                double ccPathScore = theScore - this.mAnchorPathScore;
                if (cc != null) {
                    cc.gain = Math.max(cc.gain, theAdjustedScore);
                } else {
                    cc = new Candidate(theAdjustedScore, theCandidateStr, theNodeList, ccPathLength, ccPathScore, 0.0 - this.beforeAttachGain);
                    this.ccList.put(theCandidateStr, cc);
                }

                return true;
            }

            this.shortlisted.add(new Candidate(theAdjustedScore, theCandidateStr, theNodeList, thePathLen));
        }

        return false;
    }

    public SimpleDirectedGraph<Node, DefaultWeightedEdge> getGraph() {
        return this.mGraph;
    }

    public abstract double computeAdjustedScore(double var1, int var3);

    public abstract double computeScore(double var1, List<int[]> var3, int var4);

    public abstract double computeCandidateSimScore(Candidate var1, Candidate var2);

    public abstract boolean isEndToken(Node var1);

    public abstract boolean shouldContinueTraverse(Node var1, List<int[]> var2, int var3, double var4);

    public abstract boolean isValidCandidate(String var1);

    public abstract boolean isVSN(Node var1);

    public abstract boolean isVEN(Node var1, int var2, boolean var3);

    public abstract List<int[]> getNodeOverlap(List<int[]> var1, List<int[]> var2);
}
