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

package me.lorenzo0111.elections;

import me.lorenzo0111.elections.jobs.ElectionsTask;
import me.lorenzo0111.elections.scheduler.CronHandler;
import me.lorenzo0111.pluginslib.config.ConfigExtractor;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Path;

public class GlobalMain {

    public static void init(@NotNull Path folder) throws ConfigurateException {
        ConfigExtractor manager = new ConfigExtractor(GlobalMain.class,folder.toFile(),"config.yml");
        manager.extract(); // This is fake because the file has already been extracted.
        ConfigurationNode config = manager.toConfigurate();

        if (config == null) {
            return;
        }

        JobDataMap map = new JobDataMap();
        map.put("name", config.node("chron","name").getString());

        if (config.node("chron","enabled").getBoolean()) {
            try {
                CronHandler.schedule(ElectionsTask.class, config.node("chron","syntax").getString(), map);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }

    }

    public static void shutdown() {
        try {
            CronHandler.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
