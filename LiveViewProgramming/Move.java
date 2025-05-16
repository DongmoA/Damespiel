import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Move {
    private final Checkers checkers;

    Move(Checkers checkers) {
        this.checkers = checkers;
    }

    // Checks if a position is within board bounds
    boolean isValidPosition(int x, int y) {
        return x >= 0 && x < checkers.SIZE && y >= 0 && y < checkers.SIZE;
    }

    // Checks if a move is valid (destination is empty)
    private boolean isValidMove(int x, int y) {
        return isValidPosition(x, y) && checkers.board[checkers.getPosition(x, y)] == checkers.EMPTY;
    }
    
    // Checks if one piece belongs to the opponent
   boolean isOpponentPiece(int piece, int otherPiece) {
        return piece != 0 && otherPiece != 0 && piece * otherPiece < 0;
    }

    //  method for getting possible moves
    public List<int[]> getPossibleMoves(int fromX, int fromY) {
        int fromPos = checkers.getPosition(fromX, fromY);
        int piece = checkers.board[fromPos];

        if (piece == 0) return new ArrayList<>();
        return (piece == checkers.BLACK || piece == checkers.OTHER)
                ? getSimpleMoves(fromX, fromY, piece)
                : getQueenMoves(fromX, fromY, piece);
    }

    // Gets possible moves for a simple piece
    private List<int[]> getSimpleMoves(int fromX, int fromY, int piece) {
        List<int[]> moves = new ArrayList<>();
        int direction = (piece == checkers.BLACK) ? -1 : 1;

        if (isValidMove(fromX + direction, fromY + 1))
            moves.add(new int[]{fromX, fromY, fromX + direction, fromY + 1});
        if (isValidMove(fromX + direction, fromY - 1))
            moves.add(new int[]{fromX, fromY, fromX + direction, fromY - 1});

        return moves;
    }

    // Gets possible moves for a queen
    private List<int[]> getQueenMoves(int fromX, int fromY, int piece) {
        List<int[]> moves = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx += 2) {
            for (int dy = -1; dy <= 1; dy += 2) {
                int x = fromX + dx, y = fromY + dy;
                while (isValidMove(x, y)) {
                    moves.add(new int[]{fromX, fromY, x, y});
                    x += dx;
                    y += dy;
                }
            }
        }
        return moves;
    }

