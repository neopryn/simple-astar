package com.neopryn.astar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        int widthOfGrid = 0;
        int heightOfGrid = 0;
        var gridContents = "";

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(args[0]));
            String line = reader.readLine();

            while (line != null) {
                gridContents = gridContents + line;

                heightOfGrid++;
                if (widthOfGrid == 0) widthOfGrid = line.length();
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Solver solver = new Solver(widthOfGrid, heightOfGrid, gridContents);
        solver.solve();
    }
}
