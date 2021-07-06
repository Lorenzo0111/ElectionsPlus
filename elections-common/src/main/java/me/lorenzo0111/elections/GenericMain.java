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

import me.lorenzo0111.elections.config.ConfigUpdater;
import me.lorenzo0111.elections.jobs.ElectionsTask;
import me.lorenzo0111.elections.scheduler.ChronHandler;
import me.lorenzo0111.pluginslib.config.ConfigExtractor;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GenericMain {

    public static void init(Path folder) throws ConfigurateException {
        Map<Object[],Object> values = new HashMap<>();

        values.put(new Object[]{"chron","enabled"}, false);
        values.put(new Object[]{"chron","syntax"},"0 0 1 * *");
        values.put(new Object[]{"chron","name"},"%y-%m-%d");

        ConfigUpdater updater = new ConfigUpdater(values);
        ConfigExtractor manager = new ConfigExtractor(GenericMain.class,folder.toFile(),"config.yml");
        manager.extract(); // This is fake because the file has already been extracted.
        ConfigurationNode config = updater.update(manager.getFile(), manager.toConfigurate());

        JobDataMap map = new JobDataMap();
        map.put("name", config.node("chron","syntax").getString());

        if (config.node("chron","enabled").getBoolean()) {
            try {
                ChronHandler.schedule(ElectionsTask.class,map);
            } catch (SchedulerException e) {
                e.printStackTrace();
            }
        }

    }
}
