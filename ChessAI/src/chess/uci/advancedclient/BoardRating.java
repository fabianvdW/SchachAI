package chess.uci.advancedclient;

import chess.ChessGameStatus;
import chess.bitboards.BitBoard;
import chess.bitboards.BitBoardMoves;

public class BoardRating {

    //PAWN constants
    public final static int PAWN_FACTOR=100;
    public final static int ISOLATED_PAWN_MINUS=-2;
    public final static int DOUBLE_PAWN_MINUS=-7;
    public final static double[] PASSED_PAWN_NOT_BLOCKED_BONUS={0,50,100,200,300,400,500,0};
    public final static double[] PASSED_PAWN_BLOCKED_BONUS={0,20,40,80,120,160,200,0};
    public final static double[] ADVANCEMENT_MULTIPLIER_FILE={0,1,2,3,3,2,1,0};
    public final static double[] ADVANCEMENT_MULTIPLIER_RANK={0,0,3,5,7,9,11,0};

    //Bishop constants
    public final static int BISHOP_FACTOR=340;
    public final static int TWO_BISHOP_BONUS=40;
    public final static int BLOCKED_BY_PAWN_PENALTY=-10;

    //Rook constants
    public final static int ROOK_FACTOR=500;
    public final static int SEVENTH_RANK_BONUS=20;
    public final static int FILE_SHARE_BONUS=15;
    public final static int FILE_NO_PAWNS_BONUS=10;
    public final static int FILE_NO_FRIENDLY_PAWNS_BONUS=3;
    public final static double[] KING_TROPISM={30,20,15,10,5,0,0,0};

    //Knight constants
    public final static int KNIGHT_FACTOR=325;
    public final static double[][] CENTRE_CLOSENESS={
            {-14,-11, -8, -5, -5, -8,-11,-14},
            {-11,-11, -5, -1, -1, -5,-11,-11},
            { -8, -5, -1,  3,  3, -1, -5, -8},
            { -5, -1,  3,  7,  7,  3, -1, -5},
            { -5, -1,  3,  7,  7,  3, -1, -5},
            { -8, -5, -1,  3,  3, -1, -5, -8},
            {-11,-11, -5, -1, -1, -5,-11,-11},
            {-14,-11, -8, -5, -5, -8,-11,-14}
    };
    public final static double[] KING_CLOSENESS={0,10.5,10.5,20,10.5,10.5,5,5,5,0,0,0,-7,-7,-7};

    //Queen constants
    public final static int QUEEN_FACTOR=900;
    public final static double KING_CLOSENESS_MULTIPLIER=2;
    public final static double KING_TROPISM_MULTIPLIER=2;
    public static double getBoardRating(BitBoard bb,int depth){
        //1 for pawn, 3 for bishop knight, 5 for rook, 9 for queen -
        if(bb.status!=ChessGameStatus.INGAME){
            if(bb.status==ChessGameStatus.DRAW){
                return 0;
            }else if(bb.status==ChessGameStatus.BLACKWIN){
                return -300-depth;
            }else{
                return 300+depth;
            }
        }
        //https://www.redhotpawn.com/rival/programming/evaluation.php
        long whitePawns=bb.whitePieces[BitBoard.PAWNS];
        long whiteBishops=bb.whitePieces[BitBoard.BISHOPS];
        long whiteRooks=bb.whitePieces[BitBoard.ROOKS];
        long whiteKing=bb.whitePieces[BitBoard.KING];
        long whiteKnight=bb.whitePieces[BitBoard.KNIGHTS];
        long whiteQueens=bb.whitePieces[BitBoard.QUEENS];
        long whitePieces=bb.whitePieces[BitBoard.PAWNS]|bb.whitePieces[BitBoard.KING]|bb.whitePieces[BitBoard.KNIGHTS]|bb.whitePieces[BitBoard.BISHOPS]|bb.whitePieces[BitBoard.ROOKS]|bb.whitePieces[BitBoard.QUEENS];

        long blackPawns=bb.blackPieces[BitBoard.PAWNS];
        long blackBishops=bb.blackPieces[BitBoard.BISHOPS];
        long blackRooks=bb.blackPieces[BitBoard.ROOKS];
        long blackKing=bb.blackPieces[BitBoard.KING];
        long blackKnight=bb.blackPieces[BitBoard.KNIGHTS];
        long blackQueens=bb.blackPieces[BitBoard.QUEENS];
        long blackPieces=bb.blackPieces[BitBoard.PAWNS]|bb.blackPieces[BitBoard.KING]|bb.blackPieces[BitBoard.KNIGHTS]|bb.blackPieces[BitBoard.BISHOPS]|bb.blackPieces[BitBoard.ROOKS]|bb.blackPieces[BitBoard.QUEENS];


        double whitePawnScore=pawnScore(whitePawns,blackPawns,true,whitePieces,blackPieces);
        double whiteBishopScore=bishopScore(whiteBishops,whitePawns,blackPawns);
        double whiteRookScore=rookScore(whiteRooks,blackKing,whitePawns,blackPawns,true);
        double whiteKnightScore=knightScore(whiteKnight,blackKing);
        double whiteQueenScore=queenScore(whiteQueens,blackKing);
        double whiteScore=whitePawnScore+whiteBishopScore+whiteRookScore+whiteKnightScore+whiteQueenScore;

        double blackPawnScore=pawnScore(blackPawns,whitePawns,false,blackPieces,whitePieces);
        double blackBishopScore=bishopScore(blackBishops,blackPawns,whitePawns);
        double blackRookScore=rookScore(blackRooks,whiteKing,blackPawns,whitePawns,false);
        double blackKnightScore=knightScore(blackKnight,whiteKing);
        double blackQueenScore=queenScore(blackQueens,whiteKing);

        //TODO King safety score
        double blackScore=blackPawnScore+blackBishopScore+blackRookScore+blackKnightScore+blackQueenScore;
        return (whiteScore-blackScore)/100.0;
    }
    public static double pawnScore(long myPawns, long enemyPawns,boolean myColor, long myPieces, long enemyPieces){
        double score=0;
        int whitePawnsCount= getPieces(myPawns);
        int doubleWhitePawns=0;
        int isolatedWhitePawns=0;
        double passedScore=0;
        double advancementScore=0;
        //Double stacked pawns get -7
        for(int i=0;i<8;i++){
            if(getPieces(myPawns& BitBoardMoves.FILES[i])>1){
                doubleWhitePawns++;
            }
        }
        long wPClone = myPawns;
        long whitePawn = myPawns & ~(myPawns - 1);
        while (whitePawn != 0) {
            //Isolated pawns
            int pos=63-Long.numberOfTrailingZeros(whitePawn);
            wPClone &= ~whitePawn;
            whitePawn = wPClone & ~(wPClone - 1);
            long files=0L;
            if(pos%8-1>0){
                files|=BitBoardMoves.FILES[pos%8-1];
            }
            if(pos%8+1<8){
                files|=BitBoardMoves.FILES[pos%8+1];
            }
            if(getPieces(myPawns&BitBoardMoves.KING_MOVES[pos]&files)==0){
                isolatedWhitePawns++;
            }

            //Check if pawn is passed
            if((BitBoardMoves.FILES[pos%8]&enemyPawns)==0){
                int rank=pos/8;
                //(6-rank) for black
                double passedAdd;
                if(myColor) {
                    passedAdd = PASSED_PAWN_NOT_BLOCKED_BONUS[7-rank];
                }else{
                    passedAdd= PASSED_PAWN_NOT_BLOCKED_BONUS[rank];
                }
                //Passed pawn is blocked
                if((BitBoardMoves.FILES[pos%8]&enemyPieces)!=0){
                    if(myColor){
                        passedAdd=PASSED_PAWN_BLOCKED_BONUS[7-rank];
                    }else{
                        passedAdd=PASSED_PAWN_BLOCKED_BONUS[rank];
                    }
                }
                passedScore+=passedAdd;
            }

            //Advancement score
            int advancement;
            if(myColor) {
                 advancement = 7 - pos / 8;
            }else{
                advancement=pos/8;
            }
            advancementScore= ADVANCEMENT_MULTIPLIER_RANK[advancement]*ADVANCEMENT_MULTIPLIER_FILE[pos%8];


        }

        score=PAWN_FACTOR*whitePawnsCount+DOUBLE_PAWN_MINUS*doubleWhitePawns+ISOLATED_PAWN_MINUS*isolatedWhitePawns+passedScore+advancementScore;
        return score;
    }
    public static double bishopScore(long myBishops, long myPawns,long enemyPawns){
        int bishops=getPieces(myBishops);
        int twoBishopBonus=bishops==2?TWO_BISHOP_BONUS:0;
        long bishopClone = myBishops;
        long bishop = bishopClone & ~(bishopClone - 1);
        double bishopBlockedPenalty=0;
        while (bishop != 0) {
            int pos=63-Long.numberOfTrailingZeros(bishop);
            //Get diagonally adjacent fields
            long adjacentPawnsOnDiagoanlSquares= BitBoardMoves.KING_MOVES[pos]&~BitBoardMoves.horizontalAndVerticalMoves(pos,0L)&(myPawns|enemyPawns);
            bishopBlockedPenalty=BLOCKED_BY_PAWN_PENALTY*getPieces(adjacentPawnsOnDiagoanlSquares);

            bishopClone &= ~bishop;
            bishop = bishopClone & ~(bishopClone - 1);
        }
        return bishops*BISHOP_FACTOR+twoBishopBonus+bishopBlockedPenalty;
    }
    public static double rookScore(long myRooks,long enemyKing,long myPawns,long enemyPawns,boolean myColor){
        double rookScore=0;
        long rookClone = myRooks;
        long rook = rookClone & ~(rookClone - 1);
        double bishopBlockedPenalty=0;
        while (rook != 0) {
            rookScore+=ROOK_FACTOR;
            int pos=63-Long.numberOfTrailingZeros(rook);
            //Get diagonally adjacent fields
            int rank;
            if(myColor){
                rank=7-pos/8;
            }else{
                rank=pos/8;
            }
            if(rank==6){
                rookScore+=SEVENTH_RANK_BONUS;
            }

            //Same File
            if(getPieces(myRooks&BitBoardMoves.FILES[pos%8])>1){
                rookScore+=FILE_SHARE_BONUS;
            }

            //Rook File
            long rookFile=BitBoardMoves.FILES[pos%8];

            //No pawns on file bonus
            if((rookFile&(myPawns|enemyPawns))==0){
                rookScore+=FILE_NO_PAWNS_BONUS;
            }else if((rookFile&myPawns)==0&&(rookFile&enemyPawns)!=0){
                rookScore+=FILE_NO_FRIENDLY_PAWNS_BONUS;
            }

            //King closeness
            int kingPos=63-Long.numberOfTrailingZeros(enemyKing);
            int minDist= Math.min(Math.abs(pos/8-kingPos/8),Math.abs(pos%8-kingPos%8));
            rookScore+=KING_TROPISM[minDist];
            rookClone &= ~rook;
            rook = rookClone & ~(rookClone - 1);
        }
        return rookScore;
    }
    public static double knightScore(long myKnights,long enemyKing){
        double knightScore=0;
        long knightClone = myKnights;
        long knight = knightClone & ~(knightClone - 1);
        while (knight != 0) {
            int pos=63-Long.numberOfTrailingZeros(knight);
            knightScore+=KNIGHT_FACTOR;
            knightScore+=CENTRE_CLOSENESS[pos/8][pos%8];
            //Closeness to enemy King
            int enemyKingPos=63-Long.numberOfTrailingZeros(enemyKing);
            int closeness=Math.abs(enemyKingPos/8-pos/8)+Math.abs(enemyKingPos%8-pos%8);
            knightScore+=KING_CLOSENESS[closeness];
            knightClone &= ~knight;
            knight = knightClone & ~(knightClone - 1);
        }
        return knightScore;
    }
    public static double queenScore(long myQueens,long enemyKing){
        double queenScore=0;
        long queenClone = myQueens;
        long queen = queenClone & ~(queenClone - 1);
        while (queen != 0) {
            int pos=63-Long.numberOfTrailingZeros(queen);
            queenScore+=QUEEN_FACTOR;

            //Closeness to enemy King
            int enemyKingPos=63-Long.numberOfTrailingZeros(enemyKing);
            int closeness=Math.abs(enemyKingPos/8-pos/8)+Math.abs(enemyKingPos%8-pos%8);
            queenScore+=KING_CLOSENESS[closeness]*KING_CLOSENESS_MULTIPLIER;
            //Tropism
            int minDist= Math.min(Math.abs(pos/8-enemyKingPos/8),Math.abs(pos%8-enemyKingPos%8));
            queenScore+=KING_TROPISM[minDist]*KING_TROPISM_MULTIPLIER;
            queenClone &= ~queen;
            queen = queenClone & ~(queenClone - 1);
        }
        return queenScore;
    }
    public static int getPieces(long pieceBoard){
        int count=0;
        for(int i=0;i<64;i++){
            if(((pieceBoard>>>i)&1)!=0){
                count++;
            }
        }
        return count;
    }
}
