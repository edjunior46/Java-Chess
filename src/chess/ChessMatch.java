package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassant;
    private ChessPiece promoted;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();
    
    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initialSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
    }

    public ChessPiece getEnPassant() {
        return enPassant;
    }

    public ChessPiece getPromoted() {
        return promoted;
    }

    public ChessPiece[][] getPieces() {

        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];

        for (int i = 0; i < board.getRows(); i++) {
            for (int j = 0; j < board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }

        return mat;

    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSoucePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece chessMove(ChessPosition toPosition, ChessPosition fromPosition) {
        Position to = toPosition.toPosition();
        Position from = fromPosition.toPosition();

        validateSoucePosition(to);
        validateTargetPosition(to, from);

        Piece captured = move(to, from);

        if (testCheck(currentPlayer)) {
            undoMove(to, from, captured);
            throw new ChessException("You can not put yourself in check");
        }

        ChessPiece movedPiece = (ChessPiece)board.piece(from);

        promoted = null;
        if (movedPiece instanceof Pawn) {
            if (movedPiece.getColor() == Color.WHITE && from.getRow() == 0 || movedPiece.getColor() == Color.BLACK && from.getRow() == 7) {
                promoted = (ChessPiece)board.piece(from);
                promoted = replacePromotedPiece("Q");
            }
        }

        check = (testCheck(opponent(currentPlayer))) ? true : false;

        if (testCheck(opponent(currentPlayer))) {
            checkMate = true;
        } else {
            nextTurn();
        }

        if (movedPiece instanceof Pawn && from.getRow() == to.getRow() - 2 || from.getRow() == to.getRow() + 2) {
            enPassant = movedPiece;
        } else {
            enPassant = null;
        }

        return (ChessPiece)captured;

    }

    public ChessPiece replacePromotedPiece(String piece) {
        if (promoted == null) {
            throw new IllegalStateException("There is no piece to be promoted.");
        }
        if (!piece.equals("B") && !piece.equals("H") && !piece.equals("R") && !piece.equals("Q")) {
            return promoted;
        }

        Position pos = promoted.getChessPosition().toPosition();
        Piece p = board.removePiece(pos);
        piecesOnTheBoard.remove(p);

        ChessPiece newPiece = newPiece(piece, promoted.getColor());
        board.placePiece(newPiece, pos);
        piecesOnTheBoard.add(newPiece);

        return newPiece;

    }

    private ChessPiece newPiece(String piece, Color color) {
        if (piece.equals("B")) return new Bishop(board, color);
        if (piece.equals("H")) return new Knight(board, color);
        if (piece.equals("R")) return new Rook(board, color);
        return new Queen(board, color);
    }

    private Piece move(Position to, Position from) {
        ChessPiece p = (ChessPiece)board.removePiece(to);
        p.increaseMoveCount();
        Piece captured = board.removePiece(from);

        board.placePiece(p, from);

        if (captured != null) {
            piecesOnTheBoard.remove(captured);
            capturedPieces.add(captured);
        }

        if (p instanceof King && from.getColumn() == to.getColumn() + 2) {
            Position toRook = new Position(to.getRow(), to.getColumn() + 3);
            Position fromRook = new Position(to.getRow(), to.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(toRook);
            board.placePiece(rook, fromRook);
            rook.increaseMoveCount();
        }

        if (p instanceof King && from.getColumn() == to.getColumn() - 2) {
            Position toRook = new Position(to.getRow(), to.getColumn() - 4);
            Position fromRook = new Position(to.getRow(), to.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(toRook);
            board.placePiece(rook, fromRook);
            rook.increaseMoveCount();
        }

        if (p instanceof Pawn) {
            if (to.getColumn() != from.getColumn() && captured == null) {
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(from.getRow() + 1, from.getColumn());
                } else {
                    pawnPosition = new Position(from.getRow() - 1, from.getColumn());
                }
                captured = board.removePiece(pawnPosition);
                capturedPieces.add(captured);
                piecesOnTheBoard.remove(captured);
            }
        }

        return captured;
    }

    private void undoMove(Position to, Position from, Piece capturedPiece) {
        ChessPiece p = (ChessPiece)board.removePiece(from);
        p.decreaseMoveCount();
        board.placePiece(p, to);

        if (capturedPiece != null) {
            board.placePiece(capturedPiece, from);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        if (p instanceof King && from.getColumn() == to.getColumn() + 2) {
            Position toRook = new Position(to.getRow(), to.getColumn() + 3);
            Position fromRook = new Position(to.getRow(), to.getColumn() + 1);
            ChessPiece rook = (ChessPiece)board.removePiece(fromRook);
            board.placePiece(rook, toRook);
            rook.decreaseMoveCount();
        }

        if (p instanceof King && from.getColumn() == to.getColumn() - 2) {
            Position toRook = new Position(to.getRow(), to.getColumn() - 4);
            Position fromRook = new Position(to.getRow(), to.getColumn() - 1);
            ChessPiece rook = (ChessPiece)board.removePiece(fromRook);
            board.placePiece(rook, toRook);
            rook.decreaseMoveCount();
        }

        if (p instanceof Pawn) {
            if (to.getColumn() != from.getColumn() && capturedPiece == enPassant) {
                ChessPiece pawn = (ChessPiece)board.removePiece(from);
                Position pawnPosition;
                if (p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, from.getColumn());
                } else {
                    pawnPosition = new Position(4, from.getColumn());
                }

                board.placePiece(pawn, pawnPosition);

            }
        }
    }

    private void validateSoucePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("There is no piece on source position");
        }
        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
            throw new ChessException("The chosen piece is not yours.");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("There is no possible moves for the chosen piece.");
        }
    }

    private void validateTargetPosition(Position to, Position from) {
        if (!board.piece(to).possibleMove(from)) {
            throw new ChessException("The chosen piece can't move to target position.");
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).collect(Collectors.toList());
        for (Piece p : list) {
            if (p instanceof King) {
                return (ChessPiece)p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board.");
    }

    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());
        for (Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }

    public boolean testCheckMate(Color color) {
        if (!testCheck(color)) {
            return false;
        }

        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == opponent(color)).collect(Collectors.toList());

        for (Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            for (int i = 0; i < board.getRows(); i++) {
                for (int j = 0; j < board.getColumns(); j++) {
                    if (mat[i][j]) {
                        Position to = ((ChessPiece)p).getChessPosition().toPosition();
                        Position from = new Position(i, j);
                        Piece captured = move(to, from);
                        boolean testCheck = testCheck(color);
                        undoMove(to, from, captured);
                        if (!testCheck) {
                            return false;
                        }
                    }
                }
            }
            return false;
        }

        return true;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }
    
    private void initialSetup() {
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));
        

        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
        

    }
    
}
