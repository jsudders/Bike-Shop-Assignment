// Mistletoe v0.2 -- Java callback implementation
package edu.curtin.oose.bikeApp.mistletoe;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import java.lang.reflect.*;

/**
 * NOTE: this file contains UNUSUAL linter suppression practices, which should not be emulated in
 * normal development.
 *
 * The reason: this file (unlike almost any other) is designed to be copied into different code
 * bases, which will generally use different sets of linting rules. Thus, this code is designed to
 * pass _any combination_ of linting rules, even silly ones, because we can't make assumptions
 * about them. We _must_ outright suppress any _configurable_ linting rules, as we cannot know
 * how they're going to be configured.
 */

@SuppressWarnings({
    "PMD.RedundantFieldInitializer",  // Explicit initialisation value is self-documenting.
    "PMD.UnnecessaryCast",            // Explicit casting is self-documenting.
    "PMD.AvoidLiteralsInIfCondition", // Generates arguable false positives wrt 0 and 1.
    "PMD.GodClass",                   // The project requires a large number of public methods
    "PMD.ExcessivePublicCount",       // as above
    "PMD.TooManyMethods",             // as above
    "PMD.MethodNamingConventions",    // The project requires methods/fields beginning with an uppercase letter.
    "PMD.FieldNamingConventions",     // as above
    "PMD.CommentSize",                // Unnecessarily restrictive
    "PMD.FieldDeclarationsShouldBeAtStartOfClass", // The class layout has its own logical structure

    "serial",                         // Serialisation is not a goal
    "PMD.UseConcurrentHashMap",       // Concurrency is not a goal

    "PMD.OnlyOneReturn",              // Overzealous
    "PMD.ShortVariable",              // Overzealous
    "PMD.ShortMethodName",            // Overzealous
    "PMD.ShortClassName",             // Overzealous
    "PMD.LawOfDemeter",               // Overzealous
    "PMD.MethodArgumentCouldBeFinal", // Unnecessarily verbose
    "PMD.LocalVariableCouldBeFinal",  // Unnecessarily verbose
    "PMD.UseExplicitTypes",           // Unnecessarily verbose

    "PMD.CommentRequired",            // Consider addressing in future
    "PMD.CognitiveComplexity",        // Consider addressing in future
    "PMD.CyclomaticComplexity",       // Consider addressing in future
    "PMD.NPathComplexity",            // Consider addressing in future
    "PMD.NcssCount",                  // Consider addressing in future
    "PMD.CouplingBetweenObjects",     // Consider addressing in future
})
public final class Mistletoe
{
    private Mistletoe() {}

    public static final class MTException extends RuntimeException
    {
        private MTException(String msg) { super(msg); }
        private MTException(String msg, Throwable cause) { super(msg, cause); }
    }

    public abstract static class Expr
    {
        /*default*/ static final long UNKNOWN_LENGTH = -1L;
        /*default*/ static final Expr EMPTY_EXPR = voidExpr("<empty>", scope -> {});

        @SuppressWarnings("PMD.UnusedPrivateField") // Future plans
        private final String name;

        @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName") // No good alternatives without obfuscation
        private long staticLength = UNKNOWN_LENGTH;

        /*default*/ Expr(String name) { this.name = name; }

        /*default*/ long getStaticLength() { return staticLength; }
        /*default*/ Expr setStaticLength(long staticLen)
        {
            staticLength = staticLen;
            return this;
        }
        public List<Object> staticLength()  // Expr property
        {
            return List.of(staticLength);
        }

        /*default*/ abstract void eval(Scope scope, DestFn dest);

        /*default*/ List<Object> evalMutableList(Scope scope) { return evalList(scope); }
        /*default*/ List<Object> evalList(Scope scope)
        {
            var list = new ArrayList<Object>();
            eval(scope, list::add);
            return list;
        }
        /*default*/ Object evalOne(Scope scope) { return evalOne(scope, null); }
        /*default*/ Object evalOne(Scope scope, Object defaultValue)
        {
            Object[] singleValue = {null};
            eval(scope, obj -> {
                if(singleValue[0] != null)
                {
                    throw new MTException(String.format("Expected one value but found multiple: %s, %s, ...",
                        repr(singleValue[0]), repr(obj)));
                }
                singleValue[0] = obj;
            });
            Object v = singleValue[0];
            if(v == null)
            {
                if(defaultValue != null) { v = defaultValue; }
                else { throw new MTException("Expected one value but found zero"); }
            }
            return v;
        }

        private Expr infix(String name, Object[] args, BinaryOperator<Object> op)
        {
            var right = argsToExpr(args);
            long llen = staticLength;
            long rlen = right.staticLength;

            if(llen == 0 || rlen == 0)
            {
                return EMPTY_EXPR;
            }
            else if(llen == 1)
            {
                return callExpr(name, (scope, dest) ->
                {
                    var x = evalOne(scope);
                    right.eval(scope, y -> dest.accept(op.apply(x, y)));
                })
                .setStaticLength(rlen);
            }
            else if(rlen == 1)
            {
                return callExpr(name, (scope, dest) ->
                {
                    var y = right.evalOne(scope);
                    eval(scope, x -> dest.accept(op.apply(x, y)));
                })
                .setStaticLength(llen);
            }
            else
            {
                return callExpr(name, (scope, dest) ->
                {
                    var operands1 = evalList(scope);
                    var operands2 = right.evalList(scope);

                    int len1 = operands1.size();
                    int len2 = operands2.size();
                    if(len1 == 0 || len2 == 0) { return; }

                    var it1 = operands1.iterator();
                    var it2 = operands2.iterator();
                    int len = Math.max(len1, len2);

                    for(int i = 0; i < len; i++)
                    {
                        if(!it1.hasNext())
                        {
                            it1 = operands1.iterator();
                        }
                        if(!it2.hasNext())
                        {
                            it2 = operands2.iterator();
                        }
                        dest.accept(op.apply(it1.next(), it2.next()));
                    }
                })
                .setStaticLength((llen == UNKNOWN_LENGTH || rlen == UNKNOWN_LENGTH) ? UNKNOWN_LENGTH
                                                                                    : Math.max(llen, rlen));
            }
        }

        private Expr map(String name, UnaryOperator<Object> op)
        {
            return callExpr(name, (scope, dest) -> eval(scope, v -> dest.accept(op.apply(v)))).setStaticLength(staticLength);
        }

        public Expr As(Object... args)
        {
            return assign("As", argsToExpr(args), this, Scope::put);
        }

        public Expr Call(Object... args)  { return call("Call", this, argsToExpr(args)); }
        public Expr Apply(Object... args) { return call("Apply", argsToExpr(args), this); }

