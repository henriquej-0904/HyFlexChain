package pt.unl.fct.di.blockmess.cryptonode.impl.server;

import java.net.InetAddress;
import java.net.URI;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import pt.unl.fct.di.blockmess.cryptonode.impl.server.config.ServerConfig;
import pt.unl.fct.di.blockmess.cryptonode.impl.server.resources.CryptoNodeResource;

public class CryptoNodeServer
{
	/**
	 * args[0] -> replicaId
	 * args[1] -> Server Port
	 * args[2] -> Blockmess Port
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
			if (args.length != 3)
			{
				System.err.println("Invalid parameters:\nUsage: <replicaId> <bind port> <Blockmess port>");
				System.exit(1);
			}

			int replicaId = Integer.parseInt(args[0]);
			int port = Integer.parseInt(args[1]);

			String ip = InetAddress.getLocalHost().getHostAddress();
			int blockmessPort = Integer.parseInt(args[2]);

			ServerConfig.init(replicaId, blockmessPort);

			//CryptoNodeResource resource = new CryptoNodeResource();
            
			// URI uri = new URI(String.format("https://%s:%d/api/rest", ip, port));
			URI uri = new URI(String.format("http://%s:%d/api/rest", ip, port));

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
