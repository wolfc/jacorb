package org.jacorb.poa;

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

import java.util.Vector;
import org.jacorb.config.*;
import org.slf4j.Logger;
import org.jacorb.poa.except.POAInternalError;

/**
 * This class provides and manages a pool of ready started threads for
 * request processing.
 *
 * @author Gerald Brose
 * @author Reimo Tiedemann
 * @version $Id: RPPoolManager.java,v 1.25 2009-05-03 21:36:17 andre.spiegel Exp $
 * @see org.jacorb.poa.RequestProcessor
 */

public abstract class RPPoolManager
{
    private RPPoolManagerListener pmListener;

    // the current for (un)registering the invocation contexts
    private final Current current;
    /**
     * <code>pool</code> is the set of currently available (inactive) request processors
     */
    private Vector pool;
    /**
     * <code>activeProcessors</code> is the set of currently active processors
     */
    private Vector activeProcessors;
    /**
     * <code>unused_size</code> represents the current number of unused request processors
     * in the pool.
     */
    private int unused_size;
    /**
     * <code>max_pool_size</code> is the maximum size of the pool.
     */
    private final int max_pool_size;
    /**
     * <code>min_pool_size</code> is the minimum number of request processors.
     */
    private final int min_pool_size;
    // a flag for delay the pool initialization
    private boolean inUse = false;

    private final Configuration configuration;
    private final Logger logger;

    protected RPPoolManager(Current _current, int min, int max,
                            Logger _logger, Configuration _configuration)
    {
        current = _current;
        max_pool_size = max;
        min_pool_size = min;
        logger = _logger;
        configuration = _configuration;
    }

    private void addProcessor()
    {
        RequestProcessor rp = new RequestProcessor(this);
        try
        {
            rp.configure(this.configuration);
        }
        catch (ConfigurationException ex)
        {
            throw new RuntimeException (ex.toString());
        }
        current._addContext(rp, rp);
        rp.setDaemon(true);
        pool.addElement(rp);
        unused_size++;
        rp.start();
    }

    protected synchronized void addRPPoolManagerListener(RPPoolManagerListener listener)
    {
        pmListener = EventMulticaster.add(pmListener, listener);
    }

    /**
     * invoked by clients to indicate that they won't use this poolManager anymore.
     */
    abstract void destroy();

    /**
     * shutdown this poolManager. clients should invoke {@link #destroy()} instead.
     */
    protected synchronized void destroy(boolean really)
    {
        if (pool == null || inUse == false)
        {
            return;
        }

        // wait until all active processors complete
        while (!activeProcessors.isEmpty())
        {
            try
            {
                wait();
            }
            catch (InterruptedException ex)
            {
                // ignore
            }
        }

        RequestProcessor[] rps = new RequestProcessor[pool.size()];
        pool.copyInto(rps);
        for (int i=0; i<rps.length; i++)
        {
            if (rps[i].isActive())
            {
                throw new POAInternalError("error: request processor is active (RequestProcessorPM.destroy)");
            }

            pool.removeElement(rps[i]);
            unused_size--;
            current._removeContext(rps[i]);
            rps[i].end();
        }
        inUse = false;
    }

    /**
     * returns the number of unused processors contained in the pool
     */

    protected int getPoolCount()
    {
        return (pool == null) ? 0 : pool.size();
    }

    /**
     * returns the size of the processor pool (used and unused processors)
     */

    protected synchronized int getPoolSize()
    {
        return unused_size;
    }

    /**
     * returns a processor from pool, the first call causes
     * the initialization of the processor pool,
     * if no processor available the number of processors
     * will increased until the max_pool_size is reached,
     * this method blocks if no processor available and the
     * max_pool_size is reached until a processor will released
     */

    protected synchronized RequestProcessor getProcessor()
    {
        if (!inUse)
        {
            init();
            inUse = true;
        }

        if (pool.isEmpty() && unused_size < max_pool_size)
        {
            addProcessor();
        }

        while (pool.isEmpty())
        {
            warnPoolIsEmpty();

            try
            {
                wait();
            }
            catch (InterruptedException e)
            {
            }
        }
        RequestProcessor requestProcessor = (RequestProcessor) pool.remove( pool.size() - 1 );
        activeProcessors.add (requestProcessor);

        // notify a pool manager listener
        if (pmListener != null)
        {
            pmListener.processorRemovedFromPool(requestProcessor, pool.size(), unused_size);
        }

        return requestProcessor;
    }

    protected void warnPoolIsEmpty()
    {
        if (logger.isWarnEnabled())
        {
            logger.warn("Thread pool exhausted, consider increasing "
                      + "jacorb.poa.thread_pool_max (currently: "
                      + max_pool_size + ")");
        }
    }

    private void init()
    {
        pool = new Vector(max_pool_size);
        activeProcessors = new Vector(max_pool_size);
        for (int i = 0; i < min_pool_size; i++)
        {
            addProcessor();
        }
    }

    /**
     * gives a processor back into the pool if the number of
     * available processors is smaller than min_pool_size,
     * otherwise the processor will terminate
     */

    protected synchronized void releaseProcessor(RequestProcessor rp)
    {
        activeProcessors.remove (rp);

        if (pool.size() < min_pool_size)
        {
            pool.addElement(rp);
        }
        else
        {
            unused_size--;
            current._removeContext(rp);
            rp.end();
        }
        // notify a pool manager listener
        if (pmListener != null)
            pmListener.processorAddedToPool(rp, pool.size(), unused_size);

        // notify whoever is waiting for the release of active processors
        notifyAll();
    }

    protected synchronized void removeRPPoolManagerListener(RPPoolManagerListener listener)
    {
        pmListener = EventMulticaster.remove(pmListener, listener);
    }
}
