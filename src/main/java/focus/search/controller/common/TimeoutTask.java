package focus.search.controller.common;

import focus.search.base.Common;
import focus.search.base.Constant;
import focus.search.response.search.ErrorResponse;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class TimeoutTask implements Job {
    private static final Logger logger = Logger.getLogger(TimeoutTask.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail jobDetail = context.getJobDetail();
        JobDataMap params = jobDetail.getJobDataMap();
        WebSocketSession session = (WebSocketSession) params.get("session");
        try {
            logger.warn("I WORKED!!!!!!!!!!!!!!!!!");
            Common.send(session, ErrorResponse.response(Constant.ErrorType.BI_TIMEOUT).toJSONString());
            // 删除任务
            QuartzManager.deleteJob(jobDetail.getKey());
        } catch (IOException | SchedulerException e) {
            logger.error(Common.printStacktrace(e));
        }
    }
}
