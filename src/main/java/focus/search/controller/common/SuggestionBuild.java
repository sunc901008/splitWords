package focus.search.controller.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.*;
import focus.search.bnf.tokens.TerminalToken;
import focus.search.bnf.tokens.Token;
import focus.search.bnf.tokens.TokenString;
import focus.search.meta.Column;
import focus.search.metaReceived.ColumnReceived;
import focus.search.metaReceived.SourceReceived;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * creator: sunc
 * date: 2018/3/23
 * description:
 */
public class SuggestionBuild {

    // suggestions| 出错
    public static List<FocusNode> sug(int position, FocusInst focusInst) {
        List<FocusNode> focusNodes = new ArrayList<>();
        List<String> suggestions = new ArrayList<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                FocusNode fn = fp.getNode(position);
                if (!suggestions.contains(fn.getValue())) {
                    suggestions.add(fn.getValue());
                    focusNodes.add(fn);
                }
            } else {
                position = position - fp.size();
            }
        }
        return focusNodes;
    }

    // suggestions| 输入不完整
    // todo 修改提示 按规则名提示规则内容
    public static JSONObject sug(List<FocusToken> tokens, FocusInst focusInst) {
        JSONObject json = new JSONObject();
        int index = tokens.size() - 1;
        int position = tokens.get(index).getStart();
        List<FocusNode> focusNodes = new ArrayList<>();
        Set<String> suggestions = new HashSet<>();
        for (FocusPhrase fp : focusInst.getFocusPhrases()) {
            if (fp.isSuggestion()) {
                FocusNode fn = fp.getNode(index);
                if (fn.getValue().equalsIgnoreCase(tokens.get(index).getWord())) {
                    FocusNode focusNode = fp.getNode(index + 1);
                    if (!suggestions.contains(focusNode.getValue())) {
                        suggestions.add(focusNode.getValue());
                        focusNodes.add(focusNode);
                        position = fn.getEnd() + 1;
                    }
                } else {
                    if (!suggestions.contains(fn.getValue())) {
                        suggestions.add(fn.getValue());
                        focusNodes.add(fn);
                    }
                }
            } else {
                index = index - fp.size();
            }
        }
        json.put("position", position);
        json.put("suggestions", focusNodes);
        return json;
    }

    // 根据bnf规则名字获取最底层的单元token
    public static List<TerminalToken> terminalTokens(FocusParser parser, String ruleName) {
        List<TerminalToken> tokens = new ArrayList<>();
        BnfRule br = parser.getRule(ruleName);
        if (br != null)
            for (TokenString ts : br.getAlternatives()) {
                for (Token token : ts) {
                    if (token instanceof TerminalToken) {
                        if (!((TerminalToken) token).getType().equals(Constant.FNDType.TABLE) && !isExist(tokens, (TerminalToken) token)) {
                            tokens.add((TerminalToken) token);
                        }
                    } else {
                        for (TerminalToken t : terminalTokens(parser, token.getName())) {
                            if (!isExist(tokens, t)) {
                                tokens.add(t);
                            }
                        }
                    }
                }
            }
        return tokens;
    }

    private static boolean isExist(List<TerminalToken> tokens, TerminalToken token) {
        for (TerminalToken t : tokens) {
            if (t.getType().equals(token.getType()) && t.getName().equals(token.getName())) {
                if (token.getType().equals(Constant.FNDType.COLUMN)) {
                    if (token.getColumn().getColumnId() == t.getColumn().getColumnId()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    // 随机获取几个列信息
    public static List<Column> colRandomSuggestions(JSONObject user) {
        List<SourceReceived> srs = JSONArray.parseArray(user.getJSONArray("sources").toJSONString(), SourceReceived.class);
        List<Column> columns = new ArrayList<>();
        int count = 10;
        for (SourceReceived source : srs) {
            if (count <= 0) {
                break;
            }
            for (ColumnReceived col : source.columns) {
                Column column = col.transfer();
                column.setSourceName(source.sourceName);
                column.setTableId(source.tableId);
                columns.add(column);
                count--;
            }
        }
        return columns;
    }

    /**
     * 获取数字类型的提示
     *
     * @param isInt 是否为整数
     * @return 数字提示
     */
    public static String decimalSug(boolean isInt) {
        Double d = Math.random() * 100 + 1;
        return isInt ? String.valueOf(d.intValue()) : Common.decimalFormat(d);
    }

    public static String decimalSug() {
        return decimalSug(true);
    }

}
