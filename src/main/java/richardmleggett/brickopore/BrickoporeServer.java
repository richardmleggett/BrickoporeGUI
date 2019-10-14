/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package richardmleggett.brickopore;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.datatransfer.*;
import java.awt.Toolkit;

/**
 *
 * @author leggettr
 */
public class BrickoporeServer extends Thread {
    Socket socket = null;
    ServerSocket server = null;
    int port = 2428;
    byte[] in_buffer = new byte[256];
    byte[] out_buffer = new byte [256];
    boolean running = true;
    private int maxBufferSize = 20000;
    private final static String colours[] = { "?", "BLACK", "BLUE", "GREEN", "YELLOW", "RED", "WHITE", "BROWN" };
    private final static String colourCodes[] = { "??", "Bk", "Bl", "Gr", "Ye", "Re", "Wh", "Br" };
    private final static String baseCodes [] = {"N", "N", "C", "A", "G", "T", "N", "N", "N"};
    //private final static String baseCodes [] = {"N", "N", "A", "C", "G", "T", "N", "N", "N"};
    private final static int COLOUR_BLUE = 2;
    private final static int COLOUR_GREEN = 3;
    private final static int COLOUR_YELLOW = 4;
    private final static int COLOUR_RED = 5;
    private final static int COLOUR_WHITE = 6;
    private ArrayList<Integer> colourBuffer = new ArrayList<Integer>();
    private SignalPanel signalPanel = null;
    private ArrayList signalBuffer = new ArrayList();
    private boolean commandSequence = false;
    private boolean commandWhiteAlign = false;
    private boolean commandTerminate = false;
    private boolean commandNudgeBack = false;
    private boolean commandNudgeFwd = false;
    private boolean waitForSequence = false;
    private String read = "";
    private Brickopore parentFrame;
    private long cheeringStopTime = 0;

    public BrickoporeServer(Brickopore parent, int p) {
        parentFrame = parent;
        port = p;
    }

    private static String intToColourCode(int n) {
        if (n < colourCodes.length) {
            return colourCodes[n];
        }
        
        return colourCodes[0];
    }    

    private static String intToColour(int n) {
        if (n < colours.length) {
            return colours[n];
        }
        
        return colours[0];
    }
    
    public void terminate() {
        System.out.println("Trying to terminate...");
        commandTerminate = true;
    }
    
    public void sequenceClicked() {
        System.out.println("Clicked sequence");
        signalPanel.clearSignalPlot();
        commandSequence = true;
    }
    
    public void alignClicked() {
        System.out.println("Clicked align");
        commandWhiteAlign = true;
    }
    
    public void nudgeBackClicked() {
        System.out.println("Clicked nudge back");
        commandNudgeBack = true;
    }
    
    public void nudgeFwdClicked() {
        System.out.println("Clicked nudge forward");
        commandNudgeFwd = true;
    }
    
