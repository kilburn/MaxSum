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

import es.csic.iiia.maxsum.core.Variable;
import es.csic.iiia.maxsum.core.VariableAssignment;
import es.csic.iiia.maxsum.core.factors.ConditionedIterator;
import es.csic.iiia.maxsum.core.factors.Factor;
import es.csic.iiia.maxsum.core.factors.HypercubeFactor;
import es.csic.iiia.maxsum.core.factors.MasterIterator;
import gnu.trove.iterator.TLongIterator;

import java.util.*;

/**
 * Semiring used to operate factors.
 */
public class Semiring {

    /**
     * Summarize operation to use.
     */
    private Summarize summarizeOperator = Summarize.MAX;

    /**
     * Combine operation to use.
     */
    private Combine combineOperator = Combine.SUM;

    /**
     * Normalization type to use.
     */
    private Normalize normalizationType = Normalize.NONE;

    /**
     * Builds a new factor whose scope is the given variables and with all values set to initialValue.
     *
     * @param variables variables in the scope of the new factor.
     * @param initialValue value for all the factor's values.
     * @return newly built factor.
     */
    public Factor buildFactor(Variable[] variables, double initialValue) {
        HypercubeFactor c = new HypercubeFactor(variables);
        if (initialValue != 0) {
            c.initialize(initialValue);
        }
        return c;
    }

    /**
     * Build a copy of the given factor.
     *
     * @param factor factor to copy.
     * @return copy of the given factor.
     */
    public Factor buildFactor(Factor factor) {
        return new HypercubeFactor(factor);
    }

    /**
     * Get the index of the optimal configuration.
     *
     * A random one is returned when there are multiple optimal configurations.
     *
     * @return index of the optimal configuration of this function.
     */
    public long getOptimalIndex(Factor factor) {
        ArrayList<Long> idx = new ArrayList<>();
        double optimal = summarizeOperator.getNoGood();
        TLongIterator it = factor.iterator(optimal);
        while(it.hasNext()) {
            final long i = it.next();
            final double value = factor.getValue(i);
            if (summarizeOperator.isBetter(value, optimal)) {
                optimal = value;
                idx.clear();
                idx.add(i);
            } else if (value == optimal) {
                idx.add(i);
            }
        }

        if (idx.isEmpty()) {
            throw new RuntimeException("Unable to optimize this factor");
        }

        return idx.get(new Random().nextInt(idx.size()));
    }

    /**
     * Returns the optimal assignment for the given factor.
     *
     * If an existing mapping is passed, the new assignments will be appended to it.
     * <strong>Warning:</strong> this function does not take into account these pre-assigned
     * variables to do the calculation.
     *
     * For example, if this factor is F(x,y){0,10,2,6}, summarization is set to MIN and it receives
     * {x:1,z:1} through the mapping, it will return {x:0,z:1,y:0} as the new mapping. If you want
     * to ensure an assignment consistent with an existing mapping, use
     * {@link #reduce(es.csic.iiia.maxsum.core.factors.Factor, es.csic.iiia.maxsum.core.VariableAssignment)} first.
     *
     * @param factor current variable mappings.
     * @return assignment of values to variables represents the optimal configuration.
     */
    @SuppressWarnings("unused")
    public VariableAssignment getOptimalAssignment(Factor factor) {
        final int nVariables = factor.getScope().length;
        final VariableAssignment mapping = new VariableAssignment(nVariables);

        // Empty factors have no optimal value
        if (nVariables == 0) {
            return mapping;
        }

        long i = getOptimalIndex(factor);
        mapping.putAll(factor.getMapping(i, mapping));
        return mapping;
    }

    /**
     * Negates this factor, converting all its values into their negative counterparts.
     *
     * @see es.csic.iiia.maxsum.core.op.Combine#negate(double)
     * @see #combine(Factor, Factor)
     *
     * @return newly built factor.
     */
    @SuppressWarnings("unused")
    public Factor negate(Factor factor) {
        Factor result = buildFactor(factor);
        TLongIterator it = factor.iterator(summarizeOperator.getNoGood());
        while(it.hasNext()) {
            final long i = it.next();
            final double v = combineOperator.negate(factor.getValue(i));
            if (Double.isNaN(v)) {
                throw new RuntimeException("Negation generated a NaN value. Halting.");
            }
            result.setValue(i, v);
        }
        return result;
    }

    /**
     * Inverts this factor, applying the inverse of the given operation to all it's values.
     *
     * This function is intended to convert a minimization problem factor into a maximization
     * one, or vice-versa.
     *
     * @see es.csic.iiia.maxsum.core.op.Combine#invert(double)
     *
     * @return newly built factor.
     */
    @SuppressWarnings("unused")
    public Factor invert(Factor factor) {
        Factor result = buildFactor(factor);
        TLongIterator it = factor.iterator(summarizeOperator.getNoGood());
        while(it.hasNext()) {
            final long i = it.next();
            result.setValue(i, combineOperator.invert(factor.getValue(i)));
        }
        return result;
    }

