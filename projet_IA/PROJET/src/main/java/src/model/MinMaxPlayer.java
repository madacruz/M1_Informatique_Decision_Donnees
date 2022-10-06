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

import src.logic.MoveGenerator;

/**
 * The {@code ComputerPlayer} class represents a computer player and updates the
 * board based on a model.
 */
public class MinMaxPlayer extends ComputerPlayer {

	protected boolean player;

	protected StateSet transpositionTableMax, transpositionTableMin;

	public MinMaxPlayer(boolean joueur) {
		this.player = joueur;
		this.transpositionTableMax = new StateSet();
		this.transpositionTableMin = new StateSet();
	}

	@Override
	public boolean isHuman() {
		return false;
	}
	
	private Integer maxValue(Game game, int depth, int maxdepth) {
		if (transpositionTableMax.getValue(game) != null) {
			return transpositionTableMax.getValue(game); //On a déjà calculé la valeur de cet état
		} else if (depth == maxdepth) { //on atteint la profondeur max donc on calcule l'heuristique
			int value = game.goodHeuristic(this.player);
			transpositionTableMax.add(game, value); //on enregistre la valeur dans la table
			return value;
		} else { //on n'a pas encore atteint la profondeur max de l'arbre
			List<Move> moves = new ArrayList<Move>();
			moves = getMoves(game);
			Iterator<Move> ite = moves.iterator();
			Integer v = Integer.MIN_VALUE; //simule l'infini négatif
			while (ite.hasNext()) { //On parcourt les coups pour développer leurs états fils (avec newGame = un état fils)
				Move move = ite.next();
				Game newGame = game.copy();
				newGame.move(move.getStartIndex(), move.getEndIndex());
				if(newGame.isGameOver()) { //Après avoir joué un coup, si la partie est finie, le joueur qui a joué le coup a gagné
					transpositionTableMax.add(newGame, Integer.MAX_VALUE);
					return Integer.MAX_VALUE; //MaxPlayer a gagné
				}
				if(game.isP2Turn() == this.player) {
					v = Math.max(v, maxValue(newGame, depth + 1, maxdepth));
				}
				else {
					v = Math.max(v, minValue(newGame, depth + 1, maxdepth));
				}
				transpositionTableMax.add(newGame, v);
			}
			return v;
		}
	}

	private Integer minValue(Game game, int depth, int maxdepth) {
		if (transpositionTableMin.getValue(game) != null) {
			return transpositionTableMin.getValue(game); //On a déjà calculé la valeur de cet état
		} else if (depth == maxdepth) {
			int value = game.goodHeuristic(this.player);//On a atteint la profondeur max autorisée, on calcule l'heursitique
			transpositionTableMin.add(game, value);//On enregistre la valeur dans la table
			return value;
		} else { //on n'a pas encore atteint la profondeur max de l'arbre
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
					v = Math.min(v, maxValue(newGame, depth + 1, maxdepth));
				}
				else {//MinPlayer rejoue
					v = Math.min(v, minValue(newGame, depth + 1, maxdepth));
				}
				transpositionTableMin.add(newGame, v);
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
		int maxDepth = 8;
		int startIndex = -1; //valeur sans importance
		int endIndex = -1; //valeur sans importance
		
		List<Move> moves = new ArrayList<Move>();
		moves = getMoves(game);
		Iterator<Move> ite = moves.iterator();
		
		if(game.isP2Turn() == this.player){ //C'est au MaxPlayer de jouer (faire la table de verité pour bien comprendre)
			maxValue(game, 0, maxDepth);
			states = this.transpositionTableMax;
			Integer v = Integer.MIN_VALUE;
			
			while (ite.hasNext()) {//On parcourt la table d'enregistrements déjà complétée pour retrouver le meilleur coup (la plus grande valeur)
				Move move = ite.next();
				Game newGame = game.copy();
				newGame.move(move.getStartIndex(), move.getEndIndex());
                        
				if(v < states.getValue(newGame)) {
					v = states.getValue(newGame);
					startIndex = move.getStartIndex();
					endIndex = move.getEndIndex();
				}
			}
		}
		game.move(startIndex, endIndex); //On effectue un coup avec le meilleur coup trouvé
	}

	/**
	 * Gets all the available moves and skips for the current player.
	 * 
	 * @param game the current game state.
	 * @return a list of valid moves that the player can make.
	 */
	protected List<Move> getMoves(Game game) {

		// The next move needs to be a skip
		if (game.getSkipIndex() >= 0) {

			List<Move> moves = new ArrayList<>();
			List<Point> skips = MoveGenerator.getSkips(game.getBoard(), game.getSkipIndex());
			for (Point end : skips) {
				Game copy = game.copy();
				int startIndex = game.getSkipIndex(), endIndex = Board.toIndex(end);
				copy.move(startIndex, endIndex);
				moves.add(new Move(startIndex, endIndex, copy.goodHeuristic(!copy.isP2Turn())));
			}
			Collections.sort(moves);
			return moves;
		}

		// Get the checkers
		List<Point> checkers = new ArrayList<>();
		Board b = game.getBoard();
		if (game.isP2Turn()) {
			checkers.addAll(b.find(Board.BLACK_CHECKER));
			checkers.addAll(b.find(Board.BLACK_KING));
		} else {
			checkers.addAll(b.find(Board.WHITE_CHECKER));
			checkers.addAll(b.find(Board.WHITE_KING));
		}

		// Determine if there are any skips
		List<Move> moves = new ArrayList<>();
		for (Point checker : checkers) {
			int index = Board.toIndex(checker);
			List<Point> skips = MoveGenerator.getSkips(b, index);
			for (Point end : skips) {
				Game copy = game.copy();
				int endIndex = Board.toIndex(end);
				copy.move(index, endIndex);
				Move m = new Move(index, endIndex, copy.goodHeuristic(!copy.isP2Turn()));
				moves.add(m);
			}
		}

		// If there are no skips, add the regular moves
		if (moves.isEmpty()) {
			for (Point checker : checkers) {
				int index = Board.toIndex(checker);
				List<Point> movesEnds = MoveGenerator.getMoves(b, index);
				for (Point end : movesEnds) {
					Game copy = game.copy();
					int endIndex = Board.toIndex(end);
					copy.move(index, endIndex);
					moves.add(new Move(index, endIndex, copy.goodHeuristic(!copy.isP2Turn())));
				}
			}
		}
		Collections.sort(moves);
		return moves;
	}
}
