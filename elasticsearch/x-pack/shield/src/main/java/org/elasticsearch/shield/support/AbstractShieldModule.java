/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.shield.support;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.shield.Shield;

/**
 *
 */
public abstract class AbstractShieldModule extends AbstractModule {

    protected final Settings settings;
    protected final boolean clientMode;
    protected final boolean shieldEnabled;

    public AbstractShieldModule(Settings settings) {
        this.settings = settings;
        this.clientMode = !"node".equals(settings.get(Client.CLIENT_TYPE_SETTING_S.getKey()));
        this.shieldEnabled = Shield.enabled(settings);
    }

    @Override
    protected final void configure() {
        configure(clientMode);
    }

    protected abstract void configure(boolean clientMode);

    public static abstract class Node extends AbstractShieldModule {

        protected Node(Settings settings) {
            super(settings);
        }

        @Override
        protected final void configure(boolean clientMode) {
            assert !clientMode : "[" + getClass().getSimpleName() + "] is a node only module";
            configureNode();
        }

        protected abstract void configureNode();
    }
}
