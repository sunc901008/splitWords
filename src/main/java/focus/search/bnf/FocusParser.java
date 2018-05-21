package focus.search.bnf;

import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusAnalyzer;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.tokens.*;
import focus.search.meta.AmbiguitiesRecord;
import focus.search.meta.AmbiguitiesResolve;
import focus.search.meta.Column;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusParserException;
import org.apache.log4j.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * creator: sunc
 * date: 2018/1/24
 * description:
 */
public class FocusParser implements Serializable {
    private static final Logger logger = Logger.getLogger(FocusParser.class);

    private BnfParser parser = null;
    public FocusAnalyzer focusAnalyzer = new FocusAnalyzer();
    private final static int MAX_RULE_LOOP = 20;

    public FocusParser() {
        this(Constant.Language.ENGLISH);
    }

    public FocusParser(String language) {
        init(language);
    }

    /**
     * for debug
     *
     * @param str   file path or language
     * @param debug debug is true, str is file else str is language
     */
    public FocusParser(String str, boolean debug) {
        if (!debug)
            init(str);
        else
            initBnf(str);
    }

    private void init(String language) {
        if (Constant.Language.ENGLISH.equals(language)) {
            initBnf("bnf-file/question.bnf");
            initBnf("bnf-file/function.bnf");
        } else {
            initBnf("bnf-file/chinese.bnf");
            initBnf("bnf-file/function.bnf");
        }
    }

