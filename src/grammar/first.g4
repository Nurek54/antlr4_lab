grammar first;

prog:   stat* EOF ;

stat: expr #expr_stat
    | IF_kw '(' cond=expr ')' then=block  ('else' else=block)? #if_stat
    | '>' expr #print_stat
    | 'fun' ID '(' params? ')' body=block #funDef
    | 'fun' ID '(' ID DOTS ')' body=block #funDefVar
    | 'var' ID #varDecl
    ;

block : stat #block_single
    | '{' block* '}' #block_real
    ;

params : ID (',' ID)* ;

expr:
        ('!' | NOT) expr #notOp
    |   l=expr op=(MUL|DIV) r=expr #binOp
    |   l=expr op=(ADD|SUB) r=expr #binOp
    |   l=expr op=(EQ|NEQ|LT|GT|LE|GE) r=expr #cmpOp
    |   l=expr AND r=expr #andOp
    |   l=expr OR r=expr #orOp
    |   ID '(' (expr (',' expr)*)? ')' #funcCall
    |   INT #int_tok
    |   '(' expr ')' #pars
    | <assoc=right> ID '=' expr #assign
    |   ID #IdExpr
    ;

IF_kw : 'if' ;

DIV : '/' ;
MUL : '*' ;
SUB : '-' ;
ADD : '+' ;

EQ  : '==' ;
NEQ : '!=' ;
GE  : '>=' ;
LE  : '<=' ;
GT  : '>'  ;
LT  : '<'  ;
AND : '&&' ;
OR  : '||' ;
NOT : 'not';
DOTS: '...' ;

NEWLINE : [\r\n]+ -> channel(HIDDEN);
WS : [ \t]+ -> channel(HIDDEN) ;
INT     : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z0-9_]* ;
COMMENT : '/*' .*? '*/' -> channel(HIDDEN) ;
LINE_COMMENT : '//' ~[\r\n]* -> channel(HIDDEN) ;