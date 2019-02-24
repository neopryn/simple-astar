package com.neopryn.astar;

public class Node {
    public Node parent;
    public double g = 0; // Movement cost
    public double h = 0; // Heuristic cost

    public boolean nodeClosed = false;
    public char lastDirectionVector = ' ';

    public double getF() {
        return g + h;
    }
}
