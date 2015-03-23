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

import es.csic.iiia.maxsum.core.Communicator;
import es.csic.iiia.maxsum.core.Identity;
import es.csic.iiia.maxsum.core.node.Node;
import es.csic.iiia.maxsum.core.factors.Factor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Synchronous communicator, that schedules the algorithm operation in a synchronized manner.
 */
public class SynchronousCommunicator implements Communicator {
    Logger logger = LoggerFactory.getLogger(SynchronousCommunicator.class);
    private static final Marker marker = MarkerFactory.getMarker("Message");
    
    private final Map<Identity, Node> nodes = new HashMap<>();
    
    private final List<StoredMessage> messages = new ArrayList<>();
    
    /**
     * Adds a new node for this communicator to track.
     * @param node node to add
     */
    public void addNode(Node node) {
        nodes.put(node.getIdentity(), node);
    }

    @Override
    public void send(Factor message, Identity from, Identity to) {
        logger.trace(marker, "{} -> {} : {}", from, to, message);
        messages.add(new StoredMessage(message, from, to));
    }
    
    /**
     * Runs an iteration of the communicator, delivering all messages queued
     * during the previous one.
     *
     * @return true if the algorithm has converged (all messages are equal to those in the previous iteration),
     *         or false otherwise.
     */
    public boolean run() {
        boolean converged = true;

        for (StoredMessage message : messages) {
            Node recipient = nodes.get(message.to);
            Factor lastMessage = recipient.lastMessage(message.from);
            if (!message.message.equals(lastMessage)) {
                logger.trace("Message {} -> {} differs ({}, {})", message.from, message.to, lastMessage, message.message);
                recipient.receive(message.message, message.from);
                converged = false;
            }
        }

        messages.clear();
        return converged;
    }
    
    private class StoredMessage {
        public final Factor message;
        public final Identity from;
        public final Identity to;
        
        StoredMessage(Factor message, Identity from, Identity to) {
            this.message = message;
            this.from = from;
            this.to = to;
        }

        @Override
        public String toString() {
            return from + " -> " + to + " : " + message;
        }
        
    }
    
}
