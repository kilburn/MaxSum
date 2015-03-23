/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2015 Marc Pujol <mpujol@iiia.csic.es>.
 *
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 *
 *   Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
 *   nor the names of its contributors may be used to
 *   endorse or promote products derived from this
 *   software without specific prior written permission of
 *   IIIA-CSIC, Artificial Intelligence Research Institute
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package es.csic.iiia.maxsum.core.factors;

import es.csic.iiia.maxsum.core.StringIdentity;
import es.csic.iiia.maxsum.core.Variable;
import es.csic.iiia.maxsum.core.VariableAssignment;
import es.csic.iiia.maxsum.core.op.Semiring;
import gnu.trove.iterator.TLongIterator;
import org.junit.*;

import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.Assert.*;

/**
 *
 */
@Ignore
public abstract class FactorTest {

    protected Factor instance;
    private Variable[] variables;
    protected Semiring semiring;

    protected Variable a,b,c,d;
    protected Factor f1, fda, fdc, fa, fb;

    public abstract Semiring buildFactory();

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        semiring = buildFactory();

        variables = new Variable[] {
            new Variable(new StringIdentity("a"), 3),
            new Variable(new StringIdentity("b"), 3),
            new Variable(new StringIdentity("c"), 3)
        };
        instance = semiring.buildFactor(variables, 0);

        a = new Variable(new StringIdentity("a"), 2);
        b = new Variable(new StringIdentity("b"), 2);
        c = new Variable(new StringIdentity("c"), 3);
        d = new Variable(new StringIdentity("d"), 2);

        f1 = semiring.buildFactor(new Variable[]{a, b, c}, 0);
        f1.setValues(new double[]{
            0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });

        fda = semiring.buildFactor(new Variable[]{d, a}, 0);
        fda.setValues(new double[]{0.1, 0.4, 0.3, 0.2});

        fdc = semiring.buildFactor(new Variable[]{d, c}, 0);
        fdc.setValues(new double[]{0.1, 0.15, 0.25, 0.15, 0.3, 0.15});

        fa = semiring.buildFactor(new Variable[]{a}, 0);
        fa.setValues(new double[]{0.3, 0.7});

