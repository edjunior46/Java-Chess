package aplication;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import chess.ChessException;
import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

public class App {
    
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ChessMatch chessMatch = new ChessMatch();

        List<ChessPiece> captured = new ArrayList<>();

        while (!chessMatch.getCheckMate()) {
            try {
                UI.clearScreen();

                UI.printMatch(chessMatch, captured);
                System.out.println();

                System.out.print("Source: ");
                ChessPosition to = UI.readChessPosition(sc);

                boolean[][] possibleMoves = chessMatch.possibleMoves(to);
                UI.clearScreen();

                UI.printBoard(chessMatch.getPieces(), possibleMoves);

                System.out.println();

                System.out.print("Target: ");
                ChessPosition from = UI.readChessPosition(sc);

                ChessPiece capturedPiece = chessMatch.chessMove(to, from);

                if (capturedPiece != null) {
                    captured.add(capturedPiece);
                }

                if (chessMatch.getPromoted() != null) {
                    System.out.println("Enter piece for promotion (B/H/R/Q): ");
                    String piece = sc.nextLine().toUpperCase();
                    while (!piece.equals("B") && !piece.equals("H") && !piece.equals("R") && !piece.equals("Q")) {
                        System.out.println("Invalid value! Enter piece for promotion (B/H/R/Q): ");
                        piece = sc.nextLine().toUpperCase();
                    }
                    chessMatch.replacePromotedPiece(piece);
                }

            } catch (ChessException e) {
                System.out.println(e.getMessage());
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println(e.getMessage());
                sc.nextLine();
            }
        }

        UI.clearScreen();
        UI.printMatch(chessMatch, captured);
        System.out.println();

    }

}
