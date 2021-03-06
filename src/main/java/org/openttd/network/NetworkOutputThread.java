/*
 *  Copyright (C) 2011 Nathanael Rebsch
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.openttd.network;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.net.Socket;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handling of sending packets to an OpenTTD server in a separate thread.
 * @author Nathanael Rebsch
 */
public class NetworkOutputThread implements Runnable
{
    private static final NetworkOutputThread singleton;
    private static final HashMap<Socket, BlockingQueue<Packet>> queues;

    private final Logger log = LoggerFactory.getLogger(NetworkOutputThread.class);

    static {
        singleton = new NetworkOutputThread();
        queues = new HashMap<Socket, BlockingQueue<Packet>>();
    }

    /**
     * Constructor kept private for a good reason.
     */
    private NetworkOutputThread ()
    {
        new Thread(this).start();
    }

    /**
     * Create a queue if there should not be one already.
     * @param socket Socket to bind the queue to.
     */
    private static void instanciateQueue (Socket socket)
    {
        if (!queues.containsKey(socket)) {
            queues.put(socket, new ArrayBlockingQueue<Packet>(15, false));
        }
    }

    /**
     * Get the queue bound to the given Socket.
     * @param socket Socket for which a queue should be retrieved.
     * @return The queue bound to the Socket.
     */
    private static BlockingQueue<Packet> getQueue (Socket socket)
    {
        instanciateQueue(socket);
        return queues.get(socket);
    }

    /**
     * Append a packet to the appropriate queue.
     * @param p Packet to append to the queue.
     */
    protected static void append (Packet p)
    {
        getQueue(p.getSocket()).add(p);
    }

    /**
     * Get the next packet from the top of the queue.
     * @param socket Socket of which queue to get the next Packet.
     * @return Packet from the top of the queue.
     * @throws InterruptedException
     */
    protected static synchronized Packet getNext (Socket socket) throws InterruptedException
    {
        return getQueue(socket).take();
    }

    @Override
    public void run ()
    {
        while (true) {
            for (BlockingQueue<Packet> q : queues.values()) {
                try {
                    Packet p = q.take();

                    /* if the socket is closed, remove it from the queue and leave the foreach */
                    if (p.getSocket().isClosed()) {
                        queues.remove(p.getSocket());
                        break;
                    }

                    p.send();
                    log.trace("Sending Packet {}", p.getType());
                } catch (InterruptedException ex) {
                    log.error(null, ex);
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            }
        }
    }
}
