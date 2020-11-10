package tablut;

import java.util.HashSet;
import java.util.Stack;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;

import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/** The state of a Tablut Game.
 *  @author Jeremy Ahn
 */
class Board {

    /**
     * The number of squares on a side of the board.
     */
    static final int SIZE = 9;

    /**
     * The throne (or castle) square and its four surrounding squares..
     */
    static final Square THRONE = sq(4, 4),
            NTHRONE = sq(4, 5),
            STHRONE = sq(4, 3),
            WTHRONE = sq(3, 4),
            ETHRONE = sq(5, 4);

    /**
     * Initial positions of attackers.
     */
    static final Square[] INITIAL_ATTACKERS = {
            sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
            sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
            sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
            sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /**
     * Initial positions of defenders of the king.
     */
    static final Square[] INITIAL_DEFENDERS = {
            NTHRONE, ETHRONE, STHRONE, WTHRONE,
            sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /**
     * Initializes a game board with SIZE squares on a side in the
     * initial position.
     */
    Board() {
        init();
    }

    /**
     * Initializes a copy of MODEL.
     */
    Board(Board model) {
        copy(model);
    }

    /**
     * Copies MODEL into me.
     * // FIXME
     */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        _turn = model._turn;
        _moveCount = model._moveCount;
        _repeated = model._repeated;
        _winner = model._winner;
        _board = new Piece[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                _board[y][x] = model._board[y][x];
            }
        }
    }

    /**
     * Clears the board to the initial position.
     * // FIXME
     */
    void init() {
        encoded = new HashSet<String>();
        _turn = BLACK;
        _winner = null;
        _moveCount = 0;
        _repeated = false;
        _allMoves = new Stack<Move>();
        _allSquare = new Stack<Square>();
        _allPiece = new Stack<Piece>();
        _board = new Piece[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {

                for (Square sq : INITIAL_ATTACKERS) {
                    if (sq.row() == x && sq.col() == y) {
                        _board[y][x] = BLACK;
                    }
                }

                for (Square sq : INITIAL_DEFENDERS) {
                    if (sq.row() == x && sq.col() == y) {
                        _board[y][x] = WHITE;
                    }
                }

                if (x == THRONE.row() && y == THRONE.col()) {
                    _board[y][x] = KING;
                }

                if (_board[y][x] == null) {
                    _board[y][x] = EMPTY;
                }
            }
        }
        repeat = encodedBoard();
    }

