
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import java.util.stream.IntStream;

public class Checkers implements Clerk {
  
      final int SIZE = 8;
    // Current player: 1 for player 1, -1 for player 2
      int currentPlayer = 1;
      final int BLACK = 1;
      final int OTHER = -1;
      final int EMPTY = 0;
      final int OTHER_DAME = -2;
      final int BLACK_DAME = 2;

    Set<Integer> Black_Pieces = new HashSet<>() ; 
    Set<Integer> Other_Pieces = new HashSet<>() ; 
    private Stack<Map<Integer, Integer>> capturedPiecesStack = new Stack<>(); // Stack to store captured pieces
    private final Stack<Integer> pieceStateStack = new Stack<>();


    final int[] board = new int[SIZE * SIZE];
    private final String ID;
    private final String libPath = "checkers.js";
    private final LiveView view;
  
    

    Checkers(LiveView view) {
        this.view = view;
        this.ID = Clerk.getHashID(this);

        // Load the JavaScript file
        Clerk.load(view, libPath);
        // Create the canvas in the browser
        Clerk.write(view, "<canvas id='checkersCanvas" + ID + "' width='700' height='700' style='border:1px solid black; display:block; margin:auto;'></canvas>");
        Clerk.script(view, "const checkers" + ID + " = new Checkers(document.getElementById('checkersCanvas" + ID + "'), 'checkers" + ID + "');");

        // Configure client responses
        this.view.createResponseContext("/checkers" + ID, response -> {
            String receivedString = response ;      
            String[] parts = receivedString.split(",");
      
        int[] numbers = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            numbers[i] = Integer.parseInt(parts[i]);
        }
         if(numbers.length == 1 ) {
            //if (receivedString.equals( "12")) return ; 
            try {
                Thread.sleep(1000) ;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Map<Integer , List<List<Integer>> > capture = getAllCaptures(currentPlayer);
            if (!capture.isEmpty())  
             sendPossibleCaptures(capture, "manageCapture", currentPlayer);
         }
         else if (numbers.length == 2 ) {
           List<int[]> moves = move.getPossibleMoves(numbers[0],numbers[1] );
         sendPossibleMoves(moves,"manageMove"); 
         }
         else if (numbers.length == 4 ) {
            moves(numbers[0],numbers[1],numbers[2],numbers[3]);
         }
         });
        // Initialize the board
        resetBoard();
    }


    Move move ; 
    IA  ia ; 
    Checkers() {
        this(Clerk.view());
        move = new Move(this) ;
        ia = new IA(this) ; 
    }
 
  
    // initialization of the game board and the game in Browser 
    private void resetBoard() {
        Arrays.fill(board, EMPTY);
        for (int i = 0; i < SIZE; i++) {
            if (i < 3) {
                for (int j = (i % 2); j < SIZE; j += 2) {
                    board[getPosition(i, j)] = OTHER;
                    Other_Pieces.add(getPosition(i, j)) ; 
                }
            } else if (i > 4) {
                for (int j = (i % 2); j < SIZE; j += 2) {
                    board[getPosition(i, j)] = BLACK;
                    Black_Pieces.add(getPosition(i, j)) ;
                }
            }
        }
        updateBrowser(-1, -1, -1, -1,currentPlayer);
    }

    // Updates the interface in the browser
     void updateBrowser(int fromX, int fromY, int toX, int toY,int currentPlayer) {
        StringBuilder boardState = new StringBuilder("[");
        for (int i = 0; i < board.length; i++) {
            boardState.append(board[i]);
            if (i < board.length - 1) boardState.append(",");
        }
        boardState.append("]");
    
        // Update the board with special positions for movement
        int from = getPosition(fromX, fromY);
        int to = getPosition(toX, toY);
    
        Clerk.call(view, "checkers" + ID + ".drawBoard(" + true + ","+ boardState +
            ", " + from + ", " + to + ", "+currentPlayer + ")");
    }

       // to send normalMoves to the js for update 
    private void sendPossibleMoves(List<int[]> moves, String action) {
        StringBuilder movesString = new StringBuilder("[");
        for (int i = 0; i < moves.size(); i++) {
            int[] move = moves.get(i);
            movesString.append("[")
                       .append(move[0]).append(",")
                       .append(move[1]).append(",")
                       .append(move[2]).append(",")
                       .append(move[3])
                       .append("]");
            if (i < moves.size() - 1) movesString.append(","); 
        }
        movesString.append("]");
        Clerk.call(view, "checkers" + ID + "." + action + "(" + movesString + ")");
    }


