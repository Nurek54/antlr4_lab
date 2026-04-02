    DD a
    DD b
        MOV A,#10
        MOV [a],A
        MOV A,#3
        MOV [b],A
    MOV A,#3
    PUSH A
    MOV A,[a]
    POP B
    CMP A,B
    MOV A,#1
    JLE cmp_end_3
    MOV A,#0
    cmp_end_3:
    PUSH A
    MOV A,#10
    PUSH A
    MOV A,[b]
    POP B
    CMP A,B
    MOV A,#1
    JE cmp_end_2
    MOV A,#0
    cmp_end_2:
    NOT A
    PUSH A
    MOV A,#5
    PUSH A
    MOV A,[a]
    POP B
    CMP A,B
    MOV A,#1
    JG cmp_end_1
    MOV A,#0
    cmp_end_1:
    POP B
    AND A,B
    POP B
    OR A,B
    JE label_else_0
                MOV A,#999
    JMP label_endif_0
    label_else_0:
                MOV A,#000
    label_endif_0: