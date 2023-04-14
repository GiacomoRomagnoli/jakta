:- dynamic(cell/3).

cell(0, 0, e).
cell(1, 0, e).
cell(2, 0, e).
cell(0, 1, e).
cell(1, 1, e).
cell(2, 1, e).
cell(0, 2, e).
cell(1, 2, e).
cell(2, 2, e).

vertical([cell(X, Y, S)]) :- cell(X, Y, S).
vertical([cell(X, Y1, S1), cell(X, Y2, S2) | Cs]) :-
    cell(X, Y1, S1),
    cell(X, Y2, S2),
    Y2 - Y1 =:= 1,
    vertical([cell(X, Y2, S2) | Cs]).

horizontal([cell(X, Y, S)]) :- cell(X, Y, S).
horizontal([cell(X1, Y, S1), cell(X2, Y, S2) | Cs]) :-
    cell(X1, Y, S1),
    cell(X2, Y, S2),
    X2 - X1 =:= 1,
    horizontal([cell(X2, Y, S2) | Cs]).

diagonal([cell(X, Y, S)]) :- cell(X, Y, S).
diagonal([cell(X1, Y1, S1), cell(X2, Y2, S2) | Cs]) :-
    cell(X1, Y1, S1),
    cell(X2, Y2, S2),
    X2 - X1 =:= 1,
    Y2 - Y1 =:= 1,
    diagonal([cell(X2, Y2, S2) | Cs]).

antidiagonal([cell(X, Y, S)]) :- cell(X, Y, S).
antidiagonal([cell(X1, Y1, S1), cell(X2, Y2, S2) | Cs]) :-
    cell(X1, Y1, S1),
    cell(X2, Y2, S2),
    X2 - X1 =:= 1,
    Y1 - Y2 =:= 1,
    antidiagonal([cell(X2, Y2, S2) | Cs]).

aligned(L) :- vertical(L); horizontal(L); diagonal(L); antidiagonal(L).

put(X, Y, S) :-
    retract(cell(X, Y, S)),
    assert(cell(X, Y, S)).
