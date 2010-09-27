/***** Copyright (c) 1999-2000 Object Management Group. Unlimited rights to 
       duplicate and use this code are hereby granted provided that this 
       copyright notice is included.
*****/

/***** This class is generated by an IDL compiler and is ORB-vendor specific. 
       A "dummy" implementation is provided so that the "official" org.omg.*
       packages may be compiled.  In order to actually use a Java ORB,
       the ORB vendor's implementation will provide a "real"
       implementation of the class.

       In order to be conformant the class shall support the signatures
       specified here, but will have an orb-specific implementation.

       The class may support additional vendor specific functionality.
       It shall have at least the inheritance relationships specified
       here. Any additional (vendor specific) inheritance relationships may 
       only be with other classes and interfaces that are guaranteed to be 
       present in the JDK core.
*****/

package org.omg.PortableServer;

public class ThreadPolicyValue implements org.omg.CORBA.portable.IDLEntity {

    public static final int _ORB_CTRL_MODEL = 0;
    public static final ThreadPolicyValue ORB_CTRL_MODEL = 
        new ThreadPolicyValue(_ORB_CTRL_MODEL);

    public static final int _SINGLE_THREAD_MODEL = 1;
    public static final ThreadPolicyValue SINGLE_THREAD_MODEL = 
        new ThreadPolicyValue(_SINGLE_THREAD_MODEL);

    public int value() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static ThreadPolicyValue from_int(int val) 
                       /* Issue 3669 throws org.omg.CORBA.BAD_PARAM */ {
    switch (val) {
    case _ORB_CTRL_MODEL:
        return ORB_CTRL_MODEL;
    case _SINGLE_THREAD_MODEL:
        return SINGLE_THREAD_MODEL;
    default:
        throw new org.omg.CORBA.BAD_PARAM();
    }
    }

    protected ThreadPolicyValue(int _value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