     // we create a acceptable datastructure for Javascript 
    private void sendPossibleCaptures(Map<Integer, List<List<Integer>>> captures, String action,int number) {
        StringBuilder capturesString = new StringBuilder("[");
    for (Map.Entry<Integer, List<List<Integer>>> entry : captures.entrySet()) {
    capturesString.append("[")
                  .append(entry.getKey()) 
                  .append(", ") 
                  .append(entry.getValue().toString()) 
                  .append("], "); 
    }
    if (capturesString.charAt(capturesString.length() - 2) == ',') {
    capturesString.setLength(capturesString.length() - 2); 
     }
       capturesString.append("]");    
        Clerk.call(view, "checkers" + ID + "." + action + "(" + capturesString +","+ number+ ")");
     }
    
     String determineWinnerMessage() {
        boolean blackHasOnlyQueens = Black_Pieces.stream()
                .allMatch(pos -> board[pos] == BLACK_DAME);
        boolean otherHasOnlyQueens = Other_Pieces.stream()
                .allMatch(pos -> board[pos] == OTHER_DAME);
    
        if (Black_Pieces.isEmpty()) {
            
            return "The AI (Player 2) has won! Human has no pieces left.";
        } else if (Other_Pieces.isEmpty()) {
            
            return "The Human (Player 1) has won! AI has no pieces left.";
        } else if (blackHasOnlyQueens && otherHasOnlyQueens) {
         
            return "It's a draw! Both players have only queens left.";
        } else if (Black_Pieces.isEmpty() && Other_Pieces.isEmpty()) {
           
            return "It's a draw! Both players have no pieces left.";
        } else {
            return ""; 
        }
    }
    
    boolean message() {
        String message = determineWinnerMessage();
        boolean is_not_Empty = !message.isEmpty();
        if (is_not_Empty) {
            
            Clerk.call(view, "checkers" + ID + ".drawMessage('" + message + "')");
   
        }
        return is_not_Empty;
    }
    

    // Converts row and column to a single index
    public  int getPosition(int row, int col) {
        return row * SIZE + col;
    }



    // Retrieves the row and column from a position
    public int[] getCoordinates(int position) {
        return new int[] { position / SIZE, position % SIZE };
    }   

    
    Map<Integer , List<List<Integer>> > getAllCaptures (int currentPlayer) {
       return move.getAllCaptures(currentPlayer) ; 
    }
    
    boolean isGameOver() {
        return Arrays.stream(board)
                 .noneMatch(piece -> piece == BLACK || piece == OTHER);
         }


         
    boolean destination(List<Integer> capture , int to ) {
       int k = -1; 
       boolean found = false ; 
        for (int i = capture.size()-1 ; i >=0 ; i--) {
         if(board[capture.get(i)] == 0)  {
         if( capture.get(i) ==  to ) found = true ;
          } else {
          k = i ;   
          break ;  
          }  
        }

       if(found && k > 0) {
       List<Integer> updatecapture = IntStream.range(0, k + 1).mapToObj(capture::get).collect(Collectors.toList()) ; 
        capture.clear();
        capture.addAll(updatecapture) ;
        }
         return found ; 
     }

     void undoCapture(int fromX, int fromY, int toX, int toY) {
        // Cancel the movement of the piece that performed the capture
        undoMove(fromX, fromY, toX, toY);
        // Retrieve the last map of captured pieces
        Map<Integer, Integer> capturedPieces = capturedPiecesStack.pop();
        // Restore captured pieces from the map
        for (Map.Entry<Integer, Integer> entry : capturedPieces.entrySet()) {
            int capturedPos = entry.getKey();
            int oldValue = entry.getValue();
            // Restore the captured piece on the board
            board[capturedPos] = oldValue;
            // Update piece sets
            if (oldValue == OTHER || oldValue == OTHER_DAME) {
                Other_Pieces.add(capturedPos);
            } else if (oldValue == BLACK || oldValue == BLACK_DAME) {
                Black_Pieces.add(capturedPos);
            }
        }
    }
    

    void executeCapture(List<Integer> capture, int fromX, int fromY, int toX, int toY) {
        // Create a new map to store captured pieces
        Map<Integer, Integer> capturedPieces = new HashMap<>();
        // Fill the map with captured pieces and their old values
        for (int i = 1; i < capture.size(); i++) {
            int capturedPos = capture.get(i);
            // Ignore empty squares
            if (board[capturedPos] == 0) {
                continue;
            }
            // Store the old value of the captured piece
            capturedPieces.put(capturedPos, board[capturedPos]);
            // Remove the captured piece from the board
            board[capturedPos] = 0;
            // Update piece sets
            if (currentPlayer == 1) {
                Other_Pieces.remove(capturedPos);
            } else {
                Black_Pieces.remove(capturedPos);
            }
        }
        // Add the map to the stack
        capturedPiecesStack.push(capturedPieces);
        // Execute the movement
        executeMove(fromX, fromY, toX, toY);
    }