        public Expr Eq(Object... args) { return infix("Eq", args, Mistletoe::eq); }
        public Expr Ne(Object... args) { return infix("Ne", args, (x, y) -> !eq(x, y)); }
        // public Expr Lt(Object... args) { return infix("Lt", args, compare("Lt", (x, y) -> x <  y, c -> c < 0)); }
        // public Expr Le(Object... args) { return infix("Le", args, compare("Le", (x, y) -> x <= y, c -> c <= 0)); }
        // public Expr Gt(Object... args) { return infix("Gt", args, compare("Gt", (x, y) -> x >  y, c -> c > 0)); }
        // public Expr Ge(Object... args) { return infix("Ge", args, compare("Ge", (x, y) -> x >= y, c -> c >= 0)); }
        public Expr Lt(Object... args) { return infix("Lt", args, compare("Lt", rel -> rel < 0)); }
        public Expr Le(Object... args) { return infix("Le", args, compare("Le", rel -> rel <= 0)); }
        public Expr Gt(Object... args) { return infix("Gt", args, compare("Gt", rel -> rel > 0)); }
        public Expr Ge(Object... args) { return infix("Ge", args, compare("Ge", rel -> rel >= 0)); }
        public Expr IsNaN()            { return map("IsNaN", v -> Double.isNaN(toFloat(v))); }

        public Expr In(Object... args)
        {
            var rightExpr = argsToExpr(args);
            return callExpr("In", (scope, dest) ->
            {
                var set = new HashSet<>();
                rightExpr.eval(scope, set::add);
                eval(scope, v -> dest.accept(set.contains(v)));
            })
            .setStaticLength(staticLength);
        }

        public Expr NotIn(Object... args) { return In(args).Not(); }

        public Expr Plus(Object... args)  { return infix("Plus",  args, intOrFloat((x, y) -> x + y, (x, y) -> x + y)); }
        public Expr Minus(Object... args) { return infix("Minus", args, intOrFloat((x, y) -> x - y, (x, y) -> x - y)); }
        public Expr Times(Object... args) { return infix("Times", args, intOrFloat((x, y) -> x * y, (x, y) -> x * y)); }
        public Expr Div(Object... args)   { return infix("Div",   args, intOrFloat((x, y) -> x / nonZero(y),
                                                                                   (x, y) -> x / nonZero(y))); }
        public Expr Mod(Object... args)   { return infix("Mod",   args, intOrFloat((x, y) -> x % nonZero(y),
                                                                                   (x, y) -> x % nonZero(y))); }
        public Expr Pow(Object... args)   { return infix("Pow",   args, intOrFloat((x, y) -> (long)Math.pow(x, y),
                                                                                   Math::pow)); }

        @SuppressWarnings("PMD.UselessParentheses") // No... they are needed.
        public Expr Neg() { return map("Neg", v -> (v instanceof Long) ? (Object)(-(Long)v)        : (Object)(-toFloat(v))); }
        public Expr Abs() { return map("Abs", v -> (v instanceof Long) ? (Object)Math.abs((Long)v) : (Object)Math.abs(toFloat(v))); }

        public Expr And(Object... args)   { return infix("And", args, (x, y) -> toBool(x) && toBool(y)); }
        public Expr Or(Object... args)    { return infix("Or",  args, (x, y) -> toBool(x) || toBool(y)); }
        public Expr Not() { return map("Not", v -> !toBool(v)); }

        public Expr Int()   { return map("Int",   Mistletoe::toInt); }
        public Expr Float() { return map("Float", Mistletoe::toFloat); }
        public Expr Str()   { return map("Str",   Mistletoe::toStr); }
        public Expr Tuple() { return singleExpr("Tuple", scope -> new Tuple(evalList(scope))); }
        public Expr Flatten()
        {
            return callExpr("Flatten", (scope, dest) -> eval(scope, v ->
            {
                if(v instanceof Tuple)
                {
                    ((Tuple)v).forEach(dest);
                }
                else
                {
                    dest.accept(v);
                }
            }));
        }

        public Expr Nth(Object... args)
        {
            var indexExpr = argsToExpr(args);
            return callExpr("Nth", (scope, dest) ->
            {
                var src = evalList(scope);
                int len = src.size();
                indexExpr.eval(scope, v ->
                {
                    int idx = (int)toInt(v);
                    if(idx < -len || idx >= len)
                    {
                        throw new MTException(String.format("Nth: index %d is invalid for a length-%d sequence", idx, len));
                    }
                    if(idx < 0) { idx += len; }
                    dest.accept(src.get(idx));
                });
            })
            .setStaticLength(indexExpr.staticLength);
        }

        public Expr Len()
        {
            return singleExpr("Len", scope ->
            {
                long[] count = {0L};
                eval(scope, v -> count[0]++);
                return count[0];
            });
        }

        public Expr Sum()
        {
            final class Summer implements DestFn
            {
                private boolean intsOnly = true;
                private long intSum = 0;
                private double floatSum = 0.0;
                private Summer() {}

                @Override
                public void accept(Object v)
                {
                    if(intsOnly)
                    {
                        if(v instanceof Long)
                        {
                            intSum += (Long)v;
                            return;
                        }
                        floatSum = (double)intSum;
                        intsOnly = false;
                    }
                    floatSum += toFloat(v);
                }
            }

            return singleExpr("Sum", scope ->
            {
                var summer = new Summer();
                eval(scope, summer);
                return summer.intsOnly ? (Object)summer.intSum : (Object)summer.floatSum;
            });
        }

        public Expr All()
        {
            return singleExpr("All", scope ->
            {
                try
                {
                    eval(scope, v ->
                    {
                        if(!toBool(v))
                        {
                            throw StopIteration.EX;
                        }
                    });
                    return true;
                }
                catch(StopIteration ignored)  // NOPMD -- exception intentionally used to exit callback.
                {
                    return false;
                }
            });
        }

        public Expr Any()
        {
            return singleExpr("Any", scope ->
            {
                try
                {
                    eval(scope, v ->
                    {
                        if(toBool(v))
                        {
                            throw StopIteration.EX;
                        }
                    });
                    return false;
                }
                catch(StopIteration ignored)  // NOPMD -- exception intentionally used to exit callback.
                {
                    return true;
                }
            });
        }

        public Expr SLen()
        {
            return map("SLen", v ->
            {
                String s = toStr(v);
                return (long)s.codePointCount(0, s.length());
            });
        }

        public Expr Cat(Object... args) { return infix("Cat", args, (x, y) -> toStr(x) + toStr(y)); }
        public Expr Join(Object... args)
        {
            var delimExpr = argsToExpr(args);
            return singleExpr("Join", scope ->
            {
                var sb = new StringBuilder();
                String delim = toStr(delimExpr.evalOne(scope, ""));
                if(delim.isEmpty())
                {
                    eval(scope, v -> sb.append(toStr(v)));
                }
                else
                {
                    boolean[] useDelim = {false};
                    eval(scope, v -> {
                        if(useDelim[0]) { sb.append(delim); }
                        else            { useDelim[0] = true; }
                        sb.append(toStr(v));
                    });
                }
                return sb.toString();
            });
        }

