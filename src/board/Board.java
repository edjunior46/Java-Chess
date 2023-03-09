package board;

import pieces.Pieces;

public class Board {

    private static Integer ROWS = 8;
    private static Integer COLUMNS = 8;

    private Pieces piece;

    public Board() {

    }

    public boolean getPiece(Board[][] board, Integer row, Integer column) {
        if (board[row][column] != null) {
            return true;
        } else {
            return false;
        }
    }

    public Board[][] buildBoard() {
        return new Board[Board.ROWS][Board.COLUMNS];
    }

}
