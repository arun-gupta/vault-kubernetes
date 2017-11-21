package org.examples.java;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.swing.UIManager.put;

/**
 * Hello world!
 */
public class App {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static void main(String[] args) throws VaultException, IOException {
//        System.out.println("prop: "
//                + System.getProperty("GREETING")
//                + System.getProperty("NAME"));
        System.out.println("Connecting to Vault: " + System.getenv("VAULT_ADDR"));
        final VaultConfig config = new VaultConfig()
                .address(System.getenv("VAULT_ADDR"))
//                .token(System.getenv("TOKEN"))
                .sslConfig(new SslConfig().verify(false).build())
                .build();
        final Vault vault = new Vault(config);
        String serviceToken = new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")));
        System.out.println("serviceToken: " + serviceToken);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        RequestBody body = RequestBody.create(JSON, "{\n" +
                "    \"role\": \"vault-reviewer\",\n" +
                "    \"jwt\": \"" + serviceToken + "\"\n" +
                "}");
        Request request = new Request.Builder()
                .url(System.getenv("VAULT_ADDR") + "/v1/auth/kubernetes/login")
                .post(body)
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            System.out.println("vaultToken: " + response.body().string());
        }

//        Map<String, String> map = vault.logical().write("/auth/kubernetes/login",
//                new HashMap<String, Object>() {
//                    {
//                        put("role", "vault-auth");
//                        put("jwt", serviceToken);
//                    }
//                }).getData();
//        System.out.println("vaultToken: " + map);

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
