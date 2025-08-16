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

import io.jenkins.plugins.localization.support.LocalizationContributor;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.jelly.ResourceBundle;
import org.kohsuke.stapler.jelly.ResourceBundleFactory;

@Restricted(NoExternalUse.class)
public final class ResourceBundleFactoryImpl extends ResourceBundleFactory {
    private static final String DEVELOPMENT_RESOURCES = "src/main/resources";
    private static final String JAR_INDICATOR = ".jar!";
    private static final Logger LOGGER = Logger.getLogger(ResourceBundleFactoryImpl.class.getName());

    @Override
    public ResourceBundle create(final String baseName) {
        return new ResourceBundle2(baseName);
    }

    private static class ResourceBundle2 extends ResourceBundle {

        private ResourceBundle2(String baseName) {
            super(baseName);
        }

        @Override
        protected Properties wrapUp(String locale, Properties props) {
            String cleanBaseName = "";
            if (getBaseName().contains(JAR_INDICATOR)) {
                cleanBaseName = getBaseName().substring(getBaseName().indexOf(JAR_INDICATOR) + JAR_INDICATOR.length());
            } else if (getBaseName().contains(DEVELOPMENT_RESOURCES)) {
                // support development
                cleanBaseName = getBaseName()
                        .substring(getBaseName().indexOf(DEVELOPMENT_RESOURCES) + DEVELOPMENT_RESOURCES.length());
            }

            String name = cleanBaseName + "_" + locale + ".properties";

            URL url = LocalizationContributor.findResource(name, getClass());
            if (url != null) {
                try (InputStream stream = url.openStream()) {
                    PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(stream);
                    Enumeration<String> keys = propertyResourceBundle.getKeys();
                    // TODO Java 9+ can use 'asIterator' and get rid of below collections conversion
                    List<String> keysAsSaneType = Collections.list(keys);

                    for (String localKey : keysAsSaneType) {
                        String value = propertyResourceBundle.getString(localKey);
                        props.setProperty(localKey, value);
                    }

                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Failed to load localized resources file " + name, ex);
                }
            }

            return props;
        }
    }
}
