package com.intropro.prairie.unit.kerberos;

import com.intropro.prairie.junit.PrairieRunner;
import com.intropro.prairie.unit.common.annotation.PrairieUnit;
import org.apache.directory.kerberos.client.KdcConfig;
import org.apache.directory.kerberos.client.KdcConnection;
import org.apache.directory.kerberos.client.Kinit;
import org.apache.directory.shared.kerberos.codec.types.EncryptionType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;

/**
 * Created by presidentio on 8/26/16.
 */
@RunWith(PrairieRunner.class)
public class KerberosUnitTest {

    @PrairieUnit
    private KerberosUnit kerberosUnit;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void addUserAndKinit() throws Exception {
        String username = "presidentio";
        String password = "pass";
        KerberosUser kerberosUser = kerberosUnit.getKerberosUserManager().addUser(username, password);
        Assert.assertTrue(kerberosUser.getKeytab().exists());
        KdcConfig kdcConfig = new KdcConfig();
        kdcConfig.setHostName(kerberosUnit.getKdcHost());
        kdcConfig.setKdcPort(kerberosUnit.getKdcPort());
        kdcConfig.setEncryptionTypes(new HashSet<>(Collections.singletonList(EncryptionType.DES_CBC_MD5)));
        kdcConfig.setUseUdp(false);
        KdcConnection kdcConnection = new KdcConnection(kdcConfig);
        Kinit kinit = new Kinit(kdcConnection);
        kinit.setCredCacheFile(folder.newFile());
        kinit.kinit(kerberosUser.getPrincipal(), password);
    }

}