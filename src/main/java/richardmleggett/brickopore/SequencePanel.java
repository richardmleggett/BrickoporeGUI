/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package richardmleggett.brickopore;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author leggettr
 */
public class SequencePanel extends JPanel {
    public String sequence = "";

    public void setSequence(String s) {
        sequence = s;
        this.invalidate();
        this.repaint();
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);  

        System.out.println("Redrawing... ["+sequence+"]");
        //g.setColor(Color.YELLOW);
        //g.fillRect(0, 0, 2000, 2000);

  
        g.setFont(new Font("Courier New", Font.BOLD, 48));
        int xPos = 6;

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

            g.drawString(base, xPos, 48);
            xPos += 30;
        }
    }    
}
