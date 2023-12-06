package main;

import java.util.List;

import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.controllers.AgentController;
import com.eudycontreras.othello.controllers.GameController;
import com.eudycontreras.othello.enumerations.BoardCellState;
import com.eudycontreras.othello.enumerations.BoardCellType;
import com.eudycontreras.othello.enumerations.PlayerTurn;
import com.eudycontreras.othello.models.GameBoardState;
import com.eudycontreras.othello.threading.ThreadManager;
import com.eudycontreras.othello.threading.TimeSpan;

public class ABpruningAgentONE extends Agent{

    //private PlayerTurn player_turn = PlayerTurn.PLAYER_ONE;

    private GameController gameController;
    private GameBoardState gameBoardState;

    public ABpruningAgentONE(GameController gameController) {
		this(gameController, PlayerTurn.PLAYER_ONE);
	}
	
	public ABpruningAgentONE(String name) {
		super(name, PlayerTurn.PLAYER_ONE);
	}
	
	public ABpruningAgentONE(GameController gameController, PlayerTurn playerTurn) {
		super(playerTurn);
        this.gameController = gameController;
	}

    @Override
    public AgentMove getMove(GameBoardState gameState) {
        //resetCounters();
        return getABpruningMove(gameState);
        
    }


    private AgentMove getABpruningMove(GameBoardState gameState) {

        /*
         * Set searchDepth = 2 because my computer cant handle more.
         */
        setSearchDepth(20);

        /*
         * Used to record when search time has started.
         */
        long startTime = System.currentTimeMillis();


        //List<GameBoardState> childStates = gameState.generateChildStates(gameState);
        //gameState.addChildStates(childStates.toArray(new GameBoardState[0]));

        /*
         * This score is used to be able to determine the best possible move
         * for the agent
         */
        int bestScore = UserSettings.MIN_VALUE;

        /*
         * Initialzing move to null at the moment
         */
        MoveWrapper bestMoveWrapper = new MoveWrapper(null);

        /*
         * We use the function getAvailableMoves from the games current state for the agent
         * and save them to a List of possible moves.
         */
        List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(gameState, PlayerTurn.PLAYER_ONE);

        /*
         * We then iterate through each move and call for the AB_pruning method to evaluate a score for each move and
         * what the game state would be if the agent made that move, which will then determine
         * the move that has the best outcome for the maxplayer. 
         */
        for (ObjectiveWrapper move : possibleMoves) {
           // GameBoardState newState = AgentController.getNewState(gameState, move);
            int score = AB_pruning(gameState, getSearchDepth(), UserSettings.MIN_VALUE, UserSettings.MAX_VALUE, true);
            
            /*
             * Each score that is higher than the bestscore variable gets saved to that variable
             * and the move that gives the best score is saved to the bestMoveWrapper which
             * will be executed by getMove method.
             */
            if (this.getPlayerTurn() == PlayerTurn.PLAYER_ONE && score > bestScore) {

                bestScore = score;
                bestMoveWrapper = new MoveWrapper(move);

            }

            /*
             * Break search time if 5 seconds has passed.
             */
            if (AgentController.timeLimitExceeded(UserSettings.MAX_SEARCH_TIME, startTime)) {
                break; // Stop searching if time limit exceeded
            }
        }

        /*
         * The best move is then returned to getMove method
         */
        return bestMoveWrapper;

    }

    /*
     * AB-pruning Algorithm which will be used by the computer-based agent to calculate best possible move
     * it can choose in turn-based games.
     */
    private int AB_pruning(GameBoardState node, int search_depth, int alpha, int beta, boolean max_player) {

        /*
         * Increments each nodes examined and set the nodesexamined variable to that number.
         */
        setNodesExamined(getNodesExamined() + 1);

        /*
         * To be able to update the current search depth the agent is at we take the 
         * current search depth the agent is at (getSearchDepth()) and then subtract 
         * the total searchdepth.
         */
        int currentDepth = getSearchDepth() - search_depth;
        setSearchDepth(Math.max(getSearchDepth(), currentDepth));
       
        /*
         * We check if the agent has reach the end of its search depth. that is reached the leafs
         * of the move tree or if the a terminal state has been reached.
         */
        if (search_depth == 0 || node.isTerminal())
        {
            setReachedLeafNodes(getReachedLeafNodes() + 1);
            
            //return (int) node.getStaticScore(max_player ? BoardCellState.WHITE : BoardCellState.BLACK);

            /*
             * Here we get a numeric value of how favorable the state is for the
             * agent that searches for its best possible move.
             * This also is key for the algorithm because the values that is provided
             * is used to be able to determine which branches is to be pruned and so on.
             */
            return (int) AgentController.getGameEvaluation(node, playerTurn.PLAYER_ONE);
        } 
       
        /*
         * When the AB_pruning is called we check if it max_player is true
         */
        if(max_player)
        {   
            /*
             * This is the variable which will be used by the logic to
             * compare each node with and set the best possible score for the
             * maximazing plyer. It is set to -infinity
             */
            int max_evaluation = Integer.MIN_VALUE;
            
            /*
             * We iterate trough all childstates from the root and recursivly call for the ab_pruning function.
             */
            for (GameBoardState child : node.getChildStates())
            {
                
                /*
                 * Each child state returns the best possible score that can be achived by that state
                 */
                int evaluation = AB_pruning(child, search_depth - 1, alpha, beta, false);
                
                /*
                 * This variable is updated to the best possible score
                 * for the current node that is examined by the algorithm
                 */
                max_evaluation = Math.max(max_evaluation, evaluation);
                
                /*
                 * Alpha is then updated with the best possible score that it can
                 * get from the evaluated scores for the nodes
                 */
                alpha = Math.max(alpha, evaluation);

                /*
                 * When this if statement is true it means that the agent has found a move
                 * that guarantees a score equal or better then beta and there is no point in
                 * exploring other branches because the min_player will avoid these branches anyway,
                 * i.e it will not make a move that will lead to those branches. 
                 * 
                 * So we break from the loop.
                 */
                if (alpha >= beta)
                {
                    setPrunedCounter(getPrunedCounter() + 1);
                    break;
                }
            }
            
            /*
             * Each child node gets a evaluated value returned.
             */
            return max_evaluation;
        }

        /*
         * this is the logic used by the minimazing player.
         */
        else
        {   
            /*
             * This variable will be compared to each evaluted score for each node which will benefit the
             * minimizing player. Its set to +infinity 
             */
            int min_evaluation = Integer.MAX_VALUE;
           
            for (GameBoardState child : node.getChildStates())
            {   
                /*
                 * Here we call the AB_pruning method recursively to be able to update each child node with values
                 * that will benefit the minimizing player.
                 */
                int evaluation = AB_pruning(child, search_depth - 1, alpha, beta, true);

                /*
                 * The score that gets evaluated is compared to min_evaluation so that the smallest of those 
                 * is saved. This is because the mini_player works against the max_player
                 */
                min_evaluation = Math.min(min_evaluation, evaluation);

                /*
                 * beta is then compared with that score aswell and if the beta score is smaller than
                 * the alpha score then we break from the loop because there is no point for the agent
                 * to search more branches from that node because it will not benefit the min_player doing so. 
                 */
                beta = Math.min(beta, evaluation);
                if(alpha >= beta)
                {
                    setPrunedCounter(getPrunedCounter() + 1);
                }
            }
            
            return min_evaluation;
        }
        
        
    }
    
    
}