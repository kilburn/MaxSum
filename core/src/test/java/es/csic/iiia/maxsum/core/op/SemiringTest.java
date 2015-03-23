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

import es.csic.iiia.maxsum.core.StringIdentity;
import es.csic.iiia.maxsum.core.Variable;
import es.csic.iiia.maxsum.core.VariableAssignment;
import es.csic.iiia.maxsum.core.factors.Factor;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class SemiringTest {

    protected Factor factor;
    protected Semiring instance;

    protected Variable a,b,c,d;
    protected Factor f1, fda, fdc, fa, fb;

    @Before
    public void setUp() {
        instance = new Semiring();

        Variable[] variables = new Variable[]{
                new Variable(new StringIdentity("a"), 3),
                new Variable(new StringIdentity("b"), 3),
                new Variable(new StringIdentity("c"), 3)
        };
        factor = instance.buildFactor(variables, 0);

        a = new Variable(new StringIdentity("a"), 2);
        b = new Variable(new StringIdentity("b"), 2);
        c = new Variable(new StringIdentity("c"), 3);
        d = new Variable(new StringIdentity("d"), 2);

        f1 = instance.buildFactor(new Variable[]{a, b, c}, 0);
        f1.setValues(new double[]{
                0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });

        fda = instance.buildFactor(new Variable[]{d, a}, 0);
        fda.setValues(new double[]{0.1, 0.4, 0.3, 0.2});

        fdc = instance.buildFactor(new Variable[]{d, c}, 0);
        fdc.setValues(new double[]{0.1, 0.15, 0.25, 0.15, 0.3, 0.15});

        fa = instance.buildFactor(new Variable[]{a}, 0);
        fa.setValues(new double[]{0.3, 0.7});

        fb = instance.buildFactor(new Variable[]{b}, 0);
        fb.setValues(new double[]{0.6, 0.4});
    }


    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize1() {
        Variable[] vars = new Variable[]{a};
        instance.setSummarizeOperator(Summarize.SUM);
        Factor sum = instance.summarize(f1, vars);
        Factor res = instance.buildFactor(vars, 0);
        res.setValues(new double[]{0.63, 0.37});
        assertEquals(res, sum);
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize2() {
        Variable[] vars = new Variable[]{a};
        instance.setSummarizeOperator(Summarize.MAX);
        Factor sum = instance.summarize(f1, vars);
        Factor res = instance.buildFactor(vars, 0);
        res.setValues(new double[]{0.2, 0.2});
        assertEquals(res, sum);
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize3() {
        Variable[] vars = new Variable[]{a,c};
        instance.setSummarizeOperator(Summarize.SUM);
        Factor sum = instance.summarize(f1, vars);
        Factor res = instance.buildFactor(vars, 0);
        res.setValues(new double[]{0.3,0.25,0.08,0.2,0.1,0.07});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize4() {
        Variable[] vars = new Variable[]{a,c};
        instance.setSummarizeOperator(Summarize.MAX);
        Factor sum = instance.summarize(f1, vars);
        Factor res = instance.buildFactor(vars, 0);
        res.setValues(new double[]{0.2,0.2,0.05,0.2,0.1,0.04});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize5() {
        instance.setSummarizeOperator(Summarize.SUM);
        Factor sum = instance.summarize(f1, new Variable[]{c,a});
        Factor res = instance.summarize(f1, new Variable[]{a,c});
        assertEquals(sum, res);
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize6() {
        instance.setSummarizeOperator(Summarize.MAX);
        Factor sum = instance.summarize(f1, new Variable[]{c,a});
        Factor res = instance.summarize(f1, new Variable[]{a,c});
        assertEquals(res, sum);
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize8() {
        Variable[] vars = new Variable[]{a,b,c};
        instance.setSummarizeOperator(Summarize.SUM);
        Factor sum = instance.summarize(f1, vars);
        Factor res = instance.buildFactor(new Variable[]{a, b, c}, 0);
        res.setValues(new double[]{
                0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });
        assertEquals(res, sum);
    }

    /**
     * Test of summarize method, of class CostFunction.
     */
    @Test
    public void testSummarize11() {
        Variable[] vars = new Variable[]{};
        instance.setSummarizeOperator(Summarize.MIN);
        Factor sum = instance.summarize(fa, vars);
        Factor res = instance.buildFactor(new Variable[]{}, 0);
        res.setValues(new double[]{
                0.3
        });
        assertEquals(sum, res);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombine2() {
        instance.setMode(Summarize.SUM, Combine.SUM, Normalize.NONE);
        Factor sum = instance.summarize(f1, new Variable[]{a,c});
        Factor com = instance.combine(f1, sum);
        Factor res = instance.buildFactor(new Variable[]{a, b, c}, 0);
        res.setValues(new double[]{
                0.1+0.3, 0.2+.25, 0.05+.08, 0.2+0.3, 0.05+.25, 0.03+.08, 0+0.2,
                0+0.1, 0.03+.07, 0.2+0.2, 0.1+0.1, 0.04+0.07
        });
        assertEquals(com, res);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombine3() {
        instance.setCombineOperator(Combine.PRODUCT);
        Factor sf  = instance.buildFactor(new Variable[]{a, b, c, d}, 1);
        Factor com = instance.combine(sf, fda);
        Factor res = instance.buildFactor(new Variable[]{a, b, c, d}, 0);
        res.setValues(new double[]{
                0.1, 0.3, 0.1, 0.3, 0.1, 0.3,
                0.1, 0.3, 0.1, 0.3, 0.1, 0.3,
                0.4, 0.2, 0.4, 0.2, 0.4, 0.2,
                0.4, 0.2, 0.4, 0.2, 0.4, 0.2,
        });
        assertEquals(com, res);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombine4() {
        instance.setCombineOperator(Combine.SUM);
        Factor fa  = instance.buildFactor(new Variable[]{a}, 0);
        Factor fb  = instance.buildFactor(new Variable[]{b}, 0);
        Factor fab = instance.buildFactor(new Variable[]{a, b}, 0);
        fab.setValues(new double[]{1, 5, 3, 4});

        Factor res = instance.combine(fab, Arrays.asList(new Factor[]{fa, fb}));
        assertEquals(fab, res);
    }

    @Test
    public void testCombineNogoods() {
        instance.setSummarizeOperator(Summarize.MIN);
        final double ng = instance.getSummarizeOperator().getNoGood();

        Variable x = new Variable(new StringIdentity("x"), 2);
        Variable y = new Variable(new StringIdentity("y"), 2);
        Variable z = new Variable(new StringIdentity("z"), 2);

        Factor cf1 = instance.buildFactor(new Variable[]{x, y}, 0);
        cf1.setValues(new double[]{ng, ng, ng, ng});
        Factor cf2 = instance.buildFactor(new Variable[]{y, z}, 0);
        cf2.setValues(new double[]{0.3, -0.3, -0.88, -0.12});

        Factor comb = instance.combine(cf1, cf2);
        Factor res = instance.buildFactor(new Variable[]{x, y, z}, 0);
        res.setValues(new double[]{ng, ng, ng, ng, ng, ng, ng, ng});
        assertEquals(res, comb);

        comb = instance.combine(cf2, cf1);
        assertEquals(res, comb);
    }

    @Test
    public void testSummarizeNogoods() {
        instance.setSummarizeOperator(Summarize.MAX);
        final double ng = Summarize.MAX.getNoGood();
        Variable x = new Variable(new StringIdentity("x"), 2);
        Variable y = new Variable(new StringIdentity("y"), 2);
        Variable z = new Variable(new StringIdentity("z"), 2);
        Variable t = new Variable(new StringIdentity("t"), 2);
        Variable u = new Variable(new StringIdentity("u"), 2);
        Factor cf = instance.buildFactor(new Variable[]{x, y, z, t, u}, 0);
        cf.setValues(new double[]{
                ng, ng, ng, ng, ng, ng, ng, ng, ng, ng, ng, ng, ng, ng, ng, ng, 4.61, 4.61, 4.61,
                4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61, 4.61,
                4.61, 4.61
        });

        assertTrue(cf.getValue(0) == ng);
        Factor sum = instance.summarize(cf, new Variable[0]);
        Factor res = instance.buildFactor(new Variable[0], 4.61);
        assertEquals(res, sum);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineEmptyFunction1() {
        instance.setCombineOperator(Combine.PRODUCT);
        final double nv = instance.getCombineOperator().getNeutralValue();
        Factor sf  = instance.buildFactor(new Variable[]{}, nv);
        assertEquals(1, sf.getSize());
        assertEquals(sf.getValue(0), Combine.PRODUCT.getNeutralValue(), 0.0001);
        Factor com = instance.combine(sf, fda);
        assertEquals(fda, com);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineEmptyFunction2() {
        instance.setCombineOperator(Combine.PRODUCT);
        final double nv = instance.getCombineOperator().getNeutralValue();
        Factor sf  = instance.buildFactor(new Variable[]{}, nv);
        Factor com = instance.combine(fda, sf);
        assertEquals(fda, com);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineConstantFunction() {
        instance.setCombineOperator(Combine.SUM);
        Factor sf  = instance.buildFactor(new Variable[]{}, 0);
        sf.setValue(0, 0.3);
        Factor com = instance.combine(fa, sf);
        Factor res = instance.buildFactor(new Variable[]{a}, 0);
        res.setValues(new double[]{0.6, 1.0});
        assertEquals(res, com);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineConstantFunction2() {
        instance.setCombineOperator(Combine.SUM);
        Factor sf  = instance.buildFactor(new Variable[]{}, 0);
        sf.setValue(0, 0.3);
        Factor com = instance.combine(sf, fa);
        Factor res = instance.buildFactor(new Variable[]{a}, 0);
        res.setValues(new double[]{0.6, 1.0});
        assertEquals(res, com);
    }

    /**
     * Test of combine method, of class CostFunction.
     */
    @Test
    public void testCombineListSparse() {
        instance.setCombineOperator(Combine.SUM);
        final double ng = instance.getSummarizeOperator().getNoGood();

        ArrayList<Factor> fs = new ArrayList<>();
        Factor sf  = instance.buildFactor(new Variable[]{c}, ng);
        sf.setValues(new double[]{ng, ng, 1.0});
        fs.add(sf);
        fs.add(fdc);
        sf = instance.buildFactor(new Variable[]{a}, ng);
        sf.setValues(new double[]{ng, 1.0});

        Factor com = instance.combine(sf, fs);
        Factor res = instance.buildFactor(new Variable[]{c, a, d}, 0);
        res.setValues(new double[]{
                ng, ng, ng, ng,
                ng, ng, ng, ng,
                ng, ng, 2.25, 2.15
        });
        assertEquals(res, com);
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize1() {
        instance.setNormalizationType(Normalize.SUM1);
        fa = instance.normalize(fa);
        Factor res = instance.buildFactor(new Variable[]{a}, 0);
        res.setValues(new double[]{
                0.3, 0.7
        });
        assertEquals(fa, res);
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize2() {
        instance.setNormalizationType(Normalize.SUM0);
        fa = instance.normalize(fa);
        Factor res = instance.buildFactor(new Variable[]{a}, 0);
        res.setValues(new double[]{
                -0.2, 0.2
        });
        assertEquals(fa, res);
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize3() {
        instance.setNormalizationType(Normalize.SUM1);
        Factor fac = instance.buildFactor(new Variable[]{a, b, c}, 0);
        fac.setValues(new double[]{
                0.2, 0.4, 0.1, 0.4, 0.1, 0.06, 0, 0, 0.06, 0.4, 0.2, 0.08
        });
        fac = instance.normalize(fac);
        Factor res = instance.buildFactor(new Variable[]{a, b, c}, 0);
        res.setValues(new double[]{
                0.1, 0.2, 0.05, 0.2, 0.05, 0.03, 0, 0, 0.03, 0.2, 0.1, 0.04
        });
        assertEquals(fac, res);
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize4() {
        instance.setNormalizationType(Normalize.SUM0);
        f1 = instance.normalize(f1);
        Factor res = instance.buildFactor(new Variable[]{a, b, c}, 0);
        final double r = 1d/12d;
        res.setValues(new double[]{
                0.1-r, 0.2-r, 0.05-r, 0.2-r, 0.05-r, 0.03-r,
                0-r, 0-r, 0.03-r, 0.2-r, 0.1-r, 0.04-r
        });
        assertEquals(f1, res);
    }

    /**
     * Test of normalize method, of class CostFunction.
     */
    @Test
    public void testNormalize5() {
        instance.setNormalizationType(Normalize.SUM1);
        Factor fac = instance.buildFactor(new Variable[]{a, b, c}, 0);
        fac.setValues(new double[]{
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
        });
        fac = instance.normalize(fac);
        Factor res = instance.buildFactor(new Variable[]{a, b, c}, 0);
        final double r = 1d/12d;
        res.setValues(new double[]{
                r, r, r, r, r, r, r, r, r, r, r, r
        });
        assertEquals(fac, res);
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testNegate1() {
        instance.setCombineOperator(Combine.PRODUCT);
        fa = instance.negate(fa);
        Factor res = instance.buildFactor(new Variable[]{a}, 0);
        res.setValues(new double[]{
                1/0.3, 1/0.7
        });
        assertEquals(fa, res);
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testNegate2() {
        instance.setCombineOperator(Combine.SUM);
        fa = instance.negate(fa);
        Factor res = instance.buildFactor(new Variable[]{a}, 0);
        res.setValues(new double[]{
                -0.3, -0.7
        });
        assertEquals(fa, res);
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce1() {
        VariableAssignment map = new VariableAssignment();
        map.put(a, 0);
        Factor red = instance.reduce(f1, map);

        Factor res = instance.buildFactor(new Variable[]{b, c}, 0);
        res.setValues(new double[]{
                0.1, 0.2, 0.05, 0.2, 0.05, 0.03
        });
        assertEquals(red, res);
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce2() {
        VariableAssignment map = new VariableAssignment();
        map.put(b, 0);
        Factor red = instance.reduce(f1, map);

        Factor res = instance.buildFactor(new Variable[]{a, c}, 0);
        res.setValues(new double[]{
                0.1, 0.2, 0.05, 0, 0, 0.03
        });
        assertEquals(red, res);
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce3() {
        VariableAssignment map = new VariableAssignment();
        map.put(c, 0);
        Factor red = instance.reduce(f1, map);

        Factor res = instance.buildFactor(new Variable[]{a, b}, 0);
        res.setValues(new double[]{
                0.1, 0.2, 0, 0.2
        });
        assertEquals(red, res);
    }

    /**
     * Test of negate method, of class CostFunction.
     */
    @Test
    public void testReduce4() {
        VariableAssignment map = new VariableAssignment();
        map.put(a, 0);
        map.put(b, 0);
        map.put(c, 0);
        Factor red = instance.reduce(f1, map);
        Factor res = instance.buildFactor(new Variable[0], 0);
        res.setValue(0, 0.1);
        assertEquals(red, res);
    }

}