    public void run() {
        int whiteCount = 0;
        boolean plottingSignal = false;
        System.out.println("Running on port "+port);

        try {
            server = new ServerSocket(port);
            System.out.println("Server started");

            System.out.println("Waiting for a client ...");

            socket = server.accept();
            System.out.println("Client accepted");

            // takes input from the client socket
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            
            // reads message from client until "Over" is sent
            while (running) {
                if (commandSequence) {
                    System.out.println("Sending start sequence command");
                    out_buffer[0] = '!';
                    out_buffer[1] = 'S';
                    out_buffer[2] = 'Q';
                    out_buffer[3] = 0;
                    out.write(out_buffer, 0, 4);
                    out.flush();
                    commandSequence = false;
                    waitForSequence = true;
                    whiteCount = 0;
                    plottingSignal = false;
                } else if (commandWhiteAlign) {
                    System.out.println("Sending start align command");
                    out_buffer[0] = '!';
                    out_buffer[1] = 'F';
                    out_buffer[2] = 'W';
                    out_buffer[3] = 0;
                    out.write(out_buffer, 0, 4);
                    out.flush();
                    commandWhiteAlign = false;
                } else if (commandTerminate) {
                    out_buffer[0] = '!';
                    out_buffer[1] = 'E';
                    out_buffer[2] = 'X';
                    out_buffer[3] = 0;
                    out.write(out_buffer, 0, 4);
                    out.flush();
                    commandTerminate = false;
                    running = false;
                } else if (commandNudgeBack) {
                    out_buffer[0] = '!';
                    out_buffer[1] = 'N';
                    out_buffer[2] = 'B';
                    out_buffer[3] = 9;
                    out.write(out_buffer, 0, 4);
                    out.flush();
                    commandNudgeBack = false;
                } else if (commandNudgeFwd) {
                    out_buffer[0] = '!';
                    out_buffer[1] = 'N';
                    out_buffer[2] = 'F';
                    out_buffer[3] = 9;
                    out.write(out_buffer, 0, 4);
                    out.flush();
                    commandNudgeFwd = false;
                }
                                
                if (waitForSequence) {
                    try {
                        int n = in.read(in_buffer, 0, 4);
                        if (n > 0) {
                            if (in_buffer[0] == '!') {
                                if ((in_buffer[1] == 'S') && (in_buffer[2] == 'T')) {
                                    System.out.println("GOT STOP");
                                    waitForSequence = false;
                                    doBaseCall();
                                    System.out.println("Buffer size "+colourBuffer.size());
                                    signalPanel.repaint();
                                    colourBuffer.clear();
                                    signalBuffer.clear();
                                } else {                                    
                                    if (in_buffer[2] == COLOUR_WHITE) {
                                        if (!plottingSignal) {
                                            whiteCount++;
                                        }
                                    } else {
                                        if (whiteCount > 50) {
                                            plottingSignal = true;
                                        }
                                    }
                                    
                                    colourBuffer.add(new Integer(in_buffer[2]));
                                    
                                    if (plottingSignal) {
                                        signalPanel.plotSignal(colourBuffer.size()-whiteCount, in_buffer[2]);
                                    }
                                    
                                    signalPanel.repaint();
                                }
                            }
                        } else {
                            System.out.println("Packet didn't begin with ! character");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
                
                if (cheeringStopTime != 0) {
                    if (System.currentTimeMillis() > cheeringStopTime) {
                        parentFrame.setMiniFigureState(false);
                        parentFrame.setSequenceButtonEnabled(true);
                    }
                }
                
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                   System.out.println("oops");
                }
            }
            
            System.out.println("Closing connection");

            // close connection
            socket.close();
            in.close();
            
            System.out.println("Terminated");
        } catch (IOException i) {
            System.out.println(i);
        }
    }
    
    public String getSeq(int c, int n) {
        String seq = "";
        
        System.out.println("c="+c+" n="+n);
        
        if ((c ==  COLOUR_RED) || (c == COLOUR_BLUE) || (c == COLOUR_GREEN) || (c == COLOUR_YELLOW)) {
            if ((n > 10) && (n < 30)) {
                seq = baseCodes[c];                
            } else if ((n >= 30) && (n < 50)) {
                seq = baseCodes[c] + baseCodes[c];
            } else if ((n >= 50) && (n < 70)) {
                seq = baseCodes[c] + baseCodes[c] + baseCodes[c];
            } else if (n >= 70) {
                seq = "";
                for (int i=0; i<((n+10)/20); i++) {
                    seq += baseCodes[c];
                }
            } else {
                seq = "";
            }
        } else {
            if (n > 10) {
                seq = "N";
            } else {
                seq = "";
            }
        }
        
        return seq;
    }
    
    public void doBaseCall() {
        int lastCode = 0;
        int lastCount = 0;
        boolean gotWhite = false;
        read = "";

        for (int i=0; i< colourBuffer.size(); i++) {
            int c = colourBuffer.get(i);

            if (c == lastCode) {
                lastCount++;
            } else {
                if (lastCount > 0) {
                    if ((lastCode == COLOUR_WHITE) && (lastCount > 10)) {
                        gotWhite = true;
                        System.out.println("Got sequencing adapter");
                    }  else if (gotWhite) {
                        String seq = getSeq(lastCode, lastCount);
                        read = read + seq;
                    }
                }
                
                lastCode = c;
                lastCount = 1;
            }                
        }

        if ((lastCount > 0) && (gotWhite)) {
                String seq = getSeq(lastCode, lastCount);
                read = read + seq;
        }
        
        System.out.println("");
        System.out.println("Read is: "+read);
                
        StringSelection stringSelection = new StringSelection(">seq\n"+read);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        signalPanel.setSequence(read);
        parentFrame.setMiniFigureState(true);
        cheeringStopTime = System.currentTimeMillis() + 3000;
        
    }
    
    public ArrayList getColourBuffer() {
        return colourBuffer;
    }
    
    public ArrayList getSignalBuffer() {
        return signalBuffer;
    }
        
    public void setSignalPanel(SignalPanel sp) {
        signalPanel = sp;
    }
    
    public String getRead() {
        return read;
    }
}
