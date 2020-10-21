package threeChess.agents;

import threeChess.*;
import java.lang.Math;
import java.util.concurrent.TimeUnit;

// import javax.lang.model.util.ElementScanner6;

// import threeChess.agents.BRS1;


// import java.io.Serializable;
import java.util.*;

public class MCTS extends Agent {
    private static final String name = "MCTS";
    private static final Random random = new Random();


/**
 * 
 */
    public class movepair {
        Position start, end;

        movepair(Position start, Position end) {
            this.start = start;
            this.end = end;
        }

        public Position getStart() {
            return this.start;
        }

        public Position getEnd() {
            return this.end;
        }
    }

/**
 * 
 */
    public class node {
        Board board;
        double visit;
        double[] reward;
        Position start, end;
        ArrayList<node> nodes;

        public node(Board board, Position start, Position end) {
            this.board = board;
            this.start = start;
            this.end = end;
            this.visit = 0.0;
            this.reward = new double[] {0.0, 0.0, 0.0};
            nodes = new ArrayList<node>();
        }

        public void update(double[] reward) {
            this.visit += 1;
            for(int i = 0; i < reward.length;i++){
                this.reward[i] += reward[i];
            }
        }

        public void setChildren(Board newstate, Position start, Position end) {
            nodes.add(new node(newstate, start, end));
        }

        public ArrayList<node> getChildrens() {
            return nodes;
            // ArrayList<node> nodes = new ArrayList<>();
            // Position[] piece = board.getPositions(board.getTurn()).toArray(new
            // Position[0]);
            // ArrayList<movepair> li = getlegalmoves(board, piece);
            // for (movepair mv : li) {
            // try {
            // Board boardcopy = (Board) board.clone();
            // boardcopy.move(mv.getStart(), mv.getEnd());
            // nodes.add(new node(boardcopy));
            // } catch (ImpossiblePositionException e) {
            // System.out.println("Illeagal Positison");
            // } catch (CloneNotSupportedException e) {
            // System.out.println("Clone Not Support");
            // }
            // }
        }

        public double getVisit() {
            return this.visit;
        }

        public double[] getReward() {
            return this.reward;
        }

        public Board getBoard() {
            return this.board;
        }

        public Position getStart() {
            return this.start;
        }

        public Position getEnd() {
            return this.end;
        }

        public Board getBoardcopy() {
            Board boardcopy = null;
            try{
                boardcopy = (Board)this.board.clone();
            }catch(CloneNotSupportedException e){}
            return boardcopy;
        }

    }

    public MCTS() {}
    
/**
 * 
 * @param board
 * @param piece
 * @return
 */
    public ArrayList<movepair> getlegalmoves(Board board, Position[] piece) {
        ArrayList<movepair> li = new ArrayList<movepair>();
        for (Position current : piece) {
            try {
                Piece mover = board.getPiece(current);
                PieceType type = mover.getType();
                Direction[][] steps = type.getSteps();
                Position end = current;
                if (type == PieceType.PAWN || type == PieceType.KING || type == PieceType.KNIGHT) {
                    for (int i = 0; i < steps.length; i++) {
                        end = board.step(mover, steps[i], current, current.getColour() != end.getColour());
                        if (!board.isLegalMove(current, end)) {
                            continue;
                        } else {
                            li.add(new movepair(current, end));
                        }
                    }
                } else if (type == PieceType.BISHOP || type == PieceType.QUEEN || type == PieceType.ROOK) {
                    int reps = 1 + type.getStepReps();
                    for (int i = 0; i < steps.length; i++) {
                        for (int j = 0; j < reps; j++) {
                            end = board.step(mover, steps[i], current, current.getColour() != end.getColour());
                            if (!board.isLegalMove(current, end)) {
                                continue;
                            } else {
                                li.add(new movepair(current, end));
                                current = end;
                            }
                        }
                    }
                }
            } catch (ImpossiblePositionException e) {
            }
            // catch(CloneNotSupportedException e) {}
        }
        return li;
    }

/**
 * 
 * @param parent
 * @return
 */
    public node maxUct(node parent) {
        // double max = Double.MIN_VALUE;
        // double scalar = 1/Math.sqrt(2.0);
        Colour turn = parent.getBoard().getTurn();
        ArrayList<node> childList = parent.getChildrens();
        int index = 0;
        if(turn == Colour.BLUE) index = 0;
        else if(turn == Colour.GREEN) index =1;
        else index = 2;
        int maxindex = 0;
        double[] ucb = new double[childList.size()];
        if(childList.size()==0){
            return parent;
        }
        for (int i = 0; i < childList.size(); i++) {
            node n = childList.get(i);
            if (n.visit == 0) {
                return n;
            } else {
                ucb[i] = (n.getReward()[index] / n.getVisit()) + (2 * Math.sqrt(Math.log(parent.getVisit()) / n.getVisit()));
                if (ucb[maxindex] < ucb[i]) {
                    maxindex = i;
                }
            }
        }
        return childList.get(maxindex);
    }

