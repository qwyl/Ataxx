/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;
import static java.lang.Math.min;
import static java.lang.Math.max;

/** A Player that computes its own moves.
 *  @author Qi Li Yang
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove, int sense,
                       int alpha, int beta) {
        /* We use WINNING_VALUE + depth as the winning value so as to favor
         * wins that happen sooner rather than later (depth is larger the
         * fewer moves have been made. */
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        Move best = null;
        int bestScore;
        bestScore = 0;
        if (sense == 1) {
            bestScore = -INFTY;
            ArrayList<Move> moves = possibleMoves(RED, board);
            for (int i = 0; i < moves.size(); i++) {
                Move currMove = moves.get(i);
                Board currBoard = new Board(board);
                if (currBoard.legalMove(currMove)) {
                    currBoard.makeMove(currMove);
                    int childScore = minMax(currBoard, depth - 1,
                            saveMove, 0 - sense, alpha, beta);
                    if (childScore > bestScore) {
                        bestScore = childScore;
                        alpha = max(alpha, bestScore);
                        best = currMove;
                        if (alpha >= beta) {
                            return bestScore;
                        }
                    }
                }
            }
        } else if (sense == -1) {
            bestScore = INFTY;
            ArrayList<Move> moves = possibleMoves(BLUE, board);
            for (int i = 0; i < moves.size(); i++) {
                Move currMove = moves.get(i);
                Board currBoard = new Board(board);
                if (currBoard.legalMove(currMove)) {
                    currBoard.makeMove(currMove);
                    int childScore =
                            minMax(currBoard, depth - 1, saveMove,
                                    0 - sense, alpha, beta);
                    if (childScore < bestScore) {
                        bestScore = childScore;
                        beta = min(beta, bestScore);
                        best = currMove;
                        if (alpha >= beta) {
                            return bestScore;
                        }
                    }
                }
            }
        }
        if (saveMove) {
            _lastFoundMove = best;
            if (_lastFoundMove == null) {
                _lastFoundMove = Move.pass();
            }
        }
        return bestScore;
    }

    ArrayList<Move> possibleMoves(PieceColor who, Board board) {
        ArrayList<Move> moves = new ArrayList<>();
        for (int i = 0; i < board.EXTENDED_SIDE * board.EXTENDED_SIDE; i++) {
            if (board.get(i) == who) {
                for (int c = -2; c < 3; c++) {
                    for (int r = -2; r < 3; r++) {
                        if (board.get(board.neighbor(i, c, r)) == EMPTY) {
                            int normalCol = i % board.EXTENDED_SIDE;
                            int normalRow = i / board.EXTENDED_SIDE;
                            char col0 = (char) ('g' - (8 - (normalCol)));
                            char row0 = (char) ((normalRow) + '0' - 1);
                            char col1 = (char) (col0 + c);
                            char row1 = (char) (row0 + r);
                            moves.add(Move.move(col0, row0, col1, row1));
                        }
                    }
                }
            }
        }
        moves.add(Move.pass());
        return moves;
    }


    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        } else {
            return board.redPieces() - board.bluePieces();
        }
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();
}
