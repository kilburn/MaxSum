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

import es.csic.iiia.maxsum.core.Variable;
import es.csic.iiia.maxsum.core.VariableAssignment;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.*;

/**
 * Base implementation of a factor.
 *
 * This class provides some basic methods implementing the operations that can
 * be made over cost factors, while delegating the actual cost/utility values
 * representation/storage to the concrete class that extends lit.
 *
 */
public abstract class AbstractFactor implements Factor {

    /**
     * Ordered list of variables involved in this function.
     */
    protected Variable[] variables;

    /**
     * Unordered set of variables involved in this function.
     */
    protected LinkedHashSet<Variable> variableSet;

    /**
     * Total size (in elements) of the hypercube formed by this function's
     * variables.
     */
    protected long size;

    /**
     * List of aggregated dimensionality up to "index".
     */
    protected long[] sizes;

    /**
     * Creates a new factor with unknown values.
     *
     * @param variables involved in this factor.
     */
    public AbstractFactor(Variable[] variables) {
        this.variables = variables;
        this.variableSet = new LinkedHashSet<>(Arrays.asList(variables));
        computeFunctionSize();
    }

    /**
     * Constructs a new factor by copying the given one.
     *
     * @param factor factor to copy.
     */
    public AbstractFactor(Factor factor) {
        variableSet = new LinkedHashSet<>(factor.getVariableSet());
        variables = variableSet.toArray(new Variable[variableSet.size()]);
        if (factor instanceof AbstractFactor) {
            size = factor.getSize();
            sizes = ((AbstractFactor)factor).getSizes().clone();
        } else {
            computeFunctionSize();
        }
    }

    @Override
    public void initialize(Double initialValue) {
        for (long i=0; i<size; i++) {
            setValue(i, initialValue);
        }
    }

    @Override
    public Variable[] getScope() {
        return variables;
    }

    /**
     * Computes the function's size and dimensionalities.
     * @see #size
     * @see #sizes
     */
    private void computeFunctionSize() {
        final int len = variables.length;
        size = 1;
        sizes = new long[len];
        boolean overflow = false;
        for (int i=0; i<len; i++) {
            sizes[i] = size;
            size *= variables[len-i-1].getDomain();
            if (size < 0) {
                overflow = true;
                break;
            }
        }

        if (overflow) {
            size = -1;
        }
    }

    /**
     * Get the linearized index corresponding to the given variable mapping.
     *
     * Warning: if there's more than one item matching the specified mapping,
     * only the first one is returned by this function!
     *
     * @param mapping of the desired configuration.
     * @return corresponding linearized index.
     */
    @Override
    public long getIndex(VariableAssignment mapping) {
        final int len = variables.length;
        if (len == 0) {
            // This can be an empty or a constant factor
            return size == 0 ? -1 : 0;
        }

        long idx = 0;
        for (int i = 0; i < len; i++) {
            Integer v = mapping.get(variables[i]);
            if (v != null) {
                idx += sizes[len - i - 1] * v;
            }
        }
        return idx;
    }

    /**
     * Get the linearized index corresponding to the given variable mapping.
     *
     * @param mapping of the desired configuration.
     * @return corresponding linearized index.
     */
    @Override
    public TLongList getIndexes(VariableAssignment mapping) {
        TLongList idxs = new TLongArrayList();

        final int len = variables.length;
        if (len == 0) {
            if (size > 0) {
                idxs.add(0L);
            }
            return idxs;
        }
        idxs.add(0L);

        for (int i = 0; i < len; i++) {
            Integer v = mapping.get(variables[i]);
            if (v != null) {
                // We might be tracking multiple valid indidces
                for (int j = 0; j < idxs.size(); j++) {
                    idxs.set(j, idxs.get(j) + sizes[len - i - 1] * v);
                }
            } else {
                // For each current index, we have to spawn "n" new indices,
                // where "n" is the free variable dimensionality
                for (int j = 0, ilen = idxs.size(); j < ilen; j++) {
                    final int n = variables[i].getDomain();
                    for (v = 0; v < n; v++) {
                        idxs.add(idxs.get(j) + sizes[len - i - 1] * v);
                    }
                }
            }
        }
        return idxs;
    }

    /**
     * Get the variable mapping corresponding to the given linearized index.
     *
     * @param index linearized index of the desired configuration.
     * @param mapping variable mapping to be filled. If null, a new mapping
     *                is automatically instantiated.
     * @return variable mapping filled with the desired configuration.
     */
    @Override
    public VariableAssignment getMapping(long index, VariableAssignment mapping) {
        if (mapping == null) {
            mapping = new VariableAssignment(variables.length);
        } else {
            mapping.clear();
        }

        final int len = variables.length;
        for (int i = 0; i < len; i++) {
            final int ii = len - 1 - i;
            mapping.put(variables[i], (int)(index / sizes[ii]));
            index = index % sizes[ii];
        }
        return mapping;
    }

