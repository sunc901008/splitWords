package focus.search.base;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.Collections;
import java.util.List;

/**
 * creator: sunc
 * date: 2018/2/2
 * description:
 */
public class Clients {

    private static final String MODEL_STATUS_URL = "modelstatus";
    private static final String MODEL_BUILD_URL = "buildmodel";
    private static final String UPDATE_MODEL_URL = "updatemodel";
    private static final String UPDATE_WORKSHEET_MODEL = "update_worksheet_model";
    private static final String QUERY_URL = "query";
    private static final String DATA_SOURCE_URL = "datasource";
    private static final String ALL_TABLE_URL = "alltable";
    private static final String SOURCE_TABLE_URL = "sourcetable";
    private static final String SOURCE_RELATION_URL = "sourcerelation";
    private static final String INSTRUCTION_DEP_COLS = "instruction_dep_cols";
    private static final String INSTRUCTION_FORMULAS = "instruction_formulas";
    private static final String WORKSHEET_URL = "worksheet";
    private static final String TABLE_DATA_URL = "tabledata";
    private static final String MODEL_BUILD_LIST = "buildlist";
    private static final String CHECK_JOIN_SQL_URL = "checkjoinsql";
    private static final String WORKSHEET_RELATION_URL = "worksheetrelation";
    private static final String INDEX_BUILD_URL = "indexbuild";
    private static final String MODEL_TEMPLATE_URL = "model/template";
    private static final String FORMULA_PARSE = "formulaparse";
    private static final String CHECK_RELATION = "checkRelation";


    private static JSONObject get(String url, String entity, List<Header> headers) throws Exception {
        JSONObject res = MyHttpClient.get(url, entity, headers);
        if (res.isEmpty()) {
            // todo exception controller
            throw new Exception();
        }
        return res;
    }

    public static class Bi {
        private static String baseUrl = String.format("http://%s:%d%s/", Constant.biHost, Constant.biPort, Constant.biBaseUrl);

        public JSONObject getDataSource() throws Exception {
            return get(baseUrl + DATA_SOURCE_URL, null, null);
        }

    }

    public static class WebServer {

        private static String baseUrl = String.format("http://%s:%d%s/", Constant.webServerHost, Constant.webServerPort, Constant.webServerBaseUrl);
        private static final String GET_SOURCE = "getSource";

        public static JSONObject getSource(String sourceToken) throws Exception {
            BasicHeader header = new BasicHeader("sourceToken", sourceToken);
            return get(baseUrl + GET_SOURCE, null, Collections.singletonList(header));
        }
    }

}
