/*
 * This file is part of ElectionsPlus, licensed under the MIT License.
 *
 * Copyright (c) Lorenzo0111
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.lorenzo0111.elections.scheduler;

import org.jetbrains.annotations.NotNull;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class CronHandler {
    private static final List<Class<? extends Job>> JOBS = new ArrayList<>();
    private static final SchedulerFactory FACTORY;
    private static Scheduler scheduler;

    static {
        SchedulerFactory factory;

        Properties properties = new Properties();
        properties.setProperty("org.quartz.scheduler.instanceName", "ElectionsScheduler");
        properties.setProperty("org.quartz.threadPool.threadCount", "1");

        try {
            factory = new StdSchedulerFactory(properties);
        } catch (SchedulerException e) {
            e.printStackTrace();
            factory = new StdSchedulerFactory();
        }

        FACTORY = factory;
    }

    public static Scheduler getScheduler() throws SchedulerException {
        if (scheduler == null) {
            scheduler = FACTORY.getScheduler();
            scheduler.start();
        }

        return scheduler;
    }

    public static void unSchedule(@NotNull Class<? extends Job> job) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        scheduler.unscheduleJob(TriggerKey.triggerKey(job.getName()));
        JOBS.remove(job);
    }

    public static void schedule(Class<? extends Job> job, String expression, JobDataMap values) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        JobDetail jobDetail = JobBuilder.newJob(job)
                .withIdentity(job.getName(), "elections")
                .setJobData(values)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getName(),"elections")
                .withSchedule(CronScheduleBuilder.cronSchedule(expression))
                .build();

        scheduler.scheduleJob(jobDetail,trigger);
        scheduler.start();
        JOBS.add(job);
    }

    public static void schedule(Class<? extends Job> job, String expression) throws SchedulerException {
        schedule(job, expression, new JobDataMap());
    }

    public static void shutdown() throws SchedulerException {
        if (scheduler == null) return;

        getScheduler().shutdown();
    }

    public static List<Class<? extends Job>> getJobs() {
        return JOBS;
    }
}
