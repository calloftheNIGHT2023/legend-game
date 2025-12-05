package legends.world;

import java.util.Random;
import legends.party.Party;

/**
 * Represents the game world map with various tile types.
 * Features:
 * - Procedural map generation with boss placement
 * - Color-coded danger gradient based on distance to boss
 * - Distinct tile types: COMMON, MARKET, INACCESSIBLE, BOSS
 * - ANSI color terminal display with legend
 */
public class WorldMap {
    private final int rows;
    private final int cols;
    private final TileType[][] grid;

    private static final String RESET = "\u001B[0m";
    private static final String BG_RED = "\u001B[41m";
    private static final String BG_GREEN = "\u001B[42m";
    private static final String BG_BLUE = "\u001B[44m";
    private static final String BG_YELLOW = "\u001B[43m";
    private static final String BG_CYAN = "\u001B[46m";
    private static final String BG_BLACK = "\u001B[40m";
    private static final String BG_MAGENTA = "\u001B[45m";
    private static final String FG_WHITE = "\u001B[37m";

    public WorldMap(int rows, int cols, TileType[][] grid) {
        this.rows = rows;
        this.cols = cols;
        this.grid = grid;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /**
     * Generate a random map with boss placement and accessibility validation.
     * Algorithm:
     * 1. Random initial fill (18% inaccessible, 20% market, 62% common)
     * 2. Place boss in bottom-right quadrant
     * 3. Ensure connectivity by carving paths to isolated tiles
     */
    public static WorldMap generateRandom(int rows, int cols, Random rng) {
        TileType[][] g = new TileType[rows][cols];
        // Initial random fill (walls/markets/common)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = rng.nextDouble();
                if (x < 0.18) {
                    g[r][c] = TileType.INACCESSIBLE;
                } else if (x < 0.38) {
                    g[r][c] = TileType.MARKET;
                } else {
                    g[r][c] = TileType.COMMON;
                }
            }
        }

        // Place boss on a diagonal cell (prefer bottom-right quadrant)
        int br = rows - 1;
        int bc = cols - 1;
        int bossR = br;
        int bossC = bc;
        // ensure boss not on edge wall by nudging slightly toward center when small
        if (rows > 3 && cols > 3) {
            bossR = Math.max(1, rows - 2);
            bossC = Math.max(1, cols - 2);
        }
        g[bossR][bossC] = TileType.BOSS;

        // Ensure connectivity: carve paths so that all non-INACCESSIBLE tiles are reachable
        ensureConnectivity(g);

