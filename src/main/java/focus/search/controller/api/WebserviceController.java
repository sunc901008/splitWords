package focus.search.controller.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import focus.search.analyzer.focus.FocusToken;
import focus.search.base.Clients;
import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.bnf.*;
import focus.search.bnf.exception.InvalidRuleException;
import focus.search.controller.WebsocketPinboard;
import focus.search.controller.WebsocketSearch;
import focus.search.controller.common.Base;
import focus.search.controller.common.QuartzManager;
import focus.search.meta.Column;
import focus.search.metaReceived.SourceReceived;
import focus.search.response.api.AnswerCheckResponse;
import focus.search.response.exception.AmbiguitiesException;
import focus.search.response.exception.FocusParserException;
import focus.search.response.search.ChartsResponse;
import focus.search.response.search.ErrorResponse;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

@Controller
@RequestMapping("/api/search")
public class WebserviceController {
    private static final Logger logger = Logger.getLogger(WebserviceController.class);

    @ResponseBody
    @RequestMapping(value = "/queryResult", method = RequestMethod.POST)
    public JSONObject queryResult(@RequestBody String data) throws IOException, SchedulerException {
        logger.info(Common.cut(data));
        JSONObject json = JSONObject.parseObject(data);
        String taskId = json.getString("taskId");
        String source = json.getString("source");
        JobDetail jobDetail = QuartzManager.getJob(taskId);
        ChartsResponse chartsResponse = new ChartsResponse(json.getString("question"), json.getString("sourceToken"));
        chartsResponse.setDatas(json.getJSONObject("result"));
        if (jobDetail != null) {
            JobDataMap params = jobDetail.getJobDataMap();
            WebSocketSession session = (WebSocketSession) params.get("session");
            session.sendMessage(new TextMessage(chartsResponse.response()));
            QuartzManager.deleteJob(taskId);
        } else {
            if (Constant.SearchOrPinboard.SEARCH_USER.equals(source)) {
                WebsocketSearch.queryResult(chartsResponse, taskId);
            } else {
                WebsocketPinboard.queryResult(chartsResponse, taskId);
            }
        }
        JSONObject response = new JSONObject();
        response.put("success", true);
        return response;
    }

    @ResponseBody
    @RequestMapping(value = "/getInsts", method = RequestMethod.GET)
    public JSONObject getInsts(ServletRequest request) throws IOException {
        long startTime = Calendar.getInstance().getTimeInMillis();
        String sourceToken = request.getParameter("sourceToken");
        String question = request.getParameter("question");
        return WebsocketSearch.getInsts(sourceToken, question, startTime);
    }

    @ResponseBody
    @RequestMapping(value = "/modelNameCheck", method = RequestMethod.GET)
    public JSONObject modelNameCheck(ServletRequest request) throws IOException {
        String name = request.getParameter("name");
        String language = request.getParameter("language");
        String type = request.getParameter("type");
        return WebsocketSearch.modelNameCheck(name, language, type);
    }

    @ResponseBody
    @RequestMapping(value = "/answerCheck", method = RequestMethod.POST)
    public JSONObject answerCheck(@RequestBody String data) throws IOException, AmbiguitiesException, InvalidRuleException, FocusParserException {
        JSONObject params = JSONObject.parseObject(data);
        JSONObject model = params.getJSONObject("model");
        JSONArray answers = params.getJSONArray("answers");
        JSONObject table = model.getJSONObject("table");
        JSONArray columns = model.getJSONArray("columns");
        int tblId = table.getInteger("id");
        String tblName = table.getString("tableDisplayName");
        AnswerCheckResponse response = new AnswerCheckResponse();
        for (int i = 0; i < answers.size(); i++) {
            JSONObject answer = answers.getJSONObject(i);
            JSONObject getSource;
            try {
                getSource = Clients.WebServer.getSource(answer.getString("sourceToken"));
            } catch (Exception e) {
                logger.error(ErrorResponse.response(Constant.ErrorType.ERROR, e.getMessage()));
                continue;
            }
            List<SourceReceived> srs;
            if ("success".equals(getSource.getString("status"))) {
                srs = JSONArray.parseArray(getSource.getJSONArray("sources").toJSONString(), SourceReceived.class);
            } else {
                continue;
            }
            String context = answer.getString("context");
            String language = Constant.Language.ENGLISH;
            JSONObject ambiguities = new JSONObject();
            if (context != null) {
                JSONObject contextJson = JSONObject.parseObject(context);
                ambiguities = Base.context(contextJson, srs);
                language = contextJson.getString("language");
            }
            FocusParser fp = Constant.Language.ENGLISH.equals(language) ? Base.englishParser.deepClone() : Base.chineseParser.deepClone();
            ModelBuild.buildTable(fp, srs);
            List<FocusToken> tokens = fp.focusAnalyzer.test(answer.getString("question"), language);
            FocusInst fi = fp.parseQuestion(tokens, ambiguities);
            if (fi.position < 0) {
                int n = tokens.size();
                for (FocusPhrase f : fi.getFocusPhrases()) {
                    for (int index = 0; index < f.size(); index++) {
                        if (n <= 0) {
                            break;
                        }
                        FocusNode node = f.getNodeNew(index);
                        if (Constant.FNDType.TABLE.equals(node.getType())) {
                            FocusNode tmp = f.getNodeNew(index + 1);
                            if (tmp.getColumn().getTableId() == tblId && !tmp.getColumn().getSourceName().equalsIgnoreCase(tblName)) {
                                response.affectAnswers.add(answer.getInteger("id"));
                            }
                        } else if (Constant.FNDType.COLUMN.equals(node.getType())) {
                            Column col = node.getColumn();
                            if (Base.affect(columns, col)) {
                                response.affectAnswers.add(answer.getInteger("id"));
                            }
                        }
                        n--;
                    }
                    if (n <= 0) {
                        break;
                    }
                }
            }
        }
        return response.toJSON();

    }

}

