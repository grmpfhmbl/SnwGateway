/*
 * Copyright 2015 Smart Aquifer Characterisation (SAC) Programme (http://www.gns.cri.nz/Home/Our-Science/Environment-and-Materials/Groundwater/Research-Programmes/SMART-Aquifer-Characterisation)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;

import actors.DbActor;
import play.Logger;
import play.libs.Akka;
import actors.SpaDataMessage;
import akka.actor.ActorSelection;

public class SpaCommLegacy extends Observable implements Runnable {

    private Socket clientSocket = null;
    private DataOutputStream outToServer = null;
    private BufferedReader inFromServer;
    // private MyReader clientreader = null;
    private boolean terminate = false;

    private ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
    private boolean closed = false;

    ActorSelection dbActorSel = null;

    public SpaCommLegacy(String ip, int port) throws IOException {

        clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(ip, port), 1000);

        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(
                clientSocket.getInputStream()));

    }

    @Override
    public void run() {
        Logger.info("Starting SPA comm legacy");
        dbActorSel = Akka.system().actorSelection("/user/"+ DbActor.ActorName());

        String line;
        try {
            while ((line = inFromServer.readLine()) != null) {
                for (String element : line.trim().split(";")) {
                    dbActorSel.tell(new SpaDataMessage(element), null);
                }
            }
        } catch (IOException ex) {
            Logger.error(SpaCommLegacy.class.getName() + ": " + ex);
        } finally {
            this.Terminate();
        }
        closed = true;
    }

    public void Terminate() {
        terminate = true;
        try {
            clientSocket.close();
        } catch (IOException ex) {
            Logger.error(SpaCommLegacy.class.getName() + ": " + ex.getMessage());
        }
    }

}
