/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.watcher;


import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.multibindings.Multibinder;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.watcher.support.WatcherIndexTemplateRegistry;
import org.elasticsearch.watcher.support.WatcherIndexTemplateRegistry.TemplateConfig;
import org.elasticsearch.watcher.support.validation.WatcherSettingsValidation;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class WatcherModule extends AbstractModule {

    static final String PROPERTIES_FILE = "/watcher.properties";
    static final String VERSION_FIELD = "watcher.template.version";

    public static final String HISTORY_TEMPLATE_NAME = "watch_history_" + getHistoryIndexTemplateVersion();
    public static final String TRIGGERED_TEMPLATE_NAME = "triggered_watches";
    public static final String WATCHES_TEMPLATE_NAME = "watches";
    public static final Setting<Settings> HISTORY_TEMPLATE_SETTING = Setting.groupSetting("watcher.history.index.", true,
            Setting.Scope.CLUSTER);
    public static final Setting<Settings> TRIGGERED_TEMPLATE_SETTING = Setting.groupSetting("watcher.triggered_watches.index.", true,
            Setting.Scope.CLUSTER);
    public static final Setting<Settings> WATCHES_TEMPLATE_SETTING = Setting.groupSetting("watcher.watches.index.", true,
            Setting.Scope.CLUSTER);


    public final static TemplateConfig[] TEMPLATE_CONFIGS = new TemplateConfig[]{
            new TemplateConfig(TRIGGERED_TEMPLATE_NAME, TRIGGERED_TEMPLATE_SETTING),
            new TemplateConfig(HISTORY_TEMPLATE_NAME, "watch_history", HISTORY_TEMPLATE_SETTING),
            new TemplateConfig(WATCHES_TEMPLATE_NAME, WATCHES_TEMPLATE_SETTING)
    };

    protected final Settings settings;

    public WatcherModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    protected void configure() {
        bind(WatcherLifeCycleService.class).asEagerSingleton();
        bind(WatcherSettingsValidation.class).asEagerSingleton();

        bind(WatcherIndexTemplateRegistry.class).asEagerSingleton();
        Multibinder<TemplateConfig> multibinder
                = Multibinder.newSetBinder(binder(), TemplateConfig.class);
        for (TemplateConfig templateConfig : TEMPLATE_CONFIGS) {
            multibinder.addBinding().toInstance(templateConfig);
        }
    }

    public static final Integer getHistoryIndexTemplateVersion() {
        try (InputStream is = WatcherModule.class.getResourceAsStream(PROPERTIES_FILE)) {
            Properties properties = new Properties();
            properties.load(is);
            String version = properties.getProperty(VERSION_FIELD);
            if (Strings.hasLength(version)) {
                return Integer.parseInt(version);
            }
            return null;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("failed to parse watcher template version");
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to load watcher template version");
        }
    }
}