    /**
     * Get the function's size (in number of possible configurations).
     * @return number of function's possible configurations.
     */
    @Override
    public long getSize() {
        return size;
    }

    /**
     * Get the factors's aggregated dimesionalities vector.
     * @return function's aggregated dimensionalities vector.
     */
    protected long[] getSizes() {
        return sizes;
    }

    @Override
    public String getName() {
        StringBuilder buf = new StringBuilder();
        buf.append("F(");
        if (variables.length > 0) {
            buf.append(variables[0].getId());
            for (int i = 1; i < variables.length; i++) {
                buf.append(",");
                buf.append(variables[i].getId());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    private String _formatValue(double value) {
        return String.format("%.2f", value).replace(',', '.');
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append(" {");
        if (size>0 && getValues() != null) {
            buf.append(_formatValue(getValue(0)));
            for(long i=1; i<size; i++) {
                buf.append(",");
                buf.append(_formatValue(getValue(i)));
            }
        }
        buf.append("}");

        return buf.toString();
    }

    /**
     * Get the long string representation of this function, as a hypercube table with values.
     *
     * For debugging purposes.
     *
     * @return string representation of this function.
     */
    @SuppressWarnings("unused")
    public String toLongString() {
        StringBuilder buf = new StringBuilder();
        buf.append(getName());
        buf.append(" {\n");
        if (size>0 && getValues() != null) {
            VariableAssignment map = null;
            for(long i=0; i<size; i++) {
                map = getMapping(i, map);
                for (Variable v : variables) {
                    buf.append(map.get(v));
                    buf.append(" ");
                }
                buf.append("| ");
                buf.append(_formatValue(getValue(i)));
                buf.append("\n");
            }
        }
        buf.append("}");

        return buf.toString();
    }

    @Override
    public double getValue(int[] index) {
        return getValue(subindexToIndex(index));
    }

    @Override
    public double getValue(VariableAssignment mapping) {
        long idx = this.getIndex(mapping);
        if (idx < 0) {
            throw new IllegalArgumentException(
                    "The given mapping does not assign values to all of this factor's variables");
        }
        return getValue(idx);
    }

    @Override
    public Set<Variable> getVariableSet() {
        return variableSet;
    }

    @Override
    public Set<Variable> getSharedVariables(Factor factor) {
        return getSharedVariables(factor.getVariableSet());
    }

    @Override
    public Set<Variable> getSharedVariables(Variable[] variables) {
        return getSharedVariables(Arrays.asList(variables));
    }

    @Override
    public Set<Variable> getSharedVariables(Collection<Variable> variables) {
        HashSet<Variable> res = new HashSet<>(variableSet);
        res.retainAll(variables);
        return res;
    }

    /**
     * Returns the subindices list (ordered list of values for each variable of
     * this factor) corresponding to the given values array index.
     * index.
     *
     * @param index values array index.
     * @return subindices list.
     */
    protected int[] indexToSubindex(long index) {
        int[] idx = new int[variables.length];
        final int len = variables.length;
        for (int i = 0; i < len; i++) {
            final int ii = len - 1 - i;
            idx[i] = (int)(index / sizes[ii]);
            index = index % sizes[ii];
        }
        return idx;
    }

    protected void indexToSubindex(long index, int[] idx) {
        final int len = variables.length;
        for (int i = 0; i < len; i++) {
            final int ii = len - 1 - i;
            idx[i] = (int)(index / sizes[ii]);
            index = index % sizes[ii];
        }
    }

    @Override
    public ConditionedIterator conditionedIterator(Factor f) {
        return new DefaultConditionedIterator(f);
    }

    @Override
    public void setValue(int[] index, double value) {
        setValue(subindexToIndex(index), value);
    }

    /**
     * Converts a vector of indices (one for each variable) to the corresponding
     * linearized index of the whole function.
     *
     * @param subindex vector of variable configurations (indices).
     * @return corresponding linearized index.
     */
    protected int subindexToIndex(int[] subindex) {
        // Check index lengths
        if (subindex.length != sizes.length) {
            throw new IllegalArgumentException("Invalid index specification");
        }
        // Compute subindex -> index offset
        int idx = 0;
        for (int i = 0; i < subindex.length; i++) {
            // Check domain limits
            if (variables[i].getDomain() <= subindex[i]) {
                throw new IllegalArgumentException("Invalid index " + subindex[i] + " for dimension " + i);
            }
            idx += sizes[subindex.length - i - 1] * subindex[i];
        }
        return idx;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Factor)) {
            return false;
        }
        final Factor other = (Factor) obj;

        return equals(other, 0.0001);
    }

    /**
     * Indicates whether some other factor is "equal to" this one, concerning a
     * delta.
     *
     * @param other the reference object with which to compare.
     * @param delta the maximum delta between factor values for which both
     * numbers are still considered equal.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    public boolean equals(Factor other, double delta) {

        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (this.variableSet != other.getVariableSet() &&
                (this.variableSet == null ||
                !this.variableSet.equals(other.getVariableSet())))
        {
            return false;
        }

        // Constant cost function handling
        if (variableSet.size() == 0) {
            final double e = getValue(0) - other.getValue(0);
            return Math.abs(e) <= delta;
        }

        VariableAssignment map = null;
        for (long i=0; i<size; i++) {
            map = this.getMapping(i, map);
            final double v1 = getValue(i);
            final double v2 = other.getValue(map);
            if (Double.isNaN(v1) || Double.isNaN(v2)) {
                return false;
            }
            final double e = getValue(i) - other.getValue(map);
            if (Math.abs(e) > delta) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.variableSet != null ? this.variableSet.hashCode() : 0);
        return hash;
    }

    protected class DefaultConditionedIterator implements ConditionedIterator {
        private int[] referenceIdxs;
        private int[] idxsToReference;
        private int len = variables.length;
        private AbstractFactor master;
        private int noffsets;
        private long[] offsets;
        private long idx;
        private int currentOffset;

        public DefaultConditionedIterator(Factor reference) {
            if (!(reference instanceof AbstractFactor)) {
                throw new IllegalArgumentException("Cannot build custom iterator for arbitrary CostFunction subtypes");
            }
            AbstractFactor other = (AbstractFactor)reference;
            master = other;

            referenceIdxs = new int[other.variables.length];
            idxsToReference = new int[len];

            ArrayList<Integer> freeVars = new ArrayList<>(len);
            Arrays.fill(idxsToReference, -1);
            for (int j=0; j<len; j++) {
                boolean found = false;

                for (int i=0, olen=other.variables.length; i<olen; i++) {
                    if (variables[j].equals(other.variables[i])) {
                        idxsToReference[j] = i;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    freeVars.add(j);
                }
            }

            // Ofsset computation (a single parent index maps to multiple
            // indices of this function)
            if  (!freeVars.isEmpty()) {

                // Compute the number of offsets
                noffsets = 1;
                for (int i : freeVars) {
                    noffsets *= variables[i].getDomain();
                    if (noffsets < 0) {
                        throw new RuntimeException("Offset index overflow.");
                    }
                }

                // Compute the actual offsets
                offsets = new long[noffsets];
                long multiplier = 1;
                for (int i : freeVars) {
                    final int domain = variables[i].getDomain();
                    int oidx = 0;
                    while (oidx < noffsets) {
                        for (int j=0; j<domain; j++) {
                            for (int k=0; k<multiplier; k++) {
                                offsets[oidx++] += sizes[len - i - 1]*j;
                            }
                        }
                    }
                    multiplier *= domain;
                }

            } else {
                noffsets = 1;
                offsets = new long[]{0l};
            }

            Arrays.fill(referenceIdxs, 0);
        }

        @Override
        public long next(long referenceIdx) {
            master.indexToSubindex(referenceIdx, referenceIdxs);

            // Compute subindex -> index
            idx = 0;
            for (int i = 0; i < len; i++) {
                final int referenceIdxi = idxsToReference[i];
                if (referenceIdxi >= 0) {
                    final int idxv = referenceIdxs[referenceIdxi];
                    idx += sizes[len - i - 1] * idxv;
                }
            }

            currentOffset = 0;
            return idx;
        }

        @Override
        public long nextSubidxs(int[] referenceIdxs) {
            //master.indexToSubindex(referenceIdx, referenceIdxs);

            // Compute subindex -> index
            idx = 0;
            for (int i = 0; i < len; i++) {
                final int referenceIdxi = idxsToReference[i];
                if (referenceIdxi >= 0) {
                    final int idxv = referenceIdxs[referenceIdxi];
                    idx += sizes[len - i - 1] * idxv;
                }
            }

            currentOffset = 0;
            return idx;
        }

        @Override
        public boolean hasNextOffset() {
            return currentOffset < noffsets;
        }

        @Override
        public long nextOffset() {
            return idx + offsets[currentOffset++];
        }

    }
}
