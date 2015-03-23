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
package es.csic.iiia.maxsum.core.node;

import es.csic.iiia.maxsum.core.Identity;
import es.csic.iiia.maxsum.core.Variable;
import es.csic.iiia.maxsum.core.VariableAssignment;
import es.csic.iiia.maxsum.core.factors.Factor;
import es.csic.iiia.maxsum.core.op.Semiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Arrays;
import java.util.Map;

/**
 *
 */
public class VariableNode extends AbstractNode {
    Logger logger = LoggerFactory.getLogger(VariableNode.class);

    private final Variable variable;

    private Factor belief;
    private final Marker marker;
    private Factor fixedMessage;

    public VariableNode(Identity id, Semiring semiring, Variable variable) {
        super(id, semiring);
        this.variable = variable;
        this.marker = MarkerFactory.getMarker(toString());
    }

    @Override
    public void addNeighbor(Identity neighbor, Variable variable) {
        if (!this.variable.equals(variable)) {
            throw new IllegalArgumentException("Invalid variable \"" + variable + "\" for " + toString());
        }

        super.addNeighbor(neighbor, variable);
    }

    @Override
    public void run() {
        if (fixedMessage != null) {
            logger.trace(marker, "is fixed to {}", fixedMessage);
            for (Identity neighbor : neighbors.keySet()) {
                send(fixedMessage, neighbor);
            }
            return;
        }

        belief = semiring.combine(messages.values());
        logger.trace(marker, "Messages: <{}>", (Object) messages.values());
        logger.trace(marker, "Belief: {}", belief);

        for (Identity neighbor : neighbors.keySet()) {
            Factor msg = semiring.normalize(
                    semiring.combine(belief, semiring.negate(messages.get(neighbor)))
            );
            send(msg, neighbor);
        }
    }

    public long select() {
        return semiring.getOptimalIndex(belief);
    }

    public void fix(long index) {
        fixedMessage = semiring.buildFactor(new Variable[]{variable}, semiring.getSummarizeOperator().getNoGood());
        fixedMessage.setValue(index, 0);
    }

    public Variable getVariable() {
        return variable;
    }

}
