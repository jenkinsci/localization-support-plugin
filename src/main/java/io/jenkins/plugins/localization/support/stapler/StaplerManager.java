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
package io.jenkins.plugins.localization.support.stapler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.ExtensionList;
import hudson.ExtensionListListener;
import hudson.init.Initializer;
import io.jenkins.plugins.localization.support.LocalizationContributor;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyFacet;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;

@Restricted(NoExternalUse.class)
public class StaplerManager extends ExtensionListListener {
    @Initializer
    public static void initialize() {
        { // resources for Jelly files
            WebApp webApp = WebApp.get(Jenkins.get().servletContext);

            // Override where the Jelly views look for resource bundles
            JellyFacet facet = webApp.getFacet(JellyFacet.class);
            facet.resourceBundleFactory = new ResourceBundleFactoryImpl();
        }

        { // Provide a fallback source for resources from Descriptor#doHelp
            ExtensionList.lookup(LocalizationContributor.class).addListener(new StaplerManager());

            // TODO add a dedicated feature to Stapler for this
            MetaClassLoader.debugLoader = buildMetaClassLoader();
        }
    }

    private static MetaClassLoader buildMetaClassLoader() {
        return new MetaClassLoader(new URLClassLoader(ExtensionList.lookup(LocalizationContributor.class).stream()
                .map(c -> Jenkins.get().pluginManager.whichPlugin(c.getClass()))
                .filter(Objects::nonNull)
                .map(plugin -> plugin.baseResourceURL)
                .toArray(URL[]::new)));
    }

    @Override
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void onChange() {
        MetaClassLoader.debugLoader = buildMetaClassLoader();
    }
}
