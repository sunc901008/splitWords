package focus.search.service;

import focus.search.dao.ICoreDAO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

public abstract class CoreService<E extends ICoreDAO<T, PK>, T, PK extends Serializable> {

    private E dao;

    public Boolean insert(T entity) {
        return dao.insert(entity) > 0;
    }

    public T selectByIdService(PK id) {
        return dao.selectById(id);
    }

    public List<T> selectByParamService(T param) {
        return dao.selectByParam(param);
    }

    public List<T> selectByIdList(List<PK> idList) {
        return dao.selectByIdList(idList);
    }

    public Boolean updateService(T entity) {
        return dao.update(entity) == 1;
    }

    public Boolean deleteService(List<PK> idList) {
        return dao.deletes(idList) == idList.size();
    }

    public Boolean deleteService(PK id) {
        return dao.delete(id) == 1;
    }

    protected E getDao() {
        return dao;
    }

    @Autowired
    public void setDao(E dao) {
        this.dao = dao;
    }
}
