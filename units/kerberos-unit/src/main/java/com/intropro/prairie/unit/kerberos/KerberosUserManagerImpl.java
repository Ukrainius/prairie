package com.intropro.prairie.unit.kerberos;

import com.intropro.prairie.unit.kerberos.exception.KerberosException;
import org.apache.directory.server.kerberos.shared.crypto.encryption.KerberosKeyFactory;
import org.apache.directory.server.kerberos.shared.keytab.Keytab;
import org.apache.directory.server.kerberos.shared.keytab.KeytabEntry;
import org.apache.directory.shared.kerberos.KerberosTime;
import org.apache.directory.shared.kerberos.codec.types.EncryptionType;
import org.apache.directory.shared.kerberos.codec.types.PrincipalNameType;
import org.apache.directory.shared.kerberos.components.EncryptionKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by presidentio on 8/30/16.
 */
public class KerberosUserManagerImpl implements KerberosUserManager {

    private static final Logger LOGGER = LogManager.getLogger(KerberosUserManagerImpl.class);

    private KerberosServer kerberosServer;
    private Path keytabDirectory;
    private Map<String, KerberosUser> kerberosUsers = new HashMap<>();

    public KerberosUserManagerImpl(KerberosServer kerberosServer, Path keytabDirectory) {
        this.kerberosServer = kerberosServer;
        this.keytabDirectory = keytabDirectory;
    }

    @Override
    public KerberosUser addUser(String username, String password) throws KerberosException {
        kerberosServer.addUserEntry(username, password);
        File keytab = generateKeytab(username, password);
        KerberosUser kerberosUser = new KerberosUser(username, password, keytab, kerberosServer.getPrincipal(username));
        kerberosUsers.put(username, kerberosUser);
        LOGGER.info("Added user: " + kerberosUser);
        return kerberosUser;
    }

    @Override
    public KerberosUser getUser(String username) {
        return kerberosUsers.get(username);
    }

    private File generateKeytab(String username, String passPhrase) throws KerberosException {
        final Map<String, String> users = new HashMap<>();
        users.put(username, passPhrase);
        File keytabFile = keytabDirectory.resolve(username + "-" + UUID.randomUUID() + ".ktb").toFile();
        keytabFile.getParentFile().mkdirs();
        generateKeytab(users, keytabFile);
        LOGGER.info("Generated keytab " + keytabFile + "for user " + username);
        return keytabFile;
    }

    private void generateKeytab(final Map<String, String> keytabUsers, final File keytabFile)
            throws KerberosException {
        final KerberosTime timeStamp = new KerberosTime(System.currentTimeMillis());
        final int principalType = PrincipalNameType.KRB_NT_PRINCIPAL.getValue();

        final Keytab keytab = Keytab.getInstance();
        final List<KeytabEntry> entries = new ArrayList<>();

        for (String keytabUser : keytabUsers.keySet()) {
            String principal = kerberosServer.getPrincipal(keytabUser);
            for (Map.Entry<EncryptionType, EncryptionKey> keyEntry : KerberosKeyFactory.getKerberosKeys(
                    principal, keytabUsers.get(keytabUser)).entrySet()) {
                final EncryptionKey key = keyEntry.getValue();
                final byte keyVersion = (byte) key.getKeyVersion();
                entries.add(new KeytabEntry(principal, principalType, timeStamp, keyVersion, key));
            }
        }
        keytab.setEntries(entries);
        try {
            keytab.write(keytabFile);
        } catch (IOException e) {
            throw new KerberosException("Failed to save keytab to file " + keytabFile, e);
        }
    }

}
