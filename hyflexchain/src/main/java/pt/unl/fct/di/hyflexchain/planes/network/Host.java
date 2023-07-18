package pt.unl.fct.di.hyflexchain.planes.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;

public record Host(String host, int port) {
    
    public InetSocketAddress resolve() throws UnknownHostException
    {
        var address = InetAddress.getByName(host);
        return new InetSocketAddress(address, port);
    }

    public URI httpEndpoint()
    {
        return URI.create("http://" + host + ":" + port);
    }

    public URI httpsEndpoint()
    {
        return URI.create("https://" + host + ":" + port);
    }

}
