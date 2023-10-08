package eu.sergehelfrich.prisoner;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 *
 * @author helfrich
 */
public class World {

    private final DoubleProperty b = new SimpleDoubleProperty(1.85);
    private final DoubleProperty p = new SimpleDoubleProperty(0.1);
    private final int n = 60; // board width / height
    private int[] bc; //boundary
    private final double[][] pm = new double[3][3];  // payoff matrix
    private int[][] s; // generation
    private int[][] sn; // next generation
    private double[][] payoff;

    private boolean newWorld;
    
    private long generation;

    public long getGeneration() {
        return generation;
    }

    public DoubleProperty getB() {
        return b;
    }

    public DoubleProperty getP() {
        return p;
    }

    public boolean isNewWorld() {
        return newWorld;
    }

    public void init() {
        /* We use 1-based arrays for convenience, so we can always go left:
        the boundary cell array resolves the value at 0 to n.
         */

        newWorld = true;

        pm[1][1] = 1;
        pm[1][2] = 0;
        pm[2][1] = b.get();
        pm[2][2] = 0;
        setBoundary();

        s = new int[n + 1][n + 1];
        sn = new int[n + 1][n + 1];
        payoff = new double[n + 1][n + 1];

        generate();
    }

    public synchronized void update() {
        newWorld = false;
        generation++;
        int i, j, k, l;
        double pa, hp;
        for (i = 1; i <= n; i++) {
            for (j = 1; j <= n; j++) {
                pa = 0;
                for (k = -1; k <= 1; k++) {
                    for (l = -1; l <= 1; l++) {
                        pa += pm[s[i][j]][s[bc[i + k]][bc[j + l]]];
                    }
                }
                payoff[i][j] = pa;
            }
        }

        for (i = 1; i <= n; i++) {
            for (j = 1; j <= n; j++) {
                hp = payoff[i][j];
                sn[i][j] = s[i][j];
                for (k = -1; k <= 1; k++) {
                    for (l = -1; l <= 1; l++) {
                        if (payoff[bc[i + k]][bc[j + l]] > hp) {
                            hp = payoff[bc[i + k]][bc[j + l]];
                            sn[i][j] = s[bc[i + k]][bc[j + l]];
                        }
                    }
                }
            }
        }
    }

    public synchronized void nextGeneration() {
        newWorld = false;
        // next generation to current
        // deep array copy!
        for (int i = 1; i <= n; i++) {
            System.arraycopy(sn[i], 1, s[i], 1, n);
        }
    }

    public int[][] getS() {
        return s;
    }

    public int[][] getSn() {
        return sn;
    }

    private synchronized void generate() {
        generation = 0;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= n; j++) {
                s[i][j] = 1;
                if (Math.random() < p.get()) {
                    s[i][j] = 2;
                }
            }
        }
    }

    private void setBoundary() {
        bc = new int[n + 2];
        for (int i = 1; i <= n; i++) {
            bc[i] = i;
        }
        bc[0] = n;
        bc[n + 1] = 1;
    }

    public int getN() {
        return n;
    }

}
