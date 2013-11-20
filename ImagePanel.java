package org.newdawn.spaceinvaders;

/**
 * Chang Yoon Kim
 * User: Richard
 * Date: 12/5/12
 * Time: 12:17 PM
 */
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImagePanel extends JPanel
{

    Image image;

    public ImagePanel()
    {
        image = Toolkit.getDefaultToolkit().createImage("images\\car.gif");
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        if (image != null)
        {
            g.drawImage(image, 20, 20, this);
        }
    }

//    public static void main(String[] args)
//    {
//        SwingUtilities.invokeLater(new Runnable()
//        {
//
//            @Override
//            public void run()
//            {
//                JFrame frame = new JFrame();
//                frame.add(new ImagePanel());
//
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.setSize(400, 400);
//                frame.setLocationRelativeTo(null);
//                frame.setVisible(true);
//            }
//        });
//    }
}
