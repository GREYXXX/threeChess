package threeChess.agents;

import threeChess.*;
// import java.io.Serializable;
import java.util.*;

public class BRS extends Agent{
    private static final String name = "BRS";
    private static int[][] pp, po, h, bp, bo, rp, ro, qp, qo, kp, ko;
  
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
    public BRS(){
      this.pp = new int[][]{
        {  0,   0,   0,   0,   0,   0,   0,   0},
        {  5,  10,  10, -20, -20,  10,  10,   5},
        {  5,  -5, -10,   0,  0,  -10,  -5,   5},
        {  0,   0,   0,  20,  20,   0,   0,   0}
      };
      this.po = new int[][]{
        {  0,   0,   0,   0,   0,   0,   0,   0},
        { 50,  50,  50,  50,  50,  50,  50,  50},
        { 10,  10,  20,  30,  30,  20,  10,  10},
        {  5,   5,  10,  25,  25,  10,   5,   5}
      };
      
      this.h = new int[][]{
        {-50, -40, -30, -30, -30, -30, -40, -50},
        {-40, -20,   0,   5,   5,   0, -20, -40},
        {-30,   5,  10,  15,  15,  10,   5, -30},
        {-30,   0,  15,  20,  20,  15,   0, -30}
      };
      
      this.bp = new int[][]{
        {-20, -10, -10, -10, -10, -10, -10, -20},
        {-10,   5,   0,   0,   0,   0,   5, -10},
        {-10,  10,  10,  10,  10,  10,  10, -10},
        {-10,   0,  10,  10,  10,  10,   0, -10}
      };
      this.bo = new int[][]{
        {-20, -10, -10, -10, -10, -10, -10, -20},
        {-10,   0,   0,   0,   0,   0,   0, -10},
        {-10,   0,   5,  10,  10,   5,   0, -10},
        {-10,   5,   5,  10,  10,   5,   5, -10}
      };
      
      this.rp = new int[][]{
        {  0,   0,   0,   5,   5,   0,   0,   0},
        { -5,   0,   0,   0,   0,   0,   0,  -5},
        { -5,   0,   0,   0,   0,   0,   0,  -5},
        { -5,   0,   0,   0,   0,   0,   0,  -5}
      };
      this.ro = new int[][]{
        {  0,   0,   0,   0,   0,   0,   0,   0},
        {  5,  10,  10,  10,  10,  10,  10,   5},
        { -5,   0,   0,   0,   0,   0,   0,  -5},
        { -5,   0,   0,   0,   0,   0,   0,  -5},
      };
      
      this.qp = new int[][]{
        {-20, -10, -10,  -5,  -5, -10, -10, -20},
        {-10,   0,   5,   0,   0,   0,   0, -10},
        {-10,   5,   5,   5,   5,   5,   0, -10},
        {  0,   0,   5,   5,   5,   5,   0,  -5}
      };
      this.qo = new int[][]{
        {-20, -10, -10,  -5,  -5, -10, -10, -20},
        {-10,   0,   0,   0,   0,   0,   0, -10},
        {-10,   0,   5,   5,   5,   5,   0, -10},
        { -5,   0,   5,   5,   5,   5,   0,  -5}
      };
      
      this.kp = new int[][]{
        {  0,   0,   0,   0,   0,   0,   0,   0},
        {  5,  10,  10, -20, -20,  10,  10,   5},
        {  5,  -5, -10,   0,  0,  -10,  -5,   5},
        {  0,   0,   0,  20,  20,   0,   0,   0}
      };
      this.ko = new int[][]{
        {  0,   0,   0,   0,   0,   0,   0,   0},
        { 50,  50,  50,  50,  50,  50,  50,  50},
        { 10,  10,  20,  30,  30,  20,  10,  10},
        {  5,   5,  10,  25,  25,  10,   5,   5}
      };
    }
  
    /** Can be overridden to mark an Agent as requiring manual input for moves. **/
    public boolean isAutonomous() {
        return true;
    }
  
