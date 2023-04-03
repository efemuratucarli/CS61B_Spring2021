package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author Efe Murat Uçarlı
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;
        changed = false;
        board.setViewingPerspective(side);
        int total_size = board.size();

        for(int c = total_size - 1; c >= 0; c--){
            for(int r = 0; r < total_size; r++){
                if((board.tile(c,r) != null)) {
                    boolean isChanged = processColumn(c,total_size - 1,total_size);

                    if(isChanged){
                        changed = true;
                        break;
                    }
                    break;
                }
                }
            }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        board.setViewingPerspective(Side.NORTH);
        return changed;
    }

    public boolean processColumn(int columnIndex,int currentRowIndex,int numberOfRows) {
        Tile t;
        boolean change = false;
        boolean notUpdated = true;

        for (int r = currentRowIndex; r >= 0; r--) {
            if ((r == numberOfRows - 1) || board.tile(columnIndex, r) == null) {
            }

            else {
                t = board.tile(columnIndex, r);
                if ((board.tile(columnIndex, numberOfRows - 1) != null) && notUpdated) {
                    int destRow = destinationRow(columnIndex,r,numberOfRows);
                    if(destRow == r){
                        continue;
                    }

                    else{
                        if(board.tile(columnIndex,destRow) == null){
                            board.move(columnIndex, destinationRow(columnIndex,r,numberOfRows), t);
                            notUpdated = false;
                            change = true;
                        }

                        else{
                            board.move(columnIndex, destinationRow(columnIndex,r,numberOfRows), t);
                            notUpdated = false;
                            this.score = this.score + (2 * t.value());
                            change = true;
                        }
                    }
                }

                else if(!notUpdated){
                    processColumn(columnIndex,numberOfRows - 2,numberOfRows - 1);
                }

                else{
                    board.move(columnIndex, numberOfRows - 1, t);
                    change = true;
                }
            }
        }
        return change;
    }


    public int destinationRow(int columnIndex,int rowIndex,int numberOfRows){
        Tile t;
        t = board.tile(columnIndex,rowIndex);
        int destination_row = rowIndex;

        for (int r = rowIndex + 1; r < numberOfRows; r++) {
            if(board.tile(columnIndex,r) == null){
                destination_row = r;
            }

            if(board.tile(columnIndex,r) != null && board.tile(columnIndex,r).value() != t.value()){
                return destination_row;
            }

            else{
                destination_row = r;
            }
        }
            return destination_row;
        }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        int total_size = b.size();

        for(int i = 0; i < total_size; i++){
            for(int j = 0; j < total_size; j++){
                if((b.tile(j,i) == null)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        int total_size = b.size();

        for(int i = 0; i < total_size; i++){
            for(int j = 0; j < total_size; j++){
                if((b.tile(j,i) != null) && b.tile(j,i).value() == MAX_PIECE){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        if(emptySpaceExists(b)){
            return true;
        }

        else{
            int total_size = b.size();

            for(int i = 0; i < total_size; i++){
                for(int j = 0; j < total_size; j++){
                    int current_value = b.tile(j,i).value();
                    int number_of_adjacent_tiles = 0;
                    int left_value;
                    int right_value;
                    int up_value;
                    int down_value;

                    if(i != 0){
                        up_value = b.tile(j,i-1).value();

                        if(up_value == current_value){
                            return true;
                        }
                    }

                    if(j != 0){
                        left_value = b.tile(j-1,i).value();

                        if(left_value == current_value){
                            return true;
                        }
                    }

                    if(i != total_size - 1){
                        down_value = b.tile(j,i+1).value();

                        if(down_value == current_value){
                            return true;
                        }
                    }

                    if(j != total_size - 1) {
                        right_value = b.tile(j + 1, i).value();

                        if(right_value == current_value){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}