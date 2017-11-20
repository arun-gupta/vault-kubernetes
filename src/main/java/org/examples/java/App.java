package org.examples.java;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws VaultException {
        System.out.println("prop: "
                + System.getProperty("GREETING")
                + System.getProperty("NAME"));
        System.out.println("Connecting to Vault: " + System.getProperty("VAULT_ADDR"));
        final VaultConfig config = new VaultConfig()
                .address(System.getProperty("VAULT_ADDR"))
                .sslConfig(new SslConfig().verify(false).build())
                .build();
        final Vault vault = new Vault(config);

        System.out.println("vault: "
                + vault
                    .logical()
                    .read("secret/creds")
                    .getData()
                    .get("GREETING")
                + vault
                    .logical()
                    .read("secret/creds")
                    .getData()
                    .get("NAME"));
    }
}
