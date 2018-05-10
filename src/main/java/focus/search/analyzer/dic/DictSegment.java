package focus.search.analyzer.dic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 词典树分段，表示词典树的一个分枝
 */
class DictSegment implements Comparable<DictSegment> {

    // 公用字典表，存储汉字
    private static final Map<Character, Character> charMap = new HashMap<>(16, 0.95f);

    // Map存储结构
    private Map<Character, DictSegment> childrenMap;

    // 当前节点上存储的字符
    private Character nodeChar;
    // 当前节点存储的Segment数目
    // storeSize <=ARRAY_LENGTH_LIMIT ，使用数组存储， storeSize >ARRAY_LENGTH_LIMIT ,则使用Map存储
    private int storeSize = 0;
    // 当前DictSegment状态 ,默认 0 , 1表示从根节点到当前节点的路径表示一个词
    private int nodeState = 0;

    // 默认类型
    private String nodeType = "UNKNOWN";

    DictSegment(Character nodeChar) {
        if (nodeChar == null) {
            throw new IllegalArgumentException("参数为空异常，字符不能为空");
        }
        this.nodeChar = nodeChar;
    }

    String getNodeType() {
        return this.nodeType;
    }

    Character getNodeChar() {
        return nodeChar;
    }

    /*
     * 判断是否有下一个节点
     */
    boolean hasNextNode() {
        return this.storeSize > 0;
    }

    /**
     * 匹配词段
     *
     * @return Hit
     */
    Hit match(char[] charArray) {
        return this.match(charArray, 0, charArray.length, null);
    }

    /**
     * 匹配词段
     *
     * @return Hit
     */
    Hit match(char[] charArray, int begin, int length) {
        return this.match(charArray, begin, length, null);
    }

    /**
     * 匹配词段
     *
     * @return Hit
     */
    Hit match(char[] charArray, int begin, int length, Hit searchHit) {

        if (searchHit == null) {
            // 如果hit为空，新建
            searchHit = new Hit();
            // 设置hit的其实文本位置
            searchHit.setBegin(begin);
        } else {
            // 否则要将HIT状态重置
            searchHit.setUnmatch();
        }
        // 设置hit的当前处理位置
        searchHit.setEnd(begin);

        Character keyChar = charArray[begin];
        DictSegment ds = null;

        // 引用实例变量为本地变量，避免查询时遇到更新的同步问题
        Map<Character, DictSegment> segmentMap = this.childrenMap;

        // STEP1 在节点中查找keyChar对应的DictSegment
        if (segmentMap != null) {
            // 在map中查找
            ds = segmentMap.get(keyChar);
        }

        // STEP2 找到DictSegment，判断词的匹配状态，是否继续递归，还是返回结果
        if (ds != null) {
            if (length > 1) {
                // 词未匹配完，继续往下搜索
                return ds.match(charArray, begin + 1, length - 1, searchHit);
            } else if (length == 1) {

                // 搜索最后一个char
                if (ds.nodeState == 1) {
                    // 添加HIT状态为完全匹配
                    searchHit.setMatch();
                    searchHit.setMatchedDictSegment(ds);
                }
                if (ds.hasNextNode()) {
                    // 添加HIT状态为前缀匹配
                    searchHit.setPrefix();
                    // 记录当前位置的DictSegment
                    searchHit.setMatchedDictSegment(ds);
                }
                return searchHit;
            }

        }
        // STEP3 没有找到DictSegment， 将HIT设置为不匹配
        return searchHit;
    }

    void fillSegment(char[] charArray, String type) {
        this.fillSegment(charArray, 0, charArray.length, 1, type);
    }

    // 屏蔽已经删除的word
    void removeSegment(char[] charArray) {
        this.fillSegment(charArray, 0, charArray.length, 0, "");
    }

    private synchronized void fillSegment(char[] charArray, int begin, int length, int enabled, String type) {
        // 获取字典表中的汉字对象
        Character beginChar = charArray[begin];
        Character keyChar = charMap.get(beginChar);
        // 字典中没有该字，则将其添加入字典
        if (keyChar == null) {
            charMap.put(beginChar, beginChar);
            keyChar = beginChar;
        }

        // 搜索当前节点的存储，查询对应keyChar的keyChar，如果没有则创建
        DictSegment ds = lookForSegment(keyChar, enabled);
        if (ds != null) {
            // 处理keyChar对应的segment
            if (length > 1) {
                // 词元还没有完全加入词典树
                ds.fillSegment(charArray, begin + 1, length - 1, enabled, type);
            } else if (length == 1) {
                // 已经是词元的最后一个char,设置当前节点状态为enabled，
                // enabled=1表明一个完整的词，enabled=0表示从词典中屏蔽当前词
                ds.nodeState = enabled;
                ds.nodeType = type;
            }
        }

    }

    /**
     * 查找本节点下对应的keyChar的segment	 *
     *
     * @param create =1如果没有找到，则创建新的segment ; =0如果没有找到，不创建，返回null
     */
    private DictSegment lookForSegment(Character keyChar, int create) {

        // 获取Map容器，如果Map未创建,则创建Map
        Map<Character, DictSegment> segmentMap = getChildrenMap();
        // 搜索Map
        DictSegment ds = segmentMap.get(keyChar);
        if (ds == null && create == 1) {
            // 构造新的segment
            ds = new DictSegment(keyChar);
            segmentMap.put(keyChar, ds);
            // 当前节点存储segment数目+1
            this.storeSize++;
        }

        return ds;
    }

    /**
     * 获取Map容器
     * 线程同步方法
     */
    private Map<Character, DictSegment> getChildrenMap() {
        if (this.childrenMap == null) {
            synchronized (this) {
                if (this.childrenMap == null) {
                    this.childrenMap = new HashMap<>();
                }
            }
        }
        return this.childrenMap;
    }

    /**
     * 实现Comparable接口
     *
     * @return int
     */
    public int compareTo(DictSegment o) {
        // 对当前节点存储的char进行比较
        return this.nodeChar.compareTo(o.nodeChar);
    }

    private Set<Map<Integer, String>> tmp(DictSegment dict) {
        Set<Map<Integer, String>> ss = new HashSet<>();
        if (dict.nodeState == 0) {
            for (Character key : dict.childrenMap.keySet()) {
                DictSegment child = dict.childrenMap.get(key);
                for (Map<Integer, String> map : tmp(child)) {
                    map.put(0, dict.nodeChar + map.get(0));
                    ss.add(map);
                }
            }
        } else {
            Map<Integer, String> res = new HashMap<>();
            res.put(0, String.valueOf(dict.nodeChar));
            res.put(1, dict.nodeType);
            ss.add(res);
        }
        return ss;
    }

    void reset() {
        getChildrenMap().clear();
        charMap.clear();
    }

}
