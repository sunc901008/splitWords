package focus.search.bnf;

import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Constant;
import focus.search.bnf.exception.InvalidGrammarException;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.bnf.tokens.*;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.response.exception.AmbiguitiesException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusParser {

    private BnfParser parser = null;
    public FocusAnalyzer focusAnalyzer = new FocusAnalyzer();
    private final static int MAX_RULE_LOOP = 10;

    public FocusParser() {
        init();
    }

    public FocusParser(String file) {
        init(file);
    }

    private void init() {
        init("bnf-file/question.bnf");
        init("bnf-file/function.bnf");
    }

    private void init(String file) {
        ResourceLoader resolver = new DefaultResourceLoader();
        try {
            if (parser == null) {
                parser = new BnfParser(new FileInputStream(resolver.getResource(file).getFile()));
            } else {
                parser.setGrammar(new Scanner(new FileInputStream(resolver.getResource(file).getFile())));
            }
        } catch (InvalidGrammarException | IOException e) {
            e.printStackTrace();
        }
    }

    public void addRule(BnfRule rule) {
//        init();
        parser.addRule(rule);
    }

    public void resetRule(BnfRule rule) {
        parser.resetRule(rule);
    }

    public List<TerminalToken> getTerminalTokens() {
        return parser.getTerminalTokens();
    }

    private BnfRule getRule(Token token) {
        return parser.getRule(token);
    }

    public BnfRule getRule(String name) {
        return parser.getRule(name);
    }

    public FocusInst parseQuestion(List<FocusToken> tokens, JSONObject amb) throws IOException, InvalidRuleException, AmbiguitiesException {
        List<FocusToken> copyTokens = new ArrayList<>(tokens);
        FocusInst fi = new FocusInst();
        int flag = 0;
        int position = 0;
        int error = 0;
        while (flag >= 0) {
            if (flag > 0) {
                copyTokens = copyTokens.subList(flag, copyTokens.size());
            }
            FocusSubInst fsi;
            try {
                fsi = subParse(copyTokens, amb);
            } catch (AmbiguitiesException e) {
                e.position = flag + e.position;
                throw e;
            }
            if (fsi == null) {
                fi.position = position;
                break;
            }
            flag = fsi.getIndex();

            error = position;
            position = position + flag;
            if (fsi.isError()) {// 出错，记录已解析的token和出错位置
                fi.position = position;
                fi.addPfs(fsi.getFps());
                break;
            } else {// 未出错
                boolean filter = flag != -1;// 没有解析结束
                if (filter) {
                    for (FocusPhrase fp : fsi.getFps()) {
                        if (fp.size() == flag) {
                            fi.addPf(fp);
                        }
                    }
                    if (fi.getFocusPhrases().isEmpty()) {
                        fi.addPfs(fsi.getFps());
                        break;
                    }
                } else {// 解析结束
                    fi.addPfs(fsi.getFps());
                }
            }
        }
        if (error > 0 && error < position) {
            FocusSubInst fsi = subParse(tokens.subList(error, position), amb);
            assert fsi != null;
            fi.addPfs(fsi.getFps());
            if (fsi.isError()) {
                fi.position = position;
            }
        }
        return fi;
    }

    public FocusInst parseFormula(List<FocusToken> tokens, JSONObject amb) throws IOException, InvalidRuleException, AmbiguitiesException {
        FocusInst fi = new FocusInst();
        int flag = 0;
        int position = 0;
        FocusSubInst fsi;
        try {
            fsi = subParse(tokens, amb);
        } catch (AmbiguitiesException e) {
            e.position = flag + e.position;
            throw e;
        }
        if (fsi == null) {
            fi.position = position;
            return fi;
        }
        flag = fsi.getIndex();

        boolean filter = flag != -1;// 没有解析结束
        position = position + flag;
        if (fsi.isError() || filter) {// 出错或者没有解析结束(formula只能有一个phrase或者filter)，记录已解析的token和出错位置
            fi.position = position;
            fi.addPfs(fsi.getFps());
        } else {// 未出错 或者 解析结束
            fi.addPfs(fsi.getFps());
        }
        return fi;
    }

    private FocusSubInst subParse(List<FocusToken> tokens, JSONObject amb) throws InvalidRuleException, AmbiguitiesException {
        FocusToken focusToken = tokens.get(0);

        List<FocusPhrase> focusPhrases = focusPhrases(focusToken);
        if (focusPhrases == null || focusPhrases.isEmpty()) {
            return null;
        }

        // 歧义检测
        ambiguitiesCheck(focusPhrases, 0, amb);

        for (int i = 1; i < tokens.size(); i++) {
            List<FocusPhrase> tmp = new ArrayList<>(focusPhrases);

            FocusToken ft = tokens.get(i);
            if (Constant.FNDType.COLUMNVALUE.equals(ft.getType())) {
                int loop = focusPhrases.size();
                while (loop > 0) {
                    FocusPhrase fp = focusPhrases.remove(0);
                    loop--;
                    if (fp.size() < i + 1) {
                        continue;
                    }
                    FocusNode tmpNode = fp.getNode(i);
                    if (ColumnValueTerminalToken.COLUMNVALUE.equals(tmpNode.getValue())) {
                        tmpNode.setValue(ft.getWord());
                        tmpNode.setBegin(ft.getStart());
                        tmpNode.setEnd(ft.getEnd());
                        tmpNode.setTerminal(true);
                        fp.removeNode(i);
                        fp.addPn(i, tmpNode);
                        focusPhrases.add(fp);
                    }
                }
                if (focusPhrases.isEmpty()) {// 出错结束
                    FocusSubInst fsi = new FocusSubInst();
                    fsi.setIndex(i);
                    fsi.setFps(tmp);
                    fsi.setError(true);
                    return fsi;
                }
                continue;
            }

            List<BnfRule> rules = parseRules(ft.getWord());
            if (rules.isEmpty()) {
                focusPhrases.clear();
                for (FocusPhrase fp : tmp) {
                    if (fp.size() > i && fp.getNode(i).getValue().equalsIgnoreCase(ft.getWord())) {
                        if (fp.size() == i + 1) {
                            fp.setType(Constant.INSTRUCTION);
                        }
                        focusPhrases.add(fp);
                    }
                }
                if (focusPhrases.isEmpty()) {// 出错结束
                    FocusSubInst fsi = new FocusSubInst();
                    fsi.setIndex(i);
                    fsi.setFps(tmp);
                    fsi.setError(true);
                    return fsi;
                }
            } else {
                replace(rules, focusPhrases, ft, i);
                List<FocusPhrase> remove = new ArrayList<>();
                List<FocusPhrase> startWith = new ArrayList<>();
                List<FocusPhrase> copy = new ArrayList<>(focusPhrases);
                for (FocusPhrase fp : focusPhrases) {
                    if (fp.size() <= i) {
                        remove.add(fp);
                        continue;
                    }
                    if (!fp.getNode(i).getValue().equalsIgnoreCase(ft.getWord())) {
                        remove.add(fp);
                    }
                    if (fp.getNode(i).getValue().toLowerCase().startsWith(ft.getWord().toLowerCase())) {
                        startWith.add(fp);
                    }
                }
                focusPhrases.removeAll(remove);
                if (focusPhrases.isEmpty()) {
                    for (FocusPhrase f : copy) {
                        if (!f.isSuggestion()) {
                            focusPhrases.add(f);
                        }
                    }
                    FocusSubInst fsi = new FocusSubInst();
                    fsi.setIndex(i);
                    if (!focusPhrases.isEmpty()) {
                        fsi.setFps(focusPhrases);
                        return fsi;
                    }
                    if (startWith.isEmpty()) {
                        fsi.setError(true);
                        fsi.setFps(remove);
                        return fsi;
                    } else {
                        if (i < tokens.size() - 1) {//不是最后一个token,说明中间出错
                            fsi.setError(true);
                        }
                        fsi.setFps(startWith);
                        return fsi;
                    }
                }
//                }
            }
            // 歧义检测
            ambiguitiesCheck(focusPhrases, i, amb);
        }

        FocusSubInst fsi = new FocusSubInst();
        for (FocusPhrase fp : focusPhrases) {
            if (fp.size() == tokens.size()) {
                fsi.addFps(fp);
            }
        }
        if (fsi.isEmpty()) {
            for (FocusPhrase fp : focusPhrases) {
                if (fp.size() > tokens.size()) {
                    fsi.addFps(fp);
                }
            }
        }
        fsi.setIndex(-1);

        return fsi;
    }

    /**
     * creator: sunc
     * date: 2018/3/1
     * description: 检测歧义
     */
    private void ambiguitiesCheck(List<FocusPhrase> focusPhrases, int index, JSONObject amb) throws AmbiguitiesException {
        List<AmbiguitiesRecord> ars = new ArrayList<>();
//        List<Integer> added = new ArrayList<>();
//        boolean hasSourceName = false;
//        for (FocusPhrase fp : focusPhrases) {
//            FocusNode fn = fp.getNode(index);
//            AmbiguitiesRecord ar = new AmbiguitiesRecord();
//            if (Constant.FNDType.TABLE.equals(fn.getType()) && !hasSourceName) {
//                hasSourceName = true;
//                ar.type = Constant.FNDType.TABLE;
//                ar.sourceName = fn.getValue();
//                ars.add(ar);
//            } else if (Constant.FNDType.COLUMN.equals(fn.getType())) {
//                Column column = fn.getColumn();
//                if (!added.contains(column.getColumnId())) {
//                    added.add(column.getColumnId());
//                    ar.type = Constant.FNDType.COLUMN;
//                    ar.sourceName = column.getSourceName();
//                    ar.columnId = column.getColumnId();
//                    ar.columnName = column.getColumnDisplayName();
//                    ars.add(ar);
//                }
//            }
//        }

        String value = focusPhrases.get(0).getNode(index).getValue();
        AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue(value, amb);
        boolean isResolved = false;
        AmbiguitiesRecord resolve = null;
        if (ambiguitiesResolve != null) {
            if (ambiguitiesResolve.isResolved) {
                isResolved = true;
                resolve = ambiguitiesResolve.ars.get(0);
            }
        }

        List<Integer> added = new ArrayList<>();
        List<FocusPhrase> remove = new ArrayList<>();
        for (FocusPhrase fp : focusPhrases) {
            FocusNode fn = fp.getNode(index);
            if (isResolved) {
                if (!fn.getType().equals(resolve.type) || (Constant.FNDType.COLUMN.equals(fn.getType()) && fn.getColumn().getColumnId() != resolve.columnId)) {
                    remove.add(fp);
                }
            } else if (Constant.FNDType.COLUMN.equals(fn.getType())) {
                Column column = fn.getColumn();
                AmbiguitiesRecord ar = new AmbiguitiesRecord();
                if (!added.contains(column.getColumnId())) {
                    added.add(column.getColumnId());
                    ar.type = Constant.FNDType.COLUMN;
                    ar.sourceName = column.getSourceName();
                    ar.columnId = column.getColumnId();
                    ar.columnName = column.getColumnDisplayName();
                    ars.add(ar);
                }
            }
        }
        focusPhrases.removeAll(remove);
        if (!isResolved && ars.size() > 1) {
            throw new AmbiguitiesException(ars, index);
        }
    }

    private boolean same(List<FocusPhrase> o1, List<FocusPhrase> o2) {
        if (o1.size() != o2.size()) {
            return false;
        }
        for (int i = 0; i < o1.size(); i++) {
            if (!o1.get(i).toJSON().equals(o2.get(i).toJSON())) {
                return false;
            }
        }
        return true;
    }

    private void replace(List<BnfRule> rules, List<FocusPhrase> focusPhrases, FocusToken focusToken, int position) {
        int max_rule = 1;
        while (!rules.isEmpty() && max_rule < MAX_RULE_LOOP) {
            //  记录替换token之前的phrase
            List<FocusPhrase> copy = new ArrayList<>(focusPhrases);

            max_rule++;
            int loop = focusPhrases.size();
            while (loop > 0 && !focusPhrases.isEmpty()) {
                FocusPhrase focusPhrase = focusPhrases.remove(0);
                if (focusPhrase.size() <= position) {
                    focusPhrase.setType(Constant.INSTRUCTION);
                    focusPhrases.add(focusPhrase);
                } else {
                    FocusNode fn = focusPhrase.getNode(position);
                    TerminalToken tt = terminal(fn.getValue());
                    if (tt != null) {
                        if (fn.getValue().equalsIgnoreCase(focusToken.getWord())) {
                            focusPhrase.removeNode(position);
                            fn.setTerminal(true);
                            fn.setType(tt.getType());
//                            fn.setColumn(tt.getColumn());
                            fn.setBegin(focusToken.getStart());
                            fn.setEnd(focusToken.getEnd());
                            focusPhrase.addPn(position, fn);
                            if (focusPhrase.size() == position + 1) {
                                focusPhrase.setType(Constant.INSTRUCTION);
                            }
                            focusPhrases.add(focusPhrase);
                        }
                    } else if (fn.isTerminal()) {
                        focusPhrases.add(focusPhrase);
                        loop--;
                        continue;
                    } else {
                        BnfRule br;
                        try {
                            br = findRule(rules, fn.getValue());
                        } catch (InvalidRuleException e) {
                            loop--;
                            continue;
                        }
                        for (TokenString ts : br.getAlternatives()) {
                            FocusPhrase newFp = new FocusPhrase(focusPhrase.getInstName());
                            newFp.addPns(focusPhrase.subNodes(0, position));
                            for (int i = 0; i < ts.size(); i++) {
                                Token token = ts.get(i);
                                FocusNode newFn = new FocusNode(token.getName());
                                if (token instanceof TerminalToken) {
                                    newFn.setValue(token.getName());
                                    newFn.setType(((TerminalToken) token).getType());
                                    if (i == 0) {
                                        if (newFn.getType().equals(Constant.FNDType.INTEGER)
                                                || newFn.getType().equals(Constant.FNDType.DOUBLE)
                                                || newFn.getType().equals(Constant.FNDType.COLUMNVALUE)) {
                                            newFn.setValue(focusToken.getWord());
                                        }
                                    }
                                    newFn.setColumn(((TerminalToken) token).getColumn());
                                    newFn.setTerminal(true);
                                }
                                if (i == 0) {
                                    newFn.setBegin(focusToken.getStart());
                                    newFn.setEnd(focusToken.getEnd());
                                }
                                newFp.addPn(newFn);
                            }
                            newFp.addPns(focusPhrase.subNodes(position + 1));
                            if (newFp.size() == position + 1 && newFp.getNode(position).getValue().equalsIgnoreCase(focusToken.getWord())) {
                                newFp.setType(Constant.INSTRUCTION);
                            }
                            focusPhrases.add(newFp);
                        }
                    }
                }
                loop--;
            }
            if (focusPhrases.isEmpty()) {
                focusPhrases.addAll(copy);
            }

            //  比较替换之前和替换之后的phrase，如果无变化则表示该次替换完成
            if (same(copy, focusPhrases)) {
                break;
            }
        }
    }

    private List<FocusPhrase> focusPhrases(FocusToken focusToken) throws InvalidRuleException {
        String word = focusToken.getWord();
        List<BnfRule> rules = parseRules(parser.getM_rules(), word);

        if (rules.size() == 0) {
            return null;
        }
        BnfRule rule = rules.remove(0);
        List<FocusPhrase> focusPhrases = new ArrayList<>();
        List<BnfRule> removes = new ArrayList<>();


        if (rules.isEmpty()) {
            for (TokenString ts : rule.getAlternatives()) {
                FocusPhrase fp = new FocusPhrase();
                fp.setInstName(rule.getLeftHandSide().getName());
                for (Token token : ts) {
                    FocusNode fn = new FocusNode(token.getName());
                    fn.setType(focusToken.getType());
                    fn.setTerminal(true);
                    if (token.getName().equalsIgnoreCase(focusToken.getWord())) {
                        fn.setBegin(focusToken.getStart());
                        fn.setEnd(focusToken.getEnd());
                    }
                    fp.addPn(fn);
                }
                focusPhrases.add(fp);
            }
        } else {
            for (TokenString alt : rule.getAlternatives()) {
                Token inst = alt.getFirst();
                BnfRule br = findRule(rules, inst);
                removes.add(br);
                for (TokenString ts : br.getAlternatives()) {
                    FocusPhrase fp = new FocusPhrase();
                    fp.setInstName(inst.getName());
                    for (Token token : ts) {
                        FocusNode fn = new FocusNode(token.getName());
                        TerminalToken tt = terminal(fn.getValue());
                        if (tt != null) {
                            fn.setTerminal(true);
                            fn.setType(tt.getType());
                            fn.setColumn(tt.getColumn());
                            if (token.getName().equalsIgnoreCase(focusToken.getWord())) {
                                fn.setBegin(focusToken.getStart());
                                fn.setEnd(focusToken.getEnd());
                            }
                        }
                        fp.addPn(fn);
                    }
                    focusPhrases.add(fp);
                }
            }
            rules.removeAll(removes);
        }

        replace(rules, focusPhrases, focusToken, 0);

        return focusPhrases;
    }

    private BnfRule findRule(List<BnfRule> rules, Token token) throws InvalidRuleException {
        return findRule(rules, token.getName());
    }

    private BnfRule findRule(List<BnfRule> rules, String token) throws InvalidRuleException {
        for (BnfRule rule : rules) {
            if (rule.getLeftHandSide().getName().equals(token)) {
                return rule;
            }
        }
        throw new InvalidRuleException("Cannot find rule for token " + JSONObject.toJSONString(token));
    }

    public List<BnfRule> parseRules(String word) throws InvalidRuleException {
        return parseRules(parser.getM_rules(), word);
    }

    private List<BnfRule> parseRules(List<BnfRule> rules, String word) throws InvalidRuleException {
        List<BnfRule> res = new ArrayList<>();
        for (BnfRule br : rules) {
            BnfRule rule = parse(br, word, new ArrayList<>());
            if (rule != null) {
                res.add(rule);
            }
        }
        return res;
    }

    private BnfRule parse(BnfRule rule, String word, List<TokenString> hasScanned) throws InvalidRuleException {
        BnfRule br = new BnfRule();
        br.setLeftHandSide(rule.getLeftHandSide());
        for (TokenString alt : rule.getAlternatives()) {
            boolean hasAdd = true;
            if (!hasScanned(hasScanned, alt)) {
                hasScanned.add(alt);
                hasAdd = false;
            }
            Token token = alt.getFirst();
            if (token instanceof ColumnValueTerminalToken) {
                //debug
                System.out.println(token.toString());
            }
            if (token instanceof TerminalToken) {
                if (token.match(word)) {
                    if (isTerminal(token.getName())) {
                        TokenString alternative_to_add = new TokenString();
                        alternative_to_add.add(token);
                        br.addAlternative(alternative_to_add);
                    } else {
                        br.addAlternative(alt);
                    }
                }
            } else {
                BnfRule newBr = getRule(token);
                // 过滤公式|列规则|列中值
                List<String> filter = Arrays.asList("<function-columns>", "<value>");
                if (newBr == null && !token.getName().endsWith("-column>") && !filter.contains(token.getName())) {
                    throw new InvalidRuleException("Cannot find rule for token " + JSONObject.toJSONString(token));
                } else if (newBr != null) {
                    if (!hasAdd && parse(newBr, word, hasScanned) != null) {
                        br.addAlternative(alt);
                    }
                }
            }
        }
        if (br.getAlternatives().size() == 0) {
            return null;
        }
        return br;
    }

    // 规则已扫描过
    private boolean hasScanned(List<TokenString> hasScanned, TokenString alt) {
        for (TokenString ts : hasScanned) {
            if (ts.equals(alt)) {
                return true;
            }
        }
        return false;
    }

    // 判断当前匹配是否为最小单元词
    private boolean isTerminal(String word) {
        return word.equals(IntegerTerminalToken.INTEGER) || word.equals(NumberTerminalToken.DOUBLE) || word.equals(ColumnValueTerminalToken.COLUMNVALUE);
    }

    // 判断是否为规则中的单元词
    private TerminalToken terminal(String word) {
        if (isTerminal(word)) {
            return null;
        }
        for (TerminalToken tt : parser.getTerminalTokens()) {
            if (tt.getName().equalsIgnoreCase(word)) {
                return tt;
            }
        }
        return null;
    }

}
