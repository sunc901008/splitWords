package focus.search.dao;

import java.io.Serializable;
import java.util.List;

public interface ICoreDAO<T, PK extends Serializable> {

    Integer insert(T entity);

    T selectById(PK id);

    List<T> selectByParam(T param);

    List<T> selectByIdList(List<PK> idList);

    Integer update(T entity);

    Integer deletes(List<PK> idList);

    Integer delete(PK id);

}
