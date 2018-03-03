package org.ops4j.kaiserkai.core.api.authz;

import static javax.security.enterprise.identitystore.CredentialValidationResult.INVALID_RESULT;

import java.util.Arrays;
import java.util.HashSet;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

import org.ops4j.kaiserkai.core.api.config.RegistryConfiguration;
import org.ops4j.kaiserkai.core.api.storage.file.DigestBuilder;

@ApplicationScoped
public class SimpleIdentityStore implements IdentityStore {

    @Inject
    private RegistryConfiguration config;

    public CredentialValidationResult validate(UsernamePasswordCredential credential) {
        String user = credential.getCaller();
        String password = credential.getPasswordAsString();
        if (matchesDigest(user, password, config.getOperatorDigest())) {
            return new CredentialValidationResult(user, new HashSet<>(Arrays.asList("USER")));
        }
        else if (matchesDigest(user, password, config.getAdminDigest())) {
            return new CredentialValidationResult(user, new HashSet<>(Arrays.asList("USER", "ADMIN")));
        }
        return INVALID_RESULT;
    }

    private boolean matchesDigest(String user, String password, String digest) {
        String actualDigest = DigestBuilder.computeDigest(String.format("%s:%s", user, password));
        return actualDigest.equals(digest);
    }
}
