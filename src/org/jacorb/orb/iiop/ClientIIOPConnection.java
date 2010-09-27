/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1997-2004 Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jacorb.orb.iiop;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.jacorb.orb.CDRInputStream;
import org.jacorb.orb.factory.SocketFactory;
import org.jacorb.orb.giop.TransportManager;
import org.jacorb.orb.listener.TCPConnectionEvent;
import org.jacorb.orb.listener.TCPConnectionListener;
import org.omg.CORBA.TIMEOUT;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.CSIIOP.TAG_TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.SSLIOP.SSL;
import org.omg.SSLIOP.SSLHelper;
import org.omg.SSLIOP.TAG_SSL_SEC_TRANS;

/**
 * @author Nicolas Noffke
 * @author Andre Spiegel
 * @version $Id: ClientIIOPConnection.java,v 1.32 2006/07/25 15:43:21 alphonse.bendt Exp $
 */
public class ClientIIOPConnection
    extends IIOPConnection
    implements Configurable
{
    private int timeout = 0;

    private int     ssl_port = -1;
    private int noOfRetries  = 5;
    private int retryInterval = 0;
    private boolean doSupportSSL = false;
    private TransportManager transportManager;
    private TCPConnectionListener connectionListener;
    private boolean keepAlive;

    //for testing purposes only: # of open transports
    //used by org.jacorb.test.orb.connection[Client|Server]ConnectionTimeoutTest
    public static int openTransports = 0;

    //for storing exceptions during connect
    private Exception exception = null;

    public ClientIIOPConnection()
    {
        super();

        use_ssl = false;
    }

    public void configure(Configuration configuration)
        throws ConfigurationException
    {
        super.configure(configuration);
        //get the client-side timeout property value

        timeout =
            configuration.getAttributeAsInteger("jacorb.connection.client.idle_timeout",0 );
        noOfRetries =
            configuration.getAttributeAsInteger("jacorb.retries", 5);
        retryInterval =
            configuration.getAttributeAsInteger("jacorb.retry_interval",500);
        doSupportSSL =
            configuration.getAttribute("jacorb.security.support_ssl","off").equals("on");
        transportManager =
            this.configuration.getORB().getTransportManager();

        keepAlive = configuration.getAttributeAsBoolean("jacorb.connection.client.keepalive", false);

        connectionListener = transportManager.getSocketFactoryManager().getTCPListener();
    }

    /**
     * Attempts to establish a 1-to-1 connection with a server using the
     * Listener endpoint from the given Profile description.  It shall
     * throw a COMM_FAILURE exception if it fails (e.g. if the endpoint
     * is unreachable) or a TIMEOUT exception if the given time_out period
     * has expired before a connection is established. If the connection
     * is successfully established it shall store the used Profile data.
     *
     */
    public synchronized void connect(org.omg.ETF.Profile server_profile, long time_out)
    {
        if( ! connected )
        {
            if (server_profile instanceof IIOPProfile)
            {
                this.profile = (IIOPProfile) server_profile;
            }
            else
            {
                throw new org.omg.CORBA.BAD_PARAM
                    ( "attempt to connect an IIOP connection "
                    + "to a non-IIOP profile: " + server_profile.getClass());
            }

            final IIOPLoopback loopback = getLocalLoopback() ;
            if (loopback != null)
            {
                final IIOPLoopbackInputStream lis = new IIOPLoopbackInputStream() ;
                final IIOPLoopbackOutputStream los = new IIOPLoopbackOutputStream() ;

                loopback.initLoopback(lis, los) ;

                in_stream = lis ;
                out_stream = los ;

                connected = true;

                //for testing purposes
                ++openTransports;

                return;
            }

            checkSSL();

            int retries = noOfRetries;
            while( retries >= 0 )
            {
                try
                {
                    createSocket(time_out);

                    if( timeout != 0 )
                    {
                        /* re-set the socket timeout */
                        socket.setSoTimeout( timeout /*note: this is another timeout!*/ );
                    }

                    socket.setKeepAlive(keepAlive);

                    in_stream =
                        socket.getInputStream();

                    out_stream =
                        new BufferedOutputStream( socket.getOutputStream());

                    if (logger.isInfoEnabled())
                    {
                        logger.info("Connected to " + connection_info +
                                    " from local port " +
                                    socket.getLocalPort() +
                                    ( this.isSSL() ? " via SSL" : "" ) +
                                    ( (timeout == 0) ? "" : " Timeout: " + timeout));
                    }

                    connected = true;

                    //for testing purposes
                    ++openTransports;

                    return;
                }
                catch ( IOException c )
                {
                    logger.debug("Exception", c );

                    //only sleep and print message if we're actually
                    //going to retry
                    retries--;
                    if( retries >= 0 )
                    {
                        if (logger.isInfoEnabled())
                        {
                            logger.info("Retrying to connect to " +
                                        connection_info );
                        }

                        try
                        {
                            Thread.sleep( retryInterval );
                        }
                        catch( InterruptedException i )
                        {
                        }
                    }
                }
                catch (TIMEOUT e)
                {
                    //thrown if timeout is expired
                    profile = null;
                    use_ssl = false;
                    ssl_port = -1;
                    throw e;
                }

            }

            if( retries < 0 )
            {
                profile = null;
                use_ssl = false;
                ssl_port = -1;
                throw new org.omg.CORBA.TRANSIENT
                    ( "Retries exceeded, couldn't reconnect to " +
                      connection_info );
            }
        }
    }

    private IIOPLoopback getLocalLoopback()
    {
        final IIOPProfile iiopProfile = (IIOPProfile)profile ;
        final List addressList = new ArrayList() ;
        addressList.add(iiopProfile.getAddress());
        addressList.addAll(iiopProfile.getAlternateAddresses());

        final Iterator addressIterator = addressList.iterator() ;
        final IIOPLoopbackRegistry registry =
            IIOPLoopbackRegistry.getRegistry() ;

        while (addressIterator.hasNext())
        {
            final IIOPAddress address = (IIOPAddress)addressIterator.next() ;
            final IIOPLoopback loopback = registry.getLoopback(address) ;
            if (loopback != null)
            {
                return loopback ;
            }
        }

        return null ;
    }

    /**
     * Tries to create a socket connection to any of the addresses in
     * the target profile, starting with the primary IIOP address,
     * and then any alternate IIOP addresses that have been specified.
     */
    private void createSocket(long time_out)
        throws IOException
    {
        List addressList = new ArrayList();
        addressList.add(((IIOPProfile)profile).getAddress());
        addressList.addAll(((IIOPProfile)profile).getAlternateAddresses());

        Iterator addressIterator = addressList.iterator();

        exception = null;
        socket = null;
        while (socket == null && addressIterator.hasNext())
        {
            try
            {
                IIOPAddress address = (IIOPAddress)addressIterator.next();

                final SocketFactory factory =
                    (use_ssl) ? getSSLSocketFactory() :
                    getSocketFactory();

                final String ipAddress = address.getIP();
                final int port = (use_ssl) ? ssl_port : address.getPort();
                if (ipAddress.indexOf(':') == -1)
                {
                    connection_info = ipAddress + ":" + port;
                }
                else
                {
                    connection_info = "[" + ipAddress + "]:" + port;
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("Trying to connect to " + connection_info + " with timeout=" + time_out + ( use_ssl ? " using SSL." : "."));
                }
                exception = null;

                if( time_out > 0 )
                {
                    final int truncatedTimeout = (int) time_out;
                    if (truncatedTimeout != time_out)
                    {
                        logger.warn("timeout might be changed due to conversion from long to int. old value: " + time_out + " new value: " + truncatedTimeout);
                    }
                    socket = factory.createSocket(ipAddress, port, truncatedTimeout);
                }
                else
                {
                    //no timeout --> may hang forever!
                    socket = factory.createSocket(ipAddress, port);
                }
            }
            catch (Exception e)
            {
                exception = e;
            }
            finally
            {
                if (socket != null && connectionListener.isListenerEnabled())
                {
                    connectionListener.connectionOpened
                    (
                        new TCPConnectionEvent
                        (
                            this,
                            socket.getInetAddress().toString(),
                            socket.getPort(),
                            socket.getLocalPort(),
                            getLocalhost()
                        )
                    );
                }
            }
        }

        if (exception != null)
        {
            if (exception instanceof SocketTimeoutException)
            {
                throw new TIMEOUT("connection timeout of " + timeout + " milliseconds expired: " + exception);
            }
            else if( exception instanceof IOException )
            {
                throw (IOException)exception;
            }
            else
            {
                //not expected, because all used methods just throw IOExceptions or TIMEOUT
                //but... never say never ;o)
                throw new IOException ( "Unexpected exception occured: " + exception.toString() );
            }
        }
    }

    public synchronized void close()
    {
        if (!connected)
        {
            return;
        }

        try
        {
            if (socket != null)
            {
                socket.close();
            }

            //this will cause exceptions when trying to read from
            //the streams. Better than "nulling" them.
            if( in_stream != null )
            {
                in_stream.close();
            }
            if( out_stream != null )
            {
                out_stream.close();
            }

            //for testing purposes
            --openTransports;

            if (logger.isInfoEnabled())
            {
                logger.info("Client-side TCP transport to " +
                        connection_info + " closed.");
            }

            connected = false;
        }
        catch (IOException ex)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug ("Exception when closing the socket", ex);
            }

            throw to_COMM_FAILURE (ex, socket);
        }
        finally
        {
            if (socket != null && connectionListener.isListenerEnabled())
            {
                connectionListener.connectionClosed
                (
                    new TCPConnectionEvent
                    (
                        this,
                        socket.getInetAddress().toString(),
                        socket.getPort(),
                        socket.getLocalPort(),
                        getLocalhost()
                    )
                );
            }
        }
    }

    /**
     * Check if this client should use SSL when connecting to
     * the server described by the 'profile'.  The result
     * is stored in the private fields use_ssl and ssl_port.
     */
    private void checkSSL()
    {
        // Check if SSL profile
        //if (((IIOPProfile)profile).getSSL () == null)
        //{
        //    return;
        //}

        CompoundSecMechList sas;
        try
        {
            sas = (CompoundSecMechList)((IIOPProfile)profile).getComponent
                                           (TAG_CSI_SEC_MECH_LIST.value,
                                            CompoundSecMechListHelper.class);
        }
        catch (Exception ex)
        {
            logger.info("Not able to process security mech. component");
            return;
        }

        TLS_SEC_TRANS tls = null;
        if (sas != null && sas.mechanism_list[0].transport_mech.tag == TAG_TLS_SEC_TRANS.value)
        {
            try
            {
                byte[] tagData = sas.mechanism_list[0].transport_mech.component_data;
                final CDRInputStream in = new CDRInputStream( (org.omg.CORBA.ORB)null, tagData );
                try
                {
                    in.openEncapsulatedArray();
                    tls = TLS_SEC_TRANSHelper.read( in );
                }
                finally
                {
                    in.close();
                }
            }
            catch ( Exception e )
            {
                logger.warn("Error parsing TLS_SEC_TRANS: "+e);
            }
        }

        SSL ssl = (SSL)((IIOPProfile)profile).getComponent
                                           (TAG_SSL_SEC_TRANS.value,
                                            SSLHelper.class);
        //if( sas != null &&
        //    ssl != null )
        //{
        //    ssl.target_requires |= sas.mechanism_list[0].target_requires;
        //}

        // SSL usage is decided the following way: At least one side
        // must require it. Therefore, we first check if it is
        // supported by both sides, and then if it is required by at
        // least one side. The distinction between
        // EstablishTrustInTarget and EstablishTrustInClient is
        // handled at the socket factory layer.

        //the following is used as a bit mask to check, if any of
        //these options are set
        int minimum_options =
            Integrity.value |
            Confidentiality.value |
            DetectReplay.value |
            DetectMisordering.value |
            EstablishTrustInTarget.value |
            EstablishTrustInClient.value;

        int client_required = 0;
        int client_supported = 0;

        //only read in the properties if ssl is really supported.
        if( doSupportSSL )
        {
            client_required =
                configuration.getAttributeAsInteger("jacorb.security.ssl.client.required_options", 16);
            client_supported =
                configuration.getAttributeAsInteger("jacorb.security.ssl.client.supported_options",16);
        }

        if( tls != null && // server knows about ssl...
            ((tls.target_supports & minimum_options) != 0) && //...and "really" supports it
            doSupportSSL && //client knows about ssl...
            ((client_supported & minimum_options) != 0 )&& //...and "really" supports it
            ( ((tls.target_requires & minimum_options) != 0) || //server ...
              ((client_required & minimum_options) != 0))) //...or client require it
        {
            use_ssl  = true;
            ssl_port = tls.addresses[0].port;
            if (ssl_port < 0)
            {
                ssl_port += 65536;
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting TLS for connection");
            }
        }
        else if( ssl != null && // server knows about ssl...
            ((ssl.target_supports & minimum_options) != 0) && //...and "really" supports it
            doSupportSSL && //client knows about ssl...
            ((client_supported & minimum_options) != 0 )&& //...and "really" supports it
            ( ((ssl.target_requires & minimum_options) != 0) || //server ...
              ((client_required & minimum_options) != 0))) //...or client require it
        {
            use_ssl  = true;
            ssl_port = ssl.port;
            if (ssl_port < 0)
            {
                ssl_port += 65536;
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting SSL for connection");
            }
        }
        //prevent client policy violation, i.e. opening plain TCP
        //connections when SSL is required
        else if( // server doesn't know ssl...
                 doSupportSSL && //client knows about ssl...
                 ((client_required & minimum_options) != 0)) //...and requires it
        {
            throw new org.omg.CORBA.NO_PERMISSION( "Client-side policy requires SSL/TLS, but server doesn't support it" );
        }
        else
        {
            use_ssl = false;
            ssl_port = -1;
        }
    }

    private SocketFactory getSocketFactory()
    {
        return transportManager.getSocketFactoryManager().getSocketFactory();
    }

    private SocketFactory getSSLSocketFactory()
    {
        return transportManager.getSocketFactoryManager().getSSLSocketFactory();
    }
}
