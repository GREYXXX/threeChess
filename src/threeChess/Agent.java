package threeChess;

import java.util.ArrayList;

// import org.graalvm.compiler.graph.Position;

// import jdk.internal.org.objectweb.asm.tree.analysis.Value;
// import jdk.nashorn.internal.IntDeque;
// import sun.jvm.hotspot.debugger.posix.elf.ELFFile;

/**
 * An interface for AI bots to implement.
 * They are simply given a Board object indicating the positions of all pieces, 
 * the history of the game and whose turn it is, and they respond with a move, 
 * expressed as a pair of positions.
 * For Agents submitted to the CITS3001 tournament, 
 * in addition to implementing this interface, agents must use the naming convention:
 * Agent########.java, where the hashes correspond to the authors student number;
 * and each Agent must have a zero parameter constructor (but overloaded constructors are allowed). 
 * **/ 
public abstract class Agent implements Runnable{

  private Board brd;
  private Position[] mv;

  public class movepair{
    Position start, end;
    movepair(Position start, Position end){
      this.start = start;
      this.end = end;
    }
    public Position getStart(){
      return this.start;
    }
    public Position getEnd(){
      return this.end;
    }
  }
  
  /**
   * 0 argument constructor. 
   * This is the constructor that will be used to create the agent in tournaments.
   * It may be (and probably should be) overidden in the implementing class.
   * **/
  public Agent(){}

  /** Can be overridden to mark an Agent as requiring manual input for moves. **/
  public boolean isAutonomous() {
      return true;
  }

  public int eval(Board board){
    return -1;
  }

  /** Get legal moves**/
  public ArrayList<movepair> getlegalmoves(Board boardstart, Position[] piece){
    Board board = boardstart.clone();
    ArrayList<movepair> li = new ArrayList<movepair>();
    for(Position current : piece){
      Piece mover = board.getPiece(current);
      PieceType type = mover.getType();
      Direction[][] steps = type.getSteps();
      if(type == PieceType.PAWN || type == PieceType.KING || type == PieceType.KNIGHT){
        for(int i = 0;i < steps.length;i++){
          Position end = board.step(mover, steps[i], current);
          if(!board.isLegalMove(current, end)) continue;
          else li.add(new movepair(current, end));
        }
      }

      else if(type == PieceType.BISHOP || type == PieceType.QUEEN || type == PieceType.ROOK){
        int reps = 1 + type.getStepReps();
        for(int i = 0;i < steps.length;i++){
          for(int j = 0;j < reps;j++){
            Position end = board.step(mover, steps[i], current);
            if(!board.isLegalMove(current, end)) continue;
            else li.add(new movepair(current, end));
          }
        }
      }
    }
    return li;
  }

  public int BestMove(Board board, Boolean mm, int maxturn, int alpha, int beta, int depth){
    ArrayList<movepair> moves =  new ArrayList<movepair>();
    if(mm) {
      Colour turn = board.getTurn();
      Position[] pieces = board.getPositions(turn).toArray(new Position[0]);
      moves.addAll(getlegalmoves(board, pieces));
    }
    else{
      for(int i = 0; i < 3; i++){
        if(i!=maxturn){
          Colour turn1 = Colour.values()[i];
          Position[] pieces = board.getPositions(turn1).toArray(new Position[0]);
          moves.addAll(getlegalmoves(board, pieces));
        }
      }
    }

    if(depth > 3 || board.gameOver()){
      eval(board);
    }

    // Colour turn = board.getTurn();
    if(mm){
      int value = Integer.MIN_VALUE;
      for(movepair i : moves){
        Board boardcopy = board.clone();
        boardcopy.move(i.getStart(), i.getEnd());
        value = Math.max(value, BestMove(boardcopy, !mm, maxturn, alpha, beta, depth+1));
        if(value>beta)
          return value;
        alpha=Math.max(alpha,value);
        }
        return value;
    }
    else {
      int value = Integer.MAX_VALUE;
      for(movepair i : moves){
        Board boardcopy = board.clone();
        boardcopy.move(i.getStart(), i.getEnd());
        value = Math.min(value, BestMove(boardcopy, !mm, maxturn, alpha, beta, depth+1));
        if(value<alpha)
          return value;
    		beta=Math.min(beta,value);
      }
      return value;
    }
  }

  /**
   * Play a move in the game. 
   * The agent is given a Board Object representing the position of all pieces, 
   * the history of the game and whose turn it is. 
   * They respond with a move represented by a pair (two element array) of positions: 
   * the start and the end position of the move.
   * @param board The representation of the game state.
   * @return a two element array of Position objects, where the first element is the 
   * current position of the piece to be moved, and the second element is the 
   * position to move that piece to.
   * **/
  public Position[] playMove(Board board){
    Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
    int maxturn = board.getTurn().ordinal();
    Position[] choice = new Position[2];
    int alpha = Integer.MIN_VALUE;
    int beta = Integer.MAX_VALUE;
    ArrayList<movepair> moves =  getlegalmoves(board, pieces);
    int[] vals = new int[moves.size()];
    int maxIndex = 0;
    for(int i = 0; i < moves.size(); i++) {
      Board boardcopy = board.clone();
      boardcopy.move(moves.get(i).getStart(), moves.get(i).getEnd());
      vals[i] = BestMove(boardcopy, false, maxturn, alpha, beta, 1);
      if(vals[maxIndex]<vals[i]){
        maxIndex=i;
      }
      choice[0] = moves.get(maxIndex).getStart();
      choice[1] = moves.get(maxIndex).getEnd();
      return choice;
    }
  }

  /**
   * @return the Agent's name, for annotating game description.
   * **/ 
  public abstract String toString();

  /**
   * Displays the final board position to the agent, 
   * if required for learning purposes. 
   * Other a default implementation may be given.
   * @param finalBoard the end position of the board
   * **/
  public abstract void finalBoard(Board finalBoard);

  /**
   * For running threaded games.
   * **/
  public final void setBoard(Board board){brd = board;}
  public final Position[] getMove(){return mv;}
  public void run(){
    mv = playMove(brd);
  }

}


