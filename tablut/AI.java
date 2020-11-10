package tablut;

import java.util.HashSet;
import java.util.List;

import static java.lang.Math.*;

import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Jeremy Ahn
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    /** //return ""; FIXME */
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move.
     *  // FIXME */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else if (_myPiece == BLACK) {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound.
     *  //return 0; // FIXME */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        if (sense == 1) {
            int bestsofar = -INFTY;
            if (board.legalMoves(board.turn()) == null
                    || board.legalMoves(board.turn()).size() == 0) {
                return bestsofar;
            }
            Move best = board.legalMoves(board.turn()).get(0);
            List<Move> all = board.legalMoves(board.turn());
            for (Move move : all) {
                Board clone = new Board(board);
                clone.makeMove(move);
                int ret = findMove(clone, depth - 1,
                        false, sense * -1, alpha, beta);
                clone.undo();
                if (ret > bestsofar) {
                    best = move;
                }
                bestsofar = max(bestsofar, ret);
                alpha = max(alpha, bestsofar);
                if (beta <= alpha) {
                    break;
                }
            }
            if (saveMove) {
                _lastFoundMove = best;
            }
            return bestsofar;
        } else {
            int worstsofar = INFTY;
            if (board.legalMoves(board.turn()) == null
                    || board.legalMoves(board.turn()).size() == 0) {
                return worstsofar;
            }
            Move worst = board.legalMoves(board.turn()).get(0);

            for (Move mv : board.legalMoves(board.turn())) {
                Board clone = new Board(board);
                clone.makeMove(mv);
                int val = findMove(clone, depth - 1,
                        false, sense * -1, alpha, beta);
                clone.undo();
                if (val < worstsofar) {
                    worst = mv;
                }
                worstsofar = min(worstsofar, val);
                beta = min(worstsofar, beta);
                if (beta <= alpha) {
                    break;
                }
            }
            if (saveMove) {
                _lastFoundMove = worst;
            }
            return worstsofar;
        }
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD.
     *  // return 4; // FIXME? */
    private static int maxDepth(Board board) {
        HashSet<Square> blacks = board.pieceLocations(BLACK);
        HashSet<Square> whites = board.pieceLocations(WHITE);
        int left = blacks.size() + whites.size();
        int result = left / 10;
        if (left > HIGH) {
            return result;
        } else if (left > MID) {
            return result;
        } else {
            return result;
        }
    }

    /** Return a heuristic value for BOARD.
     * // return 0;  // FIXME */
    private int staticScore(Board board) {
        edge = WILL_WIN_VALUE;
        surrounding = -WILL_WIN_VALUE;
        Square kings = board.kingPosition();
        int mine = board.pieceLocations(myPiece()).size();
        int opp = board.pieceLocations(myPiece().opponent()).size();
        if (opp > mine) {
            oppop = -WILL_WIN_VALUE;
        } else {
            myp = WILL_WIN_VALUE;
        }
        if (board.winner() == myPiece()) {
            return INFTY;
        } else if (board.winner() == myPiece().opponent()) {
            return -INFTY;
        }

        int col = kings.col();
        int row = kings.row();
        int top = Board.SIZE - row;
        int right = Board.SIZE - col;
        int toedge = Math.min(top, Math.min(row, Math.min(col, right)));
        int surrcount = 0;
        if (board.get(row + 1, col) == BLACK) {
            surrcount++;
        }
        if (board.get(row - 1, col) == BLACK) {
            surrcount++;
        }
        if (board.get(row, col - 1) == BLACK) {
            surrcount++;
        }
        if (myPiece() == WHITE) {
            toedge *= -1;
            surrcount *= -1;
        }
        return (myp * mine) + (oppop * opp)
                + (edge * toedge) + (surrounding * surrcount);
    }

    /** point for king on edge. */
    private int edge;

    /** int for my piece point. */
    private int myp;

    /** int for opponenet point. */
    private int oppop;

    /** int variable for point for surrounding. */
    private int surrounding;

    /** high for depth. */
    static final int HIGH = 40;

    /** mid for depth. */
    static final int MID = 30;
}