        public Expr To(Object... args)
        {
            var endExpr = argsToExpr(args);
            return callExpr("To", (scope, dest) ->
            {
                long from = toInt(evalOne(scope));
                long to = toInt(endExpr.evalOne(scope));
                for(long i = from; i < to; i++)
                {
                    dest.accept(i);
                }
            });
        }

        public Expr Ord()
        {
            return callExpr("Ord",
                (scope, dest) -> eval(scope, v -> toStr(v).codePoints().forEach(cp -> dest.accept((long)cp))));
        }

        public Expr Chr()
        {
            return map("Chr", v ->
            {
                long cp = toInt(v);
                if(0 <= cp && cp <= 0x10ffff && (cp < 0xd800 || cp > 0xdfff))
                {
                    return Character.toString((int)cp);
                }
                throw new MTException(
                    String.format("Chr: %s0x%s (%d) is not a valid code point",
                        (cp < 0) ? "-" : "",
                        Long.toString(Math.abs(cp), 16),
                        cp));
            });
        }

        public Expr RandShuffle()
        {
            return listExpr("RandShuffle", scope ->
            {
                var values = evalMutableList(scope);
                Collections.shuffle(values, ExecState.instance().random);
                return values;
            })
            .setStaticLength(staticLength);
        }

        public Expr RandChoose(Object... args)
        {
            var nExpr = argsToExpr(args);
            return listExpr("RandChoose", scope ->
            {
                var pop = evalMutableList(scope);
                long n = toInt(nExpr.evalOne(scope, 1L));
                if(n < 0 || n > pop.size())
                {
                    throw new MTException(String.format(
                        "RandChoose: cannot choose %d value(s) from a length-%d sequence",
                        n, pop.size()));
                }
                Collections.shuffle(pop, ExecState.instance().random);
                return pop.subList(0, (int)n);
            });
        }

        public Expr With(Object... args)
        {
            var body = argsToExpr(args);
            return callExpr("With", (outerScope, dest) ->
            {
                var innerScope = new Scope(outerScope, true, outerScope.getAllowedJumps());
                innerScope.put(SpecialKey.IT, evalList(outerScope));
                body.eval(innerScope, dest);
            })
            .setStaticLength(body.staticLength);
        }

        public Expr ForEach(Object... args)
        {
            var body = argsToExpr(args);
            return callExpr("ForEach", (outerScope, dest) ->
            {
                var state = ExecState.instance();
                var innerScope = new Scope(outerScope, true, outerScope.getAllowedJumps() | Break.bit | Continue.bit);
                try
                {
                    long[] idx = {0L};
                    var itemVar = new ArrayList<>();
                    var idxVar = new ArrayList<>();
                    itemVar.add(null);
                    idxVar.add(0L);
                    innerScope.put(SpecialKey.ITEM, itemVar);
                    innerScope.put(SpecialKey.IDX, idxVar);

                    eval(outerScope, v ->
                    {
                        itemVar.set(0, v);
                        body.eval(innerScope, dest);
                        state.endJump(Continue);
                        if(state.currentJump != null)
                        {
                            throw StopIteration.EX;
                        }
                        idx[0]++;
                        idxVar.set(0, idx[0]);
                    });
                }
                catch(StopIteration ignored) {}  // NOPMD -- exception intentionally used to exit callback.
                finally
                {
                    state.endJump(Break);
                }
            })
            .setStaticLength((staticLength == UNKNOWN_LENGTH || body.staticLength == UNKNOWN_LENGTH)
                             ? UNKNOWN_LENGTH
                             : (staticLength * body.staticLength));
        }

        public Expr Eval()
        {
            return callExpr("Eval", (outerScope, dest) -> eval(outerScope, v ->
                new Parser("(" + toStr(v) + ")").parseList(Parser.SyntaxMode.NORMAL).eval(
                    new Scope(outerScope, false, outerScope.getAllowedJumps()), dest)
            ));
        }

        @SuppressWarnings("PMD.SystemPrintln") // Required functionality
        public Expr Print()
        {
            return voidExpr("Print", scope -> eval(scope, v -> System.out.print(toStr(v))));
        }

        public Expr Done()
        {
            return voidExpr("Done", scope -> eval(scope, v -> {}));
        }

        public Expr Note(Object... ignored)
        {
            return this;
        }
    }

    public static final class VExpr extends Expr
    {
        private final Expr keyExpr;
        private VExpr(Expr keyExpr)
        {
            super("V");
            this.keyExpr = keyExpr;
        }

        @Override
        public void eval(Scope scope, DestFn dest)
        {
            keyExpr.eval(scope, k -> scope.get(k).forEach(dest));
        }

        public Expr Pop()
        {
            return callExpr("Pop", (scope, dest) -> keyExpr.eval(scope, k ->
            {
                var list = scope.get(k);
                if(!list.isEmpty())
                {
                    dest.accept(list.remove(0));
                }
            }));
        }

        public Expr Set(Object... valueArgs)
        {
            return assign("Set", keyExpr, argsToExpr(valueArgs), Scope::put);
        }

        public Expr SetGlobal(Object... valueArgs)
        {
            return assign("SetGlobal", keyExpr, argsToExpr(valueArgs),
                           (scope, k, v) -> ExecState.instance().getGlobalScope().put(k, v));
        }

        public Expr Update(Object... valueArgs)
        {
            return assign("Update", keyExpr, argsToExpr(valueArgs),
                (scope, k, v) -> {
                    var list = scope.get(k);
                    list.clear();
                    list.addAll(v);
                });
        }

        public Expr Append(Object... valueArgs)
        {
            return assign("Append", keyExpr, argsToExpr(valueArgs),
                           (scope, k, v) -> scope.get(k).addAll(v));
        }
    }

    public static final class IfExpr // Does not extend Expr
    {
        private final Expr condition;
        private IfExpr(Expr condition)
        {
            this.condition = condition;
        }

        public ThenExpr Then(Object... args)
        {
            return new ThenExpr(condition, argsToExpr(args));
        }
    }

    public static final class ThenExpr extends Expr
    {
        private final Expr condition;
        private final Expr thenBody;
        private Expr elseBody = EMPTY_EXPR;

        private ThenExpr(Expr condition, Expr thenBody)
        {
            super("If-Then");
            this.condition = condition;
            this.thenBody = thenBody;
        }

        private Expr body(Scope scope)
        {
            return toBool(condition.evalOne(scope)) ? thenBody : elseBody;
        }

        @Override public void eval(Scope scope, DestFn dest)     { body(scope).eval(scope, dest); }
        @Override public List<Object> evalList(Scope scope)       { return body(scope).evalList(scope); }
        @Override public Object evalOne(Scope scope, Object defV) { return body(scope).evalOne(scope, defV); }