    /**
     * Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     * // FIXME
     *
     * @param n input for limit for moves
     */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new IllegalArgumentException();
        } else {
            _moveLimit = n;
        }
    }

    /**
     * Return a Piece representing whose move it is (WHITE or BLACK).
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return the winner in the current position, or null if there is no winner
     * yet.
     */
    Piece winner() {
        return _winner;
    }

    /**
     * Returns true iff this is a win due to a repeated position.
     */
    boolean repeatedPosition() {
        return _repeated;
    }

    /**
     * Record current position and set winner() next mover if the current
     * position is a repeat.
     * // FIXME
     */
    private void checkRepeated() {
        String mine = this.encodedBoard();
        if (encoded.contains(mine.substring(1))) {
            _repeated = true;
            _winner = turn().opponent();
        } else {
            encoded.add(mine.substring(1));
        }
    }

    /**
     * Return the number of moves since the initial position that have not been
     * undone.
     */
    int moveCount() {
        return _moveCount;
    }

    /**
     * Return location of the king.
     * // return null; FIXME
     */
    Square kingPosition() {
        Square kings = null;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (_board[y][x] == KING) {
                    kings = sq(y, x);
                }
            }
        }
        return kings;
    }

    /**
     * Return the contents the square at S.
     */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /**
     * Return the contents of the square at (COL, ROW), where
     * 0 <= COL, ROW <= 9.
     * // return null; FIXME
     */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /**
     * Return the contents of the square at COL ROW.
     */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /**
     * Set square S to P.
     * // FIXME
     */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
    }

    /**
     * Set square S to P and record for undoing.
     * // FIXME
     */
    final void revPut(Piece p, Square s) {
        _allPiece.push(get(s));
        _allSquare.push(s);
        put(p, s);
    }

    /**
     * Set square COL ROW to P.
     */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /**
     * Return true iff FROM - TO is an unblocked rook move on the current
     * board.  For this to be true, FROM-TO must be a rook move and the
     * squares along it, other than FROM, must be empty.
     * // FIXME
     */
    boolean isUnblockedMove(Square from, Square to) {
        if (get(to.col(), to.row()) != EMPTY) {
            return false;
        } else if (!from.isRookMove(to)) {
            return false;
        } else {
            int dir = from.direction(to);
            while (from != to) {
                if (get(from.rookMove(dir, 1)) != EMPTY) {
                    return false;
                }
                from = from.rookMove(dir, 1);
            }
        }
        return true;
    }

    /**
     * Return true iff FROM is a valid starting square for a move.
     */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /**
     * Return true iff FROM-TO is a valid move.
     * // FIXME
     */
    boolean isLegal(Square from, Square to) {
        Piece p = get(from.col(), from.row());
        if (p == _turn || (p == KING && _turn == WHITE)) {
            return isUnblockedMove(from, to);
        }
        return false;
    }

    /**
     * Return true iff MOVE is a legal move in the current
     * position.
     */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     * // FIXME
     */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);
        revPut(_board[from.col()][from.row()], to);
        put(EMPTY, from);
        _allMoves.push(mv(from, to));
        captured = false;
        if (get(THRONE) != KING && get(NTHRONE) != KING && get(WTHRONE) != KING
                && get(STHRONE) != KING && get(ETHRONE) != KING) {
            for (Square sq : possible(to)) {
                capture(to, sq);
            }
        }
        if (possible(to) != null) {
            for (Square sq : possible(to)) {
                capture(to, sq);
            }
        }
        if (winner() == null) {
            int y = kingPosition().col();
            int x = kingPosition().row();
            if ((y == 0 && x == 0) || (y == 0 && x == 8)
                    || (y == 8 && x == 0) || (y == 8 && x == 8)) {
                _winner = WHITE;
            }
        }
        _moveCount++;
        checkRepeated();
        _turn = _turn.opponent();
    }

    /**
     * Move according to MOVE, assuming it is a legal move.
     */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /**
     * helper for THRONE-KING capture.
     *
     * @param to Square we're going to
     */
    void throne(Square to) {
        if (get(THRONE) == KING) {
            if (get(NTHRONE) == WHITE && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                if (get(NTHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, NTHRONE.between(to));
                    captured = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == WHITE
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                if (get(STHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, STHRONE.between(to));
                    captured = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == WHITE && get(WTHRONE) == BLACK) {
                if (get(ETHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, ETHRONE.between(to));
                    captured = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == WHITE) {
                if (get(WTHRONE.between(to)) == WHITE && get(to) == BLACK) {
                    revPut(EMPTY, WTHRONE.between(to));
                    captured = true;
                }
            }
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                revPut(EMPTY, kingPosition());
                captured = true;
                _winner = BLACK;
            }
        }
    }

    /**
     * helper to check the the THRONE and surrounding squares.
     *
     * @param to Square ti where we're going to
     */
    void check(Square to) {
        if (get(THRONE) == EMPTY) {
            if (_turn == BLACK) {
                if (get(NTHRONE) == KING) {
                    if (get(THRONE.rookMove(0, 2)) == BLACK
                            && get(NTHRONE.rookMove(1, 1)) == BLACK
                            && get(NTHRONE.rookMove(3, 1)) == BLACK) {
                        revPut(EMPTY, NTHRONE);
                        captured = true;
                        _winner = turn();
                    }
                } else if (get(STHRONE) == KING) {
                    if (get(THRONE.rookMove(2, 2)) == BLACK
                            && get(STHRONE.rookMove(3, 1)) == BLACK
                            && get(STHRONE.rookMove(1, 1)) == BLACK) {
                        revPut(EMPTY, STHRONE);
                        captured = true;
                        _winner = turn();
                    }
                } else if (get(ETHRONE) == KING) {
                    if (get(THRONE.rookMove(1, 2)) == BLACK
                            && get(ETHRONE.rookMove(0, 1)) == BLACK
                            && get(ETHRONE.rookMove(2, 1)) == BLACK) {
                        revPut(EMPTY, ETHRONE);
                        captured = true;
                        _winner = turn();
                    }
                } else if (get(WTHRONE) == KING) {
                    if (get(THRONE.rookMove(3, 2)) == BLACK
                            && get(WTHRONE.rookMove(0, 1)) == BLACK
                            && get(WTHRONE.rookMove(2, 1)) == BLACK) {
                        revPut(EMPTY, WTHRONE);
                        captured = true;
                        _winner = turn();
                    }
                }
                if (get(THRONE.between(to)) == WHITE) {
                    revPut(EMPTY, THRONE.between(to));
                    captured = true;
                }
            }
            if (_turn == WHITE) {
                if (get(THRONE.between(to)) == BLACK) {
                    revPut(EMPTY, THRONE.between(to));
                    captured = true;
                }
            }
        }
    }

    /**
     * helper for capture, all the diff situation.
     *
     * @param to the square we're going to
     * @return List<Square> list of possible square that
     * could be captured
     */
    List<Square> possible(Square to) {
        List<Square> toremove = new ArrayList<>();
        List<Square> poss = new ArrayList<>();
        throne(to);
        check(to);
        Square up = to.rookMove(0, 2);
        if (up != null) {
            poss.add(up);
        }
        Square right = to.rookMove(1, 2);
        if (right != null) {
            poss.add(right);
        }
        Square down = to.rookMove(2, 2);
        if (down != null) {
            poss.add(down);
        }
        Square left = to.rookMove(3, 2);
        if (left != null) {
            poss.add(left);
        }
        for (Square sq : poss) {
            if (get(THRONE) != KING && get(NTHRONE) != KING
                    && get(WTHRONE) != KING && get(STHRONE) != KING
                    && get(ETHRONE) != KING && get(to.between(sq)) == KING
                    && get(to) == BLACK && get(sq) == BLACK) {
                toremove.add(sq);
                revPut(EMPTY, to.between(sq));
                captured = true;
                _winner = BLACK;
            }
            if (get(to.between(sq)) == _turn.opponent() && (get(to) == _turn)
                    && get(sq) == _turn && to.between(sq) != ETHRONE
                    && to.between(sq) != WTHRONE && to.between(sq) != NTHRONE
                    && to.between(sq) != STHRONE) {
                revPut(EMPTY, to.between(sq));
                captured = true;
                toremove.add(sq);
            }
            if (get(to.between(sq)) == BLACK && (get(to) == WHITE
                    || get(to) == KING) && (get(sq) == WHITE)
                    || get(sq) == KING && get(to) == WHITE) {
                revPut(EMPTY, to.between(sq));
                captured = true;
                toremove.add(sq);
            }
        }
        return toremove;
    }

    /**
     * Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     * SQ0 and the necessary conditions are satisfied.
     * // FIXME
     */
    private void capture(Square sq0, Square sq2) {
        Piece p0 = get(sq0.col(), sq0.row());
        Piece p2 = get(sq2.col(), sq2.row());
        Square next = sq0.between(sq2);
        Piece bet = get(next.col(), next.row());
        Square sqthrone = THRONE;
        Piece pthrone = get(THRONE);
        if (pthrone == KING) {
            if (get(NTHRONE) == BLACK && get(STHRONE) == BLACK
                    && get(ETHRONE) == BLACK && get(WTHRONE) == BLACK) {
                revPut(EMPTY, kingPosition());
                captured = true;
                _winner = BLACK;
            }
        }
        if (p0 == _turn && p2 == _turn && bet == _turn.opponent()) {
            revPut(EMPTY, next);
            captured = true;
        }
        if (get(THRONE) == EMPTY) {
            pthrone = _turn;
            if (get(sqthrone.between(sq2)) == _turn.opponent()) {
                revPut(EMPTY, sqthrone.between(sq2));
                captured = true;
            }
            if (get(NTHRONE) != KING && get(STHRONE) != KING
                    && get(ETHRONE) != KING && get(WTHRONE) != KING) {
                if (get(sq0.between(sq2)) == KING
                        && p0 == BLACK && p2 == BLACK) {
                    revPut(EMPTY, kingPosition());
                    captured = true;
                    _winner = BLACK;
                }
            }
        }
    }

    /**
     * Undo one move.  Has no effect on the initial board.
     * // FIXME
     */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            _moveCount--;
        }
    }

    /**
     * Remove record of current position in the set of positions encountered,
     * unless it is a repeated position or we are at the first move.
     * // FIXME
     */
    private void undoPosition() {
        if (!_repeated || _moveCount > 2) {
            if (captured) {
                Piece p = _allPiece.pop();
                Square sq = _allSquare.pop();
                put(p, sq);
                captured = false;
            }
            Move undid = _allMoves.pop();
            Square sq = undid.from();
            Piece p = _board[undid.to().col()][undid.to().row()];
            put(p, sq);
            put(EMPTY, undid.to());
        }
        _repeated = false;
    }

    /**
     * Clear the undo stack and board-position counts. Does not modify the
     * current position or win status.
     * // FIXME
     */
    void clearUndo() {
        _allMoves.clear();
        _allSquare.clear();
        _allPiece.clear();
    }

    /**
     * Return a new mutable list of all legal moves on the current board for
     * SIDE (ignoring whose turn it is at the moment).
     * // return null;  // FIXME
     */
    List<Move> legalMoves(Piece side) {
        List<Move> lst = new ArrayList();
        HashSet<Square> squares = pieceLocations(side);
        for (Square s : squares) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (isLegal(s, sq(y, s.row()))) {
                        lst.add(mv(s, sq(y, s.row())));
                    }
                    if (isLegal(s, sq(s.col(), x))) {
                        lst.add(mv(s, sq(s.col(), x)));
                    }
                }
            }
        }
        if (lst.size() == 0) {
            _winner = _turn.opponent();
        }
        return lst;
    }

    /**
     * Return true iff SIDE has a legal move.
     * // FIXME
     */
    boolean hasMove(Piece side) {
        if (legalMoves(side).size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Return a text representation of this Board.  If COORDINATES, then row
     * and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /**
     * Return the locations of all pieces on SIDE.
     * // return null; // FIXME
     */
    HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> result = new HashSet<>();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (side == _board[y][x]) {
                    result.add(sq(y, x));
                }
            }
        }
        return result;
    }

    /**
     * Return the contents of _board in the order of SQUARE_LIST as a sequence
     * of characters: the toString values of the current turn and Pieces.
     */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /**
     * Piece whose turn it is (WHITE or BLACK).
     */
    private Piece _turn;
    /**
     * Cached value of winner on this board, or null if it has not been
     * computed.
     */
    private Piece _winner;
    /**
     * Number of (still undone) moves since initial position.
     */
    private int _moveCount;
    /**
     * True when current board is a repeated position (ending the game).
     */
    private boolean _repeated;

    /**
     * A 2D array of Piece to represent the board.
     */
    private Piece[][] _board;

    /**
     * The limit of moves a player makes.
     */
    private int _moveLimit;

    /**
     * A Stack of all the pieces that has moved.
     */
    private Stack<Piece> _allPiece;

    /**
     * A Stack of all the sqaure the pieces has moved to;
     * corresponds the piece in _allPiece.
     */
    private Stack<Square> _allSquare;

    /**
     * Stack of all the moves made on the board.
     */
    private Stack<Move> _allMoves;

    /**
     * String to see if repeat.
     */
    private String repeat;

    /**
     * Hashset of encoded version of board.
     */
    private HashSet<String> encoded;

    /**
     * true or false if capture.
     */
    private boolean captured;
}
