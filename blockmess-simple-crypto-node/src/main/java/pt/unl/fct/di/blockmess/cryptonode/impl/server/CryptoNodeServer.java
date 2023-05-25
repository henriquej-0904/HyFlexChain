package pt.unl.fct.di.blockmess.cryptonode.impl.server;

import java.net.URI;
import java.util.Arrays;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import pt.unl.fct.di.blockmess.cryptonode.impl.server.config.ServerConfig;
import pt.unl.fct.di.blockmess.cryptonode.impl.server.resources.CryptoNodeResource;

public class CryptoNodeServer
{
	public static final int MIN_ARGS = 3;

	/**
	 * args[0] -> replicaId
	 * args[1] -> Server Port
	 * args[2] -> Blockmess Port
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			if (args.length < MIN_ARGS)
			{
				System.err.println("Invalid parameters:\nUsage: <replicaId> <bind port> <Blockmess port> [blockmess_properties]");
				System.exit(1);
			}

			int replicaId = Integer.parseInt(args[0]);
			int port = Integer.parseInt(args[1]);

			//String ip = InetAddress.getLocalHost().getHostAddress();
			int blockmessPort = Integer.parseInt(args[2]);

			ServerConfig.init(replicaId, blockmessPort);

			// init blockmess
			CryptoNodeResource.setBlockmess(
				args.length == MIN_ARGS ?
					new CryptoNodeResource.BlockmessConnector()
				:
					new CryptoNodeResource.BlockmessConnector(
						Arrays.copyOfRange(args, MIN_ARGS, args.length, String[].class)
					)
			);
            
			// URI uri = new URI(String.format("https://%s:%d/api/rest", ip, port));
			URI uri = new URI(String.format("http://%s:%d/api/rest", "0.0.0.0", port));

			ResourceConfig config = new ResourceConfig();
			config.register(CryptoNodeResource.class);
            
			// SSLContext sslContext = ServerConfig.getSSLContext();
			JdkHttpServerFactory.createHttpServer(uri, config);

			System.out.println("CryptoNode is running on " + uri.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