// all valid Capture for the currentPlayer 
  Map<Integer , List<List<Integer>> > getAllCaptures (int currentPlayer) {
        Map<Integer, List<List<Integer>>> allMoves = new HashMap<>() ;
        Set<Integer> playerPieces = (currentPlayer > 0 ) ? checkers.Black_Pieces : checkers.Other_Pieces;
        for (int piecePosition : playerPieces) {
            int[] pos = checkers.getCoordinates(piecePosition) ; 
            int fromX = pos[0] , fromY= pos[1] ; 
            List<List<Integer>> possibleCaptures = getPossibleCaptures(fromX,fromY);
            if (!possibleCaptures.isEmpty()) {
                // we add the capture for this piece 
                allMoves.put(piecePosition, possibleCaptures);
            }
        }
        return allMoves ; 
    }


    // Explores all possible captures
    public List<List<Integer>> getPossibleCaptures(int fromX, int fromY) {
        int piece = checkers.board[checkers.getPosition(fromX, fromY)];
        List<List<Integer>> captures = new ArrayList<>();
        if (piece == 0) return captures;

        List<Integer> initialPositions = new ArrayList<>();
        initialPositions.add(checkers.getPosition(fromX, fromY));
        exploreCaptures(fromX, fromY, piece, captures, initialPositions);
        return captures;
    }

    // to add a 
   boolean CheckPositions(List<List<Integer>> captures , List<Integer> positions) {
    boolean val = false ; 
    List<Integer> cap = captures.getLast() ; 
   
   for ( int i = 0 ; i < cap.size() ; i++ ) {
      if(i < positions.size() ) {
      if (!cap.get(i).equals(positions.get(i))) return true ; } else break ;
   }

    return  val ; 
   }


    // Recursive capture exploration
    private boolean exploreCaptures(int fromX, int fromY, int piece, List<List<Integer>> captures, List<Integer> positions) {
        // Handle captures using a ternary operator
        boolean hasFurtherCaptures = false ; 
         hasFurtherCaptures = 
            (piece == checkers.BLACK || piece == checkers.OTHER) 
                ? handleSimplePieceCaptures(fromX, fromY, piece, positions, captures)
                :  handleQueenCaptures(fromX, fromY, piece, positions, captures) ; 
        // If no further captures are possible, add the capture path to the results
        if (!hasFurtherCaptures && positions.size() > 2) { // Ensure at least one capture exists
            if (captures.isEmpty() || CheckPositions(captures, positions)) {
                captures.add(new ArrayList<>(positions));
            } else if (captures.getLast().size() < positions.size()) {
                captures.remove(captures.size() - 1);
                captures.add(new ArrayList<>(positions));
            }
        }
        return hasFurtherCaptures;
    }
    

    // Handles captures for simple pieces
    private boolean handleSimplePieceCaptures(int fromX, int fromY, int piece, List<Integer> positions, List<List<Integer>> captures) {
        boolean hasFurtherCaptures = false;
        for (int dx = -2; dx <= 2; dx += 4) {
            for (int dy = -2; dy <= 2; dy += 4) {
                int midX = fromX + dx / 2, midY = fromY + dy / 2;
                int toX = fromX + dx, toY = fromY + dy;

                if (isValidMove(toX, toY) && isOpponentPiece(checkers.board[checkers.getPosition(midX, midY)], piece)) {
                    int mid = checkers.getPosition(midX, midY);
                    int to = checkers.getPosition(toX, toY);

                    int capturedPiece = checkers.board[mid];
                    checkers.board[mid] = checkers.EMPTY;

                    List<Integer> newPositions = new ArrayList<>(positions);
                    newPositions.add(mid);
                    newPositions.add(to);

                    if (exploreCaptures(toX, toY, piece, captures, newPositions)) {
                        hasFurtherCaptures = true;
                    }

                    checkers.board[mid] = capturedPiece;
                }
            }
        }
        return hasFurtherCaptures;
    }

  // Handles captures for queens
private boolean handleQueenCaptures(int fromX, int fromY, int piece, List<Integer> positions, List<List<Integer>> captures) {
    boolean hasFurtherCaptures = false;
    int[] directions = {-1, 1};

    for (int dx : directions) {
        for (int dy : directions) {
            boolean hasCapture = processDirection(fromX, fromY, dx, dy, piece, positions, captures);
            if (hasCapture) hasFurtherCaptures = true;
        }
    }

    return hasFurtherCaptures;
}


// Processes captures in a specific direction for a queen
private boolean processDirection(int fromX, int fromY, int dx, int dy, int piece, 
                                 List<Integer> positions, List<List<Integer>> captures) {
    int x = fromX + dx, y = fromY + dy, capturedPos = -1;
    boolean foundOpponent = false;

    while (isValidPosition(x, y)) {
        int currentPiece = checkers.board[checkers.getPosition(x, y)];

        if (foundOpponent) {
            if (isValidMove(x, y)) {
                int capturedPiece = checkers.board[capturedPos];
                checkers.board[capturedPos] = checkers.EMPTY;

                List<Integer> newPositions = new ArrayList<>(positions);
                newPositions.add(capturedPos);
                while (isValidMove(x, y)) {
                    newPositions.add(checkers.getPosition(x, y));
                    if (exploreCaptures(x, y, piece, captures, newPositions)) return true;
                    x += dx; y += dy;
                }

                checkers.board[capturedPos] = capturedPiece;
            }
            break;
        } else if (isOpponentPiece(currentPiece, piece)) {
            foundOpponent = true;
            capturedPos = checkers.getPosition(x, y);
        } else if (currentPiece != 0) break;

        x += dx; y += dy;
    }

    return false;
}

 
}