    /**
     * Combine the given factors.
     *
     * @param first first factor to combine.
     * @param second second factor to combine.
     * @return a new factor which is the result of the combination between the first and the second one.
     */
    @SuppressWarnings("unused")
    public Factor combine(Factor first, Factor second) {
        // Combination with null factors gives a null / the other factor
        if (second == null || second.getSize()==0) {
            return buildFactor(first);
        }
        if (first == null || first.getSize() == 0) {
            return buildFactor(second);
        }

        // Compute the variable set intersection (sets doesn't allow duplicates)
        LinkedHashSet<Variable> varSet = new LinkedHashSet<>(first.getVariableSet());
        varSet.addAll(second.getVariableSet());
        Variable[] vars = varSet.toArray(new Variable[varSet.size()]);

        Factor result = buildFactor(vars, combineOperator.getNeutralValue());
        _combine(first, second, result);
        return result;
    }

    private void _combine(Factor first, Factor second, Factor result) {
        MasterIterator it = result.masterIterator();
        ConditionedIterator i1 = first.conditionedIterator(result);
        ConditionedIterator  i2 = second.conditionedIterator(result);
        final int[] subidx      = it.getIndices();
        while (it.hasNext()) {
            final long i = it.next();
            final double v1 = first.getValue(i1.nextSubidxs(subidx));
            final double v2 = second.getValue(i2.nextSubidxs(subidx));
            final double v = combineOperator.eval(v1, v2);
            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value (" + v1 + "," + v2 + ") while"
                        + " combining " + first + " and " + second + ".");
            }
            result.setValue(i, v);
        }
    }

    /**
     * Combines the given list of factors into a single one.
     *
     * @param factors factors to combine.
     * @return factor that represents the combination of all input factors.
     */
    @SuppressWarnings("unused")
    public Factor combine(Collection<Factor> factors) {
        // Remove null factors (without editing the input list)
        List<Factor> fs = new ArrayList<>(factors.size());
        for (Factor f : factors) {
            if (f != null) {
                fs.add(f);
            }
        }

        return _combine(fs);
    }

    /**
     * Combines the given list of factors into a single one.
     *
     * @param factors factors to combine.
     * @return factor that represents the combination of all input factors.
     */
    @SuppressWarnings("unused")
    public Factor combine(Factor factor, Collection<Factor> factors) {
        // Remove null factors (without editing the input list)
        List<Factor> fs = new ArrayList<>(factors.size()+1);
        fs.add(factor);
        for (Factor f : factors) {
            if (f != null) {
                fs.add(f);
            }
        }

        return _combine(fs);
    }

    private Factor _combine(List<Factor> fs) {
        // Basic cases: single (or no) function
        if (fs.isEmpty()) {
            return buildFactor(new Variable[0], combineOperator.getNeutralValue());
        } else if (fs.size() == 1) {
            return fs.get(0);
        }

        // Compute the union of scopes
        LinkedHashSet<Variable> varSet = new LinkedHashSet<>();
        for (Factor f : fs) {
            varSet.addAll(f.getVariableSet());
        }
        Variable[] vars = varSet.toArray(new Variable[varSet.size()]);

        // Iterate over the result positions, fetching the values from all factors.
        Factor result = buildFactor(vars, combineOperator.getNeutralValue());
        _combine(fs, result);

        return result;
    }

    private void _combine(List<Factor> fs, Factor result) {
        final int nIterators = fs.size();
        ConditionedIterator[] iterators = new ConditionedIterator[nIterators];
        for (int i=0; i<nIterators; i++) {
            iterators[i] = fs.get(i).conditionedIterator(result);
        }

        MasterIterator it = result.masterIterator();
        // subIndices is updated automatically by the master iterator
        final int[] subIndices = it.getIndices();
        while (it.hasNext()) {
            final long idx = it.next();
            double v = fs.get(0).getValue(iterators[0].nextSubidxs(subIndices));
            for (int i=1; i<nIterators; i++) {
                final long idx2 = iterators[i].nextSubidxs(subIndices);
                v = combineOperator.eval(v, fs.get(i).getValue(idx2));
            }

            if (Double.isNaN(v)) {
                throw new RuntimeException("Combination generated a NaN value. Halting.");
            }

            result.setValue(idx, v);
        }
    }

    /**
     * Normalizes the given factor.
     *
     * @param factor factor to normalize.
     * @return normalized factor.
     */
    @SuppressWarnings("unused")
    public Factor normalize(Factor factor) {
        // Basic case: no normalization
        if (normalizationType == Normalize.NONE) {
            return factor;
        }

        // Calculate aggregation
        Factor result = buildFactor(factor);
        long size = 0;
        TLongIterator it = factor.iterator(summarizeOperator.getNoGood());
        double sum = 0;
        while(it.hasNext()) {
            sum += factor.getValue(it.next());
            size++;
        }

        final double avg = sum / size;
        if (Double.isNaN(avg)) {
            throw new RuntimeException("Normalization generated a NaN value. Halting.");
        }
        it = factor.iterator(summarizeOperator.getNoGood());
        switch (normalizationType) {
            case SUM0:
                while(it.hasNext()) {
                    final long i = it.next();
                    double v = factor.getValue(i) - avg;
                    if (Double.isNaN(v)) {
                        throw new RuntimeException("Normalization generated a NaN value. Halting.");
                    }
                    result.setValue(i, v);
                }
                break;
            case SUM1:
                // Used to avoid division by 0
                final double fallback = 1/(double)size;
                while(it.hasNext()) {
                    final long i = it.next();
                    final double value = factor.getValue(i);
                    final double v = sum != 0 ? value/sum : fallback;
                    result.setValue(i, v);
                }
                break;
        }

        return result;
    }

    /**
     * Reduces the given factor, fixing the specified variable-value assignments.
     *
     * @param factor factor to reduce.
     * @param assignment variable-value assignments to fix.
     * @return newly reduced factor.
     */
    @SuppressWarnings("unused")
    public Factor reduce(Factor factor, VariableAssignment assignment) {
        // Basic case: if the mapping is empty, the factor is unaltered
        if (assignment == null || assignment.isEmpty()) {
            return factor;
        }

        // Calculate the new factor's variables
        LinkedHashSet<Variable> newVariables = new LinkedHashSet<>(factor.getVariableSet());
        newVariables.removeAll(assignment.keySet());

        // Basic case: the factor reduces to a constant
        if (newVariables.size() == 0) {
            return buildFactor(new Variable[0], factor.getValue(assignment));
        }

        // Basic case: the factor is unaltered
        if (newVariables.size() == factor.getScope().length) {
            return factor;
        }

        Factor result = buildFactor(newVariables.toArray(new Variable[newVariables.size()]), 0);
        MasterIterator it = result.masterIterator();
        ConditionedIterator i1 = factor.conditionedIterator(result);

        // subIndices is updated automatically by the master iterator
        final int[] subIndices = it.getIndices();
        while (it.hasNext()) {
            final long i = it.next();
            final double v = factor.getValue(i1.nextSubidxs(subIndices));
            result.setValue(i, v);
        }

        return result;
    }

    /**
     * Summarize the given factor over the specified variables.
     *
     * @param factor factor to summarize.
     * @param vars variables to summarize over.
     * @return a new factor which is the result of summarizing the given one over the specified variables.
     */
    @SuppressWarnings("unused")
    public Factor summarize(Factor factor, Variable[] vars) {
        Factor result = buildFactor(vars, summarizeOperator.getNoGood());

        if (factor.getSharedVariables(vars).size() < vars.length) {
            throw new IllegalArgumentException("You can only summarize to a subset of the variables in the function.");
        }

        MasterIterator it = factor.masterIterator();
        // subIndices is updated automatically by the master iterator
        final int[] subIndices = it.getIndices();
        ConditionedIterator rit = result.conditionedIterator(factor);
        while (it.hasNext()) {
            final long i = it.next();

            // This value is lost during the summarization
            rit.nextSubidxs(subIndices);
            while (rit.hasNextOffset()) {
                final long idx = rit.nextOffset();
                result.setValue(idx, summarizeOperator.eval(factor.getValue(i), result.getValue(idx)));
            }
        }
        return result;
    }

    /**
     * Set the operation mode of this semiring.
     * <p>
     *     This is a shortcut to set both the summarize operator, combine operator and
     *     normalization type at once.
     * </p>
     *
     * @see #setSummarizeOperator(Summarize)
     * @see #setCombineOperator(Combine)
     * @see #setNormalizationType(Normalize)
     *
     * @param summarizeOperation summarize operator to use.
     * @param combineOperation combine operator to use.
     * @param normalizationType normalization type to use.
     */
    public void setMode(Summarize summarizeOperation,
            Combine combineOperation,
            Normalize normalizationType) {
        this.combineOperator = combineOperation;
        this.summarizeOperator = summarizeOperation;
        this.normalizationType = normalizationType;
    }

    /**
     * Get the combine operator used in this semiring.
     * @return combine operator used in this semiring.
     */
    public Combine getCombineOperator() {
        return combineOperator;
    }

    /**
     * Set the combine operator to use in this semiring.
     * @param combineOperator combine operator to use.
     */
    public void setCombineOperator(Combine combineOperator) {
        this.combineOperator = combineOperator;
    }

    /**
     * Get the normalization type used in this semiring.
     * @return normalization type.
     */
    @SuppressWarnings("unused")
    public Normalize getNormalizationType() {
        return normalizationType;
    }

    /**
     * Set the normalization type to use in this semiring.
     * @param normalizationType normalization type.
     */
    public void setNormalizationType(Normalize normalizationType) {
        this.normalizationType = normalizationType;
    }

    /**
     * Get the summarize operator employed in this semiring.
     * @return summarize operator employed in this semiring.
     */
    public Summarize getSummarizeOperator() {
        return summarizeOperator;
    }

    /**
     * Set the summarize operator to use in this semiring.
     * @param summarizeOperator summarize operator to use.
     */
    public void setSummarizeOperator(Summarize summarizeOperator) {
        this.summarizeOperator = summarizeOperator;
    }

}
