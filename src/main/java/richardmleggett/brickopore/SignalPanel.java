package richardmleggett.brickopore;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Random;
import javax.swing.JPanel;

public class SignalPanel extends JPanel {
    private BrickoporeServer server = null;
    private final static String colours[] = { "?", "Blk", "Blu", "Gre", "Yel", "Red", "Whi", "Bro" };
    private final static int NUM_COLOURS = 7;
    private int eventDistance = 40;
    private int xOff = 16;
    private int eventSize = 2;
    private Random rnd = new Random();
    private int signalPrevX = -1;
    private int signalPrevY = -1;
    private int xOffset = 50;
    private int yOffset = 8;
    private int yScale = 40;
    private int imageWidth = 505 * eventSize;
    private int imageHeight = (NUM_COLOURS * yScale) + yOffset + yOffset;
    private BufferedImage signalImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
    private String sequence = "";    
    private boolean isBeeTrail = false;
    private boolean showResult = false;

    public SignalPanel() {
        clearSignalPlot();
    }

    private static String intToColour(int n) {
        if (n < colours.length) {
            return colours[n];
        }
        
        return colours[0];
    }
    
    public void clearSignalPlot() {
        Graphics g = signalImage.getGraphics();
        Graphics2D g2 = (Graphics2D)g;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imageWidth, imageHeight);
        g.setColor(Color.BLACK);
        g.drawLine(xOffset, (NUM_COLOURS * yScale) + yOffset, imageWidth, (NUM_COLOURS * yScale) + yOffset);
        g.drawLine(xOffset, yOffset, xOffset, (NUM_COLOURS * yScale) + yOffset);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);   
        for (int i=0; i<=NUM_COLOURS; i++) {
            int y = ((NUM_COLOURS - i) * yScale) + yOffset;
            g.drawLine(xOffset - 10, y, xOffset, y);
            g.drawString(Integer.toString(i*10), xOffset - 32, y+4);
        }
        
        sequence = "";
        signalPrevX = -1;
        signalPrevY = -1;
    }
    
    public void plotSignal(int s, int c) {
        Graphics g = signalImage.getGraphics();
        int minX = xOffset + ((s -1) * eventSize);
        int maxX = xOffset + (s * eventSize);

        g.setColor(Color.BLACK);
        
        if (signalPrevX == -1) {
            signalPrevX = minX;
            signalPrevY = yOffset + ((NUM_COLOURS - c) * yScale);
        }

        if (minX < (imageWidth -1)) {
            int y = yOffset + ((NUM_COLOURS - c) * yScale) - 5 + rnd.nextInt(10);
        
            if (maxX >= (imageWidth - 1)) {
                maxX = imageWidth - 1;
            }
            
            g.drawLine(signalPrevX, signalPrevY, minX, y);
            g.drawLine(minX, y, maxX, y);
            
            signalPrevX = maxX;
            signalPrevY = y;            
        }
        
        
        //for (int i=0; i<eventSize; i++) {
        //    signalBuffer.add((c * 20) + rnd.nextInt(10));
        //}
    }    
    
    public void setSequence(String s) {
        sequence = s;
    }
        
    public void paintComponent(Graphics g) {
        super.paintComponent(g);   
        g.drawImage(signalImage, 50, 50, null);
//        if (server != null) {
//            ArrayList<Integer> colourBuffer = server.getColourBuffer();
//            ArrayList<Integer> signalBuffer = server.getSignalBuffer();
//            for (int i=0; i<colourBuffer.size(); i++) {
//                int c = colourBuffer.get(i);
//                g.setColor(Color.BLACK);
//                g.drawString(intToColour(c), xOff + (i*eventDistance), 230);                
//            }
//            
//            double multiplier = eventDistance / eventSize;
//            for (int i=0; i<signalBuffer.size(); i++) {
//                int xa = xOff + (int)(i*multiplier);
//                int xb = xOff + (int)((i+1)*multiplier);
//                g.drawLine(xa, 200 - (signalBuffer.get(i)), xb, 200 - (signalBuffer.get(i)));
//                if (i > 0) {
//                    g.drawLine(xa, 200 - (signalBuffer.get(i-1)), xa, 200 - (signalBuffer.get(i)));
//                }
//            }
//        }

            g.setFont(new Font("Courier New", Font.BOLD, 64));
            int xPos = 130;
            
            for (int i=0; i<sequence.length(); i++) {
                String base = sequence.substring(i, i+1);
                
                if (base.equals("A")) {
                    g.setColor(BrickoporeServer.COLOR_A);
                } else if (base.equals("C")) {
                    g.setColor(BrickoporeServer.COLOR_C);
                } else if (base.equals("G")) {
                    g.setColor(BrickoporeServer.COLOR_G);
                } else if (base.equals("T")) {
                    g.setColor(BrickoporeServer.COLOR_T);
                } else {
                    g.setColor(Color.BLACK);
                }
                
                g.drawString(base, xPos, imageHeight + 125);
                xPos += 40;
            }
            
            
    }     
    
    public void setServer(BrickoporeServer s) {
        server = s;
        server.setSignalPanel(this);
    }
    
    public void setIsBeeTrail(boolean t) {
        isBeeTrail = t;
    }
    
    public void setShowResult(boolean t) {
        showResult = t;
    }
}
