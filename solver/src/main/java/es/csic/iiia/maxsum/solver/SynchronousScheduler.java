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

package es.csic.iiia.maxsum.solver;

import es.csic.iiia.maxsum.core.VariableAssignment;
import es.csic.iiia.maxsum.core.node.Node;
import es.csic.iiia.maxsum.core.node.VariableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SynchronousScheduler implements Scheduler {
    private static final Logger logger = LoggerFactory.getLogger(SynchronousScheduler.class);

    private final SynchronousCommunicator communicator;

    private int maxIterations = 3;

    private List<Node> nodes;

    private List<VariableNode> variableNodes = new ArrayList<>();

    public SynchronousScheduler(SynchronousCommunicator communicator) {
        this.communicator = communicator;
    }

    @Override
    public VariableAssignment run(List<Node> nodes) {
        this.nodes = nodes;
        this.setup();
        VariableAssignment solution = new VariableAssignment();

        int nFixedVariables = 0;
        while (!variableNodes.isEmpty()) {
            boolean converged = false;
            int i = 0;
            for (; i < maxIterations && !converged; i++) {
                logger.trace("======= Iteration #{}.{} ======", nFixedVariables, i + 1);
                converged = iterate();
            }

            // Just info...
            if (converged) {
                logger.info("Converged after {} iterations.", i);
            } else {
                logger.info("Not converged after {} iterations", i);
            }

            // Fix one variable
            VariableNode node = variableNodes.remove(variableNodes.size()-1);
            long choice = node.select();
            node.fix(choice);
            solution.put(node.getVariable(), (int)choice);
            nFixedVariables++;
        }

        return solution;
    }

    /**
     * Initializes the algorithm
     */
    private void setup() {
        for (Node node : nodes) {
            if (node instanceof VariableNode) {
                variableNodes.add((VariableNode)node);
            }

            node.setCommunicator(communicator);
            communicator.addNode(node);
        }
    }

    /**
     * Runs an iteration of the algorithm.
     *
     * @return true if the algorithm has converged or false otherwise.
     */
    private boolean iterate() {
        for(Node node : nodes) {
            node.run();
        }

        return communicator.run();
    }

}
