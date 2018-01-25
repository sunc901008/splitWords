package focus.search;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/1/25
 * description:
 */
public class TTT {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        int loop = list.size();

        while (loop > 0) {
            System.out.println(loop);
            String str = list.remove(0);
            if (str.equals("a")) {
                for (int i = 0; i < 5; i++) {
                    list.add("a" + i);
                }
            } else {
                list.add(str);
            }
            loop--;
        }
        System.out.println(JSON.toJSONString(list));

    }

}
