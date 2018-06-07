package focus.search.service;

import focus.search.dao.HistoryDAO;
import focus.search.entity.History;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * creator: sunc
 * date: 2018/6/5
 * description:
 */
@Service
public class HistoryServiceImpl extends CoreService<HistoryDAO, History, Integer> {

    public List<History> selectByUserId(Integer userId, String language) {
        return getDao().selectByUserId(userId, language);
    }

    public History exist(Integer userId, String language, String question) {
        return getDao().exist(userId, language, question);
    }

}
