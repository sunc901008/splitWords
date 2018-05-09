package focus.search.controller.common;

import focus.search.response.search.ExceptionResponse;
import org.quartz.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class TimeoutTask implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap params = jobDetail.getJobDataMap();
        WebSocketSession session = (WebSocketSession) params.get("session");
        try {
            // TODO: 2018/5/8 超时异常返回
            session.sendMessage(new TextMessage(ExceptionResponse.response("timeout").toJSONString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            // 删除任务
            QuartzManager.deleteJob(jobDetail.getKey());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
