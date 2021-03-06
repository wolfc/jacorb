package demo.interceptors;

import org.omg.PortableInterceptor.*;
import org.omg.CosNaming.*;

/**
 * This class registers the ClientForwardInterceptor 
 * with the ORB.
 *
 * @author Nicolas Noffke
 * @version 
 */

public class ClientInitializer 
    extends org.omg.CORBA.LocalObject 
    implements ORBInitializer
{

    public ClientInitializer() {
    }

    /**
     * This method resolves the NameService and registers the 
     * interceptor.
     */

    public void post_init(ORBInitInfo info) 
    {
        try
        {
            NamingContextExt nc = NamingContextExtHelper.narrow
                (info.resolve_initial_references("NameService"));

            info.add_client_request_interceptor
                (new ClientForwardInterceptor(nc));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void pre_init(ORBInitInfo info) {    
    }
} // ClientInitializer
