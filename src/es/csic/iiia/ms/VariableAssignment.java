/*
 * Software License Agreement (BSD License)
 *
 * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
 * All rights reserved.
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

package es.csic.iiia.ms;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Marc Pujol <mpujol at iiia.csic.es>
 */
public class VariableAssignment extends HashMap<Variable, Integer> {
    private static final long serialVersionUID = 1L;

    public VariableAssignment() {
        super();
    }

    public VariableAssignment(VariableAssignment other) {
        super(other);
    }

    public VariableAssignment(int initialCapacity) {
        super(initialCapacity);
    }

    public VariableAssignment(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    @Override
    public String toString() {
        // Sorted for an easier visualization
        TreeMap<Variable, Integer> sorted = new TreeMap<>(this);

        StringBuilder buf = new StringBuilder("{");
        int i = sorted.size();
        for(Variable v : sorted.keySet()) {
            buf.append(v.getId()).append(":").append(sorted.get(v));
            if (--i != 0) {
                buf.append(",");
            }
        }
        return buf.append("}").toString();
    }

    public VariableAssignment filter(Set<Variable> vars) {
        VariableAssignment filtered = new VariableAssignment(this);
        for (Variable v : keySet()) {
            if (!vars.contains(v)) {
                filtered.remove(v);
            }
        }
        return filtered;
    }

}
