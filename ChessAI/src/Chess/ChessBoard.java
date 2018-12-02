package Chess;

import Chess.pieces.*;
import helpers.StringColor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingDeque;

public class ChessBoard {
    private ChessPiece[][] board;

    public List<ChessPiece> WHITE_PIECES;
    public ChessPiece WHITE_KING;
    public ChessPiece WHITE_QUEEN;
    public List<ChessPiece> WHITE_PAWNS;
    public List<ChessPiece> WHITE_ROOKS;
    public List<ChessPiece> WHITE_BISHOPS;
    public List<ChessPiece> WHITE_KNIGHTS;

    public List<ChessPiece> BLACK_PIECES;
    public ChessPiece BLACK_KING;
    public ChessPiece BLACK_QUEEN;
    public List<ChessPiece> BLACK_PAWNS;
    public List<ChessPiece> BLACK_ROOKS;
    public List<ChessPiece> BLACK_BISHOPS;
    public List<ChessPiece> BLACK_KNIGHTS;

    public ChessColor move;
    public ChessColor winner;
    public ChessGameStatus status;
    public List<ChessMove> history;

    public ChessPiece[][] getBoard() {
        return this.board;
    }

    public boolean noSync() {
        for (ChessPiece p : WHITE_PIECES) {
            if (!p.equals(this.getChessPiece(p.position))) {
                System.out.println("Piece " + p.toString() + " " + p.position.toString() + " not found on board");
                return false;
            }
        }
        for (ChessPiece p : BLACK_PIECES) {
            if (!p.equals(this.getChessPiece(p.position))) {
                System.out.println("Piece " + p.toString() + " " + p.position.toString() + " not found on board");
                return false;
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece p = this.getChessPiece(new ChessPosition(i, j));
                if (p != null) {
                    if (!WHITE_PIECES.contains(p) && !BLACK_PIECES.contains(p)) {
                        System.out.println("Piece " + p.toString() + " " + p.position.toString() + " not found in list");
                        return false;
                    }
                } else {
                    for (ChessPiece a : WHITE_PIECES) {
                        if (a.position.equals(new ChessPosition(i, j))) {
                            System.out.println("Piece " + p.toString() + " " + p.position.toString() + " found, but board empty");
                            return false;
                        }
                    }
                    for (ChessPiece a : BLACK_PIECES) {
                        if (a.position.equals(new ChessPosition(i, j))) {
                            System.out.println("Piece " + p.toString() + " " + p.position.toString() + " found, but board empty");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public ChessBoard() {
        this.history = new ArrayList<>();
        this.move = ChessColor.WHITE;
        this.status = ChessGameStatus.INGAME;
        board = new ChessPiece[8][8];
        WHITE_KING = new King(ChessColor.WHITE, new ChessPosition(4, 7), this);
        WHITE_QUEEN = new Queen(ChessColor.WHITE, new ChessPosition(3, 7), this);
        WHITE_PAWNS = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            WHITE_PAWNS.add(new Pawn(ChessColor.WHITE, new ChessPosition(i, 6), this));
        }
        WHITE_ROOKS = new ArrayList<>();
        {
            WHITE_ROOKS.add(new Rook(ChessColor.WHITE, new ChessPosition(0, 7), this));
            WHITE_ROOKS.add(new Rook(ChessColor.WHITE, new ChessPosition(7, 7), this));
        }
        WHITE_BISHOPS = new ArrayList<>();
        {
            WHITE_BISHOPS.add(new Bishop(ChessColor.WHITE, new ChessPosition(2, 7), this));
            WHITE_BISHOPS.add(new Bishop(ChessColor.WHITE, new ChessPosition(5, 7), this));
        }
        WHITE_KNIGHTS = new ArrayList<>();
        {
            WHITE_KNIGHTS.add(new Knight(ChessColor.WHITE, new ChessPosition(1, 7), this));
            WHITE_KNIGHTS.add(new Knight(ChessColor.WHITE, new ChessPosition(6, 7), this));
        }
        {
            WHITE_PIECES = new ArrayList<>();
            WHITE_PIECES.add(WHITE_KING);
            WHITE_PIECES.add(WHITE_QUEEN);
            WHITE_PIECES.addAll(WHITE_PAWNS);
            WHITE_PIECES.addAll(WHITE_ROOKS);
            WHITE_PIECES.addAll(WHITE_BISHOPS);
            WHITE_PIECES.addAll(WHITE_KNIGHTS);
        }

        BLACK_KING = new King(ChessColor.BLACK, new ChessPosition(4, 0), this);
        BLACK_QUEEN = new Queen(ChessColor.BLACK, new ChessPosition(3, 0), this);
        BLACK_PAWNS = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            BLACK_PAWNS.add(new Pawn(ChessColor.BLACK, new ChessPosition(i, 1), this));
        }
        BLACK_ROOKS = new ArrayList<>();
        {
            BLACK_ROOKS.add(new Rook(ChessColor.BLACK, new ChessPosition(0, 0), this));
            BLACK_ROOKS.add(new Rook(ChessColor.BLACK, new ChessPosition(7, 0), this));
        }
        BLACK_BISHOPS = new ArrayList<>();
        {
            BLACK_BISHOPS.add(new Bishop(ChessColor.BLACK, new ChessPosition(2, 0), this));
            BLACK_BISHOPS.add(new Bishop(ChessColor.BLACK, new ChessPosition(5, 0), this));
        }
        BLACK_KNIGHTS = new ArrayList<>();
        {
            BLACK_KNIGHTS.add(new Knight(ChessColor.BLACK, new ChessPosition(1, 0), this));
            BLACK_KNIGHTS.add(new Knight(ChessColor.BLACK, new ChessPosition(6, 0), this));
        }
        {
            BLACK_PIECES = new ArrayList<>();
            BLACK_PIECES.add(BLACK_KING);
            BLACK_PIECES.add(BLACK_QUEEN);
            BLACK_PIECES.addAll(BLACK_PAWNS);
            BLACK_PIECES.addAll(BLACK_ROOKS);
            BLACK_PIECES.addAll(BLACK_BISHOPS);
            BLACK_PIECES.addAll(BLACK_KNIGHTS);
        }
    }

    public void applyChessMove(ChessMove cm) {
        if (cm.moved.color == this.move && this.status == ChessGameStatus.INGAME) {
            this.move = cm.moved.color == ChessColor.WHITE ? ChessColor.BLACK : ChessColor.WHITE;
            ChessPiece oldPiece = this.getChessPiece(cm.to);
            ChessPiece movedPiece= this.getChessPiece(cm.from);
            List<ChessMove> moves = movedPiece.getPossibleMoves(this, false);
            if (moves.contains(cm)) {
                this.history.add(cm);
                this.setChessPiece(cm.from, null);
                this.setChessPiece(cm.to, movedPiece );
                if (oldPiece != null) {
                    oldPiece.onBoard = false;
                    if (this.move == ChessColor.BLACK) {
                        this.BLACK_PIECES.remove(oldPiece);
                    } else {
                        this.WHITE_PIECES.remove(oldPiece);
                    }
                }

                //Pawn transforms into Queen
                if (movedPiece instanceof Pawn) {
                    if (movedPiece.color == ChessColor.WHITE) {
                        if (movedPiece.position.getY() == 0) {
                            this.WHITE_PIECES.remove(movedPiece);
                            movedPiece.onBoard = false;
                            this.WHITE_PIECES.add(new Queen(ChessColor.WHITE, new ChessPosition(movedPiece.position.getX(), 0), this));
                        }
                    } else {
                        if (movedPiece.position.getY() == 7) {
                            this.BLACK_PIECES.remove(movedPiece);
                            movedPiece.onBoard = false;
                            this.BLACK_PIECES.add(new Queen(ChessColor.BLACK, new ChessPosition(movedPiece.position.getX(), 7), this));
                        }
                    }
                }
                //StaleMate
                if(ChessLogic.getAllPossibleMoves(this,this.move).isEmpty()){
                    this.status=ChessGameStatus.DRAW;
                }
                //CheckMate
                if (ChessLogic.isCheckMate(this)) {
                    this.status = (movedPiece.color == ChessColor.WHITE ? ChessGameStatus.WHITEWIN : ChessGameStatus.BLACKWIN);
                    this.winner =movedPiece.color;
                } else if (this.WHITE_PIECES.size() == 1 && this.BLACK_PIECES.size() == 1) {                    //1-King endgame
                    this.status = ChessGameStatus.DRAW;
                }
            } else {
                //TODO write specific exception
                System.out.println(moves);
                throw new RuntimeException("Illegal Move requested: Piece " + cm.moved.representation + " wants to move to " + cm.to.toString() + " from " + cm.from.toString());
            }
        } else {
            if (this.status == ChessGameStatus.INGAME) {
                throw new RuntimeException("Illegal Move, wrong player!");
            } else {
                throw new RuntimeException("Game is already over!");
            }
        }
    }

    public ChessPiece getChessPiece(ChessPosition cpos) {
        return this.board[cpos.getX()][cpos.getY()];
    }

    public void setChessPiece(ChessPosition cpos, ChessPiece cp) {
        this.board[cpos.getX()][cpos.getY()] = cp;
        if (cp != null) {
            cp.position = cpos;
            cp.onBoard = true;
        }
    }


    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                s += StringColor.RESET;
                if (j == 0) {
                    s += "|";
                }
                s += "\t";
                ChessPiece cp = this.getChessPiece(new ChessPosition(j, i));
                if (cp == null) {
                    s += "";
                } else {
                    if (cp.color == ChessColor.WHITE) {
                        s += StringColor.BLACK;

                    } else {
                        s += StringColor.YELLOW;
                    }
                    s += cp.representation;
                }
                s += StringColor.RESET;
                s += "\t";
                s += "|";
            }
            s += "\n";
        }
        return s;
    }
}
