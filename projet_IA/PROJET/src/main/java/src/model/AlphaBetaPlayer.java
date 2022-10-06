/* Name: ComputerPlayer
 * Author: Devon McGrath
 * Description: This class represents a computer player which can update the
 * game state without user interaction.
 */

package src.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

/**
 * The {@code ComputerPlayer} class represents a computer player and updates
 * the board based on a model.
 */
public class AlphaBetaPlayer extends MinMaxPlayer {

	//public boolean player;
	
	public AlphaBetaPlayer(boolean joueur) {
		super(joueur);
	}
	@Override
	public boolean isHuman() {
		return false;
	}

    private Integer maxValue(Game game, int depth, int maxdepth, int alpha, int beta) {
        Integer v = Integer.MIN_VALUE; //simule l'infini négatif
		if (transpositionTableMax.getValue(game) != null) {
			return transpositionTableMax.getValue(game); //On a déjà calculé la valeur de cet état
		} else if (depth == maxdepth) { //on a atteint la profondeur max, on calcule l'heuristique
			int value = game.goodHeuristic(this.player);
			transpositionTableMax.add(game, value);
			return value;
		} else {
			List<Move> moves = new ArrayList<Move>();
			moves = getMoves(game);
			Iterator<Move> ite = moves.iterator();
			while (ite.hasNext()) { //On parcourt les coups pour développer leurs états fils (avec newGame = un état fils)
				Move move = ite.next();
				Game newGame = game.copy();
				newGame.move(move.getStartIndex(), move.getEndIndex());
				if(newGame.isGameOver()) { //Après avoir joué un coup, si la partie est finie, le joueur qui a joué le coup a gagné
					transpositionTableMax.add(newGame, Integer.MAX_VALUE);
					return Integer.MAX_VALUE; //MaxPlayer a gagné
				}
				if(game.isP2Turn() == this.player) {
					v = Math.max(v, maxValue(newGame, depth + 1, maxdepth, alpha, beta));
				}
				else {
					v = Math.max(v, minValue(newGame, depth + 1, maxdepth, alpha, beta));
				}
				transpositionTableMax.add(newGame, v);
                if (v >= beta) {
                	return v;
                }
                alpha = Math.max(alpha, v);     
            }
		}
		return v;
	}

	private Integer minValue(Game game, int depth, int maxdepth, int alpha, int beta) {
		if (transpositionTableMin.getValue(game) != null) {
			return transpositionTableMin.getValue(game); //On a déjà calculé la valeur de cet état
		} else if (depth == maxdepth) {
			int value = game.goodHeuristic(this.player);//On a atteint la profondeur max autorisée, on calcule l'heursitique
			transpositionTableMin.add(game, value);//On enregistre la valeur dans la table
			return value;
		} else {
			List<Move> moves = new ArrayList<Move>();
			moves = getMoves(game);
			Iterator<Move> ite = moves.iterator();
			Integer v = Integer.MAX_VALUE;//Integer.MAX_VALUE est le plus grand Integer possible avec l'objet "Integer"
			while (ite.hasNext()) {//On parcourt les coups pour développer leurs états fils (avec newGame = un état fils)
				Move move = ite.next();
				Game newGame = game.copy();
				newGame.move(move.getStartIndex(), move.getEndIndex());
				if(newGame.isGameOver()) {//Après avoir joué un coup, si la partie est finie, le joueur qui a joué le coup a gagné
					transpositionTableMax.add(newGame, Integer.MIN_VALUE);
					return Integer.MIN_VALUE;//MinPlayer a gagné, la valeur renvoyée est le plus petit Integer possible
				}
				if(game.isP2Turn() == !this.player) {//MaxPlayer joue après MinPlayer
					v = Math.min(v, maxValue(newGame, depth + 1, maxdepth, alpha, beta));
				}
				else{//MinPlayer rejoue
					v = Math.min(v, minValue(newGame, depth + 1, maxdepth, alpha, beta));
				}
				transpositionTableMin.add(newGame, v);
                if (v <= alpha) {
                	return v;
                }          
                beta = Math.min(beta, v); 
			}
			return v;
		}
	}

	@Override
	public void updateGame(Game game) {

		// Nothing to do
		if (game == null || game.isGameOver()) {
			return;
		}
		
		transpositionTableMax = new StateSet(); //Table d'enregistrements des coups du MaxPlayer
		transpositionTableMin = new StateSet(); //Table d'enregistrements des coups du MinPlayer
		
		StateSet states;
		int maxDepth = 6;
		int startIndex = -1; //valeur sans importance
		int endIndex = -1; //valeur sans importance
		
		List<Move> moves = new ArrayList<Move>();
		moves = getMoves(game);
		Iterator<Move> ite = moves.iterator();
		
		if(game.isP2Turn() == this.player){ //C'est au MaxPlayer de jouer (faire la table de verité pour bien comprendre)
			Integer v = maxValue(game, 0, maxDepth, Integer.MAX_VALUE, Integer.MIN_VALUE);
			states = this.transpositionTableMax;
			while (ite.hasNext()) {//On parcourt les actions possibles et on cherche celle qui a la valeur 'v'
                Move move = ite.next();
                Game newGame = game.copy();
                newGame.move(move.getStartIndex(), move.getEndIndex());
                if(v == states.getValue(newGame)) {
					startIndex = move.getStartIndex();
					endIndex = move.getEndIndex();
                }
                            
            }
		}
		game.move(startIndex, endIndex); //On effectue un coup avec le meilleur coup trouvé
	}

}