        public Expr Else(Object... args)
        {
            elseBody = argsToExpr(args);

            long thenLen = thenBody.getStaticLength();
            long elseLen = elseBody.getStaticLength();
            if(thenLen != UNKNOWN_LENGTH && thenLen == elseLen)
            {
                setStaticLength(thenLen);
            }
            return this;
        }
    }

    public static final class WhileExpr // does not implement Expr
    {
        private final Expr condition;
        private WhileExpr(Expr condition)
        {
            this.condition = condition;
        }

        public Expr Do(Object... args)
        {
            var body = argsToExpr(args);
            return callExpr("While-Do", (outerScope, dest) ->
            {
                var state = ExecState.instance();
                var innerScope = new Scope(outerScope, true, outerScope.getAllowedJumps() | Break.bit | Break.bit);
                try
                {
                    long idx = 0L;
                    var idxVar = new ArrayList<>();
                    idxVar.add(idx);
                    innerScope.put(SpecialKey.IDX, idxVar);

                    while(toBool(condition.evalOne(innerScope)))
                    {
                        body.eval(innerScope, dest);
                        state.endJump(Continue);
                        if(state.currentJump != null)
                        {
                            break;
                        }
                        idx++;
                        idxVar.set(0, idx);
                    }
                }
                finally
                {
                    state.endJump(Break);
                }
            });
        }
    }

    public static final class TryExpr extends Expr
    {
        private final Expr tryBody;
        private Expr finallyBody = EMPTY_EXPR;
        private final List<Expr> catchPatterns = new ArrayList<>();
        private final List<Expr> catchBodies   = new ArrayList<>();

        private TryExpr(Expr tryBody)
        {
            super("Try");
            this.tryBody = tryBody;
        }

        private void _addCatch(Expr catchPattern, Expr catchBody)
        {
            catchPatterns.add(catchPattern);
            catchBodies.add(catchBody);
        }

        public CatchExpr Catch(Object... args)
        {
            return new CatchExpr(this, argsToExpr(args));
        }

        public Expr Finally(Object... args)
        {
            this.finallyBody = argsToExpr(args);
            return this;
        }

        @Override
        public void eval(Scope scope, DestFn dest)
        {
            evalList(scope).forEach(dest);
        }

        @Override
        @SuppressWarnings("PMD.NullAssignment") // state.exception = null is a sentinel value
        public List<Object> evalList(Scope scope)
        {
            int nCatches = catchPatterns.size();
            if(nCatches == 0 && finallyBody == null)
            {
                throw new MTException("Try: expected at least one Catch and/or a Finally");
            }

            var values = tryBody.evalMutableList(scope);
            var state = ExecState.instance();

            if(state.endJump(Jump.THROW))
            {
                scope.put(SpecialKey.EX, state.exception);
                scope.put(SpecialKey.PARTIAL, values);

                var cPatternIt = catchPatterns.iterator();
                var cBodyIt = catchBodies.iterator();
                var match = false;
                while(cPatternIt.hasNext())
                {
                    var pattern = cPatternIt.next().evalList(scope);
                    var pSize = pattern.size();
                    var cBody = cBodyIt.next();
                    if(state.exception.size() >= pSize && state.exception.subList(0, pSize).equals(pattern))
                    {
                        state.exception = null;
                        values = cBody.evalMutableList(scope);
                        match = true;
                        break;
                    }
                }
                if(!match)
                {
                    state.currentJump = Jump.THROW;
                    values.clear();
                }
            }
            finallyBody.eval(scope, values::add);
            return values;
        }
    }

    public static final class CatchExpr  // Does not derive from Expr
    {
        private final TryExpr tryExpr;
        private final Expr catchPattern;

        private CatchExpr(TryExpr tryExpr, Expr catchPattern)
        {
            this.tryExpr = tryExpr;
            this.catchPattern = catchPattern;
        }

        public TryExpr Do(Object... args)
        {
            tryExpr._addCatch(catchPattern, argsToExpr(args));
            return tryExpr;
        }
    }

    @SuppressWarnings("PMD.DataClass") // Defined by the project requirements
    public static final class M
    {
        public static final Expr TRUE     = singleExpr("M.TRUE",  scope -> true);
        public static final Expr FALSE    = singleExpr("M.FALSE", scope -> false);

        public static final Expr Val      = storedListExpr("M.Val",     SpecialKey.VAL);
        public static final Expr Arg      = storedListExpr("M.Arg",     SpecialKey.ARG);
        public static final Expr ThisFn   = storedListExpr("M.ThisFn",  SpecialKey.THIS_FN).setStaticLength(1);
        public static final Expr Idx      = storedListExpr("M.Idx",     SpecialKey.IDX).setStaticLength(1);
        public static final Expr Item     = storedListExpr("M.Item",    SpecialKey.ITEM).setStaticLength(1);
        public static final Expr It       = storedListExpr("M.It",      SpecialKey.IT);
        public static final Expr Ex       = storedListExpr("M.Ex",      SpecialKey.EX);
        public static final Expr Partial  = storedListExpr("M.Partial", SpecialKey.PARTIAL);

        public static final Expr Time = singleExpr("M.Time", scope -> (double)System.currentTimeMillis() / 1000.0);
        public static final Expr Rand = singleExpr("M.Rand", scope -> ExecState.instance().random.nextDouble());
        public static final Expr Pi   = singleExpr("M.Pi",   scope -> Math.PI);
        public static final Expr Inf  = singleExpr("M.Inf",  scope -> Double.POSITIVE_INFINITY);
        public static final Expr NaN  = singleExpr("M.NaN",  scope -> Double.NaN);
    }

    public static final Jump Break    = new Jump("Break", 0x01);
    public static final Jump Continue = new Jump("Continue", 0x02);
    public static final Jump Return   = new Jump("Return", 0x04);

    @SuppressWarnings("try")
    public static List<Object> MT(Object... args)
    {
        var state = ExecState.instance();
        state.reset();
        var gs = state.getGlobalScope();
        var ret = argsToExpr(args).evalList(new Scope(gs, false, gs.getAllowedJumps()));
        if(state.endJump(Jump.THROW))
        {
            throw new MTException("Unhandled exception: " + valsRepr(state.exception));

        }
        return ret;
    }

    @SuppressWarnings("try")
    public static Expr Scope(Object... args)
    {
        var body = argsToExpr(args);
        return callExpr("Scope", (outerScope, dest) -> body.eval(new Scope(outerScope, false, outerScope.getAllowedJumps()), dest));
    }

    public static Expr C(Object... args)
    {
        return argsToExpr(args);
    }

    public static Expr I(Object... args)
    {
        return argsToExpr(args).map("I", Mistletoe::toInt);
    }

    public static Expr F(Object... args)
    {
        return argsToExpr(args).map("F", Mistletoe::toFloat);
    }

