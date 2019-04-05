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

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.operator.name", defaultValue = "operator")
    String operatorName;

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.operator.digest", defaultValue = "sha256:1e54dae5e77cea8d5be042243b0137e63b7c27625fe3e84717e645237589914c")
    String operatorDigest;

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.admin.name", defaultValue = "admin")
    String adminName;

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.admin.digest", defaultValue = "sha256:8da193366e1554c08b2870c50f737b9587c3372b656151c4a96028af26f51334")
    String adminDigest;

    @PostConstruct
    void init() {
        if (registryRoot.isEmpty()) {
            registryRoot = home + "/kaiserkai";
        }
    }

    void logConfiguration(@Observes StartupEvent event) {

        log.info("=== Docker Registry Configuration:");
        log.info("    registryRoot = {}", registryRoot);
        log.info("    operatorName = {}", operatorName);
        log.info("    adminName    = {}", adminName);
    }

    @Override
    public File getRegistryRoot() {
        return new File(registryRoot);
    }

    /**
     * @return the operatorName
     */
    @Override
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * @return the operatorDigest
     */
    @Override
    public String getOperatorDigest() {
        return operatorDigest;
    }

    /**
     * @return the adminName
     */
    @Override
    public String getAdminName() {
        return adminName;
    }

    /**
     * @return the adminDigest
     */
    @Override
    public String getAdminDigest() {
        return adminDigest;
    }
}
