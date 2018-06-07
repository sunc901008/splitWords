package focus.search.dao;

import focus.search.entity.History;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/5
 * description:
 */
public interface HistoryDAO extends ICoreDAO<History, Integer> {

    List<History> selectByUserId(@Param("userId") Integer userId, @Param("language") String language);

    History exist(@Param("userId") Integer userId, @Param("language") String language, @Param("question") String question);

}
