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

package me.lorenzo0111.elections.handlers;

import com.google.inject.Inject;
import me.lorenzo0111.elections.ElectionsPlus;
import me.lorenzo0111.pluginslib.config.ConfigExtractor;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.nio.file.Path;
import java.util.Objects;

public final class Configs {
    @Inject @ConfigDir(sharedRoot = false)
    private static Path directory;

    public static ConfigurationNode config() throws ConfigurateException {
        return Objects.requireNonNull(new ConfigExtractor(ElectionsPlus.class, directory.toFile(), "config.yml")
                .extract())
                .toConfigurate();
    }

    public static ConfigurationNode messages() throws ConfigurateException {
        return Objects.requireNonNull(new ConfigExtractor(ElectionsPlus.class, directory.toFile(), "messages.yml")
                .extract())
                .toConfigurate();
    }

}
