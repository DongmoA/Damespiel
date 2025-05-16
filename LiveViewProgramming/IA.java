import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class IA {

    Checkers checkers ; 
    Move move ; 
    int MAX_DEPTH  = 9;
    private final Map<String, Integer> memoizationMap = new HashMap<>(); 

    IA(Checkers game) {
     checkers = game ; 
     move = game.move ; 
    }


    private int evaluateBoard(int[] board) {
        int score = 0;
        final int PAWN_VALUE = 3;
        final int QUEEN_VALUE = 5;
        final int THREAT_PENALTY = -3;
    
        for (int i = 0; i < board.length; i++) {
            int piece = board[i];
            if (piece == checkers.OTHER || piece == checkers.OTHER_DAME) {
                score += (piece == checkers.OTHER) ? PAWN_VALUE : QUEEN_VALUE;
                if (isThreatened(i)) {
                    score += THREAT_PENALTY;
                }
            } else if (piece ==checkers.BLACK || piece == checkers.BLACK_DAME) {
                score -= (piece == checkers.BLACK) ? PAWN_VALUE : QUEEN_VALUE;
            }
        }
        return score;
    }
    
    private boolean isThreatened(int position) {
        int[] coords = checkers.getCoordinates(position);
        int x = coords[0], y = coords[1];
        int piece = checkers.board[position];
    
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int[] dir : directions) {
            int attackerX = x + dir[0];
            int attackerY = y + dir[1];
            int jumpX = x - dir[0];
            int jumpY = y - dir[1];
    
            if (checkers.move.isValidPosition(jumpX, jumpY) && checkers.move.isValidPosition(attackerX, attackerY)) {
                int attackerPos = checkers.getPosition(attackerX, attackerY);
                int attackerPiece = checkers.board[attackerPos];
                if (checkers.move.isOpponentPiece(attackerPiece, piece) && checkers.board[checkers.getPosition(jumpX, jumpY)] == checkers.EMPTY) {
                    return true;
                }
            }
        }
        return false;
    }



    List<int[]> getNormalMoves (int currentPlayer) {
      List<int[]> allmoves = new ArrayList<>(); 
      Set<Integer> Pieces = (currentPlayer > 0) ? checkers.Black_Pieces : checkers.Other_Pieces ;
      for(int piece : Pieces ) {
        int[] pos = checkers.getCoordinates(piece) ; 
       List<int[]> result = move.getPossibleMoves(pos[0], pos[1]) ; 
       allmoves.addAll(result) ; 

      }

      return allmoves ; 
    }

    List<List<Integer>> getCaptureMove (int currentPlayer) {
        List<List<Integer>> allcaptures = new ArrayList<>(); 
      Map<Integer , List<List<Integer>>> result =    move.getAllCaptures(currentPlayer) ;
      for(int key : result.keySet()) {
         List<List<Integer>> capture = result.get(key) ; 
        allcaptures.addAll(capture) ; 
      }
      
     return allcaptures ; 
    }

    
    

    List<int[]> generateToPositionAfterCaptures(List<Integer> capture) {
        List<int[]> moves = new ArrayList<>();
        int piecePosition = capture.get(0); 
        int[] Coordfrom = checkers.getCoordinates(piecePosition) ; 
        int pieceType = Math.abs(checkers.board[piecePosition]);
    
        if (pieceType == 2) { 
            
            for (int i = capture.size() - 1; i >= 0; i--) {
                int pos = capture.get(i);
                if (checkers.board[pos] == 0) { 
                    int[] coords = checkers.getCoordinates(pos);
                    moves.add(new int[]{Coordfrom[0],Coordfrom[1],coords[0],coords[1]}); 
                    
                } else break ; 
            }
        } else {
            
            int to = capture.getLast();
            int[] Coordto = checkers.getCoordinates(to) ; 
            moves.add(new int[]{Coordfrom[0],Coordfrom[1],Coordto[0],Coordto[1]});
        }
    
        return moves;
    }

   // Minimax algorithm with Alpha-Beta pruning
int minimax(int depth, boolean isMaximizingPlayer, int alpha, int beta) {
    // Stop condition: maximum depth reached or game over
    if (depth == 0 || checkers.isGameOver()) {
        return evaluateBoard(checkers.board);
    }

    // Determine the current player and possible moves
    int currentPlayer = isMaximizingPlayer ? checkers.BLACK : checkers.OTHER;
    List<int[]> normalMoves = getNormalMoves(currentPlayer);
    List<List<Integer>> captureMoves = getCaptureMove(currentPlayer);

    // Initialize the best evaluation based on the player
    int bestEval = isMaximizingPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE;

    // Explore captures first (if available)
    if (!captureMoves.isEmpty()) {
        bestEval = exploreCaptures(captureMoves, depth, isMaximizingPlayer, alpha, beta, bestEval);
    } else {
        // Explore normal moves
        bestEval = exploreNormalMoves(normalMoves, depth, isMaximizingPlayer, alpha, beta, bestEval);
    }

    return bestEval;
}

  // Explore all possible capture moves
