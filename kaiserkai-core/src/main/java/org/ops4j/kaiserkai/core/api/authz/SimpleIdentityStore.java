package org.ops4j.kaiserkai.core.api.authz;

import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;

import java.util.Arrays;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.credential.Password;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

import org.ops4j.kaiserkai.core.api.config.RegistryConfiguration;

@ApplicationScoped
public class SimpleIdentityStore implements IdentityStore {

    @Inject
    private RegistryConfiguration config;

    public CredentialValidationResult validate(UsernamePasswordCredential credential) {
        String user = credential.getCaller();
        Password password = credential.getPassword();
        if (user.equals(config.getOperatorName()) && password.compareTo(config.getOperatorDigest())) {
            return new CredentialValidationResult(user, new HashSet<>(Arrays.asList("USER")));
        }
        else if (user.equals(config.getAdminName()) && password.compareTo(config.getAdminDigest())) {
            return new CredentialValidationResult(user, new HashSet<>(Arrays.asList("USER", "ADMIN")));
        }
        return INVALID_RESULT;
    }
}
