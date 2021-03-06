package org.jacorb.test.poa;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
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

import junit.framework.*;

public class AllTest 
    extends TestCase
{
   public AllTest(String name)
   {
      super(name);
   }

   public static Test suite ()
   {
        TestSuite suite = new TestSuite("All POA Tests");

        suite.addTest(new TestSuite(POAUtilTest.class));
        suite.addTest(org.jacorb.test.poa.ByteArrayKeyTest.suite());
        suite.addTest(org.jacorb.test.poa.Deactivate.suite());
        suite.addTest(org.jacorb.test.poa.QueueNoWaitTest.suite());
        suite.addTest(org.jacorb.test.poa.QueueWaitTest.suite());
        suite.addTest(new TestSuite(POAActivateTest.class));
        suite.addTestSuite(ImplNameTest.class);
        suite.addTest(BiDirDelegateTest.suite());
        
        return suite;
   }
}
