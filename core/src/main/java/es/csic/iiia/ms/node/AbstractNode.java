/*
 * Software License Agreement (BSD License)
 *
 * Copyright 2014 Marc Pujol <mpujol@iiia.csic.es>.
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
package es.csic.iiia.ms.node;

import es.csic.iiia.ms.Communicator;
import es.csic.iiia.ms.Identity;
import es.csic.iiia.ms.Variable;
import es.csic.iiia.ms.functions.CostFunction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public abstract class AbstractNode implements Node {
    private final Communicator comunicator;
    private final Identity id;

    private final Map<Identity, CostFunction> messages = new TreeMap<>();
    private final TreeMap<Identity, Variable> neighbors = new TreeMap<>();

    private CostFunction potential;
    private CostFunction belief;

    public AbstractNode(Identity id, Communicator communicator, CostFunction potential) {
        this.id = id;
        this.potential = potential;
        this.comunicator = communicator;
    }

    @Override
    public Identity getIdentity() {
        return id;
    }

    @Override
    public CostFunction getPotential() {
        return potential;
    }

    @Override
    public CostFunction getBelief() {
        return belief;
    }

    @Override
    public void addNeighbor(Identity neighbor, Variable variable) {
        // Add an empty message for the first iteration
        CostFunction msg = potential.getFactory().buildCostFunction(
                new Variable[]{variable}, 
                potential.getFactory().getCombineOperation().getNeutralValue());
        neighbors.put(neighbor, variable);
        messages.put(neighbor, msg.normalize());
    }
    
    private static CostFunction combine(Collection<CostFunction> functions) {
        if (functions.isEmpty()) {
            throw new RuntimeException("Combining empty function list.");
        }
        
        List<CostFunction> flist = new ArrayList<>(functions);
        CostFunction f = flist.remove(flist.size()-1);
        return f.combine(flist);
    }

    @Override
    public void run() {
        belief = getPotential().combine(messages.values());
        System.out.print(this + "[");
        for (Variable v : getPotential().getVariableSet()) {
            System.out.print(v + ",");
        }
        System.out.println("]");
        System.out.println("belief: " + belief.normalize());
        System.out.println("msgs: " + messages.values());
        System.out.println("smsm: " + belief.combine(getPotential().negate()));
        System.out.println("sms2: " + combine(messages.values()).normalize());
        for (Identity neighbor : neighbors.keySet()) {
            CostFunction invIncomingMsg = messages.get(neighbor).negate();
            CostFunction msg = belief
                    .summarize(new Variable[]{neighbors.get(neighbor)})
                    .combine(invIncomingMsg)
                    .normalize();
            send(msg, neighbor);
        }
    }

    @Override
    public void receive(CostFunction message, Identity neighbor) {
        messages.put(neighbor, message);
    }

    private void send(CostFunction message, Identity neighbor) {
        this.comunicator.send(message, id, neighbor);
    }

    @Override
    public String toString() {
        return "N" + id;
    }

    
}
