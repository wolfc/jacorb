package org.jacorb.test.common;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2002  Gerald Brose.
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

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import java.io.*;

/**
 * This is a version of the TestServer class that works for JacORB prior
 * to version 2.2.
 * <p>
 * @author Andre Spiegel <spiegel@gnu.org>
 * @version $Id: TestServer_before_2_2.java,v 1.1 2005-05-13 13:07:57 andre.spiegel Exp $
 */
public class TestServer_before_2_2
{
    public static void main (String[] args)
    {
        try
        {
            //init ORB
            ORB orb = ORB.init( args, null );

            //init POA
            POA poa =
                POAHelper.narrow( orb.resolve_initial_references( "RootPOA" ));
            poa.the_POAManager().activate();

            String className = args[0];
            Class servantClass = Class.forName (className);
            Servant servant = ( Servant ) servantClass.newInstance();

            // create the object reference
            org.omg.CORBA.Object obj = poa.servant_to_reference( servant );

            System.out.println ("SERVER IOR: " + orb.object_to_string(obj));
            System.out.flush();

            // wait for requests
            orb.run();
        }
        catch( Exception e )
        {
            System.err.println ("ERROR " + e);
        }
    }
}