    public static Expr S(Object... args)
    {
        return argsToExpr(args).map("S", Mistletoe::toStr);
    }

    public static Expr Fn(Object... args)
    {
        var body = argsToExpr(args);
        return singleExpr("Fn", scope -> new FunctionDef(scope, body));
    }

    public static Expr T(Object... args)
    {
        var body = argsToExpr(args);
        return singleExpr("T", scope -> new Tuple(body.evalList(scope)));
    }

    public static VExpr V(Object... keyArgs)
    {
        return new VExpr(argsToExpr(keyArgs));
    }

    public static IfExpr If(Object... args)
    {
        return new IfExpr(argsToExpr(args));
    }

    public static WhileExpr While(Object... args)
    {
        return new WhileExpr(argsToExpr(args));
    }

    public static TryExpr Try(Object... args)
    {
        return new TryExpr(argsToExpr(args));
    }

    public static Expr Throw(Object... args)
    {
        var exExpr = argsToExpr(args);
        return voidExpr("Throw", scope ->
        {
            var state = ExecState.instance();
            state.exception = exExpr.evalList(scope);
            state.currentJump = Jump.THROW;
        });
    }

    public static Expr Note(Object... s)
    {
        return Expr.EMPTY_EXPR;
    }


    // --- Internal types, variables and functions ---


    private static final class StopIteration extends RuntimeException
    {
        private static final StopIteration EX = new StopIteration();
    }

    @FunctionalInterface
    private interface BiDoublePredicate
    {
        boolean test(double x, double y);
    }

    @FunctionalInterface private interface DestFn extends Consumer<Object> {}
    @FunctionalInterface private interface EvalFn extends BiConsumer<Scope,DestFn> {}
    @FunctionalInterface private interface EvalListFn extends Function<Scope,List<Object>> {}
    @FunctionalInterface private interface EvalSingleFn extends Function<Scope,Object> {}

    private static final class Scope
    {
        private final Scope parent;
        private final boolean shallow;
        private final Map<Object,List<Object>> vars = new HashMap<>();
        private final int allowedJumps;

        private Scope(Scope parent, boolean shallow, int allowedJumps)
        {
            this.parent = parent;
            this.shallow = shallow;
            this.allowedJumps = allowedJumps;
        }

        private int getAllowedJumps()
        {
            return allowedJumps;
        }

        private List<Object> get(Object key)
        {
            return getOptional(key)
                .orElseThrow(() -> new MTException(String.format(
                    "Variable \u201c%s\u201d (%s-named) is not defined", toStr(key), typeName(key))));
        }

        private Optional<List<Object>> getOptional(Object key)
        {
            var value = Optional.ofNullable(vars.get(key));
            if(value.isEmpty() && parent != null)
            {
                value = parent.getOptional(key);
            }
            return value;
        }

        private void put(Object key, List<Object> value)
        {
            if(key instanceof SpecialKey || !shallow)
            {
                vars.put(key, value);
            }
            else
            {
                parent.put(key, value);
            }
        }

        @Override
        public String toString()
        {
            return vars + " -> " + parent;
        }
    }

    private static final class Jump
    {
        private static final Jump THROW = new Jump("Throw()", 0x8);
        private final String name;
        private final int bit;
        private Jump(String name, int bit)
        {
            this.name = name;
            this.bit = bit;
        }
        @Override public String toString() { return name; }
    }

    private static final class ExecState
    {
        private static ThreadLocal<ExecState> inst = new ThreadLocal<>()
        {
            @Override public ExecState initialValue()
            {
                return new ExecState();
            }
        };

        private final Random random = new Random();
        private final Scope globalScope = new Scope(null, false, 0);
        private Jump currentJump = null;
        private List<Object> exception = null;

        private static ExecState instance()
        {
            return inst.get();
        }

        @SuppressWarnings("PMD.NullAssignment") // null is a sentinel value here
        private void reset()
        {
            currentJump = null;
        }

        private Scope getGlobalScope()
        {
            return globalScope;
        }

        @SuppressWarnings({
            "PMD.CompareObjectsWithEquals", // object reference comparison is deliberate
            "PMD.NullAssignment"            // null is a sentinel value here
        })
        private boolean endJump(Jump j)
        {
            if(currentJump == j)
            {
                currentJump = null;
                return true;
            }
            return false;
        }
    }

    private static final class FunctionDef
    {
        private final Scope parentScope;
        private final Expr body;
        private FunctionDef(Scope parentScope, Expr body)
        {
            this.parentScope = parentScope;
            this.body = body;
        }

        private Scope getParentScope() { return parentScope; }
        private Expr getBody() { return body; }

        @Override public String toString() { return "Fn(...)"; }
    }

    private static final class Tuple extends ArrayList<Object>
    {
        private Tuple(List<Object> values) { super(values); }

        @Override
        public String toString()
        {
            return "T(" + stream().map(Mistletoe::repr).collect(Collectors.joining(", ")) + ")";
        }
    }

    private enum SpecialKey { VAL, ARG, THIS_FN, IDX, ITEM, IT, EX, PARTIAL }

    private static Expr callExpr(String name, EvalFn fn)
    {
        return new Expr(name)
        {
            @Override public void eval(Scope scope, DestFn dest)
            {
                fn.accept(scope, dest);
            }
        };
    }

    private static Expr voidExpr(String name, Consumer<Scope> fn)
    {
        return new Expr(name)
        {
            @Override public void eval(Scope scope, DestFn dest)
            {
                fn.accept(scope);
            }
        }
        .setStaticLength(0);
    }

    private static Expr generalListExpr(String name, EvalListFn fn, EvalListFn mutableListFn)
    {
        return new Expr(name)
        {
            @Override public List<Object> evalList(Scope scope)        { return fn.apply(scope); }
            @Override public List<Object> evalMutableList(Scope scope) { return mutableListFn.apply(scope); }
            @Override public void eval(Scope scope, DestFn dest)       { fn.apply(scope).forEach(dest); }
            @Override public Expr Len()
            {
                return singleExpr("Len", scope -> (long)evalList(scope).size());
            }
        };
    }

    private static Expr listExpr(String name, EvalListFn fn)
    {
        return generalListExpr(name, fn, fn);
    }

    private static Expr storedListExpr(String name, Object key)
    {
        return generalListExpr(name, scope -> scope.get(key),
                                     scope -> new ArrayList<>(scope.get(key)));
    }

    private static Expr singleExpr(String name, EvalSingleFn fn)
    {
        return new Expr(name)
        {
            @Override public void eval(Scope scope, DestFn dest)       { dest.accept(fn.apply(scope)); }
            @Override public Object evalOne(Scope scope)               { return fn.apply(scope); }
            @Override public Object evalOne(Scope scope, Object defV)  { return fn.apply(scope); }
            @Override public List<Object> evalMutableList(Scope scope) { return evalList(scope); }
            @Override public List<Object> evalList(Scope scope)
            {
                var list = new ArrayList<>();
                list.add(fn.apply(scope));
                return list;
            }
        }
        .setStaticLength(1);
    }

