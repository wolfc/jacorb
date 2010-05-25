/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2004 Gerald Brose
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
 *
 */

package org.jacorb.notification.impl;

import org.jacorb.notification.filter.CurrentTimeUtil;
import org.jacorb.notification.filter.ETCLEvaluator;
import org.jacorb.notification.filter.EvaluationContext;
import org.jacorb.notification.interfaces.EvaluationContextFactory;

/**
 * @author Alphonse Bendt
 * @version $Id: DefaultEvaluationContextFactory.java,v 1.2 2005-10-08 11:06:12 alphonse.bendt Exp $
 */
public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
    private final ETCLEvaluator evaluator_;
    private final CurrentTimeUtil currentTimeUtil_;
    
    public DefaultEvaluationContextFactory(ETCLEvaluator evaluator, CurrentTimeUtil currentTimeUtil) {
        evaluator_ = evaluator;
        currentTimeUtil_ = currentTimeUtil;
    }
    
    public DefaultEvaluationContextFactory(ETCLEvaluator evaluator)
    {
        this (evaluator, CurrentTimeUtil.LOCAL_TIME);
    }
    
    public EvaluationContext newEvaluationContext() {
        return new EvaluationContext(evaluator_, currentTimeUtil_);
    }
}