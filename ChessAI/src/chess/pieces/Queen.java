package chess.pieces;

import chess.*;

import java.util.ArrayList;
import java.util.List;

public class Queen extends ChessPiece {
    public Queen(ChessColor color, ChessPosition position, ChessBoard board) {
        super(color, position, board);
        if (this.color == ChessColor.WHITE) {
            this.representation = "\u2655";
        } else {
            this.representation = "\u265B";
        }
    }

    @Override
    public List<ChessMove> getPossibleMoves(ChessBoard b, boolean pinFlag) {

        List<ChessMove> result = new ArrayList<>();

        ChessColor enemyColor = this.color == ChessColor.BLACK ? ChessColor.WHITE : ChessColor.BLACK;
        ChessPiece myKing= this.color==ChessColor.WHITE? b.WHITE_KING:b.BLACK_KING;
        for (int i = 0; i < 8; i++) {
            ChessPosition kingPosition = b.WHITE_KING.position;
            int xIncrementor = 0;
            int yIncrementor = 0;
            switch (i) {
                case 0:
                    xIncrementor = 1;
                    yIncrementor = 1;
                    break;
                case 1:
                    xIncrementor = 1;
                    yIncrementor = 0;
                    break;
                case 2:
                    xIncrementor = 1;
                    yIncrementor = -1;
                    break;
                case 3:
                    xIncrementor = 0;
                    yIncrementor = 1;
                    break;
                case 4:
                    xIncrementor = 0;
                    yIncrementor = -1;
                    break;
                case 5:
                    xIncrementor = -1;
                    yIncrementor = 1;
                    break;
                case 6:
                    xIncrementor = -1;
                    yIncrementor = 0;
                    break;
                case 7:
                    xIncrementor = -1;
                    yIncrementor = -1;
                    break;

            }
            int xCoordinate = this.position.getX();
            int yCoordinate = this.position.getY();
            ChessPosition cp = null;
            do {
                xCoordinate += xIncrementor;
                yCoordinate += yIncrementor;
                if (!(ChessLogic.isValidX(xCoordinate) && ChessLogic.isValidY(yCoordinate))) {
                    break;
                }
                cp = new ChessPosition(xCoordinate, yCoordinate);
                ChessPiece cPiece = b.getChessPiece(cp);
                ChessMove cm = new ChessMove(this.position.clone(), cp, this, cPiece);
                if (cPiece != null && cPiece.color != enemyColor) {
                    break;
                }
                if (!pinFlag && ChessLogic.isPositionThreatened(myKing.position,cm, b,enemyColor)) {
                    if(cPiece!=null &&cPiece.color==enemyColor){
                        break;
                    }
                    continue;
                }
                if (cPiece == null) {
                    result.add(cm);
                } else if (cPiece.color == enemyColor) {
                    result.add(cm);
                    break;
                }
            } while (true);

        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Queen) {
            Queen b = (Queen) o;
            return b.position.equals(this.position);
        }
        return false;
    }
}