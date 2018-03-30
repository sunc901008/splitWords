package focus.search.analyzer.focus;

import focus.search.base.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/3/30
 * description:
 */
public class MergeToken {

    public static List<FocusToken> mergeUserInput(List<FocusToken> tokens, String search) {
        List<Integer> positions = quotePosition(tokens);
        int count = positions.size();
        if (count == 0) {// 不包含列中值
            return tokens;
        }
        boolean cp = count % 2 == 0;// 引号是否成对出现

        List<FocusToken> merge = new ArrayList<>();
        int end = 0;
        while (cp ? count > 0 : count > 1) {
            int start = positions.remove(0);
            if (merge.isEmpty()) {
                merge.addAll(tokens.subList(0, start + 1));
            } else {
                merge.addAll(tokens.subList(end, start + 1));
            }
            end = positions.remove(0);
            count = count - 2;
            merge.add(getUserInput(tokens.get(start).getEnd(), tokens.get(end).getStart(), search));
        }
        merge.addAll(tokens.subList(end, tokens.size()));

        return merge;
    }


    /**
     * @param tokens 分词列表
     * @return 引号的个数
     */
    private static List<Integer> quotePosition(List<FocusToken> tokens) {
        List<Integer> position = new ArrayList<>();
        for (FocusToken token : tokens) {
            if ("TYPE_QUOTE".equals(token.getType())) {
                position.add(tokens.indexOf(token));
            }
        }
        return position;
    }

    private static FocusToken getUserInput(int start, int end, String search) {
        if (start == end) {
            return new FocusToken("", Constant.FNDType.COLUMNVALUE, end, end);
        }
        return new FocusToken(search.substring(start, end), Constant.FNDType.COLUMNVALUE, start, end);
    }

}
