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
import org.junit.Ignore;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 *
 */
@Ignore
public abstract class AbstractFactorTest extends FactorTest {

    private AbstractFactor _instance;

    @Override
    public void setUp() {
        super.setUp();
        if (instance instanceof AbstractFactor) {
            _instance = (AbstractFactor) instance;
        } else {
            throw new RuntimeException("Invalid CostFunction factor!");
        }
    }

    /**
     * Test of subindexToIndex method, of class CostFunction.
     */
    @Test
    public void testSubindexToIndex1() {
        int[] sub = {0, 0, 0};
        int index = 0;
        assertEquals(_instance.subindexToIndex(sub), index);
    }

    /**
     * Test of subindexToIndex method, of class CostFunction.
     */
    @Test
    public void testSubindexToIndex2() {
        int[] sub = {1, 2, 0};
        int index = 15;
        assertEquals(_instance.subindexToIndex(sub), index);
    }

    /**
     * Test of subindexToIndex method, of class CostFunction.
     */
    @Test
    public void testSubindexToIndex3() {
        int[] sub = {2, 2, 2};
        int index = 26;
        assertEquals(_instance.subindexToIndex(sub), index);
    }

    /**
     * Test of indexToSubindex method, of class CostFunction.
     */
    @Test
    public void testindexToSubindex1() {
        int[] sub = {0, 0, 0};
        int index = 0;
        assertArrayEquals(_instance.indexToSubindex(index), sub);
    }

    /**
     * Test of subindexToIndex method, of class CostFunction.
     */
    @Test
    public void testindexToSubindex2() {
        int[] sub = {1, 2, 0};
        int index = 15;
        assertArrayEquals(_instance.indexToSubindex(index), sub);
    }

    /**
     * Test of subindexToIndex method, of class CostFunction.
     */
    @Test
    public void testindexToSubindex3() {
        int[] sub = {2, 2, 2};
        int index = 26;
        assertArrayEquals(_instance.indexToSubindex(index), sub);
    }

    /**
     * Test of subindexToIndex method, of class CostFunction.
     */
    @Test
    public void testindexToSubindex4() {
        final double nv = semiring.getCombineOperator().getNeutralValue();
        AbstractFactor f = (AbstractFactor)
                semiring.buildFactor(new Variable[]{
                        new Variable(new StringIdentity("a"), 2),
                        new Variable(new StringIdentity("b"), 2),
                        new Variable(new StringIdentity("c"), 3),
                }, nv);
        int[] sub = {0, 1, 1};
        int index = 4;
        assertArrayEquals(f.indexToSubindex(index), sub);
    }

}