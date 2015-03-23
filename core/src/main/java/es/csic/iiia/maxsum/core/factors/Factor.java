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
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.TLongList;
import java.util.Collection;
import java.util.Set;

/**
 * Definition of a cost function and its operations.
 */
public interface Factor {

    /**
     * For debugging purposes, get the "name" of this function, which includes the variables in its scope but not
     * the function's values.
     *
     * @return name of this function
     */
    public String getName();

    /**
     * Sets the initial cost/utility of all the factor configurations to the given initial value.
     *
     * @param initialValue initial value of all configurations.
     */
    public void initialize(Double initialValue);

    /**
     * Returns <strong>the first</strong> index of the values array corresponding to the specified
     * variables mapping.
     *
     * @param mapping variable/value mapping table.
     * @return index of the values array corresponding to the given mapping.
     */
    public long getIndex(VariableAssignment mapping);

    /**
     * Get all the linearized indices corresponding to the given variable mapping.
     *
     * @param mapping of the desired configuration.
     * @return corresponding linearized index(es).
     */
    @SuppressWarnings("unused")
    public TLongList getIndexes(VariableAssignment mapping);

    /**
     * Returns the variable/value mapping corresponding to the specified index of the values array.
     *
     * @param index of the values array.
     * @param mapping mapping table to fill, instantiated if null.
     * @return variable/value mapping corresponding to the given index.
     */
    public VariableAssignment getMapping(long index, VariableAssignment mapping);

    /**
     * Get the function's size (in number of possible configurations).
     *
     * @return number of function's possible configurations.
     */
    public long getSize();

    /**
     * Gets the value of this factor for the given variable states.
     *
     * @param index list of variable states.
     * @return value corresponding factor value.
     */
    public double getValue(int[] index);

    /**
     * Gets the value of this factor for the given linearized index.
     *
     * @param index of the state.
     * @return value corresponding factor value.
     */
    public double getValue(long index);

    /**
     * Gets the value of this factor for the given variable/value mapping.
     *
     * @param mapping variable/value mapping.
     * @return value corresponding factor value.
     */
    public double getValue(VariableAssignment mapping);

    /**
     * Gets all values of this factor, in their linearized order.
     *
     * @return array of all values of this factor.
     */
    public double[] getValues();

    /**
     * Get the scope of this function.
     * <p>
     *     <strong>Warning:</strong> never edit this scope directly!
     * </p>
     *
     * @return scope of this function.
     */
    public Variable[] getScope();

    /**
     * Gets the set of variables of this factor.
     *
     * @return set of variables of this factor.
     */
    public Set<Variable> getVariableSet();

    /**
     * Gets the set of variables shared with the given factor.
     *
     * @param factor to compare against.
     * @return set of variables shared with the given factor.
     */
    @SuppressWarnings("unused")
    public Set<Variable> getSharedVariables(Factor factor);

    /**
     * Gets the set of variables shared with the given variable collection.
     *
     * @param variables collection to compare against.
     * @return set of variables shared with the given variable collection.
     */
    public Set<Variable> getSharedVariables(Collection<Variable> variables);

    /**
     * Gets the set of variables shared with the array of variables.
     *
     * @param variables array of variables to compare against.
     * @return set of variables shared with the given array of variables.
     */
    public Set<Variable> getSharedVariables(Variable[] variables);

    /**
     * Sets the value of this factor for the given variable states.
     *
     * @param index list of variable states.
     * @param value corresponding factor value.
     */
    public void setValue(int[] index, double value);

    /**
     * Sets the value of this factor for the given linealized variable states.
     *
     * @param index linealized variable states.
     * @param value value for this serialized state.
     */
    public void setValue(long index, double value);

    /**
     * Sets the complete list of values for all possible variable states.
     *
     * @param values list of values for all possible variable states.
     */
    public void setValues(double[] values);

    /**
     * Obtains an iterator over the linearized indices of non-infinity elements of this cost
     * function.
     *
     * @param noGoodValue value which is considered a <em>nogood</em>.
     * @return Iterator over the indices of this cost function.
     */
    public TLongIterator iterator(double noGoodValue);

    /**
     * Obtains an iterator over the linearized indices of valid elements of this cost
     * function, and maintains a non-linearized version of the indices too.
     *
     * @return Iterator over the indices of this cost function.
     */
    public MasterIterator masterIterator();

    /**
     * Obtains an iterator over the linearized indices of this cost function, following the natural
     * order of the master's CostFunction indices.
     *
     * @param master CostFunction whose natural indices will be followed.
     * @return Iterator over the indices of this cost function.
     */
    public ConditionedIterator conditionedIterator(Factor master);

}
