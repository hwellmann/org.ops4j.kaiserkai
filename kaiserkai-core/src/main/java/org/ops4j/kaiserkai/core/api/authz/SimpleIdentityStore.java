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
        char[] password = credential.getPassword().getValue();
        if (matchesDigest(user, password, config.getOperatorDigest())) {
            return new CredentialValidationResult(user, new HashSet<>(Arrays.asList("USER")));
        }
        else if (matchesDigest(user, password, config.getAdminDigest())) {
            return new CredentialValidationResult(user, new HashSet<>(Arrays.asList("USER", "ADMIN")));
        }
        return INVALID_RESULT;
    }

    private boolean matchesDigest(String user, char[] password, String digest) {
        String actualDigest = DigestBuilder.computeDigest(concatenate(user, password));
        return actualDigest.equals(digest);
    }

    private char[] concatenate(String user, char[] password) {
        char[] charArray = new char[user.length() + password.length + 1];
        System.arraycopy(user.toCharArray(), 0, charArray, 0, user.length());
        charArray[user.length()] = ':';
        System.arraycopy(password, 0, charArray, user.length() + 1, password.length);
        return charArray;
    }
}
