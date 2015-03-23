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

import es.csic.iiia.maxsum.core.Identity;
import es.csic.iiia.maxsum.core.StringIdentity;
import es.csic.iiia.maxsum.core.Variable;
import es.csic.iiia.maxsum.core.VariableAssignment;
import es.csic.iiia.maxsum.core.factors.Factor;
import es.csic.iiia.maxsum.core.node.FunctionNode;
import es.csic.iiia.maxsum.core.node.Node;
import es.csic.iiia.maxsum.core.node.VariableNode;
import es.csic.iiia.maxsum.core.op.Semiring;

import java.util.*;

public class Solver {

    private Scheduler scheduler;

    public Solver(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Solves the given problem.
     *
     * @param problem
     */
    public void solve(Semiring semiring, Problem problem) {
        List<FunctionNode> functions = buildFactorNodes(semiring, problem);
        Map<Identity, VariableNode> variables = buildVariableNodes(semiring, problem);
        linkNodes(functions, variables);

        List<Node> nodes = new ArrayList<>(functions.size() + variables.size());
        nodes.addAll(functions);
        nodes.addAll(variables.values());

        VariableAssignment solution = scheduler.run(nodes);
        System.out.println("Solution: " + solution);
        System.out.println("Value: " + problem.getValue(solution));
    }

    private List<FunctionNode> buildFactorNodes(Semiring semiring, Problem problem) {
        List<Factor> functions = problem.getFactors();
        List<FunctionNode> nodes = new ArrayList<>(functions.size());

        for (Factor function : functions) {
            StringIdentity id = new StringIdentity(function.getName());
            nodes.add(new FunctionNode(id, semiring, function));
        }

        return nodes;
    }

    private Map<Identity, VariableNode> buildVariableNodes(Semiring semiring, Problem problem) {
        Set<Variable> variables = problem.getVariables();
        HashMap<Identity, VariableNode> nodes = new HashMap<>(variables.size());

        for (Variable variable : variables) {
            Identity id = variable.getId();
            nodes.put(id, new VariableNode(id, semiring, variable));
        }

        return nodes;
    }

    private void linkNodes(List<FunctionNode> functions, Map<Identity, VariableNode> variables) {
        for (FunctionNode function : functions) {
            for (Variable var : function.getPotential().getVariableSet()) {
                VariableNode variable = variables.get(var.getId());
                link(function, variable, var);
            }
        }
    }

    private void link(FunctionNode function, VariableNode variable, Variable var) {
        function.addNeighbor(variable.getIdentity(), var);
        variable.addNeighbor(function.getIdentity(), var);
    }

}
