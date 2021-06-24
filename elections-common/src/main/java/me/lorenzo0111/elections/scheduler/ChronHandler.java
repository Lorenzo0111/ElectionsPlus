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

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.MutableTrigger;

import java.util.ArrayList;
import java.util.List;


public class ChronHandler {
    private static final List<Class<? extends Job>> JOBS = new ArrayList<>();
    private static final SchedulerFactory FACTORY = new StdSchedulerFactory();
    private static Scheduler scheduler;

    public static Scheduler getScheduler() throws SchedulerException {
        if (scheduler != null) return scheduler;
        Scheduler scheduler = FACTORY.getScheduler();
        scheduler.start();
        return scheduler;
    }

    public static void unSchedule(Class<? extends Job> job) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        scheduler.unscheduleJob(TriggerKey.triggerKey(job.getName()));
        JOBS.remove(job);
    }

    public static void schedule(Class<? extends Job> job, JobDataMap values) throws SchedulerException {
        Scheduler scheduler = getScheduler();
        JobDetail jobDetail = JobBuilder.newJob(job)
                .withIdentity(job.getName())
                .setJobData(values)
                .build();

        MutableTrigger trigger = CronScheduleBuilder.cronSchedule("").build();
        scheduler.scheduleJob(jobDetail,trigger);
        scheduler.start();
        JOBS.add(job);
    }

    public static void schedule(Class<? extends Job> job) throws SchedulerException {
        schedule(job,new JobDataMap());
    }

    public static void shutdown() throws SchedulerException {
        getScheduler().shutdown();
    }

    public static List<Class<? extends Job>> getJobs() {
        return JOBS;
    }
}
