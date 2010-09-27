/***** Copyright (c) 1999 Object Management Group. Unlimited rights to 
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

package org.omg.CORBA;

public class SetOverrideType implements org.omg.CORBA.portable.IDLEntity {

    public static final int _SET_OVERRIDE = 0;
    public static final SetOverrideType SET_OVERRIDE = 
        new SetOverrideType(_SET_OVERRIDE);

    public static final int _ADD_OVERRIDE = 1;
    public static final SetOverrideType ADD_OVERRIDE = 
        new SetOverrideType(_ADD_OVERRIDE);

    public int value() {
        return _value;
    }

    public static SetOverrideType from_int(int val) 
            throws org.omg.CORBA.BAD_PARAM {
        switch (val) {
        case _SET_OVERRIDE:
            return SET_OVERRIDE;
        case _ADD_OVERRIDE:
            return ADD_OVERRIDE;
        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }

    protected SetOverrideType(int _value) {
        this._value = _value;
    }

    private int _value;

}
