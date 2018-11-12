/*
 * The MIT License
 *
 * Copyright (c) 2018 suren, CloudBees, Inc.
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

import hudson.PluginWrapper;
import hudson.util.HttpResponses;
import io.jenkins.plugins.localization.support.stapler.ResourceBundleFactoryImpl;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import hudson.Extension;
import hudson.model.AdministrativeMonitor;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.jelly.JellyFacet;
import org.kohsuke.stapler.jelly.ResourceBundleFactory;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * This admin monitor ensures that Stapler's JellyFacet uses this plugin's implementation.
 */
@Extension
@Symbol("localizationMonitor")
@Restricted(NoExternalUse.class)
public class LocalizationMonitor extends AdministrativeMonitor {

    // TODO extend with a check for the Metaclassloader.debugLoader

    private PluginWrapper plugin;

    @Override
    public String getDisplayName() {
        return Messages.LocalizationMonitor_DisplayName();
    }

    @Override
    public boolean isActivated() {
        Jenkins jenkins = Jenkins.get();

        WebApp webContext = WebApp.get(jenkins.servletContext);
        JellyFacet facet = webContext.getFacet(JellyFacet.class);
        ResourceBundleFactory factory = facet.resourceBundleFactory;

        Class<?> factoryClazz = factory.getClass();
        plugin = jenkins.getPluginManager().whichPlugin(factoryClazz);

        return !(factory instanceof ResourceBundleFactoryImpl);
    }

    @RequirePOST
    @Restricted(NoExternalUse.class)
    public HttpResponse doAct(StaplerRequest req) throws IOException {
        if (req.hasParameter("no")) {
            disable(true);
            return HttpResponses.redirectViaContextPath("/manage");
        } else {
            return HttpResponses.redirectViaContextPath("/pluginManager/installed");
        }
    }

    // Used in Stapler
    @Restricted(NoExternalUse.class)
    public PluginWrapper getPlugin() {
        return plugin;
    }
}