    private static Expr argToExpr(Object arg)
    {
        if(arg instanceof Jump)
        {
            var j = (Jump)arg;
            return voidExpr(j.name, scope ->
            {
                if((scope.getAllowedJumps() & j.bit) == 0)
                {
                    throw new MTException(j.name + " not permitted here");
                }
                ExecState.instance().currentJump = j;
            });
        }
        else if(arg instanceof Expr)
        {
             return (Expr)arg;
        }
        else if(arg instanceof Double || arg instanceof Float)
        {
            double d = ((Number)arg).doubleValue();
            return (d % 1.0 == 0.0) ? singleExpr("<int-literal>",   scope -> (long)d)
                                    : singleExpr("<float-literal>", scope -> d);
        }
        else if(arg instanceof Long || arg instanceof Integer)
        {
            long l = ((Number)arg).longValue();
            return singleExpr("<int-literal>", scope -> l);
        }
        else if(arg instanceof String)
        {
            return new Parser(escapeStr((String)arg, "%")).parseStringLiteral();
        }
        else
        {
            throw new MTException(
                String.format("Unsupported host language value %s \u201c%s\u201d", arg.getClass().getName(), arg));
        }
    }

    @SuppressWarnings("PMD.UseVarargs") // Only supplied with pre-existing arrays
    private static Expr argsToExpr(Object[] args)
    {
        switch(args.length)
        {
            case 0: return Expr.EMPTY_EXPR;
            case 1: return argToExpr(args[0]);
            default:
                var exprList = Arrays.stream(args).map(Mistletoe::argToExpr).collect(Collectors.toList());
                long staticLen = 0L;
                for(var expr : exprList)
                {
                    long len = expr.getStaticLength();
                    if(len == Expr.UNKNOWN_LENGTH)
                    {
                        staticLen = Expr.UNKNOWN_LENGTH;
                        break;
                    }
                    staticLen += len;
                }

                return callExpr("<expr-list>", (scope, dest) ->
                {
                    var state = ExecState.instance();
                    for(var expr : exprList)
                    {
                        expr.eval(scope, dest);
                        if(state.currentJump != null)
                        {
                            break;
                        }
                    }
                })
                .setStaticLength(staticLen);
        }
    }

    private static long toInt(Object obj)
    {
        if(obj instanceof Number)
        {
            return ((Number)obj).longValue();
        }
        if(obj instanceof String)
        {
            try
            {
                return (long)Double.parseDouble((String)obj);
            }
            catch(NumberFormatException e) {} // NOPMD -- deliberately fall-through to the throw.
        }
        throw new MTException(String.format(
            "Cannot convert %s \u201c%s\u201d to Int", typeName(obj), repr(obj)));
    }

    private static double toFloat(Object obj)
    {
        if(obj instanceof Number)
        {
            return ((Number)obj).doubleValue();
        }
        if(obj instanceof String)
        {
            try
            {
                return Double.parseDouble((String)obj);
            }
            catch(NumberFormatException e) {} // NOPMD -- deliberately fall-through to the throw.
        }
        throw new MTException(String.format(
            "Cannot convert %s \u201c%s\u201d to Float", typeName(obj), repr(obj)));
    }

    private static boolean toBool(Object obj)
    {
        if(obj instanceof Boolean)
        {
            return (Boolean)obj;
        }
        throw new MTException(String.format(
            "Expected Bool but found %s \u201c%s\u201d", typeName(obj), repr(obj)));
    }

    private static String toStr(Object obj)
    {
        if(obj instanceof String) { return (String)obj; }
        if(obj instanceof Boolean)
        {
            return (Boolean)obj ? "M.TRUE" : "M.FALSE";
        }
        if(obj instanceof Double)
        {
            Double d = (Double)obj;
            if(Double.isNaN(d)) { return "M.NaN"; }
            else if(d == Double.POSITIVE_INFINITY) { return "M.Inf"; }
            else if(d == Double.NEGATIVE_INFINITY) { return "M.Inf.Neg()"; }
        }
        return String.valueOf(obj);
    }