    private void initBnf(String file) {
        ResourceLoader resolver = new DefaultResourceLoader();
        try {
            if (parser == null) {
                parser = new BnfParser(new FileInputStream(resolver.getResource(file).getFile()));
            } else {
                parser.setGrammar(new Scanner(new FileInputStream(resolver.getResource(file).getFile())));
            }
        } catch (Exception e) {
            logger.error(Common.printStacktrace(e));
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

    public FocusInst parseQuestion(List<FocusToken> tokens, JSONObject amb) throws AmbiguitiesException, FocusParserException {
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

    public FocusInst parseFormula(List<FocusToken> tokens, JSONObject amb) throws FocusParserException, AmbiguitiesException {
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

    private FocusSubInst subParse(List<FocusToken> tokens, JSONObject amb) throws AmbiguitiesException, FocusParserException {
        FocusToken focusToken = tokens.get(0);

        List<FocusPhrase> focusPhrases = focusPhrases(focusToken);
        if (focusPhrases == null || focusPhrases.isEmpty()) {
            return null;
        }
        logger.info("Adaptation parse bnf size:" + focusPhrases.size());

        // 歧义检测
        ambiguitiesCheck(focusToken, focusPhrases, 0, amb);

        for (int i = 1; i < tokens.size(); i++) {
            FocusToken ft = tokens.get(i);
            List<FocusPhrase> tmp = new ArrayList<>(focusPhrases);

            if (Constant.FNDType.COLUMNVALUE.equals(ft.getType())) {
                int loop = focusPhrases.size();
                while (loop > 0) {
                    FocusPhrase fp = focusPhrases.remove(0);
                    loop--;
                    if (fp.size() < i + 1) {
                        continue;
                    }
                    FocusNode tmpNode = fp.getNodeNew(i);
                    if (ColumnValueTerminalToken.COLUMNVALUE.equals(tmpNode.getValue())) {
                        tmpNode.setValue(ft.getWord());
                        tmpNode.setBegin(ft.getStart());
                        tmpNode.setEnd(ft.getEnd());
                        tmpNode.setType(Constant.FNDType.COLUMNVALUE);
                        tmpNode.setTerminal();
//                        fp.removeNode(i);
//                        fp.addPn(i, tmpNode);
                        fp.replaceNode(i, tmpNode);
                        focusPhrases.add(fp);
                    }
                }
                if (focusPhrases.isEmpty()) {// 出错结束
                    FocusSubInst fsi = new FocusSubInst();
                    fsi.setIndex(i);
                    fsi.setFps(tmp);
                    fsi.setError();
                    return fsi;
                }
                continue;
            }

            List<BnfRule> rules = parseRules(ft.getWord());
            if (rules.isEmpty()) {
                focusPhrases.clear();
                for (FocusPhrase fp : tmp) {
                    if (fp.size() > i && fp.getNodeNew(i).getValue().equalsIgnoreCase(ft.getWord())) {
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
                    fsi.setError();
                    return fsi;
                }
            } else {
                logger.info("replace focusPhrase loop: " + i);
                replace(rules, focusPhrases, ft, i);
                List<FocusPhrase> remove = new ArrayList<>();
                List<FocusPhrase> startWith = new ArrayList<>();
                List<FocusPhrase> copy = new ArrayList<>(focusPhrases);
                for (FocusPhrase fp : focusPhrases) {
                    if (fp.size() <= i) {
                        remove.add(fp);
                        continue;
                    }
                    if (!fp.getNodeNew(i).getValue().equalsIgnoreCase(ft.getWord())) {
                        remove.add(fp);
                    }
                    if (fp.getNodeNew(i).getValue().toLowerCase().startsWith(ft.getWord().toLowerCase())) {
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
                        fsi.setError();
                        fsi.setFps(remove);
                        return fsi;
                    } else {
                        if (i < tokens.size() - 1) {//不是最后一个token,说明中间出错
                            fsi.setError();
                        }
                        fsi.setFps(startWith);
                        return fsi;
                    }
                }
//                }
            }
            // 歧义检测
            ambiguitiesCheck(ft, focusPhrases, i, amb);

            // 去除重复
            distinct(focusPhrases);
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
    private void ambiguitiesCheck(FocusToken token, List<FocusPhrase> focusPhrases, int index, JSONObject amb) throws AmbiguitiesException {
        List<AmbiguitiesRecord> ars = new ArrayList<>();

        String value = focusPhrases.get(0).getNodeNew(index).getValue();
        AmbiguitiesResolve ambiguitiesResolve = AmbiguitiesResolve.getByValue(value, amb);
        boolean isResolved = false;
        AmbiguitiesRecord resolve = null;
        if (ambiguitiesResolve != null) {
            if (ambiguitiesResolve.isResolved) {
                isResolved = true;
                resolve = ambiguitiesResolve.ars.get(0);
            }
        }

        logger.debug("check ambiguities. index:" + index + ". realValue:" + token.getWord() + ". Ambiguities:" + amb + ". resolve:" + JSONObject.toJSONString
                (resolve));
        List<Integer> added = new ArrayList<>();
        List<FocusPhrase> remove = new ArrayList<>();
        for (FocusPhrase fp : focusPhrases) {
            FocusNode fn = fp.getNodeNew(index);
            if (!token.getWord().equals(fn.getValue())) {
                continue;
            }
            if (isResolved) {
                if (!fn.getType().equals(resolve.type) || (Constant.AmbiguityType.COLUMN.equals(fn.getType()) && fn.getColumn().getColumnId() != resolve.columnId)) {
                    remove.add(fp);
                }
            } else if (Constant.FNDType.COLUMN.equals(fn.getType())) {
                Column column = fn.getColumn();
                AmbiguitiesRecord ar = new AmbiguitiesRecord();
                if (!added.contains(column.getColumnId())) {
                    added.add(column.getColumnId());
                    ar.type = Constant.AmbiguityType.COLUMN;
                    ar.sourceName = column.getSourceName();
                    ar.columnId = column.getColumnId();
                    ar.columnName = column.getColumnDisplayName();
                    ar.realValue = ar.columnName;
                    ar.possibleValue = ar.columnName;
                    ars.add(ar);
                }
            }
        }
        focusPhrases.removeAll(remove);
        if (!isResolved && ars.size() > 1) {
            throw new AmbiguitiesException(ars, token.getStart(), token.getEnd(), index);
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

    private void distinct(List<FocusPhrase> focusPhrases) {
        List<FocusPhrase> copy = new ArrayList<>(focusPhrases);
        for (FocusPhrase fp0 : copy) {
            List<Integer> remove = new ArrayList<>();
            for (int j = 0; j < focusPhrases.size(); j++) {
                FocusPhrase fp1 = focusPhrases.get(j);
                if (fp0.equals(fp1)) {
                    remove.add(j);
                }
            }
            if (remove.size() > 1) {
                for (int j = remove.size() - 1; j >= 0; j--) {
                    int index = remove.get(j);
                    focusPhrases.remove(index);
                }
                focusPhrases.add(fp0);
            }
        }

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
                    FocusNode fn = focusPhrase.getNodeNew(position);
                    TerminalToken tt = terminal(fn.getValue());
                    if (tt != null) {
                        if (fn.getValue().equalsIgnoreCase(focusToken.getWord())) {
                            fn.setTerminal();
                            fn.setType(tt.getType());
//                            fn.setColumn(tt.getColumn());
                            fn.setBegin(focusToken.getStart());
                            fn.setEnd(focusToken.getEnd());
                            focusPhrase.replaceNode(position, fn);
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
                        } catch (FocusParserException e) {
                            loop--;
                            continue;
                        }
                        String brName = br.getLeftHandSide().getName();
                        if ("<number-function>".equals(brName)) {
                            replaceBnf(br);
                        }
                        for (TokenString ts : br.getAlternatives()) {
                            FocusPhrase newFp = new FocusPhrase(brName);
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
                                    newFn.setTerminal();
                                }
                                if (i == 0) {
                                    newFn.setBegin(focusToken.getStart());
                                    newFn.setEnd(focusToken.getEnd());
                                }
                                newFp.addPn(newFn);
                            }
                            FocusPhrase focusPhraseNew = JSONObject.parseObject(focusPhrase.toJSON().toJSONString(), FocusPhrase.class);
                            FocusNode focusNodeNew = new FocusNode(brName);
                            focusNodeNew.setChildren(newFp);
                            focusPhraseNew.replaceNode(position, focusNodeNew);
                            if (focusPhraseNew.size() == position + 1 && focusPhraseNew.getNodeNew(position).getValue().equalsIgnoreCase(focusToken
                                    .getWord())) {
                                focusPhraseNew.setType(Constant.INSTRUCTION);
                            }
                            focusPhrases.add(focusPhraseNew);
                        }
                    }
                }
                loop--;
            }
            if (focusPhrases.isEmpty()) {
                focusPhrases.addAll(copy);
            }

            //  比较替换之前和替换之后的phrase，如果无变化则表示该次替换完成
//            if (same(copy, focusPhrases)) {
//                break;
//            }
        }
    }

    private List<FocusPhrase> focusPhrases(FocusToken focusToken) throws FocusParserException {
        String word = focusToken.getWord();
        List<BnfRule> rules = parseRules(parser.getM_rules(), word);

        if (rules.size() <= 1) {
            return null;
        }
        BnfRule rule = rules.remove(0);
        List<FocusPhrase> focusPhrases = new ArrayList<>();
        List<BnfRule> removes = new ArrayList<>();

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
                        fn.setTerminal();
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

        replace(rules, focusPhrases, focusToken, 0);

        return focusPhrases;
    }

    private BnfRule findRule(List<BnfRule> rules, Token token) throws FocusParserException {
        return findRule(rules, token.getName());
    }

    private BnfRule findRule(List<BnfRule> rules, String token) throws FocusParserException {
        for (BnfRule rule : rules) {
            if (rule.getLeftHandSide().getName().equals(token)) {
                return rule;
            }
        }
        throw new FocusParserException("Cannot find rule for token " + JSONObject.toJSONString(token));
    }

    public List<BnfRule> parseRules(String word) throws FocusParserException {
        return parseRules(parser.getM_rules(), word);
    }

    private List<BnfRule> parseRules(List<BnfRule> rules, String word) throws FocusParserException {
        List<BnfRule> res = new ArrayList<>();
        for (BnfRule br : rules) {
            BnfRule rule = parse(br, word);
            if (rule != null) {
                res.add(rule);
            }
        }

        logger.info("adaptation " + word + " rules size:" + rules.size());
        return res;
    }

    private BnfRule parse(BnfRule rule, String word) throws FocusParserException {
        BnfRule br = new BnfRule();
        br.setLeftHandSide(rule.getLeftHandSide());
        for (TokenString alt : rule.getAlternatives()) {
            Token token = alt.getFirst();
//            if (token instanceof ColumnValueTerminalToken) {
//                //debug
//                System.out.println(token.toString());
//            }
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
//                List<String> filter = Arrays.asList("<function-columns>", "<realValue>");
                if (newBr == null && !token.getName().endsWith("-column>")) {
                    throw new FocusParserException("Cannot find rule for token " + JSONObject.toJSONString(token));
                } else if (newBr != null) {
                    if (newBr.equals(rule) || parse(newBr, word) != null) {
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

    // 迭代替换bnf规则
//<number-function-column> := <number-function-column> + <integer> |
//                            <integer> + <integer>;
//							替换为
//<number-function-column> := <number-function-column> + <integer> + <integer> |
//							<integer> + <integer> + <integer> |
//                            <integer> + <integer>;
    private void replaceBnf(BnfRule br) {
        List<TokenString> list = new ArrayList<>();
        BnfRule rule = new BnfRule();
        rule.addAlternatives(br.getAlternatives());

        for (TokenString ts : rule.getAlternatives()) {
            boolean in = false;
            for (int i = 0; i < ts.size(); i++) {
                Token token = ts.get(i);
                if (token.getName().equals(br.getLeftHandSide().getName())) {
                    in = true;
                    for (TokenString t : br.getAlternatives()) {
                        TokenString tokenString = new TokenString();
                        tokenString.addAll(ts.subList(0, i));
                        tokenString.addAll(t);
                        if (i + 1 < ts.size()) {
                            tokenString.addAll(ts.subList(i + 1, ts.size()));
                        }
                        list.add(tokenString);
                    }
                    break;
                }
            }
            if (!in)
                list.add(ts);
        }
        br.resetAlternatives(list);
    }

    // 拷贝对象
    public FocusParser deepClone() {
        FocusParser outer = null;
        try {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bao);
            oos.writeObject(this);
            ByteArrayInputStream bai = new ByteArrayInputStream(bao.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bai);
            outer = (FocusParser) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return outer;
    }

}
