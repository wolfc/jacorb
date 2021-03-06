package org.omg.CORBA;

/**
 * orbos/98-03-10
 */
abstract public class ServerRequest {

	/** 
	 * @deprecated use operation() 
	 */
	public java.lang.String op_name() 
	{     
		return operation();
	}   

	public String operation() 
	{     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}   

	abstract public org.omg.CORBA.Context ctx();

	/**  
	 * @deprecated use arguments()  
	 */
	public void params(org.omg.CORBA.NVList params) 
	{
		arguments(params);
	}

	public void arguments(org.omg.CORBA.NVList params) 
	{
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	/**
	 * @deprecated use set_result()    
	 */   
	public void result(org.omg.CORBA.Any result) 
	{     
		set_result(result);
	}

	public void set_result(org.omg.CORBA.Any result) 
	{     
		throw new org.omg.CORBA.NO_IMPLEMENT();    
	}

	/**
	 * @deprecated use set_exception()
	 */
	public void except(org.omg.CORBA.Any except) 
	{     
		set_exception(except);
	}   

	public void set_exception(org.omg.CORBA.Any except) 
	{     
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}    
} 


