package threeChess.agents;

import threeChess.*;
import java.io.*;
// import java.io.Serializable;
import java.util.*;

public class BRS2 extends Agent{
	private Scanner myObj;// = new Scanner(System.in);	// PAUSE PROGRAM FOR INPUT
    private static final String name = "BRS2";
    private static final PrintStream s = System.out;
    private ArrayList hist;
    private ArrayDeque history;
    private int alpha = Integer.MIN_VALUE;
    private int beta = Integer.MAX_VALUE;
//    private int count;
    private static int[][] pp, po, h, bp, bo, rp, ro, qp, qo, kp, ko;
    private static int p, n, b, r, q, k;
    private ArrayList removals;
  
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
    public BRS2(){
    	this.myObj = new Scanner(System.in);	// PAUSE PROGRAM FOR INPUT
      this.hist = new ArrayList<Position[]>();
      this.removals = new ArrayList<Position>();
      this.history = new ArrayDeque<Position[]>(4);
//      this.count = 0;
      this.p = 100;
      this.n = 320;
      this.b = 330;
      this.r = 500;
      this.q = 900;
      this.k = 20000;
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
    	{  5,  15, -10,   0,  0,  -10,  -5,   5},
    	{-50, -50, -50, -50, -50, -50, -50, -50},
    	{-50, -50, -50, -50, -50, -50, -50, -50},
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
      ArrayList<movepair> li = new ArrayList<movepair>();
      for(Position current : piece){
        try{
          Piece mover = board.getPiece(current);
          PieceType type = mover.getType();
          Direction[][] steps = type.getSteps();
          Position end = current;
          if(type == PieceType.PAWN || type == PieceType.KING || type == PieceType.KNIGHT){
            for(int i = 0;i < steps.length;i++){
              end = board.step(mover, steps[i], current, current.getColour()!=end.getColour());
              if(!board.isLegalMove(current, end)) {
            	  continue;
              }
              else {
            	  li.add(new movepair(current, end));
              }
            }
          }
          else if(type == PieceType.BISHOP || type == PieceType.QUEEN || type == PieceType.ROOK){
            int reps = 1 + type.getStepReps();
            for(int i = 0;i < steps.length;i++){
              for(int j = 0;j < reps;j++){
                end = board.step(mover, steps[i], current, current.getColour()!=end.getColour());
                if(!board.isLegalMove(current, end)) {
                	continue;
                }
                else {
                	li.add(new movepair(current, end));
                	current = end;
                }
              }
            }
          }
        }catch(ImpossiblePositionException e) {}
        // catch(CloneNotSupportedException e) {}
      }
      return li;
    }
    
    
    /**Best-Reply-Search */
    public int bm(Board board, int depth, Boolean mm) {
    	if(board.gameOver()) {
    		s.println("pontential ko at turn " +board.getMoveCount());
//    		String userName = myObj.nextLine();		// PAUSE PROGRAM FOR INPUT
    		return Integer.MAX_VALUE;
    	}
    	if (depth > 0) {
    		int e = eval(board);
//    		s.println("depth0 eval: "+e);
    		return e;
    	}

		
    	Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
    	ArrayList<movepair> moves =  getlegalmoves(board, pieces);
    	
    	//MAXING
    	if(mm) {
    		int value = Integer.MIN_VALUE;
        	try {
        		for(movepair m : moves) {
        			Board boardcopy = (Board)board.clone();
        			boardcopy.move(m.getStart(), m.getEnd());
        			value = Math.max(bm(boardcopy, depth + 1, !mm), value);
//        			if(value>beta)
//	    				return value;
//	    			alpha=Math.max(beta,value);
        		}
        	}catch(ImpossiblePositionException e){System.out.println("Illeagal Positison");}
        	catch(CloneNotSupportedException e){System.out.println("Clone Not Support");}
        	return value;
    	}
    	
    	//MINIMISING
    	else {
	    	int value = Integer.MAX_VALUE;
	    	
	    	
	    	
	    	try {
	    		//Next opp moves
//		    	Position[] firstMove = testMove(board);
//		    	board.move(firstMove[0], firstMove[1]);
	    		testMove(board);
		    	
		    	
		    	pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
		    	moves =  getlegalmoves(board, pieces);

	    		for(movepair m : moves) {
	    			Board boardcopy = (Board)board.clone();
	    			boardcopy.move(m.getStart(), m.getEnd());
	    			value = Math.min(bm(boardcopy, depth + 1, !mm), value);
//	    			if(value<alpha)
//	    				return value;
//	    			beta=Math.min(beta,value);
	    		}
	    	}catch(ImpossiblePositionException e){System.out.println("Illeagal Positison");}
	    	catch(CloneNotSupportedException e){System.out.println("Clone Not Support");}
	    	return value;
    	}
        
    }
    
