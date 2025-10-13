/*
  Vault 3
  (C) Copyright 2025, Eric Bergman-Terrell
  
  This file is part of Vault 3.

  Vault 3 is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Vault 3 is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Vault 3.  If not, see <http://www.gnu.org/licenses/>.
*/

package mainPackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author Eric Bergman-Terrell
 * 
 * This class ensures that a single instance of the application is run using a
 * tcp/ip socket.
 * 
 * Based on the following: 
 * 
 * http://www.rbgrn.net/blog/2008/05/java-single-application-instance.html
 * http://java.sun.com/developer/onlineTraining/Programming/BasicJava2/socket.html
 */

public class SingletonApplication {
	private ServerSocket server = null;
	
	/**
	 * Try to listen to the specified socket. If the socket can be used, listen for the "SHOW" command.
	 * When one comes in, make sure the main application window is visible.
	 * 
	 * @return true if the application should keep running, false if it should close (because another instance
	 * is already running).
	 */
	public boolean listenSocket() {
		boolean programShouldContinue = false;
		
		try {
			final int port = Globals.getPreferenceStore().getInt(PreferenceKeys.SingletonSocketPort);
			Globals.getLogger().info(String.format("Listening to port %d", port));

			server = new ServerSocket(port, 10, InetAddress.getLocalHost());
			
			programShouldContinue = true;
			
			try {
				try {
					final Thread socketListenerThread = new Thread(() -> {
                        boolean socketClosed = false;

                        while (!socketClosed) {
                            BufferedReader in = null;
                            Socket client = null;

                            try {
                                client = server.accept();
                                Globals.getLogger().info("accepted");

                                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                                String message = in.readLine();

                                Globals.getLogger().info(String.format("Read message %s", message));

                                if (message.equals("SHOW")) {
                                    Globals.getLogger().info("Processing SHOW command");

                                    Globals.getMainApplicationWindow().getShell().getDisplay().asyncExec(() -> Globals.getMainApplicationWindow().displayAlreadyRunningMessage());
                                }
                            }
                            catch (Throwable ex) {
                                ex.printStackTrace();
                                Globals.getLogger().info("Socket closed");
                                socketClosed = true;
                            }
                            finally {
                                if (in != null) {
                                    try {
                                        in.close();
                                    }
                                    catch (Throwable ex) {
                                        Globals.getLogger().info(String.format("Could not close buffered reader: %s", ex.getMessage()));
                                    }
                                }

                                if (client != null) {
                                    try {
                                        client.close();
                                    }
                                    catch (Throwable ex) {
                                        Globals.getLogger().info(String.format("Could not close client socket: %s", ex.getMessage()));
                                    }
                                }
                            }
                        }
                    });
					
					socketListenerThread.start();
				} catch (Throwable e) {
					e.printStackTrace();
					Globals.getLogger().info("Read failed");
				}
			} catch (Throwable e) {
				e.printStackTrace();
				Globals.getLogger().info("Could not accept");
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Globals.getLogger().info("Could not listen to port");
			
			int port = Globals.getPreferenceStore().getInt(PreferenceKeys.SingletonSocketPort);
			Globals.getLogger().info(String.format("Sending SEND command to port %d", port));

			// Could not get server port, so send the "SHOW" command and exit.

			try {
				final Socket clientSocket = new Socket(InetAddress.getLocalHost(), port);
				final OutputStream out = clientSocket.getOutputStream();
				out.write("SHOW\n".getBytes());
				out.close();
				clientSocket.close();
				
				Globals.getLogger().info("Sent SHOW command");
				
				programShouldContinue = false;
			}
			catch (Throwable ex) {
				programShouldContinue = true;
				ex.printStackTrace();
				
				Globals.getLogger().info(String.format("Could not send SHOW command: %s", ex.getMessage()));
			}
		}
		
		return programShouldContinue;
	}
	
	/**
	 * Close the socket.
	 */
	public void releaseSocket() {
		if (server != null) {
			try {
				server.close();
			}
			catch (Throwable ex) {
				ex.printStackTrace();
				
				Globals.getLogger().info(String.format("Could not close server socket: %s", ex.getMessage()));
				server = null;
			}
		}
	}
}
