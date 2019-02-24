package com.neopryn.astar;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class Solver {
    private int width;
    private int height;
    private LinkedList<Node> nodeArray;

    private PriorityQueue<Node> openList;

    private Node startNode;
    private Node goalNode;

    public Solver(int width, int height, String contents) {
        this.width = width;
        this.height = height;
        this.nodeArray = new LinkedList();

        Comparator<Node> nodeCostComparator = Comparator.comparingDouble(Node::getF);
        this.openList = new PriorityQueue(nodeCostComparator);

        var contentAsChars = contents.toCharArray();

        for(char val : contentAsChars) {
            switch(val) {
                case 'X':
                    nodeArray.add(null);
                    break;
                case ' ':
                    nodeArray.add(new Node());
                    break;
                case 'S':
                    startNode = new Node();
                    openList.add(startNode);
                    nodeArray.add(startNode);
                    break;
                case 'O':
                    goalNode = new Node();
                    nodeArray.add(goalNode);
                    break;
            }
        }

        var goalNodeIndex = nodeArray.indexOf(goalNode);
        var goalNodeY = (int) Math.floor(goalNodeIndex / width);
        var goalNodeX = goalNodeIndex % width;

        // Calculate heuristic for each node
        nodeArray.parallelStream().forEach(n -> {
            if (n == null) {
                return;
            }

            var nodeIndex = nodeArray.indexOf(n);
            var nodeY = (int) Math.floor(nodeIndex / width);
            var nodeX = nodeIndex % width;

            // Set euclidean distance as heuristic
            n.h = Math.sqrt(Math.pow(goalNodeX - nodeX, 2) + Math.pow(goalNodeY - nodeY, 2));
        });
    }

    public void solve() {
        var routeFound = false;
        while (!openList.isEmpty() && !routeFound) {
            var nextBestNode = openList.remove();

            if (nextBestNode == goalNode) {
                // Route found, bail
                routeFound = true;
                continue;
            }

            calculateNeighbouringNodes(nextBestNode);
            nextBestNode.nodeClosed = true;
        }

        if (!routeFound) {
            System.out.println("Not reachable.");
        } else {
            var nodesForOptimalPath = recursivelyCollectNodesByParent(goalNode);
            var solutionContents = nodeArray
                    .stream()
                    .map(n -> {
                        if (n == null) {
                            return 'X';
                        } else if (n == startNode) {
                            return 'S';
                        } else if (n == goalNode) {
                            return 'O';
                        } else if (nodesForOptimalPath.contains(n)) {
                            return n.lastDirectionVector;
                        } else {
                            return ' ';
                        }
                    })
                    .map(String::valueOf)
                    .collect(Collectors.joining());

            var solutionRows = solutionContents.split("(?<=\\G.{" + width + "})");

            for (String row : solutionRows) {
                System.out.println(row);
            }
            System.out.println("Cost: " + goalNode.g)
        }

        return;
    }

    private LinkedList<Node> recursivelyCollectNodesByParent(Node node) {
        if (node.parent != null) {
            var parentList = recursivelyCollectNodesByParent(node.parent);
            parentList.add(node);

            return parentList;
        } else {
            var rootList = new LinkedList<Node>();
            rootList.add(node);

            return rootList;
        }

    }

    private void calculateNeighbouringNodes(Node node) {
        var index = this.nodeArray.indexOf(node);

        var nodeY = (int) Math.floor(index / width);
        var nodeX = index % width;

        var xIsMin = nodeX == 0;
        var xIsMax = nodeX == (width - 1);
        var yIsMin = nodeY == 0;
        var yIsMax = nodeY == (height - 1);

        var diagonalCost = Math.sqrt(2);

        Node nodeN = null; Node nodeE = null; Node nodeS = null; Node nodeW = null;
        if (!yIsMin) {
            // N
            nodeN = calculateNodeAt(nodeX + width * (nodeY - 1), 1, '^', node);
        }

        if (!xIsMax) {
            // E
            nodeE = calculateNodeAt((nodeX + 1) + width * nodeY, 1, '>', node);
        }

        if (!yIsMax) {
            // S
            nodeS = calculateNodeAt(nodeX + width * (nodeY + 1), 1, 'v', node);
        }

        if (!xIsMin) {
            // W
            nodeW = calculateNodeAt((nodeX - 1) + width * nodeY, 1, '<', node);
        }

        if (!yIsMin && !xIsMax && (nodeN != null && nodeE != null)) {
            // NE
            calculateNodeAt((nodeX + 1) + width * (nodeY - 1), diagonalCost, '/', node);
        }

        if (!yIsMax && !xIsMax && (nodeS != null && nodeE != null)) {
            // SE
            calculateNodeAt((nodeX + 1) + width * (nodeY + 1), diagonalCost, '\\', node);
        }


        if (!yIsMax && !xIsMin && (nodeS != null && nodeW != null)) {
            // SW
            calculateNodeAt((nodeX - 1) + width * (nodeY + 1), diagonalCost, ',', node);
        }

        if (!xIsMin && !yIsMin && (nodeN != null && nodeW != null)) {
            // NW
            calculateNodeAt((nodeX - 1) + width * nodeY, diagonalCost, '`', node);
        }
    }

    private Node calculateNodeAt(int index, double movementCost, char directionVector, Node parentingNode) {
        Node nodeAtIndex = this.nodeArray.get(index);

        if (nodeAtIndex == null) {
            // Not passable
            return null;
        }

        // Check if node requires processing/re-parenting
        if(!nodeAtIndex.nodeClosed && (nodeAtIndex.g == 0 || nodeAtIndex.g >= (parentingNode.g + movementCost))) {
            // Update node properties
            nodeAtIndex.g = parentingNode.g + movementCost;
            nodeAtIndex.parent = parentingNode;
            nodeAtIndex.lastDirectionVector = directionVector;

            // Add to list of available nodes to search if not already added
            if (!openList.contains(nodeAtIndex)) {
                openList.add(nodeAtIndex);
            }
        }

        return nodeAtIndex;
    }
}
