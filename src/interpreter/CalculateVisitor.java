package interpreter;

import grammar.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class CalculateVisitor extends firstBaseVisitor<Integer> {
    private TokenStream tokStream = null;
    private CharStream input=null;
    private final HashMap<String, Integer> variables = new HashMap<>();
    private final HashMap<String, firstParser.FunDefContext> functions = new HashMap<>();
    public CalculateVisitor(CharStream inp) {
        super();
        this.input = inp;
    }

    public CalculateVisitor(TokenStream tok) {
        super();
        this.tokStream = tok;
    }
    public CalculateVisitor(CharStream inp, TokenStream tok) {
        super();
        this.input = inp;
        this.tokStream = tok;
    }
    private String getText(ParserRuleContext ctx) {
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        if(input==null) throw new RuntimeException("Input stream undefined");
        return input.getText(new Interval(a,b));
    }
    @Override
    public Integer visitIf_stat(firstParser.If_statContext ctx) {
        Integer result = 0;
        if (visit(ctx.cond)!=0) {
            result = visit(ctx.then);
        }
        else {
            if(ctx.else_ != null)
                result = visit(ctx.else_);
        }
        return result;
    }

    @Override
    public Integer visitPrint_stat(firstParser.Print_statContext ctx) {
        var st = ctx.expr();
        var result = visit(st);
        System.out.printf("|%s=%d|\n", st.getText(), result); //nie drukuje ukrytych ani pominiętych spacji
        System.out.printf("|%s=%d|\n", getText(st),  result); //drukuje wszystkie spacje
        System.out.printf("|%s=%d|\n", tokStream.getText(st),  result); //drukuje spacje z ukrytego kanału, ale nie ->skip
        return result;
    }

    @Override
    public Integer visitInt_tok(firstParser.Int_tokContext ctx) {
        return Integer.valueOf(ctx.INT().getText());
    }

    @Override
    public Integer visitPars(firstParser.ParsContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Integer visitBinOp(firstParser.BinOpContext ctx) {
        Integer result=0;
        switch (ctx.op.getType()) {
            case firstLexer.ADD:
                result = visit(ctx.l) + visit(ctx.r);
                break;
            case firstLexer.SUB:
                result = visit(ctx.l) - visit(ctx.r);
                break;
            case firstLexer.MUL:
                result = visit(ctx.l) * visit(ctx.r);
                break;
            case firstLexer.DIV:
                try {
                    result = visit(ctx.l) / visit(ctx.r);
                } catch (Exception e) {
                    System.err.println("Div by zero");
                    throw new ArithmeticException();
                }
        }
        return result;
    }
    @Override
    public Integer visitAssign(firstParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        Integer value = visit(ctx.expr());
        variables.put(name, value);
        return value;
    }

    @Override
    public Integer visitIdExpr(firstParser.IdExprContext ctx) {
        String name = ctx.ID().getText();
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else {
            throw new RuntimeException("Zmienna " + name + " nie istnieje!");
        }
    }

    @Override
    public Integer visitFuncCall(firstParser.FuncCallContext ctx) {
        String name = ctx.ID().getText();
        List<Integer> args = ctx.expr().stream().map(this::visit).collect(Collectors.toList());

        switch (name) {
            case "abs":
                if (args.size() != 1) throw new RuntimeException("abs wymaga 1 argumentu");
                return Math.abs(args.get(0));
            case "max":
                if (args.size() != 2) throw new RuntimeException("max wymaga 2 argumentów");
                return Math.max(args.get(0), args.get(1));
            case "min":
                if (args.size() != 2) throw new RuntimeException("min wymaga 2 argumentów");
                return Math.min(args.get(0), args.get(1));
            case "pow":
                if (args.size() != 2) throw new RuntimeException("pow wymaga 2 argumentów");
                return (int) Math.pow(args.get(0), args.get(1));
            case "sqrt":
                if (args.size() != 1) throw new RuntimeException("sqrt wymaga 1 argumentu");
                return (int) Math.sqrt(args.get(0));
            default:
                if (!functions.containsKey(name)) {
                    throw new RuntimeException("Nieznana funkcja: " + name);
                }
                firstParser.FunDefContext funDef = functions.get(name);
                // Tu pobieranie parametrów
                List<String> paramNames = new ArrayList<>();
                if (funDef.params() != null) {
                    paramNames = funDef.params().ID().stream().map(ParseTree::getText).toList();
                }
                // ile argumentow?
                if (args.size() != paramNames.size()) {
                    throw new RuntimeException(name + " wymaga " + paramNames.size() + " argumentów, podano " + args.size());
                }
                // zapamiętuj stare wartości zmiennych
                HashMap<String, Integer> savedVars = new HashMap<>();
                for (int i = 0; i < paramNames.size(); i++) {
                    String param = paramNames.get(i);
                    if (variables.containsKey(param)) {
                        savedVars.put(param, variables.get(param));
                    }
                    variables.put(param, args.get(i));
                }
                // wykonaj ciało
                Integer result = visit(funDef.body);
                // przywróć wartości
                for (String param : paramNames) {
                    if (savedVars.containsKey(param)) {
                        variables.put(param, savedVars.get(param));
                    } else {
                        variables.remove(param);
                    }
                }
                return result;
        }
    }

    @Override
    public Integer visitFunDef(firstParser.FunDefContext ctx) {
        String name = ctx.ID().getText();
        if (functions.containsKey(name)) {
            throw new RuntimeException("Funkcja " + name + " już istnieje!");
        }
        functions.put(name, ctx);
        return 0;
    }

    @Override
    public Integer visitBlock_real(firstParser.Block_realContext ctx) {
        Integer result = 0;
        for (var block : ctx.block()) {
            result = visit(block);
        }
        return result;
    }

    @Override
    public Integer visitBlock_single(firstParser.Block_singleContext ctx) {
        return visit(ctx.stat());
    }

    @Override
    public Integer visitVarDecl(firstParser.VarDeclContext ctx) {
        String name = ctx.ID().getText();
        variables.put(name, 0);
        return 0;
    }
}