private int exploreCaptures(List<List<Integer>> captureMoves, int depth, boolean isMaximizingPlayer, 
int alpha, int beta, int bestEval) {
for (List<Integer> capture : captureMoves) {
List<int[]> possibleMoves = generateToPositionAfterCaptures(capture);
for (int[] move : possibleMoves) {
// Execute the capture
checkers.executeCapture(capture, move[0], move[1], move[2], move[3]);

// Evaluate the move with Minimax
int eval = minimax(depth - 1, !isMaximizingPlayer, alpha, beta);

// Undo the capture
checkers.undoCapture(move[0], move[1], move[2], move[3]);

// Update the best evaluation
if (isMaximizingPlayer) {
bestEval = Math.max(bestEval, eval);
alpha = Math.max(alpha, eval);
} else {
bestEval = Math.min(bestEval, eval);
beta = Math.min(beta, eval);
}

// Alpha-Beta pruning
if (beta <= alpha) break;
}
}
return bestEval;
} 


// Explore all possible normal moves
private int exploreNormalMoves(List<int[]> normalMoves, int depth, boolean isMaximizingPlayer, 
                              int alpha, int beta, int bestEval) {
    for (int[] move : normalMoves) {
        // Execute the move
        checkers.executeMove(move[0], move[1], move[2], move[3]);

        // Evaluate the move with Minimax
        int eval = minimax(depth - 1, !isMaximizingPlayer, alpha, beta);

        // Undo the move
        checkers.undoMove(move[0], move[1], move[2], move[3]);

        // Update the best evaluation
        if (isMaximizingPlayer) {
            bestEval = Math.max(bestEval, eval);
            alpha = Math.max(alpha, eval);
        } else {
            bestEval = Math.min(bestEval, eval);
            beta = Math.min(beta, eval);
        }

        // Alpha-Beta pruning
        if (beta <= alpha) break;
    }
    return bestEval;
}
    
  private int calculateDynamicDepth() {
    int iaPieceCount = 0;
    for (int value : checkers.board) {
        if (value == checkers.OTHER || value == checkers.OTHER_DAME) {
            iaPieceCount++; 
        }
    }

    
    if (iaPieceCount < 8) {
        return 4; 
    } else if (iaPieceCount <= 10) {
        return 3;  
    } else {
        return 2;  
    }
}
 

    int[] bestMove() {
      MAX_DEPTH = calculateDynamicDepth() ; 
    
       
        int[] bestMove = new int[]{-1, -1, -1, -1};
        int bestValue = Integer.MIN_VALUE;
        List<int[]> normalMoves = getNormalMoves(checkers.currentPlayer);
        List<List<Integer>> captureMoves = getCaptureMove(checkers.currentPlayer);
    
        if (!captureMoves.isEmpty()) {
           
            for (List<Integer> capture : captureMoves) {
                List<int[]> possibleMoves = generateToPositionAfterCaptures(capture);
                for (int[] move : possibleMoves) {
                   
                    checkers.executeCapture(capture, move[0], move[1], move[2], move[3]);
    
                    
                    int moveValue = minimax(MAX_DEPTH, false, Integer.MIN_VALUE, Integer.MAX_VALUE); 
    
                    
                    checkers.undoCapture(move[0], move[1], move[2], move[3]);
    
                    
                    if (moveValue >= bestValue) {
                        bestValue = moveValue;
                        bestMove = move;
                    }
                }
            }
        } else {
            
            for (int[] move : normalMoves) {
                checkers.executeMove(move[0], move[1], move[2], move[3]);
                int moveValue = minimax(MAX_DEPTH, false, Integer.MIN_VALUE, Integer.MAX_VALUE); 
                checkers.undoMove(move[0], move[1], move[2], move[3]);
                if (moveValue >= bestValue) {
                    bestValue = moveValue;
                    bestMove = move;
                }
            }
        }
    
        return bestMove;
    }

    public void playAIMove() {
        
      
        int[] bestMove = bestMove();
    
      
        if (bestMove[0] != -1) {
            int fromX = bestMove[0];
            int fromY = bestMove[1];
            int toX = bestMove[2];
            int toY = bestMove[3];
    
            
            checkers.moves(fromX, fromY, toX, toY);
            memoizationMap.clear();
      
    
           
        } 
    }

}