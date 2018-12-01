package Chess;

import Chess.pieces.ChessPiece;
import Chess.pieces.King;

import java.util.ArrayList;
import java.util.List;

public class ChessLogic {
    public static boolean isValidX(int x) {
        return x >= 0 && x < 8;
    }

    public static boolean isValidY(int y) {
        return y >= 0 && y < 8;
    }

    public static boolean isThreatened(ChessMove cm, ChessBoard cb, ChessColor enemyColor) {
        List<ChessPiece> enemyPieces = (enemyColor == ChessColor.WHITE) ? cb.WHITE_PIECES : cb.BLACK_PIECES;
        ChessPosition cpNew= cm.to;

        ChessPiece chessPiece = cb.getChessPiece(cpNew);
        applyChessMoveToBoardWithoutLogic(cm,cb);
        for (int i = 0; i < enemyPieces.size(); i++) {
            ChessPiece cPiece = enemyPieces.get(i);
            if (cPiece.onBoard) {
                if (cPiece instanceof King) {
                    if (Math.abs(cPiece.position.getX() - cpNew.getX()) <= 1 && Math.abs(cPiece.position.getY() - cpNew.getY()) <= 1) {
                        reverseChessMoveToBoardWithoutLogic(cm,cb);
                        return true;
                    }
                } else {
                    if(threatensPosition(cPiece,cb,cpNew)){
                        reverseChessMoveToBoardWithoutLogic(cm,cb);
                        return true;
                    }
                }
            }
        }
        reverseChessMoveToBoardWithoutLogic(cm,cb);
        return false;
    }
    public static List<ChessPiece> getThreats(ChessPosition cpos, ChessBoard cb, ChessColor enemyColor){
        ArrayList<ChessPiece> result = new ArrayList<>();
        List<ChessPiece> enemyPieces= (enemyColor==ChessColor.WHITE) ? cb.WHITE_PIECES: cb.BLACK_PIECES;
        for(ChessPiece cPiece: enemyPieces){
            if(threatensPosition(cPiece,cb,cpos)){
                result.add(cPiece);
            }
        }
        return result;
    }
    public static boolean threatensPosition(ChessPiece cPiece,ChessBoard cb, ChessPosition cpos){
        for (ChessMove cMove : cPiece.getPossibleMoves(cb,true)) {
            ChessPosition cPos= cMove.to;
            if (cPos.equals(cpos)) {
                return true;
            }
        }
        return false;
    }

    public static void applyChessMoveToBoardWithoutLogic(ChessMove cm, ChessBoard cb){
        cb.setChessPiece(cm.from,null);
        cb.setChessPiece(cm.to,cm.moved);
    }

    public static void reverseChessMoveToBoardWithoutLogic(ChessMove cm, ChessBoard cb){
        cb.setChessPiece(cm.to,cm.old);
        cb.setChessPiece(cm.from,cm.moved);
    }

    //this depends on color
    public static List<ChessMove> getAllPossibleMoves(ChessBoard cb,ChessColor color){
        List<ChessPiece> pieces = color==ChessColor.WHITE? cb.WHITE_PIECES: cb.BLACK_PIECES;
        List<ChessMove> moves = new ArrayList<>();
        for(ChessPiece cPiece: pieces){
            if(cPiece.onBoard){
                moves.addAll(cPiece.getPossibleMoves(cb,false));
            }
        }
        return moves;
    }
    public static boolean isPinned(ChessMove cm,ChessBoard cb){
        ChessPiece king = cm.moved.color==ChessColor.WHITE?cb.WHITE_KING:cb.BLACK_KING;
        if(isThreatened(new ChessMove(king.position,king.position,king,king),cb,king.color==ChessColor.WHITE?ChessColor.BLACK:ChessColor.WHITE)){
            return true;
        }
        return false;
    }
    public static boolean isChessMate(ChessBoard cb){
        ChessPiece k=(cb.move==ChessColor.WHITE? cb.WHITE_KING: cb.BLACK_KING);
        ChessColor enemyColor= (k.color==ChessColor.WHITE? ChessColor.BLACK:ChessColor.WHITE);
        List<ChessPiece> threatsToKing= getThreats(k.position,cb,enemyColor);
        if(!threatsToKing.isEmpty()) {
            if (k.getPossibleMoves(cb, false).isEmpty()) {
                for(ChessMove cm : getAllPossibleMoves(cb,cb.move)){
                    if(cm.moved.equals(k)){
                        continue;
                    }
                    applyChessMoveToBoardWithoutLogic(cm,cb);
                    boolean noThreats=true;
                    for(ChessPiece cp:threatsToKing){
                        if(threatensPosition(cp,cb,k.position)){
                            noThreats=false;
                            break;
                        }
                    }
                    reverseChessMoveToBoardWithoutLogic(cm,cb);
                    if(noThreats){
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
