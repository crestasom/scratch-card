
package com.crestasom.scratchcard.entity;

public class CurrentMatrix {

    private String[][] matrixValues;

    public CurrentMatrix(int row, int col) {
        matrixValues = new String[row][col];
    }

    public void setValue(int x, int y, String value) {
        matrixValues[x][y] = value;
    }

    public String getValue(int x, int y) {
        return matrixValues[x][y];
    }

    @Override
    public String toString() {
        String val = "\n[";
        for (int i = 0; i < matrixValues.length; i++) {
            for (int j = 0; j < matrixValues[i].length; j++) {
                val += matrixValues[i][j];
                val += j != matrixValues[i].length - 1 ? "," : "";
            }
            val += i != matrixValues.length - 1 ? "\n" : "";
        }
        val += "]";
        return val;
    }
}