    public double[] mcts(node currentnode) {
        double[] result;
        node bestChild = maxUct(currentnode);
        if(bestChild.getVisit() == 0){
            Expand(bestChild);
            result = playOut(bestChild);
        }
        else{
            result = mcts(bestChild);
        }
        bestChild.update(result);
        //if (currentnode.getChildrens().size() == 0) {
            // if(currentnode.getVisit() == 0){
            //     result = playOut(currentnode);
            // }
            // else{
            //     Expand(currentnode);
            // }
        //}
        // for(node i : currentnode.getChildrens())
        //     System.out.println("children" + " " + i.getStart());
        // else{
            
        // }
        // if (bestChild.getVisit() == 0) {
        //     // Expand(bestChild);
        //     result = playOut(bestChild);
        //    // System.out.println("result" + " " + result);
        // } else {
        //     result = mcts(bestChild);
        // }
        //bestChild.update(result);
        // System.out.println("bestchild visit" + " " + bestChild.getVisit());
        // System.out.println("bestchild reward" + " " + bestChild.getReward());
        return result;
    }

/**
 * 
 * @param node
 */
    public void Expand(node node) {
        Board board = node.getBoard();
        Position[] piece = board.getPositions(board.getTurn()).toArray(new Position[0]);
        ArrayList<movepair> li = getlegalmoves(board, piece);
        for (movepair mv : li) {
            try {
                Board boardcopy = (Board) board.clone();
                boardcopy.move(mv.getStart(), mv.getEnd());
                node.setChildren(boardcopy, mv.getStart(), mv.getEnd());
                // nodes.add(new node(boardcopy));
            } catch (ImpossiblePositionException e) {
                System.out.println("Illeagal Positison");
            } catch (CloneNotSupportedException e) {
                System.out.println("Clone Not Support");
            }
        }
    }

/**
 * 
 * @param node
 * @return
 */
    public double[] playOut(node node) {
        Board board = node.getBoard();
        Colour turn = board.getTurn();

        Board boardcopy = null;
        try{
            boardcopy = (Board) board.clone();
        }catch(CloneNotSupportedException e){}
            // catch (CloneNotSupportedException e) {}
            // ArrayList<movepair> moves = getlegalmoves(board, piece);
        int count = 0;
        while (!boardcopy.gameOver()) {
            //System.out.println(boardcopy.getTurn());
            Position[] piece = boardcopy.getPositions(boardcopy.getTurn()).toArray(new Position[0]);
            ArrayList<movepair> moves = getlegalmoves(boardcopy, piece);
            if(count > 500 || moves.size()==0) {
                System.out.println("yes");
                double[] result = new double[] {1,1,1};
                return result;
            }
           // System.out.println("score is" + " " + boardcopy.score(boardcopy.getTurn()));
            // if(moves.size() == 0) {
            //     double[] result = new double[] {1,1,1};
            //     return result;
            //     //System.out.println("colour is " + " " + boardcopy.getTurn() + "size is" + " "+ moves.size()); 
            // }

            // BRS1 brs = new BRS1();
            // Position start, end;
            // Position[] choice = brs.playMove(boardcopy);
            // start = choice[0];
            // end = choice[1];
            // System.out.println("brs move is" + " " +start + " " + end);

            // int[] vals = new int[moves.size()];
            // int maxindex = 0;
            // for(int i=0;i < moves.size();i++){
            //     //Board boardcopy1 = null;
            //     try{
            //         Board boardcopy1 = (Board) boardcopy.clone();
            //         boardcopy1.move(moves.get(i).getStart(), moves.get(i).getEnd());
            //         vals[i] = brs.eval(boardcopy1);
            //         //System.out.println(vals[i]);
            //         if(vals[maxindex]<vals[i]){
            //             maxindex = i;
            //         }
            //     }catch (CloneNotSupportedException e){System.out.println("Clone Not Support");}
            //     catch(ImpossiblePositionException e) {System.out.println("Impossible Position");}
            // }

            //System.out.println("maxindex is" + " " + maxindex + "and move is : " + " " + moves.get(maxindex).getStart() + " " + moves.get(maxindex).getEnd());
            // Position[] choice = brs.playMove(boardcopy);
            //{System.out.println("yes"); System.out.println(board.getWinner());}
           
           // System.out.println("colour is " + " " + boardcopy.getTurn() + "size is" + " "+ moves.size()); 
            
            // System.out.println(board.getWinner());
            
            // Position start = moves.get(random.nextInt(moves.size())).getStart();
            // Position end = moves.get(random.nextInt(moves.size())).getEnd();

            movepair mv = moves.get(random.nextInt(moves.size()));
            Position start = mv.getStart();
            Position end = mv.getEnd();

            // Position start = moves.get(maxindex).getStart();
            // Position end = moves.get(maxindex).getEnd();
            
            // System.out.println("start is" + " " + start + "end is" + " " + end);
            //System.out.println("move size is" + " " + moves.size());
            try {
                boardcopy.move(start, end);
            } catch (ImpossiblePositionException e) {}
            //System.out.println(boardcopy.getCaptured(Colour.BLUE));
            count++;
        }
        Colour winner = boardcopy.getWinner();
        // System.out.println("count is:" + " " + count);
        System.out.println("winner is:" + " " + winner);
        double[] result;
        if(winner == Colour.BLUE) {result = new double[] {1, 0, 0};}
        else if(winner == Colour.GREEN) {result = new double[] {0, 1, 0};}
        else {result = new double[] {0, 0, 1};}
        return result;
    }
/**
 * Return the final moves with highest win rate.
 * Can apply robustChild() and maxChild() policy.
 * @param root
 * @return 
 */
    public Position[] chooseFinalMove(node root){
        Colour turn = root.getBoard().getTurn();
        int index = 0;
        if(turn == Colour.BLUE) index = 0;
        else if(turn == Colour.GREEN) index =1;
        else index = 2;
        node bestChild = bestChild(root, index);
        Position[] choice = new Position[2];
        System.out.println("bestchild is :" + bestChild.getReward()[index]);
        choice[0] = bestChild.getStart();
        choice[1] = bestChild.getEnd();
        return choice;
    }

/**
 * Return the node with highest win rate.
 * @param node
 * @param index
 * @return node
 */
    public node bestChild(node node, int index){
        ArrayList<node> childlist = node.getChildrens();
        int maxindex = 0;
        double[] reward = new double[childlist.size()];
        for(int i=0; i < childlist.size();i++){
            reward[i] = childlist.get(i).getReward()[index] / childlist.get(i).getVisit();
            if(reward[maxindex] < reward[i]){
                maxindex = i;
            }
        }
        node bestChild = childlist.get(maxindex);
        return bestChild;
    } 

/**
 * Return the node with highest visits.
 * @param node
 * @param index
 * @return node
 */
    public node robustChild(node node, int index){
        ArrayList<node> childlist = node.getChildrens();
        int maxindex = 0;
        double[] reward = new double[childlist.size()];
        for(int i=0; i < childlist.size();i++){
            reward[i] = childlist.get(i).getVisit();
            if(reward[maxindex] < reward[i]){
                maxindex = i;
            }
        }
        node bestChild = childlist.get(maxindex);
        return bestChild;
    } 

/**
 * Return the node with highest score.
 * @param node
 * @param index
 * @return node
 */
    public node maxChild(node node, int index){
        ArrayList<node> childlist = node.getChildrens();
        int maxindex = 0;
        double[] reward = new double[childlist.size()];
        for(int i=0; i < childlist.size();i++){
            reward[i] = childlist.get(i).getReward()[index];
            if(reward[maxindex] < reward[i]){
                maxindex = i;
            }
        }
        node bestChild = childlist.get(maxindex);
        return bestChild;
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
        Colour maxturn = board.getTurn();
        int n = 0;
        // System.out.println(maxturn);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(board.getTimeLeft(maxturn));
        //System.out.println(seconds);
        node root = new node(board, null, null);
        while(n < 1000){
            try{
                //System.out.println(1);
                double[] result = mcts(root);
                // System.out.println(result);
                root.update(result);
                n++;
            }
            catch (IndexOutOfBoundsException e) {}
            catch (NullPointerException e) {}
        }
        System.out.println("root visit is" + " " + root.getVisit());
        //System.out.println("reward is" + " " + root.getReward());
        return chooseFinalMove(root);
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



