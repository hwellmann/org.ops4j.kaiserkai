package org.ops4j.kaiserkai.core.api.config;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class RegistryConfigurationImpl implements RegistryConfiguration {

    private static Logger log = LoggerFactory.getLogger(RegistryConfigurationImpl.class);

    @Inject
    @ConfigProperty(name = "user.home")
    private String home;

    @Inject
    @ConfigProperty(name = "kaiserkai.fs.root")
    private String registryRoot;

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.operator.name", defaultValue = "operator")
    private String operatorName;

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.operator.digest", defaultValue = "sha256:1e54dae5e77cea8d5be042243b0137e63b7c27625fe3e84717e645237589914c")
    private String operatorDigest;

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.admin.name", defaultValue = "admin")
    private String adminName;

    @Inject
    @ConfigProperty(name = "kaiserkai.auth.admin.digest", defaultValue = "sha256:8da193366e1554c08b2870c50f737b9587c3372b656151c4a96028af26f51334")
    private String adminDigest;

    @PostConstruct
    void init() {
        if (registryRoot == null) {
            registryRoot = home + "/docker-registry-java";
        }

    }

    void logConfiguration(@Observes @Initialized(ApplicationScoped.class) Object event) {
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
