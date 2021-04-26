package org.testshift.testcube.explore;

import org.eclipse.jdt.internal.core.util.Util;

import java.util.ArrayList;
import java.util.List;

public class ExplorationVisJSON {

    private List<Node> nodes;
    private List<Edge> edges;

    public ExplorationVisJSON() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public void addNode(Node node) {
        nodes.add(node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public void removeEdgeWithTarget(String id) {
        edges.removeIf(edge -> {
           return edge.target.equals(id);
        });
    }

    public class Node {
        private String signature;
        private String className;
        private String id;
        public List<Line> lines;
        private boolean addCovered;
        private int nodeLevel;

        public Node() {
            lines = new ArrayList<>();
        }

        public Node setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        public Node setId(String id) {
            this.id = id;
            return this;
        }

        public String getId() {
            return id;
        }

        public Node setAddCovered(boolean addCovered) {
            this.addCovered = addCovered;
            return this;
        }

        public Node setNodeLevel(int nodeLevel) {
            this.nodeLevel = nodeLevel;
            return this;
        }

        public Node setClassName(String className) {
            this.className = className;
            return this;
        }
    }

    public class Line {
        private String code;
        private boolean callsMethod;
        private boolean covered;
        private boolean addCovered;

        public Line() {

        }

        public Line setCode(String code) {
            this.code = code;
            return this;
        }

        public Line setCallsMethod(boolean callsMethod) {
            this.callsMethod = callsMethod;
            return this;
        }

        public Line setCovered(boolean covered) {
            this.covered = covered;
            return this;
        }

        public Line setAddCovered(boolean addCovered) {
            this.addCovered = addCovered;
            return this;
        }
    }

    public class Edge {
        private String source; // id
        private String target;
        private int sourceAnchor; // 0 = top of box, 1..n = left side of n-th line
        private final int targetAnchor = 0;

        public Edge() {

        }

        public Edge setSource(String source) {
            this.source = source;
            return this;
        }

        public Edge setTarget(String target) {
            this.target = target;
            return this;
        }

        public Edge setSourceAnchor(int sourceAnchor) {
            this.sourceAnchor = sourceAnchor;
            return this;
        }
    }
}