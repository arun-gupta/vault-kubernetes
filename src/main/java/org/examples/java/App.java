package org.examples.java;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 * @author Arun Gupta
 */
public class App {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static void main(String[] args) throws VaultException, IOException {
        String serviceToken = getServiceToken();
        String clientToken = getClientToken(serviceToken);

        final Vault vault = getVaultClient(clientToken);
        System.out.println("vault: "
                + getSecret(vault, "GREETING")
                + getSecret(vault, "NAME"));
    }

    private static String getSecret(Vault vault, String secret) throws VaultException {
        return vault
                .logical()
                .read("secret/creds")
                .getData()
                .get(secret);
    }

    private static Vault getVaultClient(String clientToken) throws VaultException {
        System.out.println("Connecting to Vault: " + System.getenv("VAULT_ADDR"));
        final VaultConfig config = new VaultConfig()
                .address(System.getenv("VAULT_ADDR"))
                .token(clientToken)
                .sslConfig(new SslConfig().verify(false).build())
                .build();
        return new Vault(config);
    }

    private static String getServiceToken() throws IOException {
        String serviceToken = new String(Files.readAllBytes(Paths.get("/var/run/secrets/kubernetes.io/serviceaccount/token")));
        System.out.println("serviceToken: " + serviceToken);
        return serviceToken;
    }

    private static String getClientToken(String serviceToken) throws IOException {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

//        Map<String, String> map = vault.logical().write("/v1/auth/kubernetes/login",
//                new HashMap<String, Object>() {
//                    {
//                        put("role", "demo");
//                        put("jwt", serviceToken);
//                    }
//                }).getData();
//        System.out.println("vaultToken: " + map);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
        RequestBody body = RequestBody.create(JSON, "{\n" +
                "    \"role\": \"demo\",\n" +
                "    \"jwt\": \"" + serviceToken + "\"\n" +
                "}");
        Request request = new Request.Builder()
                .url(System.getenv("VAULT_ADDR") + "/v1/auth/kubernetes/login")
                .post(body)
                .build();
        String clientToken;
        try (Response response = httpClient.newCall(request).execute()) {
            JSONObject json = new JSONObject(response.body().string());
            clientToken = json.getJSONObject("auth").getString("client_token");
            System.out.println("client token: " + clientToken);
        }
        return clientToken;
    }
}
