package pt.unl.fct.di.blockmess.cryptonode.impl.server.resources;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import blockmess.applicationInterface.ApplicationInterface;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.blockmess.cryptonode.api.Transaction;
import pt.unl.fct.di.blockmess.cryptonode.api.server.CryptoNodeAPI;
import pt.unl.fct.di.blockmess.cryptonode.impl.server.config.ServerConfig;
import pt.unl.fct.di.blockmess.cryptonode.util.Utils;
import pt.unl.fct.di.blockmess.cryptonode.util.result.Result;

public class CryptoNodeResource extends ApplicationInterface implements CryptoNodeAPI
{
    protected static final Logger LOG = Logger.getLogger(CryptoNodeResource.class.getSimpleName());

    public CryptoNodeResource()
	{
		super(new String[]
            {
                "port=" + ServerConfig.getBlockmessPort(),
                "redirectFile=blockmess-logs/" + ServerConfig.getReplicaId() + ".log",
                "genesisUUID=" + UUID.randomUUID()
            });
	}

    protected byte[] invokeOrdered(byte[] request)
    {
        var res = invokeSyncOperation(request);
        if (res.getLeft() == null)
            throw new InternalServerErrorException("Invoke ordered returned null");

        return res.getLeft();
    }

    protected Void executeOrderedRequest(Transaction tx)
    {
        byte[] requestBytes = toJson(tx);
        byte[] resultBytes = invokeOrdered(requestBytes);

        @SuppressWarnings("unchecked")
        Result<Void> result = this.fromJson(resultBytes, Result.class);

        return result.resultOrThrow();
    }

    protected boolean verifySignature(Transaction tx)
    {
        try {
            return tx.checkSignature();
        } catch (InvalidKeyException | SignatureException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            throw new BadRequestException(e.getMessage(), e);
        }
    }

    @Override
    public final void sendTransaction(Transaction tx)
    {
        try {
            if (verifySignature(tx))
                throw new WebApplicationException(Status.UNAUTHORIZED);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            throw e;
        }

        executeOrderedRequest(tx);

        // log operation if successful
        // LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d",
        // originId, destId, transaction.getType(), value));

        throw new WebApplicationException(
                Response.status(Status.ACCEPTED).build());
    }

    protected byte[] toJson(Object obj)
    {
        try {
            return Utils.json.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected <T> T fromJson(byte[] json, Class<T> valueType)
    {
        try {
            return Utils.json.readValue(json, valueType);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected <T> T fromJson(byte[] json, TypeReference<T> valueTypeRef)
    {
        try {
            return Utils.json.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    @Override
    public byte[] processOperation(byte[] operation) {
        try {
            Transaction tx = fromJson(operation, Transaction.class);
            Result<Void> result = null;

            try {
                if (verifySignature(tx))
                    throw new WebApplicationException(Status.UNAUTHORIZED);

                result = Result.ok();
            } catch (WebApplicationException e) {
                LOG.info(e.getMessage());
                result = Result.error(e);
            }

            byte[] res = toJson(result);
            return res;
        } catch (Exception e) {
            Utils.logError(e, LOG);
            return null;
        }
    }
}