    /** Get legal moves**/
    public ArrayList<movepair> getlegalmoves(Board board, Position[] piece){
      // for(Position current : piece){
      //   System.out.println(board.getPiece(current));
      // }
      // // System.out.println(piece.length);
      ArrayList<movepair> li = new ArrayList<movepair>();
      //board = (Board) boardstart.clone();
        //li = new ArrayList<movepair>();
      for(Position current : piece){
        try{
          Piece mover = board.getPiece(current);
          PieceType type = mover.getType();
          // System.out.println(mover.getColour() + type.name());
          Direction[][] steps = type.getSteps();
          Position end = current;
            // Position end = current;
          if(type == PieceType.PAWN || type == PieceType.KING || type == PieceType.KNIGHT){
            for(int i = 0;i < steps.length;i++){
              end = board.step(mover, steps[i], current, current.getColour()!=end.getColour());
              if(!board.isLegalMove(current, end)) continue;
              else li.add(new movepair(current, end)); //System.out.println(end);
            }
          }
          else if(type == PieceType.BISHOP || type == PieceType.QUEEN || type == PieceType.ROOK){
            int reps = 1 + type.getStepReps();
            for(int i = 0;i < steps.length;i++){
              for(int j = 0;j < reps;j++){
                end = board.step(mover, steps[i], current, current.getColour()!=end.getColour());
                if(!board.isLegalMove(current, end)) continue;
                else li.add(new movepair(current, end)); //System.out.println(end);
              }
            }
          }
        }catch(ImpossiblePositionException e) {}
        // catch(CloneNotSupportedException e) {}
      }
      return li;
    }
    /**Best-Reply-Search */
    public int BestMove(Board board, Boolean mm, int maxturn, int alpha, int beta, int depth){
      ArrayList<movepair> moves =  new ArrayList<movepair>();
      if(mm) {
        Colour turn = Colour.values()[maxturn];
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
  
      if(depth > 2 || board.gameOver()){
        return eval(board);
      }
  
      // Colour turn = board.getTurn();
      if(mm){
        Board boardcopy = null;
        int value = Integer.MIN_VALUE;
        try{
          for(movepair i : moves){
            boardcopy = (Board) board.clone();
            boardcopy.move(i.getStart(), i.getEnd());
            value = Math.max(value, BestMove(boardcopy, !mm, maxturn, alpha, beta, depth+1));
            if(value>beta)
              return value;
            alpha=Math.max(alpha,value);
          }
        }catch(ImpossiblePositionException e){System.out.println("Illeagal Positison");}
        catch(CloneNotSupportedException e){System.out.println("Clone Not Support");}
        return value;
      }
      
      else {
        int value = Integer.MAX_VALUE;
        try{
          for(movepair i : moves){
            Board boardcopy = (Board)board.clone();
            boardcopy.move(i.getStart(), i.getEnd());
            value = Math.min(value, BestMove(boardcopy, !mm, maxturn, alpha, beta, depth+1));
            if(value<alpha)
              return value;
            beta=Math.min(beta,value);
          }
        }catch(ImpossiblePositionException e){System.out.println("Illeagal Positison");}
        catch(CloneNotSupportedException e){System.out.println("Clone Not Support");}
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
      // System.out.println("maturn is: " + maxturn + '\n');
      Position[] choice = new Position[2];
      int alpha = Integer.MIN_VALUE;
      int beta = Integer.MAX_VALUE;
      ArrayList<movepair> moves =  getlegalmoves(board, pieces);
      // for(int i = 0; i < moves.size();i++){
      //   System.out.println("ASDADS");
      //  // System.out.println(""+ moves.get(i).getStart() +" "+ moves.get(i).getEnd());
      // }
      int[] vals = new int[moves.size()];
      int maxIndex = 0;
      try{
        for(int i = 0; i < moves.size(); i++) {
          Board boardcopy = (Board) board.clone();
          boardcopy.move(moves.get(i).getStart(), moves.get(i).getEnd());
          vals[i] = BestMove(boardcopy, false, maxturn, alpha, beta, 1);
          if(vals[maxIndex]<vals[i]){
              maxIndex=i;
          }
        }
       }catch(ImpossiblePositionException e){System.out.println("Illeagal Positison");}
       catch(CloneNotSupportedException e){System.out.println("Clone Not Support");}
      choice[0] = moves.get(maxIndex).getStart();
      choice[1] = moves.get(maxIndex).getEnd();
      // System.out.println(choice[0] + choice[1] + '\n');
      return choice;
    }


    static public int eval(Board board) {
      int total = 0;
      Colour turn = board.getTurn();
      for(Position p: Position.values()){
          Piece piece = board.getPiece(p);
          if(piece!=null) {
            int v = getPosValues(p, piece);
            if(piece.getColour()==turn) {
              total+=piece.getValue();
              total+=v;
            }
            else{
              total-=piece.getValue();
              total-=v;
            }
  //	    	  System.out.format("Piece: %s, Pos: %s, PosVal: %d.%n", piece, p, v);
          }
        }
      System.out.println(turn + ": " + total);
      System.out.println();
      return total;
    }
  
    
    static public int getPosValues(Position pos, Piece piece) {
      int r = pos.getRow();
      int c = pos.getColumn();
      switch(piece.getType()) {
        case PAWN: return ((pos.getColour() == piece.getColour()) ? pp[r][c] : po[r][c]);
        case KNIGHT: return h[r][c];
        case BISHOP: return ((pos.getColour() == piece.getColour()) ? bp[r][c] : bo[r][c]);
        case ROOK: return ((pos.getColour() == piece.getColour()) ? rp[r][c] : ro[r][c]);
        case QUEEN: return ((pos.getColour() == piece.getColour()) ? qp[r][c] : qo[r][c]);
        case KING: return ((pos.getColour() == piece.getColour()) ? kp[r][c] : ko[r][c]);
      }
      return 0;
      
    }
  
    /**
     * @return the Agent's name, for annotating game description.
     * **/ 
    public String toString(){return name;}
  
    /**
     * Displays the final board position to the agent, 
     * if required for learning purposes. 
     * Other a default implementation may be given.
     * @param finalBoard the end position of the board
     * **/
    public  void finalBoard(Board finalBoard) {}
  
    /**
     * For running threaded games.
     * **/

  }
