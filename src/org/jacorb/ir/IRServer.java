package org.jacorb.ir;

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


/**
 * The main server that starts the Interface Repository
 *
 * @author Gerald Brose
 * @version $Id: IRServer.java,v 1.12 2008-11-25 17:20:50 nick.cross Exp $
 */

public class IRServer
{
    /**
     * @param  args a vector  of commandline arguments,  where args[1]
     * needs to be a filename string and args[0] a classpath string
     */
    public static void main( String args[] )
    {
        System.setProperty ("jacorb.implname", "InterfaceRepository");

        if( args.length != 2)
        {
            System.err.println("Usage: java org.jacorb.ir.IRServer <classpath> <IOR filename>");
            System.exit(1);
        }

        try
        {
            java.util.StringTokenizer strtok =
                new java.util.StringTokenizer( args[0], java.io.File.pathSeparator );

            java.net.URL [] urls = new java.net.URL[strtok.countTokens()];
            for( int i = 0; strtok.hasMoreTokens(); i++ )
            {
                urls[i] = new java.io.File( strtok.nextToken() ).toURL();
            }

            java.net.URLClassLoader classLoader = new java.net.URLClassLoader( urls );

            Class repositoryClass = classLoader.loadClass("org.jacorb.ir.RepositoryImpl");

            Object repository =
                repositoryClass.getConstructors()[0].newInstance(
                                     new Object[]{ args[0] ,
                                                   args[1],
                                                   classLoader });

            repositoryClass.getDeclaredMethod("loadContents", (Class[]) null ).invoke( repository, (Object[]) null );

            Object lock = new Object();
            synchronized( lock )
            {
                lock.wait();
            }

        }
        catch( Exception e )
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
