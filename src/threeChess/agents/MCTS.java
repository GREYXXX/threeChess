package threeChess.agents;

import threeChess.*;
import java.lang.Math;

import java.util.*;

public class MCTS extends Agent {
    private static final String name = "MCTS";
    private static final Random random = new Random();


/**
 * move class with start and end.
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
 * Node class
 * Each node will contain board state, moves, number of visit and score.
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
/**
 * update the node for back propagate
 * @param reward
 */
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

    }

    public MCTS() {}
    
/**
 * Return the legale moves of current board state
 * @param board The current board state.
 * @param piece All of the pieces for player. 
 * @return li The legale moves of current board state
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
            } catch (ImpossiblePositionException e) {}
        }
        return li;
    }

/**
 * If parent is a leaf node, return; 
 * Else return the children node with maximum ucb value
 * @param parent
 * @return best child node
 */
    public node maxUct(node parent) {
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

/**
 * Selection and back propagation
 * @param currentnode
 * @return result The score, win is 1, lose is 0.
 */
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
        return result;
    }

/**
 * Adding all possible moves for current node as the childrens
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
            } catch (ImpossiblePositionException e) {
                System.out.println("Illeagal Positison");
            } catch (CloneNotSupportedException e) {
                System.out.println("Clone Not Support");
            }
        }
    }

/**
 * Play random moves from current node until the game is over.
 * @param node
 * @return result The array with the score corresponding to the player achieved win
 */
    public double[] playOut(node node) {
        Board board = node.getBoard();
        Board boardcopy = null;
        try{
            boardcopy = (Board) board.clone();
        }catch(CloneNotSupportedException e){}
        int count = 0;
        while (!boardcopy.gameOver()) {
            Position[] piece = boardcopy.getPositions(boardcopy.getTurn()).toArray(new Position[0]);
            ArrayList<movepair> moves = getlegalmoves(boardcopy, piece);
            //If over than 500 moves but the game isn't over, return a draw.
            if(count > 500 || moves.size()==0) {
                double[] result = new double[] {1,1,1};
                return result;
            }
            movepair mv = moves.get(random.nextInt(moves.size()));
            Position start = mv.getStart();
            Position end = mv.getEnd();
            try {
                boardcopy.move(start, end);
            } catch (ImpossiblePositionException e) {}
            count++;
        }
        Colour winner = boardcopy.getWinner();
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
 * @return  choice The final moves
 */
    public Position[] chooseFinalMove(node root){
        Colour turn = root.getBoard().getTurn();
        int index = 0;
        if(turn == Colour.BLUE) index = 0;
        else if(turn == Colour.GREEN) index =1;
        else index = 2;
        node bestChild = bestChild(root, index);
        Position[] choice = new Position[2];
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
 * Return the node with highest number of visits.
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
        int n = 0;
        node root = new node(board, null, null);
        while(n < 80){
            try{
                double[] result = mcts(root);
                root.update(result);
                n++;
            }
            catch (IndexOutOfBoundsException e) {}
            catch (NullPointerException e) {}
        }
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



