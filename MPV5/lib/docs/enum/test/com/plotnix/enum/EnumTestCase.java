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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.*;

/**
 *
 * @author Dmitri Plotnikov
 * @version $Revision$ $Date$
 */
public class EnumTestCase extends TestCase {
    /**
     * Exercises this test case only
     */
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }


    // ---------------------------------------------------------- Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public EnumTestCase(String name)
    {
        super(name);
    }


    // -------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp()
    {
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite()
    {
        return (new TestSuite(EnumTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown()
    {
    }


    // ------------------------------------------------ Individual Test Methods

    /**
     */
    public void testEnum() throws Exception {
        Color blue = Color.BLUE;
        assertEquals("blue.stringValue", "BLUE", blue.stringValue());
        assertEquals("blue.intValue", 2, blue.intValue());
        assertEquals("blue.toString", "Blue", blue.toString());

        Enum array[] = Enum.enum(Color.class);
        assertNotNull("enum(class) != null", array);
        assertEquals("enum(class).length", 3, array.length);
        assertEquals("enum(class)[0]", Color.RED, array[0]);
        assertEquals("enum(class)[1]", Color.GREEN, array[1]);
        assertEquals("enum(class)[2]", Color.BLUE, array[2]);

        Color sameColor = (Color)Enum.enum(Color.class, "BLUE");
        assertEquals("enum(class, id)", blue, sameColor);

        ComparisonResult less = ComparisonResult.LESS_THAN;
        assertEquals("less.stringValue", "LESS_THAN", less.stringValue());
        assertEquals("less.intValue", -1, less.intValue());

        ComparisonResult sameResult = (ComparisonResult)Enum.enum(ComparisonResult.class, -1);
        assert("enum(class, int)", sameResult == less);

        Flower rose = Flower.ROSE;
        assertEquals("rose.stringValue", "rose", rose.stringValue());
        assertEquals("rose.intValue", 0, rose.intValue());
        assertEquals("rose.toString", "Rosa", rose.toString());
        assertEquals("rose.toString(italian)", "una rosa", rose.toString(Locale.ITALY));
        assertEquals("daisy.toString(italian)", "una margherita", Flower.enum("daisy").toString(Locale.ITALY));

        assertEquals("EMPLOYED.stringValue", "EMPLOYED", Enum.enum(Employment.class, Employment.EMPLOYED).stringValue());
        assertEquals("EMPLOYED.intValue", Employment.EMPLOYED, Enum.enum(Employment.class, Employment.EMPLOYED).intValue());

        Gender male = Gender.MALE;
        assertEquals("male.stringValue", "M", male.stringValue());
        assertEquals("mail.intValue", 1, male.intValue());
        assertEquals("mail.toString", "Male", male.toString());

        Color newBlue = (Color)reserialize(blue);
        assert("Reserialization", newBlue == blue);
    }

    private Object reserialize(Object object) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);

        byte[] array = baos.toByteArray();

        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(array);

        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }
}