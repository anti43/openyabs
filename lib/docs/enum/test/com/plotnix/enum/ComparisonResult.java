/*
 * $Header$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * The PLOTNIX Software License, Version 1.0
 *
 *
 * Copyright (c) 2001 The PLOTNIX Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        PLOTNIX, Inc (http://www.plotnix.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "PLOTNIX" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission. For written permission, please contact dmitri@plotnix.com.
 *
 * 5. Products derived from this software may not be called "PLOTNIX",
 *    nor may "PLOTNIX" appear in their name, without prior written
 *    permission of the PLOTNIX, Inc.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * For more information on PLOTNIX, Inc, please see <http://www.plotnix.com/>.
 */
package com.plotnix.enum;

import java.util.*;

/**
 * General purpose test bean for JUnit tests for the "jpath" component.
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public class ComparisonResult extends Enum {

    public static ComparisonResult LESS_THAN = new ComparisonResult(-1);
    public static ComparisonResult EQUAL_TO = new ComparisonResult(0);
    public static ComparisonResult GREATER_THAN = new ComparisonResult(1);

    protected ComparisonResult(int integer){
        super(integer);
    }
}
