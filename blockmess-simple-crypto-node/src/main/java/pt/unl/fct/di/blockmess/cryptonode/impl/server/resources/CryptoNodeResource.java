package pt.unl.fct.di.blockmess.cryptonode.impl.server.resources;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;
import java.util.logging.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import applicationInterface.ApplicationInterface;
import jakarta.inject.Singleton;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.blockmess.cryptonode.api.Transaction;
import pt.unl.fct.di.blockmess.cryptonode.api.server.CryptoNodeAPI;
import pt.unl.fct.di.blockmess.cryptonode.impl.server.config.ServerConfig;
import pt.unl.fct.di.blockmess.cryptonode.util.Utils;
import pt.unl.fct.di.blockmess.cryptonode.util.result.Result;

@Singleton
public class CryptoNodeResource implements CryptoNodeAPI
{
    protected static final Logger LOG = Logger.getLogger(CryptoNodeResource.class.getSimpleName());

    protected static BlockmessConnector blockmess;

    /**
     * @return the blockmess
     */
    public static BlockmessConnector getBlockmess() {
        return blockmess;
    }

    public static void setBlockmess(BlockmessConnector blockmess)
    {
        CryptoNodeResource.blockmess = blockmess;
    }

    public CryptoNodeResource()
	{
		
	}

    protected byte[] invokeOrdered(byte[] request)
    {
        var res = blockmess.invokeSyncOperation(request);
        if (res.getLeft() == null)
            throw new InternalServerErrorException("Invoke ordered returned null");

        return res.getLeft();
    }

    protected void executeOrderedRequest(Transaction tx)
    {
        byte[] requestBytes = toJson(tx);
        byte[] resultBytes = invokeOrdered(requestBytes);

        @SuppressWarnings("unchecked")
        Result<Void> result = fromJson(resultBytes, Result.class);

        result.resultOrThrow();
    }

    protected static boolean verifySignature(Transaction tx)
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
    public final String sendTransaction(Transaction tx)
    {
        try {
            if (!verifySignature(tx))
                throw new WebApplicationException(Status.UNAUTHORIZED);
        } catch (WebApplicationException e) {
            LOG.info(e.getMessage());
            
            throw e;
        }

        executeOrderedRequest(tx);

        // log operation if successful
        // LOG.info(String.format("ORIGIN: %s, DEST: %s, TYPE: %s, VALUE: %d",
        // originId, destId, transaction.getType(), value));

        return tx.calcHash();
    }

    protected static byte[] toJson(Object obj)
    {
        try {
            return Utils.json.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected static <T> T fromJson(byte[] json, Class<T> valueType)
    {
        try {
            return Utils.json.readValue(json, valueType);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    protected static <T> T fromJson(byte[] json, TypeReference<T> valueTypeRef)
    {
        try {
            return Utils.json.readValue(json, valueTypeRef);
        } catch (IOException e) {
            throw new InternalServerErrorException(e.getMessage(), e);
        }
    }

    public static class BlockmessConnector extends ApplicationInterface
    {

        public BlockmessConnector() {
            super(defaultBlockmessProperties());
        }

        public BlockmessConnector(String[] blockmessProperties) {
            super(concat(defaultBlockmessProperties(), blockmessProperties));
        }

        public static String[] defaultBlockmessProperties()
        {
            return new String[]
            {
                "port=" + ServerConfig.getBlockmessPort(),
                "redirectFile=blockmess-logs/" + ServerConfig.getReplicaId() + ".log",
                "genesisUUID=" + UUID.randomUUID()
            };
        }

        protected static String[] concat(String[] array1, String[] array2)
        {
            var res = new String[array1.length + array2.length];
            System.arraycopy(array1, 0, res, 0, array1.length);
            System.arraycopy(array2, 0, res, array1.length, array2.length);
            return res;
        }

        @Override
        public byte[] processOperation(byte[] operation) {
            try {
                Result<Void> result = null;
    
                try {
                    Transaction tx = fromJson(operation, Transaction.class);
    
                    if (!verifySignature(tx))
                        throw new WebApplicationException(Status.UNAUTHORIZED);
    
                    result = Result.ok();
    
                    LOG.info("Send Transaction - OK");
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
}

