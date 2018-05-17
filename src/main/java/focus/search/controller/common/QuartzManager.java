package focus.search.controller.common;


import focus.search.base.Common;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.web.socket.WebSocketSession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * creator: sunc
 * date: 2018/5/8
 * description:
 */
public class QuartzManager {
    private static final Logger logger = Logger.getLogger(QuartzManager.class);

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    private static SchedulerFactory sf = new StdSchedulerFactory();

    /**
     * 添加一个定时任务，触发表示查询超时
     *
     * @param taskId  taskId
     * @param session websocket session
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
            trigger.setCronExpression(Common.getCron());//时间设置，参考quartz说明文档
            trigger.setName(taskId);
            // add job to scheduler with trigger
            Date ft = sched.scheduleJob(job, trigger);
            sched.start();
            logger.info(key.getName() + " start at : " + sdf.format(ft) + ", time scheduler cron: " + trigger.getCronExpression());
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
            logger.info(jobKey.getName() + " delete at : " + sdf.format(Calendar.getInstance().getTime()));
        }
    }

}