        return new WorldMap(rows, cols, g);
    }

    // Carve connections between components so no accessible tile is isolated from others
    private static void ensureConnectivity(TileType[][] g) {
        int rows = g.length;
        int cols = g[0].length;

        // find any initial accessible tile to start flood fill
        int sr = -1, sc = -1;
        for (int r = 0; r < rows && sr == -1; r++) {
            for (int c = 0; c < cols; c++) {
                if (g[r][c] != TileType.INACCESSIBLE) {
                    sr = r; sc = c; break;
                }
            }
        }
        if (sr == -1) {
            // make (0,0) accessible
            g[0][0] = TileType.COMMON;
            sr = 0; sc = 0;
        }

        boolean[][] seen = new boolean[rows][cols];
        java.util.Queue<int[]> q = new java.util.ArrayDeque<>();
        q.add(new int[] { sr, sc });
        seen[sr][sc] = true;
        int[][] dirs = new int[][] { {1,0},{-1,0},{0,1},{0,-1} };
        while (!q.isEmpty()) {
            int[] cur = q.remove();
            int r = cur[0], c = cur[1];
            for (int[] d : dirs) {
                int nr = r + d[0], nc = c + d[1];
                if (nr < 0 || nr >= rows || nc < 0 || nc >= cols) continue;
                if (seen[nr][nc]) continue;
                if (g[nr][nc] == TileType.INACCESSIBLE) continue;
                seen[nr][nc] = true;
                q.add(new int[] { nr, nc });
            }
        }

        // any tile that is non-INACCESSIBLE but not seen => connect it by carving
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (g[r][c] != TileType.INACCESSIBLE && !seen[r][c]) {
                    // carve a simple path from nearest seen cell to (r,c)
                    // find nearest seen cell via simple search
                    int[] nearest = findNearestSeen(seen, r, c);
                    if (nearest != null) {
                        int rr = nearest[0], cc = nearest[1];
                        // carve horizontal then vertical
                        int stepR = rr < r ? 1 : -1;
                        for (int x = rr; x != r + stepR; x += stepR) {
                            if (g[x][cc] == TileType.INACCESSIBLE) g[x][cc] = TileType.COMMON;
                            seen[x][cc] = true;
                        }
                        int stepC = cc < c ? 1 : -1;
                        for (int y = cc; y != c + stepC; y += stepC) {
                            if (g[r][y] == TileType.INACCESSIBLE) g[r][y] = TileType.COMMON;
                            seen[r][y] = true;
                        }
                    } else {
                        // as a fallback, just make this tile common
                        g[r][c] = TileType.COMMON;
                        seen[r][c] = true;
                    }
                }
            }
        }
    }

    private static int[] findNearestSeen(boolean[][] seen, int r, int c) {
        int rows = seen.length; int cols = seen[0].length;
        int bestR = -1, bestC = -1; int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < rows; i++) for (int j = 0; j < cols; j++) {
            if (seen[i][j]) {
                int d = Math.abs(i - r) + Math.abs(j - c);
                if (d < bestDist) { bestDist = d; bestR = i; bestC = j; }
            }
        }
        if (bestR == -1) return null;
        return new int[] { bestR, bestC };
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public TileType getTileType(int r, int c) {
        return grid[r][c];
    }

    public int[] findAnyAccessible() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != TileType.INACCESSIBLE) {
                    return new int[]{ r, c };
                }
            }
        }
        return new int[]{ 0, 0 };
    }

    /**
     * Find any accessible (non-inaccessible and non-boss) tile suitable for player spawn.
     */
    public int[] findAnyAccessibleNonBoss() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] != TileType.INACCESSIBLE && grid[r][c] != TileType.BOSS) {
                    return new int[]{ r, c };
                }
            }
        }
        return findAnyAccessible();
    }

    /**
     * Print the map to console with color-coded tiles and hero position.
     * Danger gradient (blue->cyan->yellow) based on distance to boss.
     */
    public void print(Party party) {
        legends.LegendsGame.getGlobalIO().println("World Map:");
        // column headers
        StringBuilder header = new StringBuilder();
        header.append("   ");
        for (int c = 0; c < cols; c++) {
            header.append(String.format(" %2d", c));
        }
        legends.LegendsGame.getGlobalIO().println(header.toString());

        for (int r = 0; r < rows; r++) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%2d ", r));
            for (int c = 0; c < cols; c++) {
                boolean isHero = (r == party.getRow() && c == party.getCol());
                TileType t = grid[r][c];
                String cell;
                if (isHero) {
                    // Distinct hero overlay: dark background + white "H"
                    if (t == TileType.BOSS) {
                        cell = BG_MAGENTA + FG_WHITE + "HB" + RESET; // standing on boss
                    } else {
                        cell = BG_BLACK + FG_WHITE + "H " + RESET;
                    }
                } else if (t == TileType.INACCESSIBLE) {
                    cell = BG_RED + " X" + RESET;
                } else if (t == TileType.MARKET) {
                    cell = BG_GREEN + " $" + RESET;
                } else if (t == TileType.BOSS) {
                    cell = BG_MAGENTA + " B" + RESET; // boss tile
                } else {
                    // For common tiles, show a danger gradient based on distance to boss
                    int[] boss = findBossPosition();
                    if (boss[0] >= 0) {
                        int maxDist = (rows - 1) + (cols - 1);
                        int d = Math.abs(r - boss[0]) + Math.abs(c - boss[1]);
                        double closeness = 1.0 - ((double) d / (double) Math.max(1, maxDist));
                        // Use distinct backgrounds that do not overlap with Market/Inaccessible:
                        // low = BLUE, mid = CYAN, high = YELLOW
                        if (closeness >= legends.Config.DANGER_THRESHOLD_HIGH) {
                            cell = BG_YELLOW + "  " + RESET; // high danger
                        } else if (closeness >= legends.Config.DANGER_THRESHOLD_LOW) {
                            cell = BG_CYAN + "  " + RESET; // medium danger
                        } else {
                            cell = BG_BLUE + "  " + RESET; // low danger
                        }
                    } else {
                        cell = BG_BLUE + "  " + RESET;
                    }
                }
                sb.append("[").append(cell).append("]");
            }
            legends.LegendsGame.getGlobalIO().println(sb.toString());
        }
        printLegend();
    }

    /**
     * Print color-coded legend explaining map symbols and danger levels.
     * Danger gradient indicates proximity to the final boss.
     */
    private void printLegend() {
        legends.LegendsGame.getGlobalIO().println("Legend:");
        legends.LegendsGame.getGlobalIO().println("[" + BG_BLACK + FG_WHITE + "H " + RESET + "] Hero position");
        legends.LegendsGame.getGlobalIO().println("[" + BG_RED + " X" + RESET + "] Inaccessible");
        legends.LegendsGame.getGlobalIO().println("[" + BG_GREEN + " $" + RESET + "] Market");
        legends.LegendsGame.getGlobalIO().println("[" + BG_BLUE + "  " + RESET + "] Common - Low danger (far from boss)");
        legends.LegendsGame.getGlobalIO().println("[" + BG_CYAN + "  " + RESET + "] Common - Medium danger");
        legends.LegendsGame.getGlobalIO().println("[" + BG_YELLOW + "  " + RESET + "] Common - High danger (near boss)");
        legends.LegendsGame.getGlobalIO().println("[" + BG_MAGENTA + " B" + RESET + "] Final Boss");
    }

    public void setTileTypeAt(int r, int c, TileType t) {
        if (inBounds(r, c)) {
            grid[r][c] = t;
        }
    }
    /**
     * Find the boss tile coordinates, or return {-1,-1} if none found.
     */
    public int[] findBossPosition() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == TileType.BOSS) return new int[] { r, c };
            }
        }
        return new int[] { -1, -1 };
    }

    /**
     * Find the nearest accessible tile to the given target coordinates (Manhattan distance).
     */
    public int[] findNearestAccessibleTo(int targetRow, int targetCol) {
        int bestDist = Integer.MAX_VALUE;
        int bestRow = -1;
        int bestCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == TileType.INACCESSIBLE) {
                    continue;
                }
                int dist = Math.abs(r - targetRow) + Math.abs(c - targetCol);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestRow = r;
                    bestCol = c;
                }
            }
        }
        if (bestRow == -1) {
            return findAnyAccessible();
        }
        return new int[]{ bestRow, bestCol };
    }

    /**
     * Move the boss tile to the specified location, converting inaccessible tiles if needed.
     */
    public void moveBossTo(int row, int col) {
        int[] current = findBossPosition();
        if (current[0] >= 0 && current[1] >= 0) {
            grid[current[0]][current[1]] = TileType.COMMON;
        }
        if (!inBounds(row, col)) {
            return;
        }
        if (grid[row][col] == TileType.INACCESSIBLE) {
            grid[row][col] = TileType.COMMON;
        }
        grid[row][col] = TileType.BOSS;
    }

}
