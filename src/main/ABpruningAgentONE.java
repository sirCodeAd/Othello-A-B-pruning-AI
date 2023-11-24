package main;

import java.util.List;

import com.eudycontreras.othello.capsules.AgentMove;
import com.eudycontreras.othello.capsules.MoveWrapper;
import com.eudycontreras.othello.capsules.ObjectiveWrapper;
import com.eudycontreras.othello.controllers.Agent;
import com.eudycontreras.othello.controllers.AgentController;
import com.eudycontreras.othello.enumerations.BoardCellState;
import com.eudycontreras.othello.enumerations.BoardCellType;
import com.eudycontreras.othello.enumerations.PlayerTurn;
import com.eudycontreras.othello.models.GameBoardState;
import com.eudycontreras.othello.threading.ThreadManager;
import com.eudycontreras.othello.threading.TimeSpan;

public class ABpruningAgentONE extends Agent{

    //private PlayerTurn player_turn = PlayerTurn.PLAYER_ONE;

    public ABpruningAgentONE() {
		this(PlayerTurn.PLAYER_ONE);
	}
	
	public ABpruningAgentONE(String name) {
		super(name, PlayerTurn.PLAYER_ONE);
	}
	
	public ABpruningAgentONE(PlayerTurn playerTurn) {
		super(playerTurn);
	
	}

    @Override
    public AgentMove getMove(GameBoardState gameState) {
        return getABpruningMove(gameState);
        
    }


    private AgentMove getABpruningMove(GameBoardState gameState) {
        
        long startTime = System.currentTimeMillis();
        int maxSearchTime = 5000; // 5 seconds in milliseconds

        int bestScore = this.getPlayerTurn() == PlayerTurn.PLAYER_ONE ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        MoveWrapper bestMoveWrapper = null;

        List<ObjectiveWrapper> possibleMoves = AgentController.getAvailableMoves(gameState, this.getPlayerTurn());

        for (ObjectiveWrapper move : possibleMoves) {
            GameBoardState newState = AgentController.getNewState(gameState, move);
            int score = AB_pruning(newState, UserSettings.MAX_SEARCH_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE, this.getPlayerTurn() != PlayerTurn.PLAYER_ONE);

            if ((this.getPlayerTurn() == PlayerTurn.PLAYER_ONE && score > bestScore) || 
                (this.getPlayerTurn() != PlayerTurn.PLAYER_ONE && score < bestScore)) {
                bestScore = score;
                bestMoveWrapper = new MoveWrapper(move);
            }

            if (AgentController.timeLimitExceeded(maxSearchTime, startTime)) {
                break; // Stop searching if time limit exceeded
            }
        }

        return bestMoveWrapper;

    }

    private int AB_pruning(GameBoardState node, int search_depth, int alpha, int beta, boolean max_player) {
        
        if (search_depth == 0 || node.isTerminal())
        {
            return (int) node.getStaticScore(max_player ? BoardCellState.WHITE : BoardCellState.BLACK);
        }

        if(max_player)
        {
            int max_evaluation = Integer.MIN_VALUE;
            
            for (GameBoardState child : node.getChildStates())
            {
                int evaluation = AB_pruning(child, search_depth - 1, alpha, beta, false);
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
                int evaluation = AB_pruning(child, search_depth - 1, alpha, beta, true);

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