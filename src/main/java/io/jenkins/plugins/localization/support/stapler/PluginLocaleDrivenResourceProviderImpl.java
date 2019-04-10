/*
 * The MIT License
 *
 * Copyright (c) 2019 CloudBees, Inc., Daniel Beck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.jenkins.plugins.localization.support.stapler;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.PluginWrapper;
import io.jenkins.plugins.localization.support.LocalizationContributor;
import jenkins.model.Jenkins;
import jenkins.PluginLocaleDrivenResourceProvider;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.logging.Logger;

@Extension
public class PluginLocaleDrivenResourceProviderImpl implements PluginLocaleDrivenResourceProvider {

    public URL lookup(@Nonnull String path) {
        PluginWrapper pluginWrapper = findPluginForResourceUrl(path);
        if (pluginWrapper != null) {
            path = path.substring(pluginWrapper.baseResourceURL.toString().length());
            LOGGER.fine("Looking up localized file '" + path + "' from plugin '" + pluginWrapper + "'");
        } else {
            LOGGER.fine("Looking up localized file '" + path + "' from webapp");
        }
        for (LocalizationContributor contributor : ExtensionList.lookup(LocalizationContributor.class)) {
            URL url;

            if (pluginWrapper == null) {
                url = contributor.getResource(path);
            } else {
                url = contributor.getPluginResource(path, pluginWrapper);
            }
            if (url != null) {
                LOGGER.fine("Found localized file '" + path + "'");
                return url;
            }
        }
        LOGGER.fine("Failed to find localized file '" + path + "'");
        return null;
    }

    private PluginWrapper findPluginForResourceUrl(String s) {
        for (PluginWrapper wrapper : Jenkins.get().pluginManager.getPlugins()) {
            if (s.startsWith(wrapper.baseResourceURL.toString())) {
                return wrapper;
            }
        }
        return null;
    }

    private static final Logger LOGGER = Logger.getLogger(PluginLocaleDrivenResourceProviderImpl.class.getName());
}
