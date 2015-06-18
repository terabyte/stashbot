package com.palantir.stash.stashbot.persistence;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

/**
 * This class persists authentication credentials (i.e. ssh keys)
 * 
 * TODO: instead of keying on a name, generate a unique one for each repo for better granularity.
 * Before we can do this, however, we need to figure out how to insert credentials into jenkins.
 * Otherwise each key we generate will have to be manually put into jenkins, which is a no-go.
 * 
 * @author cmyers
 */
@Table("AuthCred001")
public interface AuthenticationCredential extends Entity {

    public String getName();

    public void setName(String name);

    public String getPublicKey();

    public void setPublicKey(String publicKey);

    public String getPrivateKey();

    public void setPrivateKey(String privateKey);

}