    public void testMove(Board board) {
    	Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
    	ArrayList<movepair> moves =  getlegalmoves(board, pieces);
    	if(pieces.length == 0 || moves.size() == 0)
    		return;
    	
    	
    	int[] vals = new int[moves.size()];
        int index = 0;
        Colour player = board.getTurn();
        
        try {
	        for(int i = 0; i < moves.size(); i++) {
	          Board boardcopy = (Board) board.clone();
	          boardcopy.move(moves.get(i).getStart(), moves.get(i).getEnd());
	          vals[i] = eval(boardcopy, player);
	          if(vals[i]>vals[index])
	        	  index = i;
	        }
	        
	        Position[] move = {moves.get(index).getStart(), moves.get(index).getEnd()};
	        board.move(move[0], move[1]);
	        
        }catch(ImpossiblePositionException e){System.out.println("Illeagal Positison");}
        catch(CloneNotSupportedException e){System.out.println("Clone Not Support");}
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
        int size = hist.size();
//        s.println(removals.size());
        Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
        
        //*******************************************************************************
//        CHECKING FOR LOOPING ERRORS
        checkLoop();
        if(removals.size()>0) {
        	ArrayList temp = new ArrayList<Position>();
        	for(Position p : pieces) {
        		if(removals.contains(p)) {
        			continue;
        		}
        		temp.add(p);
        	}
        	if (removals.size()>6) {
        		removals.removeAll(removals);
        	}
        	pieces = (Position[])temp.toArray(new Position[0]);
        }
    	
    	
        //*******************************************************************************
        
        Position[] choice = new Position[2];
        this.alpha = Integer.MIN_VALUE;
        this.beta = Integer.MAX_VALUE;
        ArrayList<movepair> moves =  getlegalmoves(board, pieces);
        if(moves.size() == 0){
        	removals.removeAll(removals);
        	return playMove(board);
        }
        //Make first move
        int[] vals = new int[moves.size()];
        int maxIndex = 0;
        try{
          for(int i = 0; i < moves.size(); i++) {
            Board boardcopy = (Board) board.clone();
            boardcopy.move(moves.get(i).getStart(), moves.get(i).getEnd());
            if(boardcopy.gameOver()) {
            	choice[0] = moves.get(i).getStart();
                choice[1] = moves.get(i).getEnd();
                return choice;
            }
//            if(board.captured(bo).getType()==PieceType.KING) gameOver=true;
            vals[i] = bm(boardcopy, 0, false);
            if(vals[maxIndex]<vals[i]){
                maxIndex=i;
            }
          }
         }catch(ImpossiblePositionException e){System.out.println("Illeagal Positison");}
         catch(CloneNotSupportedException e){System.out.println("Clone Not Support");}
        choice[0] = moves.get(maxIndex).getStart();
        choice[1] = moves.get(maxIndex).getEnd();
        
//        s.println(vals[maxIndex]);
//        Boolean fuckssake =false;
//        hist.add(choice);
//        for(int i = 0; i < moves.size(); i++) {
//        	s.println(moves.get(i).getStart()+" "+moves.get(i).getEnd()+" "+vals[i]);
//        	if(vals[i] == Integer.MAX_VALUE) {
//        		fuckssake = true;
//        	}
//        }
//        if(fuckssake) {
//        	String userName = myObj.nextLine();
//        }
//        if(size>10) {
////        	Scanner myObj = new Scanner(System.in);	// PAUSE PROGRAM FOR INPUT
//            String userName = myObj.nextLine();		// PAUSE PROGRAM FOR INPUT
//        }
        
        
        return choice;
      }

    public Boolean checkLoop() {
    	int size = hist.size();
        if(size > 8) {
        	Position[] previous = (Position[])hist.get(size-1);
        	for(Position[] p : new ArrayList<Position[]>(hist.subList(size-4, size-1))) {
//        		s.println("within last 5");
        		if(p[0] == previous[0] && p[1] == previous[1]) {
        			for(Position[] q : new ArrayList<Position[]>(hist.subList(size-7, size-4)))
        				if(q[0] == previous[0] && q[1] == previous[1]) {
        					removals.add(previous[0]);
        					return true;
        				}
        			
        		}
        	}
        }
        return false;
    }
    
    public int eval(Board board) {
    	return eval(board, board.getTurn());
    }
    public int eval(Board board, Colour player) {
      int total = 0;
      
      //Sum piece value and position
//      Colour turn = board.getTurn();
      for(Position p: Position.values()){
          Piece piece = board.getPiece(p);
          if(piece!=null) {
            int v = getPosValues(p, piece);
            if(piece.getColour()==player) {
              total+=getPieceVals(piece);
              total+=v;
            }
            else{
              total-=getPieceVals(piece);
              total-=v;
            }
  //	    	  System.out.format("Piece: %s, Pos: %s, PosVal: %d.%n", piece, p, v);
          }
        }
//      System.out.println("Eval " + player + ": " + total);
      for(Piece p : board.getCaptured(player)) {
    	  total+=getPieceVals(p);
      }
      return total;
    }
  
    
    public int getPosValues(Position pos, Piece piece) {
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
    
    public int getPieceVals(Piece piece) {
    	switch(piece.getType()) {
        case PAWN: return p;
        case KNIGHT: return n;
        case BISHOP: return b;
        case ROOK: return r;
        case QUEEN: return q;
        case KING: return k;
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
//    Scanner myObj = new Scanner(System.in);	// PAUSE PROGRAM FOR INPUT
//    String userName = myObj.nextLine();		// PAUSE PROGRAM FOR INPUT

  }
