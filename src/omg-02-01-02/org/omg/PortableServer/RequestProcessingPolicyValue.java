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

public class RequestProcessingPolicyValue implements org.omg.CORBA.portable.IDLEntity {

    public static final int _USE_ACTIVE_OBJECT_MAP_ONLY = 0;
    public static final RequestProcessingPolicyValue USE_ACTIVE_OBJECT_MAP_ONLY = 
        new RequestProcessingPolicyValue(_USE_ACTIVE_OBJECT_MAP_ONLY);

    public static final int _USE_DEFAULT_SERVANT = 1;
    public static final RequestProcessingPolicyValue USE_DEFAULT_SERVANT = 
        new RequestProcessingPolicyValue(_USE_DEFAULT_SERVANT);

    public static final int _USE_SERVANT_MANAGER = 2;
    public static final RequestProcessingPolicyValue USE_SERVANT_MANAGER = 
        new RequestProcessingPolicyValue(_USE_SERVANT_MANAGER);
    public int value() {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

    public static RequestProcessingPolicyValue from_int(int val) 
                       /* Issue 3669 throws org.omg.CORBA.BAD_PARAM */ {
    switch (val) {
    case _USE_ACTIVE_OBJECT_MAP_ONLY:
        return USE_ACTIVE_OBJECT_MAP_ONLY;
    case _USE_DEFAULT_SERVANT:
        return USE_DEFAULT_SERVANT;
    case _USE_SERVANT_MANAGER:
        return USE_SERVANT_MANAGER;
    default:
        throw new org.omg.CORBA.BAD_PARAM();
    }
    }

    protected RequestProcessingPolicyValue(int _value) {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }

}