    private boolean handleCapture(int fromX, int fromY, int toX, int toY) {
    int from = getPosition(fromX, fromY), to = getPosition(toX, toY);
    // Retrieve all possible captures for the current player
    Map<Integer, List<List<Integer>>> allCaptures = getAllCaptures(currentPlayer);
    // Check if any captures are available
    boolean found = allCaptures.isEmpty();
    if (!found) {
        // Ensure `from` is a valid key in `allCaptures`
        if (!allCaptures.containsKey(from)) {
            throw new RuntimeException("Capture is mandatory. Possible starting positions: " +
                allCaptures.keySet().stream()
                    .map(pos -> Arrays.toString(getCoordinates(pos)))
                    .collect(Collectors.joining(", ")));
        }
        boolean validCapture = false;
        // Iterate through all possible captures starting from `from`
        List<List<Integer>> captures = allCaptures.get(from);
        for (List<Integer> capture : captures) {
            // Check if the move ends at the correct position
            if (destination(capture, to)) {
                 validCapture = true ; 
                executeCapture(capture, fromX, fromY, toX, toY);
                pieceStateStack.pop();
                capturedPiecesStack.clear();
                break; // Exit the loop after successfully handling the capture
            }
        }
        if (!validCapture) {
            throw new RuntimeException("Invalid capture. You must end your move at a valid position. ");
        }

        // Switch to the next player
        currentPlayer *= -1; 
        updateBrowser(fromX, fromY, toX, toY, currentPlayer); 
        
        // If it's the AI's turn, make the AI play
        if (currentPlayer == -1) {
         ia.playAIMove();
        }
    } 

    // Return whether no captures were available
    return found;
    }



    // Move a piece
    public void moves(int fromX, int fromY, int toX, int toY) {
        
        
        int from = getPosition(fromX, fromY);
        if (board[from] == 0) {
            throw new RuntimeException("Invalid piece selection: (" + fromX + "," + fromY + ")");
        }
        if (board[from] * currentPlayer <= 0) {
            throw new RuntimeException("It's not your turn.");
        }
     
     boolean found =  handleCapture(fromX, fromY, toX, toY);
   
     if(found ) {
        List<int[]> moves = move.getPossibleMoves(fromX, fromY);
        if (moves.stream().anyMatch(valid -> valid[0] == fromX && valid[1] == fromY && valid[2] == toX && valid[3] == toY)) {
            executeMove(fromX, fromY, toX, toY);
            pieceStateStack.pop();
            currentPlayer *= -1;
             updateBrowser(fromX, fromY, toX, toY,currentPlayer); 
            if (currentPlayer == -1) {
                ia.playAIMove();
            }

        } else {
            throw new RuntimeException("Invalid move.");
        }

    }   

if(message()) return ; 
    }
    


 void executeMove(int fromX, int fromY, int toX, int toY) {
    int from = getPosition(fromX, fromY);
    int to = getPosition(toX, toY);
    
    pieceStateStack.push(board[from]);
    if (board[from] > 0) {
        Black_Pieces.remove(from);
        Black_Pieces.add(to);
    } else if (board[from] < 0) {
        Other_Pieces.remove(from);
        Other_Pieces.add(to);
    }

    board[to] = board[from];
    board[from] = 0;
    
    if ((board[to] == BLACK && toX == 0) || (board[to] == OTHER && toX == SIZE - 1)) {
        board[to] = (board[to] == BLACK) ? BLACK_DAME : OTHER_DAME;
    }
}


void undoMove(int fromX, int fromY, int toX, int toY) {
    int from = getPosition(fromX, fromY);
    int to = getPosition(toX, toY);

    
    board[from] = pieceStateStack.pop(); 
    board[to] = 0;


    if (board[from] > 0) {
        Black_Pieces.remove(to);
        Black_Pieces.add(from);
    } else if (board[from] < 0) {
        Other_Pieces.remove(to);
        Other_Pieces.add(from);
    }
}

    
@Override 
public String toString() {

String result = "\n"+"+-----+-----+-----+-----+-----+-----+-----+-----+"+"\n"; 
for(int i = 0 ; i < SIZE ; i++) {
  for(int j = 0 ; j < SIZE ; j++ ) {
    int pos = getPosition(i, j) ;
   if((i+j)%2 == 0) {
    if(board[pos]!= 0) {

      if( board[pos] == OTHER )  result += "|  X  " ;
      else if( board[pos]== BLACK_DAME )  result += "|  B  " ;
      else if( board[pos]== OTHER_DAME )   result+= "|  W  ";
      else result+= "|  O  ";
    }
    else result += "|     " ;
  } else result+= "|  :  ";

  }
  result +="|  "+i +"\n"+ "+-----+-----+-----+-----+-----+-----+-----+-----+"+"\n";  
}
result += "   0     1     2     3     4     5     6     7 ";
return result ; 
}

}
 