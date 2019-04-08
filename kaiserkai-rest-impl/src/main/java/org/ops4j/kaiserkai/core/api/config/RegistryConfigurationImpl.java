package org.ops4j.kaiserkai.core.api.config;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RegistryConfigurationImpl implements RegistryConfiguration {

    private static Logger log = LoggerFactory.getLogger(RegistryConfigurationImpl.class);

    @Inject
    @ConfigProperty(name = "user.home")
    String home;

    @Inject
    @ConfigProperty(name = "kaiserkai.fs.root", defaultValue = "")
    String registryRoot;

    @PostConstruct
    void init() {
        if (registryRoot.isEmpty()) {
            registryRoot = home + "/kaiserkai";
        }
    }

    void logConfiguration(@Observes StartupEvent event) {

        log.info("=== Docker Registry Configuration:");
        log.info("    registryRoot = {}", registryRoot);
    }

    @Override
    public File getRegistryRoot() {
        return new File(registryRoot);
    }
}
