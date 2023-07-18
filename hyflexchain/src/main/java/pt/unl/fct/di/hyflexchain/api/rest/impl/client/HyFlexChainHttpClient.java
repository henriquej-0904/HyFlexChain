package pt.unl.fct.di.hyflexchain.api.rest.impl.client;

import java.io.Closeable;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.InvocationCallback;
import pt.unl.fct.di.hyflexchain.api.rest.TransactionInterfaceRest;
import pt.unl.fct.di.hyflexchain.planes.data.transaction.HyFlexChainTransaction;

/**
 * A HTTP client for the HyFlexChain Server
 */
public class HyFlexChainHttpClient implements Closeable {

    private static final int CONNECT_TIMEOUT = 60 * 3;
    private static final int READ_TIMEOUT = 60 * 3;

    private static final String PREPEND_URL = "api/rest";

    protected Client client;

    
    public HyFlexChainHttpClient()
    {
        this.client = getClientBuilder().build();
    }

    public HyFlexChainHttpClient(SSLContext sslContext)
    {
        this.client = getClientBuilder()
            .sslContext(sslContext)
            .hostnameVerifier((arg0, arg1) -> true)
            .build();
    }

    /* public LedgerClient(String replicaId, URI endpoint, SecureRandom random, SSLContext sslContext) throws KeyStoreException
    {
        ClientBuilder builder = getClientBuilder().sslContext(sslContext)
            .hostnameVerifier((arg0, arg1) -> true);

        this.client = builder.build();
        this.endpoint = endpoint;

        this.serverPublicKey = Crypto.getTrustStore().getCertificate(replicaId).getPublicKey();
    } */

    private static ClientBuilder getClientBuilder()
    {
        return ClientBuilder.newBuilder().connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);
    }

    public Future<String> sendTransactionAsync(URI endpoint, HyFlexChainTransaction tx,
        InvocationCallback<String> callback)
    {
        return this.client.target(endpoint).path(PREPEND_URL).path(TransactionInterfaceRest.PATH)
            .path("transaction")
            .request()
            .async()
            .post(Entity.json(tx), callback);
    }

    @Override
    public void close() {
        this.client.close();
    }
    
}