        fb = semiring.buildFactor(new Variable[]{b}, 0);
        fb.setValues(new double[]{0.6, 0.4});
    }

    @After
    public void tearDown() {
    }

    /**
     * Test equality, of class Factor.
     */
    @Test
    public void testEquals() {
        double[] values = {
            000d, 001d, 002d,
            010d, 011d, 012d,
            020d, 021d, 022d,
            100d, 101d, 102d,
            110d, 111d, 112d,
            120d, 121d, 122d,
            200d, 201d, 202d,
            210d, 211d, 212d,
            220d, 221d, 222d,
        };
        instance.setValues(values);

        Factor instance2 = semiring.buildFactor(new Variable[]{
                variables[0],
                variables[2],
                variables[1],
        }, 0);
        assertFalse(instance.equals(instance2));


        double[] values2 = {
            000d, 010d, 020d,
            001d, 011d, 021d,
            002d, 012d, 022d,
            100d, 110d, 120d,
            101d, 111d, 121d,
            102d, 112d, 122d,
            200d, 210d, 220d,
            201d, 211d, 221d,
            202d, 212d, 222d,
        };
        instance2.setValues(values2);
        assertEquals(instance, instance2);

        instance.setValue(new int[]{0,0,0}, 0.5d);
        assertFalse(instance.equals(instance2));
    }

    /**
     * Test of copy constructor, of class Factor.
     */
    @Test
    public void testCopyConstructor() {
        Factor f = semiring.buildFactor(f1);

        assertNotSame(f, f1);
        assertEquals(f, f1);
        assertNotSame(f.getVariableSet(), f1.getVariableSet());
    }


    /**
     * Test of getIndex method, of class Factor.
     */
    @Test
    public void testGetIndex1() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 0);
        map.put(variables[1], 0);
        map.put(variables[2], 0);
        assertEquals(0, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class Factor.
     */
    @Test
    public void testGetIndex2() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(15, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class Factor.
     */
    @Test
    public void testGetIndex3() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        assertEquals(26, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class Factor.
     */
    @Test
    public void testGetIndex4() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        // Non-functor variables in the mapping should be ignored
        map.put(new Variable(new StringIdentity("a"), 20), 2);
        map.put(new Variable(new StringIdentity("d"), 3), 2);
        assertEquals(26, instance.getIndex(map));
    }

    /**
     * Test of getIndex method, of class Factor.
     */
    @Test
    public void testGetIndex5() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        // Double-assignment to the mapping should just overwrite the previous
        // value
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(15, instance.getIndex(map));
    }

    /**
     * Test of getMapping method, of class Factor.
     */
    @Test
    public void testGetMapping1() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 0);
        map.put(variables[1], 0);
        map.put(variables[2], 0);
        assertEquals(map, instance.getMapping(0, null));
    }

    /**
     * Test of getMapping method, of class Factor.
     */
    @Test
    public void testGetMapping2() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 1);
        map.put(variables[1], 2);
        map.put(variables[2], 0);
        assertEquals(map, instance.getMapping(15, null));
    }

    /**
     * Test of getMapping method, of class Factor.
     */
    @Test
    public void testGetMapping3() {
        VariableAssignment map = new VariableAssignment();
        map.put(variables[0], 2);
        map.put(variables[1], 2);
        map.put(variables[2], 2);
        assertEquals(map, instance.getMapping(26, null));
    }

    /**
     * Test of getMapping method, of class Factor.
     */
    @Test
    public void testGetMapping4() {
        Variable[] vars = new Variable[]{
            new Variable(new StringIdentity("a"), 2),
            new Variable(new StringIdentity("b"), 2),
            new Variable(new StringIdentity("c"), 3),
        };
        Factor f = semiring.buildFactor(vars, 0);
        VariableAssignment map = new VariableAssignment();
        map.put(vars[0], 0);
        map.put(vars[1], 1);
        map.put(vars[2], 1);
        assertEquals(map, f.getMapping(4, null));
    }

    /**
     * Test of setValue method, of class Factor.
     */
    @Test
    public void testSetValue1() {
        int[] sub = {1, 2, 0};
        double value = 5.2d;
        instance.setValue(sub, value);
        assertEquals(instance.getValue(sub), value, 0);
    }

    /**
     * Test of setValue method, of class Factor.
     */
    @Test
    public void testSetValue2() {
        int[] sub = {2, 2, 2};
        int index = 26;
        double value = 5.2d;
        instance.setValue(sub, value);
        assertEquals(instance.getValue(sub), value, 0);
        assertEquals(instance.getValue(index), value, 0);
    }

    /**
     * Test of setValue method, of class Factor.
     */
    @Test
    public void testSetValue3() {
        int[] sub = {1, 2, 0};
        int index = 15;
        double value = 5.2d;
        instance.setValue(sub, value);
        assertEquals(instance.getValue(sub), value, 0);
        assertEquals(instance.getValue(index), value, 0);
    }

    /**
     * Test of setValue method, of class Factor.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testSetValue4() {
        int[] index = {2, 2};
        double value = 5.2d;
        instance.setValue(index, value);
        fail("Value set for an invalid index!");
    }

    /**
     * Test of setValue method, of class Factor.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testSetValue5() {
        int[] index = {2, 4, 2};
        double value = 5.2d;
        instance.setValue(index, value);
        fail("Value set for an invalid index!");
    }

    /**
     * Test of setValues method, of class Factor.
     */
    @Test
    public void testSetValues1() {
        double[] values = {
            000d, 001d, 002d,
            010d, 011d, 012d,
            020d, 021d, 022d,
            100d, 101d, 102d,
            110d, 111d, 112d,
            120d, 121d, 122d,
            200d, 201d, 202d,
            210d, 211d, 212d,
            220d, 221d, 222d,
        };
        instance.setValues(values);
    }

    /**
     * Test of setValues method, of class Factor.
     */
    @Test(expected=IllegalArgumentException.class)
    public void testSetValues2() {
        double[] values = {0};
        instance.setValues(values);
        fail("Correctly set an invalid number of values!");
    }

    /**
     * Test of iterator method, of class Factor.
     */
    @Test(expected=NoSuchElementException.class)
    public void testIterator() {
        final double ng = semiring.getSummarizeOperator().getNoGood();

        // Testing missing initial, existing end
        f1.setValue(0, ng);
        f1.setValue(1, ng);
        int len = (int) (f1.getSize() - 2);
        long offset = 2;
        long[] idxs1 = new long[len];
        long[] idxs2 = new long[len];
        TLongIterator it = f1.iterator(ng);
        for (int i = 0; i < len; i++) {
            assertTrue(it.hasNext());
            idxs1[i] = i + offset;
            idxs2[i] = it.next();
        }
        Arrays.sort(idxs1);
        Arrays.sort(idxs2);
        for (int i = 0; i < len; i++) {
            assertEquals(idxs1[i], idxs2[i]);
        }

        assertFalse(it.hasNext());
        it.next();
        fail("Calling next() should raise NoSuchElementException at this point.");
    }

    /**
     * Test of iterator method, of class Factor.
     */
    @Test(expected=NoSuchElementException.class)
    public void testIteratorExistingInitialMissingEnd() {
        final double ng = semiring.getSummarizeOperator().getNoGood();

        // Testing existing initial, missing end
        f1.setValue(0, 0);
        f1.setValue(1, ng);
        f1.setValue(f1.getSize()-1, ng);
        int len = (int) (f1.getSize() - 2);
        int offset = 1;

        long[] idxs1  = new long[len];
        long[] idxs2  = new long[len];
        TLongIterator it = f1.iterator(ng);
        idxs1[0] = 0;
        idxs2[0] = it.next();
        for (int i=1; i<len; i++) {
            assertTrue(it.hasNext());
            idxs1[i] = i+offset;
            idxs2[i] = it.next();
        }
        Arrays.sort(idxs1);
        Arrays.sort(idxs2);
        for (int i=1; i<len; i++) {
            assertEquals(idxs1[i], idxs2[i]);
        }

        assertFalse(it.hasNext());
        it.next();
        fail("Calling next() should raise NoSuchElementException at this point.");
    }

}
