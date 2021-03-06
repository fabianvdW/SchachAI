package chess.pieces;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class King extends ChessPiece {
    public static ChessVector[] minimalUnit = {new ChessVector(0, 1), new ChessVector(1, 1), new ChessVector(1, 0), new ChessVector(1, -1), new ChessVector(0, -1), new ChessVector(-1, -1), new ChessVector(-1, 0), new ChessVector(-1, 1)};

    public King(ChessColor color, ChessPosition position, ChessBoard board) {
        super(color, position, board);
        if (this.color == ChessColor.WHITE) {
            this.representation = "\u2654";
        } else {
            this.representation = "\u265A";
        }
    }

    @Override
    public List<ChessMove> getPossibleMoves(ChessBoard b, boolean pinFlag) {
        List<ChessMove> result = new ArrayList<>();
        if (b.initialized) {
            if (this.color == ChessColor.WHITE) {
                return b.WHITE_MOVES.getOrDefault(this, result);
            } else {
                return b.BLACK_MOVES.getOrDefault(this, result);
            }
        }
        ChessColor enemyColor = this.color == ChessColor.BLACK ? ChessColor.WHITE : ChessColor.BLACK;
        //Check Castle
        if (this.moves == 0 && !ChessLogic.isPositionThreatened(this.position, null, b, enemyColor)) {
            for (int i = 0; i < 2; i++) {
                ChessPiece x = null;
                if (i == 0) {
                    x = b.getChessPiece(new ChessPosition(0, this.position.getY()));
                } else {
                    x = b.getChessPiece(new ChessPosition(7, this.position.getY()));
                }
                if (x instanceof Rook) {
                    Rook r = (Rook) x;
                    if (r.moves == 0) {
                        int xIncrementor = i == 0 ? -1 : 1;
                        ChessPosition pos1 = new ChessPosition(this.position.getX() + xIncrementor, this.position.getY());
                        ChessPosition pos2 = new ChessPosition(this.position.getX() + 2 * xIncrementor, this.position.getY());
                        //Fields free
                        if (b.getChessPiece(pos1) == null && b.getChessPiece(pos2) == null) {
                            if (!ChessLogic.isPositionThreatened(pos1, null, b, enemyColor) && !ChessLogic.isPositionThreatened(pos2, null, b, enemyColor)) {
                                result.add(new CastleMove(this.position.clone(), pos2, this, null, r));
                            }
                        }
                    }
                }
            }
        }
        //Check normal Moves
        for (int i = 0; i < King.minimalUnit.length; i++) {
            ChessVector cv = King.minimalUnit[i];
            ChessPosition cp = this.position.addChessVector(cv);
            if (cp == null) {
                continue;
            }
            ChessPiece cPiece = b.getChessPiece(cp);
            ChessMove cm = new ChessMove(this.position.clone(), cp, this, cPiece);
            if (cPiece != null && cPiece.color != enemyColor) {
                continue;
            }
            if (!pinFlag && ChessLogic.isPositionThreatened(cm.to, cm, b, enemyColor)) {
                continue;
            }
            result.add(cm);
        }


        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof King) {
            King b = (King) o;
            return b.position.equals(this.position);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return (this.position.getX()+this.position.getY()*8)*(this.color==ChessColor.WHITE?2:1);
    }
}
