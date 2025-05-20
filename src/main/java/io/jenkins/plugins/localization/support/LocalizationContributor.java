/*
 * The MIT License
 *
 * Copyright (c) 2018 CloudBees, Inc.
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
package io.jenkins.plugins.localization.support;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.PluginWrapper;
import jenkins.model.Jenkins;

import java.net.URL;
import java.util.logging.Logger;

public abstract class LocalizationContributor implements ExtensionPoint {

    private static final Logger LOGGER = Logger.getLogger(LocalizationContributor.class.getName());

    /**
     * Returns the specific resource located in the resources searched by the specific implementation, or null if not found.
     * @param resource The resource to look for as absolute path (cf. ClassLoader#getResource)
     * @return the URL for the specified resource, or null if not found
     */
    @CheckForNull
    public URL getResource(@NonNull String resource) {
        String modifiedPath = resource;
        if (resource.startsWith("/")) {
            modifiedPath = modifiedPath.substring(1);
        }
        URL url = getClass().getClassLoader().getResource(modifiedPath);
        if (url != null) {
            LOGGER.fine(() -> "Found localized resource " + resource + " at " + url + " in " + getClass());
        }
        return url;
    }

    /**
     * Returns the name of this implementation, defaulting to the class name.
     * @return the name of this implementation
     */
    @NonNull
    public String getName() {
        return getClass().getName();
    }

    /**
     * Returns the plugin that this implementation is located in, or null if the plugin could not be determined.
     * @return the plugin that this implementation is located in, or null if the plugin could not be determined.
     */
    @CheckForNull
    public final String getPluginName() {
        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) {
            PluginWrapper plugin = jenkins.pluginManager.whichPlugin(getClass());
            if (plugin != null) {
                return plugin.getShortName();
            }
        }
        return null;
    }

    /**
     * Finds a specified localizable resource.
     * @param resource the resource name to look for
     * @param clazz the class context for the resource name
     * @return the resource
     */
    public static URL findResource(String resource, Class clazz) {
        // Translate resource from clazz-relative to absolute based on Class#getResource algorithm
        String resourceName;
        if (resource.startsWith("/")) {
            resourceName = resource.substring(1);
        } else {
            resourceName = clazz.getPackage().getName().replace('.', '/') + "/" + resource;
        }

        for (LocalizationContributor contributor : ExtensionList.lookup(LocalizationContributor.class)) {
            URL url = contributor.getResource(resourceName);
            if (url != null) {
                LOGGER.finer(() -> "Found localized resource " + resource + " for " + clazz.getName() + " at " + url);
                return url;
            }
        }

        // fallback to default: Looking in the class context
        return clazz.getResource(resource);
    }

    /**
     * Look up a resource for a plugin.
     *
     * @param resource the resource to look up
     * @param plugin the plugin
     * @return resource for a plugin, or null if not found
     */
    @CheckForNull
    public URL getPluginResource(@NonNull String resource, @NonNull PluginWrapper plugin) {
        return getResource(resource);
    }
}
