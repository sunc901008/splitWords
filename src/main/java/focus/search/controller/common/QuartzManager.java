package focus.search.controller.common;


import focus.search.base.Common;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.web.socket.WebSocketSession;

import java.text.ParseException;


/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class QuartzManager {
    private static final Logger logger = Logger.getLogger(QuartzManager.class);

    private static SchedulerFactory sf = new StdSchedulerFactory();

    /**
     * 添加一个定时任务，触发表示查询超时
     *
     * @param taskId taskId
     * @param time   时间设置，参考quartz说明文档
     */
    public static void addJob(String taskId, WebSocketSession session) {
        try {
            Scheduler sched = sf.getScheduler();
            // create job
            JobDetailImpl job = new JobDetailImpl();
            JobKey key = new JobKey(taskId);
            job.setJobClass(TimeoutTask.class);
            job.setKey(key);
            // add params
            JobDataMap map = new JobDataMap();
            map.put("session", session);
            job.setJobDataMap(map);
            // create trigger
            CronTriggerImpl trigger = new CronTriggerImpl();
            trigger.setCronExpression(Common.getCron());
            trigger.setName("TRIGGER_GROUP");
            // add job to scheduler with trigger
            sched.scheduleJob(job, trigger);
            sched.start();
        } catch (ParseException | SchedulerException e) {
            logger.error(Common.printStacktrace(e));
        }
    }

    /**
     * 获取一个任务
     *
     * @param taskId taskId
     * @return jobDetail
     */
    public static JobDetail getJob(String taskId) throws SchedulerException {
        Scheduler sched = sf.getScheduler();
        JobKey jobKey = new JobKey(taskId);
        return sched.getJobDetail(jobKey);
    }

    /**
     * 删除一个任务
     *
     * @param taskId taskId
     */
    public static void deleteJob(String taskId) throws SchedulerException {
        JobKey jobKey = new JobKey(taskId);
        deleteJob(jobKey);
    }

    /**
     * 删除一个任务
     *
     * @param jobKey jobKey
     */
    public static void deleteJob(JobKey jobKey) throws SchedulerException {
        Scheduler sched = sf.getScheduler();
        if (sched.getJobDetail(jobKey) != null) {
            sched.deleteJob(jobKey);
        }
    }

}

