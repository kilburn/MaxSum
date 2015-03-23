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

package es.csic.iiia.maxsum.core.op;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class CombineTest {
    private static final double EPSILON = 1e-5;
    private static final double inf = Double.POSITIVE_INFINITY;
    private static final double ninf = Double.NEGATIVE_INFINITY;
    private static final double nan = Double.NaN;

    private Combine product = Combine.PRODUCT;
    private Combine sum = Combine.SUM;

    private static final double[][] EVAL_CASES = new double[][]{
            // v1, v2, sum(v1,v2), prod(v1,v2)
            new double[]{.5, .5, 1., .25},
            new double[]{0, 0, 0, 0},
            new double[]{1, 0, 1, 0},
            new double[]{inf, 2, inf, inf},
            new double[]{inf, 0, inf, nan},
            new double[]{inf, ninf, nan, ninf},
            new double[]{ninf, 2, ninf, ninf},
            new double[]{ninf, 0, ninf, nan},
    };

    private static final double[][] NEGATION_CASES = new double[][]{
            // v1, sum.neg(v1), sum.inv(v1), prod.neg(v1)
            new double[]{.5, -.5, -.5, 2},
            new double[]{0d, 0, 0, inf},
            new double[]{-0d, 0, 0, -inf},
            new double[]{1, -1, -1, 1},
            new double[]{inf, inf, ninf, 0},
            new double[]{ninf, ninf, inf, 0},
    };

    @Test
    public void testEval() throws Exception {
        for (double[] test : EVAL_CASES) {
            assertEquals(test[2], sum.eval(test[0], test[1]), EPSILON);
            assertEquals(test[2], sum.eval(test[1], test[0]), EPSILON);
            assertEquals(test[3], product.eval(test[0], test[1]), EPSILON);
            assertEquals(test[3], product.eval(test[1], test[0]), EPSILON);
        }
    }

    @Test
    public void testGetNeutralValue() throws Exception {
        assertEquals(0, sum.getNeutralValue(), EPSILON);
        assertEquals(1, product.getNeutralValue(), EPSILON);

        for (double[] test : EVAL_CASES) {
            for (double value : test) {
                assertEquals(value, sum.eval(value, sum.getNeutralValue()), EPSILON);
                assertEquals(value, product.eval(value, product.getNeutralValue()), EPSILON);
            }
        }
    }

    @Test
    public void testNegate() throws Exception {
        testNegate(sum, 1);
        testNegate(product, 3);
    }

    private void testNegate(Combine op, int idx) {
        double[] expected = new double[NEGATION_CASES.length];
        double[] result = new double[NEGATION_CASES.length];
        for (int i=0; i<NEGATION_CASES.length; i++) {
            expected[i] = NEGATION_CASES[i][idx];
            result[i] = op.negate(NEGATION_CASES[i][0]);
        }

        assertArrayEquals(expected, result, EPSILON);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testInvert() throws Exception {
        double[] expected = new double[NEGATION_CASES.length];
        double[] result = new double[NEGATION_CASES.length];
        for (int i=0; i<NEGATION_CASES.length; i++) {
            expected[i] = NEGATION_CASES[i][2];
            result[i] = sum.invert(NEGATION_CASES[i][0]);
        }

        assertArrayEquals(expected, result, EPSILON);
        product.invert(0d);
    }
}