package compiler;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import grammar.firstBaseVisitor;
import grammar.firstParser;
import java.util.HashMap;
import java.util.List;
import grammar.firstLexer;

public class EmitVisitor extends firstBaseVisitor<ST> {
    private final STGroup stGroup;
    private HashMap<String, Integer> paramOffsets = new HashMap<>();
    private Integer labelCounter = 0;
    public EmitVisitor(STGroup group) {
        super();
        this.stGroup = group;
    }

    @Override
    protected ST defaultResult() {
        return stGroup.getInstanceOf("deflt");
    }

    @Override
    protected ST aggregateResult(ST aggregate, ST nextResult) {
        if(nextResult!=null)
            aggregate.add("elem",nextResult);
        return aggregate;
    }


    @Override
    public ST visitTerminal(TerminalNode node) {
        return null;
    }

    @Override
    public ST visitInt_tok(firstParser.Int_tokContext ctx) {
        ST st = stGroup.getInstanceOf("int");
        st.add("i",ctx.INT().getText());
        return st;
    }

    @Override
    public ST visitBinOp(firstParser.BinOpContext ctx) {
        String templateName;
        switch (ctx.op.getType()) {
            case firstLexer.ADD: templateName = "dodaj";   break;
            case firstLexer.SUB: templateName = "odejmij";  break;
            case firstLexer.MUL: templateName = "mnoz";     break;
            case firstLexer.DIV: templateName = "dziel";    break;
            default: throw new RuntimeException("Nieznany operator");
        }
        ST st = stGroup.getInstanceOf(templateName);
        st.add("p1", visit(ctx.l));
        st.add("p2", visit(ctx.r));
        return st;
    }

    @Override
    public ST visitVarDecl(firstParser.VarDeclContext ctx) {
        String name = ctx.ID().getText();
        ST st = stGroup.getInstanceOf("declare");
        st.add("n", name);
        return st;
    }

    @Override
    public ST visitAssign(firstParser.AssignContext ctx) {
        String name = ctx.ID().getText();
        ST value = visit(ctx.expr());
        ST st = stGroup.getInstanceOf("store");
        st.add("n", name);
        st.add("v", value);
        return st;
    }

    @Override
    public ST visitIdExpr(firstParser.IdExprContext ctx) {
        String name = ctx.ID().getText();
        ST st = stGroup.getInstanceOf("load");
        st.add("n", name);
        return st;
    }

    @Override
    public ST visitPars(firstParser.ParsContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public ST visitIf_stat(firstParser.If_statContext ctx) {
        int label = labelCounter++;
        ST cond = visit(ctx.cond);
        ST thenCode = visit(ctx.then);
        ST elseCode = (ctx.else_ != null)
                ? visit(ctx.else_)
                : stGroup.getInstanceOf("deflt");

        ST st = stGroup.getInstanceOf("if_stat");
        st.add("cond", cond);
        st.add("then_code", thenCode);
        st.add("else_code", elseCode);
        st.add("label", label);
        return st;
    }

    @Override
    public ST visitCmpOp(firstParser.CmpOpContext ctx) {
        String templateName;
        switch (ctx.op.getType()) {
            case firstLexer.EQ:  templateName = "cmp_eq";  break;
            case firstLexer.NEQ: templateName = "cmp_neq"; break;
            case firstLexer.LT:  templateName = "cmp_lt";  break;
            case firstLexer.GT:  templateName = "cmp_gt";  break;
            case firstLexer.LE:  templateName = "cmp_le";  break;
            case firstLexer.GE:  templateName = "cmp_ge";  break;
            default: throw new RuntimeException("Nieznany operator porównania");
        }
        ST st = stGroup.getInstanceOf(templateName);
        st.add("p1", visit(ctx.l));
        st.add("p2", visit(ctx.r));
        st.add("label", labelCounter++);   // ← dodaj tę linię
        return st;
    }

    @Override
    public ST visitOrOp(firstParser.OrOpContext ctx) {
        ST st = stGroup.getInstanceOf("or_op");
        st.add("p1", visit(ctx.l));
        st.add("p2", visit(ctx.r));
        return st;
    }

    @Override
    public ST visitAndOp(firstParser.AndOpContext ctx) {
        ST st = stGroup.getInstanceOf("and_op");
        st.add("p1", visit(ctx.l));
        st.add("p2", visit(ctx.r));
        return st;
    }

    @Override
    public ST visitNotOp(firstParser.NotOpContext ctx) {
        ST st = stGroup.getInstanceOf("not_op");
        st.add("p", visit(ctx.expr()));
        return st;
    }
}
