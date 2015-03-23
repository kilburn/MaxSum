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
import es.csic.iiia.maxsum.core.Communicator;
import es.csic.iiia.maxsum.core.Variable;
import es.csic.iiia.maxsum.core.factors.Factor;
import es.csic.iiia.maxsum.core.op.Semiring;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public abstract class AbstractNode implements Node {
    private final Identity id;
    protected final Semiring semiring;

    private Communicator communicator;

    protected final Map<Identity, Factor> messages = new TreeMap<>();
    protected final TreeMap<Identity, Variable> neighbors = new TreeMap<>();

    public AbstractNode(Identity id, Semiring semiring) {
        this.id = id;
        this.semiring = semiring;
    }

    @Override
    public void setCommunicator(Communicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public Identity getIdentity() {
        return id;
    }

    @Override
    public Factor lastMessage(Identity neighbor) {
        return messages.get(neighbor);
    }

    @Override
    public void receive(Factor message, Identity neighbor) {
        messages.put(neighbor, message);
    }

    protected void send(Factor message, Identity neighbor) {
        this.communicator.send(message, id, neighbor);
    }

    protected Factor buildNeutralMessage(Variable variable) {
        final double neutralValue = semiring.getCombineOperator().getNeutralValue();
        return semiring.buildFactor(new Variable[]{variable}, neutralValue);
    }

    @Override
    public void addNeighbor(Identity neighbor, Variable variable) {
        neighbors.put(neighbor, variable);
        messages.put(neighbor, buildNeutralMessage(variable));
    }

    @Override
    public String toString() {
        return "N(" + id + ")";
    }

    
}
