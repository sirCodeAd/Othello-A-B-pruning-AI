package main;

import java.util.List;

import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.controllers.AgentController;
import com.eudycontreras.othello.enumerations.BoardCellState;
import com.eudycontreras.othello.enumerations.PlayerTurn;
import com.eudycontreras.othello.models.GameBoardState;
import com.eudycontreras.othello.threading.ThreadManager;
import com.eudycontreras.othello.threading.TimeSpan;

public class ABpruningAgentTWO extends Agent{

    //private PlayerTurn player_turn = PlayerTurn.PLAYER_ONE;

    public ABpruningAgentTWO() {
		this(PlayerTurn.PLAYER_TWO);
	}
	
	public ABpruningAgentTWO(String name) {
		super(name, PlayerTurn.PLAYER_TWO);
	}
	
	public ABpruningAgentTWO(PlayerTurn playerTurn) {
		super(playerTurn);
	
	}

    @Override
    public AgentMove getMove(GameBoardState gameState) {
        return getABpruningMove(gameState);
        
    }


    private AgentMove getABpruningMove(GameBoardState gameState) {
        
        long startTime = System.currentTimeMillis();

        BoardCellState maxColor = this.getPlayerTurn() == PlayerTurn.PLAYER_ONE ? BoardCellState.WHITE : BoardCellState.BLACK; 

        int bestScore = UserSettings.MIN_VALUE;
        MoveWrapper bestMoveWrapper = null;

        List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(gameState, this.getPlayerTurn());

        for (ObjectiveWrapper move : possibleMoves) {
            GameBoardState newState = AgentController.getNewState(gameState, move);
            int score = AB_pruning(newState, AgentController.MAX_SEARCH_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, true, maxColor);

            if (score > bestScore) {

                bestScore = score;
                bestMoveWrapper = new MoveWrapper(move);

            }

            if (AgentController.timeLimitExceeded(UserSettings.MAX_SEARCH_TIME, startTime)) {
                break; // Stop searching if time limit exceeded
            }
        }

        return bestMoveWrapper;

    }

    private int AB_pruning(GameBoardState node, int search_depth, int alpha, int beta, boolean max_player, BoardCellState maxColor) {
        
        if (search_depth == 0 || node.isTerminal())
        { 
            return (int) node.getStaticScore(maxColor);
        }

        if(max_player)
        {
            int max_evaluation = Integer.MIN_VALUE;
            
            for (GameBoardState child : node.getChildStates())
            {
                int evaluation = AB_pruning(child, search_depth - 1, alpha, beta, false, maxColor);
                max_evaluation = Math.max(max_evaluation, evaluation);
                alpha = Math.max(alpha, evaluation);
                if (alpha >= beta)
                {
                    break;
                }
            }
            return max_evaluation;
        }
        else
        {
            int min_evaluation = Integer.MAX_VALUE;
            
            for (GameBoardState child : node.getChildStates())
            {
                int evaluation = AB_pruning(child, search_depth - 1, alpha, beta, true, maxColor);

                min_evaluation = Math.min(min_evaluation, evaluation);
                beta = Math.min(beta, evaluation);
                if(alpha >= beta)
                {
                    break;
                }
            }
            return min_evaluation;
        }
    
    }
    
}