    private static String escapeStr(String s, String percent)
    {
        var sb = new StringBuilder("\"");
        s.chars().forEach(ch ->
        {
            switch(ch)
            {
                case '\t': sb.append("\\t"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '%':  sb.append(percent); break;
                default:   sb.append((char)ch); break;
            }
        });
        sb.append('"');
        return sb.toString();
    }

    private static String repr(Object obj)
    {
        return (obj instanceof String) ? escapeStr((String)obj, "%%") : toStr(obj);
    }

    private static String valsRepr(List<Object> vals)
    {
        return "[" + vals.stream().map(Mistletoe::repr).collect(Collectors.joining(", ")) + "]";
    }

    private static String typeName(Object obj)
    {
        if(obj instanceof String)       { return "Str"; }
        if(obj instanceof Long)         { return "Int"; }
        if(obj instanceof Double)       { return "Float"; }
        if(obj instanceof Boolean)      { return "Bool"; }
        if(obj instanceof FunctionDef) { return "Fn"; }
        if(obj instanceof Tuple)       { return "Tuple"; }
        if(obj instanceof SpecialKey) { return "SpecialKey"; }
        throw new IllegalArgumentException();
    }

    private static boolean eq(Object x, Object y)
    {
        if(x instanceof Number && y instanceof Number)
        {
            return ((Number)x).doubleValue() == ((Number)y).doubleValue();
        }
        return x.equals(y);
    }

    // private static BinaryOperator<Object> compare(String name, BiDoublePredicate floatTest, IntPredicate strTest)
    // {
    //     return (x, y) ->
    //     {
    //         if(x instanceof Number && y instanceof Number)
    //         {
    //             return floatTest.test(((Number)x).doubleValue(), ((Number)y).doubleValue());
    //         }
    //         if(x instanceof String && y instanceof String)
    //         {
    //             return strTest.test(((String)x).compareTo((String)y));
    //         }
    //         throw new MTException(String.format(
    //             "%s: %s \u201c%s\u201d and %s \u201c%s\u201d are not comparable",
    //             name, typeName(x), repr(x), typeName(y), repr(y)));
    //     };
    // }

    private static BinaryOperator<Object> compare(String name, IntPredicate predicate)
    {
        return (x, y) ->
        {
            int rel;
            if(x instanceof Long && y instanceof Long)
            {
                rel = Long.compare((Long)x, (Long)y);
            }
            else if(x instanceof Number && y instanceof Number)
            {
                double xd = ((Number)x).doubleValue();
                double yd = ((Number)y).doubleValue();
                if(Double.isNaN(xd) || Double.isNaN(yd))
                {
                    return false;
                }
                rel = Double.compare(((Number)x).doubleValue(), ((Number)y).doubleValue());
            }
            else if(x instanceof String && y instanceof String)
            {
                rel = ((String)x).compareTo((String)y);
            }
            else
            {
                throw new MTException(String.format(
                    "%s: %s \u201c%s\u201d and %s \u201c%s\u201d are not comparable",
                    name, typeName(x), repr(x), typeName(y), repr(y)));
            }
            return predicate.test(rel);
        };
    }

    private static BinaryOperator<Object> intOrFloat(BiFunction<Long,Long,Object> intFn,
                                                      BiFunction<Double,Double,Object> floatFn)
    {
        return (x, y) -> (x instanceof Long && y instanceof Long) ? intFn.apply((Long)x, (Long)y)
                                                                  : floatFn.apply(toFloat(x), toFloat(y));
    }

    private static double nonZero(double n)
    {
        if(n == 0.0) { throw new MTException("Division by zero"); }
        return n;
    }

    private static long nonZero(long n)
    {
        if(n == 0L) { throw new MTException("Division by zero"); }
        return n;
    }

    @FunctionalInterface
    private interface Mutator
    {
        void set(Scope scope, Object key, List<Object> values);
    }

    @SuppressWarnings("try")
    private static Expr assign(String name, Expr keyExpr, Expr valueExpr, Mutator mutator)
    {
        return voidExpr(name, outerScope ->
        {
            var keys = keyExpr.evalList(outerScope);
            if(keys.isEmpty()) { throw new MTException("Cannot assign to an empty list of variables"); }

            var oldValues = keys.stream()
                .flatMap(k -> outerScope.getOptional(k).orElse(List.of()).stream())
                .collect(Collectors.toList());

            var innerScope = new Scope(outerScope, true, outerScope.getAllowedJumps());
            innerScope.put(SpecialKey.VAL, oldValues);
            List<Object> newValues = valueExpr.evalList(innerScope);

            var valueIt = newValues.iterator();
            int nKeys = keys.size();
            for(int i = 0; i < nKeys - 1; i++)
            {
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Separate list objects required for each iteration
                var valueList = new ArrayList<>();
                if(valueIt.hasNext())
                {
                    valueList.add(valueIt.next());
                }
                mutator.set(outerScope, keys.get(i), valueList);
            }

            var valueList = new ArrayList<>();
            while(valueIt.hasNext())
            {
                valueList.add(valueIt.next());
            }
            mutator.set(outerScope, keys.get(nKeys - 1), valueList);
        });
    }

    @SuppressWarnings("try")
    private static Expr call(String name, Expr fnExpr, Expr fnArgExpr)
    {
        return callExpr(name, (outerScope, dest) ->
        {
            var fnArgs = fnArgExpr.evalList(outerScope);
            fnExpr.eval(outerScope, fnIdent ->
            {
                FunctionDef fn;
                if(fnIdent instanceof FunctionDef)
                {
                    fn = (FunctionDef)fnIdent;
                }
                else
                {
                    var lookup = outerScope.getOptional(fnIdent).orElse(List.of());
                    if(lookup.size() == 1 && lookup.get(0) instanceof FunctionDef)
                    {
                        fn = (FunctionDef)lookup.get(0);
                    }
                    else
                    {
                        throw new MTException(String.format("No function named \"%s\"", fnIdent));
                    }
                }

                var innerScope = new Scope(fn.getParentScope(), false, Return.bit);
                innerScope.put(SpecialKey.ARG, fnArgs);
                innerScope.put(SpecialKey.THIS_FN, List.of(fn));
                try
                {
                    fn.getBody().eval(innerScope, dest);
                }
                finally
                {
                    ExecState.instance().endJump(Return);
                }
            });
        });
    }

    private static final class Parser
    {
        private enum TT
        {
            OPEN      ("\u201c(\u201d",  "\\("),
            CLOSE     ("\u201c)\u201d",  "\\)"),
            COMMA     ("\u201c,\u201d",  ","),
            DOT       ("\u201c.\u201d",  "\\."),
            HEX_NUMBER("<hex-number>",   "[-+]?0x[0-9a-fA-F]+"),
            NUMBER    ("<number>",       "[-+]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)([eE][+-]?[0-9]+)?"),
            STR_DOUBLE("\u201c\"\u201d", "\""),
            STR_SINGLE("\u201c'\u201d",  "'"),
            KEYWORD   ("<keyword>",      "[A-Z][a-zA-Z0-9]*"),
            IDENTIFIER("<identifier>",   "[a-z][a-zA-Z0-9_]*");

            private final String label;
            private final Pattern pattern;
            TT(String label, String p) {
                this.label = label;
                this.pattern = Pattern.compile(p);
            }
        }

        private enum SyntaxMode { NORMAL, STRING_EMBEDDED }

        private static final Pattern ESCAPE = Pattern.compile("[%DH(]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4}|U[0-9a-fA-F]{6}");
        private static final int ERROR_CONTEXT = 15;
        private final String input;
        private String token = null;
        private int pos = 0;

        private Parser(String input)
        {
            this.input = input;
        }

        private void addLastStr(List<Expr> components, StringBuilder sb)
        {
            var lastStr = sb.toString();
            int len = lastStr.length();
            if(len > 0)
            {
                sb.delete(0, len);
                components.add(singleExpr("<str-component>", scope -> lastStr));
            }
        }

        private Expr parseStringLiteral()
        {
            var components = new ArrayList<Expr>();
            var sb = new StringBuilder();
            var escapeMatcher = ESCAPE.matcher(input);
            int inputLen = input.length();
            int startPos = pos;

            char delimCh = input.charAt(pos);
            pos += 1;

            while(pos < inputLen)
            {
                char ch = input.charAt(pos);
                if(ch == delimCh)
                {
                    pos += 1;
                    addLastStr(components, sb);
                    return singleExpr("<str-literal>", scope ->
                    {
                        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Only created on final iteration
                        var finalSb = new StringBuilder();
                        for(var c : components)
                        {
                            c.eval(scope, v -> finalSb.append(toStr(v)));
                        }
                        return finalSb.toString();
                    });
                }
                switch(ch)
                {
                    case '\\':
                        switch(((pos + 1) < inputLen) ? input.charAt(pos + 1) : '\0')
                        {
                            case 't':  sb.append('\t'); break;
                            case 'n':  sb.append('\n'); break;
                            case 'r':  sb.append('\r'); break;
                            case '"':  sb.append('"');  break;
                            case '\\': sb.append('\\'); break;
                            default:
                                throw parseErr("illegal \\-escape");
                        }
                        pos += 2;
                        break;

                    case '%':
                        if(!escapeMatcher.region(pos + 1, inputLen).lookingAt())
                        {
                            throw parseErr("illegal %%-escape (use \u201c%%%%\u201d for a literal \u201c%%\u201d)");
                        }
                        var escape = escapeMatcher.group();
                        pos += 1 + escape.length();
                        switch(escape.charAt(0))
                        {
                            case '%':  sb.append('%'); break;
                            case '\'': sb.append('\''); break;
                            case 'D':  sb.append('$'); break;
                            case 'H':  sb.append('#'); break;
                            case 'x': case 'u': case 'U':
                                sb.append(Character.toString(Integer.parseInt(escape.substring(1), 16)));
                                break;
                            case '(':
                                pos -= 1;
                                addLastStr(components, sb);
                                components.add(parseList(SyntaxMode.STRING_EMBEDDED));
                                break;
                            default:
                                throw new AssertionError();
                        }
                        break;

                    case '"':
                        throw parseErr("illegal char \u201c\"\u201d (use \\\")");

                    case '$': case '#':
                        throw parseErr("illegal char \u201c%c\u201d (use a %%-escape)", ch);

                    default:
                        pos += 1;
                        sb.append(ch);
                }
            }
            pos = startPos;
            throw parseErr("unclosed string literal");
        }

        private Object parseOperator(Object expr, Class<?> type, SyntaxMode syntaxMode)
        {
            try
            {
                for(var m : type.getMethods())
                {
                    if(m.getName().equals(token))
                    {
                        if(m.getParameterCount() == 0)
                        {
                            next(TT.OPEN);
                            next(TT.CLOSE);
                            return m.invoke(expr);
                        }
                        else
                        {
                            @SuppressWarnings("AvoidInstantiatingObjectsInLoops") // Needed to invoke reflective call
                            Object[] reflectionArgs = { parseList(syntaxMode) };
                            return m.invoke(expr, (Object)reflectionArgs);
                        }
                    }
                }
                return type.getField(token).get(expr);
            }
            catch(InvocationTargetException | IllegalAccessException e)
            {
                var cause = e.getCause();
                if(cause instanceof MTException)
                {
                    throw (MTException)cause; // NOPMD (PreserveStackTrace) -- the point is to re-throw the original exception.
                }
                throw new AssertionError(e);
            }
            catch(ReflectiveOperationException e)
            {
                pos -= token.length();
                throw parseErr(e, "unexpected symbol \u201c%s\u201d", token);
            }
        }

        private Expr parseList(SyntaxMode syntaxMode)
        {
            TT strTT = (syntaxMode == SyntaxMode.NORMAL) ? TT.STR_DOUBLE : TT.STR_SINGLE;
            TT identifierTT = (syntaxMode == SyntaxMode.NORMAL) ? null : TT.IDENTIFIER;

            var args = new ArrayList<Object>();
            next(TT.OPEN);
            TT tokenType = next(TT.HEX_NUMBER, TT.NUMBER, strTT, identifierTT, TT.KEYWORD, TT.CLOSE);
            while(tokenType != TT.CLOSE)
            {
                switch(tokenType)
                {
                    case NUMBER:     args.add(Double.parseDouble(token));                   break;
                    case HEX_NUMBER: args.add(Long.parseLong(token.replace("0x", ""), 16)); break;
                    case STR_DOUBLE:
                    case STR_SINGLE:
                        pos -= token.length();
                        args.add(parseStringLiteral());
                        break;

                    case IDENTIFIER:
                    case KEYWORD:
                        Object expr;
                        if(tokenType == TT.IDENTIFIER)
                        {
                            String varName = token;

                            @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops") // Logically necessary
                            Object newExpr = new VExpr(singleExpr("<identifier>", scope -> varName));

                            expr = newExpr;
                        }
                        else if("M".equals(token))
                        {
                            next(TT.DOT);
                            next(TT.KEYWORD);
                            expr = parseOperator(null, M.class, syntaxMode);
                        }
                        else
                        {
                            expr = parseOperator(null, Mistletoe.class, syntaxMode);
                        }

                        if(expr instanceof Jump)
                        {
                            args.add(expr);
                        }
                        else
                        {
                            while(next(TT.DOT, TT.COMMA, TT.CLOSE) == TT.DOT)
                            {
                                next(TT.KEYWORD);
                                expr = parseOperator(expr, expr.getClass(), syntaxMode);
                            }
                            pos -= token.length();
                            if(!(expr instanceof Expr))
                            {
                                throw parseErr("incomplete expression");
                            }
                            args.add((Expr)expr);
                        }
                        break;

                    default: throw new AssertionError();
                }
                tokenType = next(TT.COMMA, TT.CLOSE);
                if(tokenType == TT.COMMA)
                {
                    tokenType = next(TT.HEX_NUMBER, TT.NUMBER, strTT, identifierTT, TT.KEYWORD);
                }
            }
            return argsToExpr(args.toArray());
        }

        @SuppressWarnings("PMD.LawOfDemeter") // False positive -- use of regex API
        private TT next(TT... expectedTokens)
        {
            int inputLen = input.length();
            while(pos < inputLen && Character.isWhitespace(input.charAt(pos)))
            {
                pos++;
            }
            for(TT tokenType : expectedTokens)
            {
                if(tokenType == null) { continue; }
                var matcher = tokenType.pattern.matcher(input);
                if(matcher.region(pos, inputLen).lookingAt())
                {
                    token = matcher.group();
                    pos += token.length();
                    return tokenType;
                }
            }

            if(expectedTokens.length == 1)
            {
                throw parseErr("expected %s", expectedTokens[0].label);
            }
            throw parseErr(
                "expected one of %s",
                Arrays.stream(expectedTokens)
                    .filter(Objects::nonNull)
                    .map(tokenType -> tokenType.label)
                    .collect(Collectors.joining(", ")));
        }

        private MTException parseErr(String format, Object... args)
        {
            return parseErr(null, format, args);
        }

        private MTException parseErr(Throwable cause, String format, Object... args)
        {
            int pos1 = pos;
            String inp = input;
            if(inp.startsWith("(") && inp.endsWith(")"))
            {
                pos1 -= 1;
                inp = inp.substring(1, inp.length() - 1);
            }

            int line = 1 + (int)inp.substring(0, pos1).chars().filter(ch -> ch == '\n').count();
            int col = pos1 - inp.lastIndexOf('\n', pos1 - 1);

            int preCutoff = Math.max(0, pos1 - ERROR_CONTEXT);
            String preContext = (preCutoff > 0 ? "..." : "") + inp.substring(preCutoff, pos1);

            int postCutoff = Math.min(inp.length(), pos1 + ERROR_CONTEXT);
            String postContext = inp.substring(pos1, postCutoff) + (postCutoff < inp.length() ? "..." : "");

            return new MTException(
                String.format(
                    "Parsing error at line %d, col %d: %s in \u201c%s\u25b6%s\u201d",
                    line, col, String.format(format, args), preContext, postContext),
                cause);
        }
    }
}
