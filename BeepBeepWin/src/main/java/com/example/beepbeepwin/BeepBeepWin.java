package com.example.beepbeepwin;

import static javax.swing.SwingConstants.HORIZONTAL;
import com.acmerobotics.roadrunner.Vector2d;
import com.example.wgwsimcore.WGWsimCore;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.*;

public class BeepBeepWin extends JFrame {

    final int SEEK_BAR_SCALE = 10;

    BufferedImage robotlayer;
    Graphics robotGraphic;
    JTextArea textView;
    JTextArea mouseInfo;
    JSlider slider;
    JMenu menu;
    JButton playPause;
    boolean startNewRun = true;
    boolean showMouseCursor = true;


    // initializing using constructor
    BeepBeepWin() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SpringLayout sprLayout = new SpringLayout();
        setLayout(sprLayout);

        // the graphics area of the field, contained this way to make scalable maintaining aspect ratio
        FieldPanel fieldPanel = new FieldPanel();
        JPanel jP = new JPanel();
        GridBagLayout grid = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        jP.setLayout(grid);
        jP.add(fieldPanel,gbc);
        add(jP);

        // create the text box to contain the messages from the robots
        textView = new JTextArea("Robot text will show here");
        textView.setEditable(false);
        textView.setSize(400,400);
        textView.setPreferredSize(textView.getSize());
        add(textView);


        mouseInfo = new JTextArea("Field Coordinates");
        mouseInfo.setEditable(false);
        add(mouseInfo);

        slider = new JSlider(HORIZONTAL,30*SEEK_BAR_SCALE);
        add(slider);
        slider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                double time = ((double) slider.getValue() / slider.getMaximum()) * wgwSimCore.getActionDuration();
                wgwSimCore.showAtTimeMs(time);
                wgwSimCore.pauseAction();
                playPause.setText("Play");
            }
        });


        playPause = new JButton("Pause");
        add(playPause);
        playPause.addActionListener(actionEvent -> {
            if (wgwSimCore.isPaused()) {
                wgwSimCore.unpause();
                playPause.setText("Pause");
            } else {
                wgwSimCore.pauseAction();
                playPause.setText("Play");
            }
        });

        JButton rePLay = new JButton("rerun");
        add(rePLay);
        rePLay.addActionListener(actionEvent -> startNewRun = true);

        String[] positionOptions = {"Left", "Center", "Right"};
        JComboBox positionSel = new JComboBox(positionOptions);
        add(positionSel);
        positionSel.addActionListener(actionEvent -> {
            int max = menu.getItemCount();
            for(int i = 0; i<max;i++) {
                JMenu subMenu = (JMenu) menu.getItem(i);
                for (int j = 0; j < subMenu.getItemCount(); j++) {
                    if (!subMenu.getItem(0).isSelected()) {
                        subMenu.getItem(positionSel.getSelectedIndex() + 1).setSelected(true);
                    }
                }
                startNewRun = true;
            }
        });


        // setup the spring layout constraints to position all the controls in the window
        sprLayout.putConstraint(SpringLayout.WEST, jP,   5, SpringLayout.WEST, getContentPane());
        sprLayout.putConstraint(SpringLayout.NORTH, jP,  5, SpringLayout.NORTH, getContentPane());

        // constraints for textView placement
        sprLayout.putConstraint(SpringLayout.WEST,  textView,  5, SpringLayout.EAST, jP);
        sprLayout.putConstraint(SpringLayout.NORTH, textView, 5, SpringLayout.NORTH, jP);

        // constraints for slider bar placement
        sprLayout.putConstraint(SpringLayout.NORTH, slider, 5, SpringLayout.SOUTH, jP);
        sprLayout.putConstraint(SpringLayout.WEST, slider,  0, SpringLayout.WEST, jP);
        sprLayout.putConstraint(SpringLayout.EAST, slider, 0, SpringLayout.EAST, jP);

        // constraints for PLay/Pause button
        sprLayout.putConstraint(SpringLayout.NORTH, playPause, 10, SpringLayout.SOUTH, textView);
        sprLayout.putConstraint(SpringLayout.WEST, playPause, 5,  SpringLayout.EAST, jP);

        // constraints for PLay/Pause button
        sprLayout.putConstraint(SpringLayout.NORTH, rePLay, 0, SpringLayout.NORTH, playPause);
        sprLayout.putConstraint(SpringLayout.WEST, rePLay, 5,  SpringLayout.EAST, playPause);

        // constraints for positionSel
        sprLayout.putConstraint(SpringLayout.NORTH, positionSel, 0, SpringLayout.NORTH, rePLay);
        sprLayout.putConstraint(SpringLayout.WEST, positionSel, 5,  SpringLayout.EAST, rePLay);

        // constraints for mouseInfo
        sprLayout.putConstraint(SpringLayout.NORTH, mouseInfo, 5, SpringLayout.SOUTH, playPause);
        sprLayout.putConstraint(SpringLayout.WEST, mouseInfo, 5,  SpringLayout.WEST, playPause);


        // bottom and right edge of window.
        sprLayout.putConstraint(SpringLayout.EAST, getContentPane(), 5, SpringLayout.EAST, textView);
        sprLayout.putConstraint(SpringLayout.SOUTH, getContentPane(),5, SpringLayout.SOUTH, slider);


        BufferedImage image=null;
        // todo fix this file load - can't get images to save in projects as resources?
        // this path is relative to the "working Directory" set in the "Edit Configuration" section of "run/debug config" dropdown
        File file = new File("src\\main\\java\\com\\example\\beepbeepwin\\resources\\images\\fieldsmall.bmp");
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fieldPanel.addLayer(FieldPanel.LayerNames.BACKGROUND, image);

        robotlayer = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        robotGraphic = robotlayer.getGraphics();
        fieldPanel.addLayer(FieldPanel.LayerNames.ROBOTS, robotlayer);
        BufferedImage mouseLayer = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        fieldPanel.addLayer(FieldPanel.LayerNames.MOUSE, mouseLayer);
        fieldPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                float x = mouseEvent.getX();
                float y = mouseEvent.getY();
                double fieldInches = wgwSimCore.getFieldDimensionInches();
                double relativeX =  x * fieldInches / fieldPanel.getWidth() - fieldInches / 2.0;
                double relativeY = -y * fieldInches / fieldPanel.getHeight() + fieldInches / 2.0;
                String formatedStr = String.format("clicked x: %.2f   y: %.2f\n", relativeX, relativeY);
                mouseInfo.setText(formatedStr);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
                    showMouseCursor = !showMouseCursor;
                }
            }
            @Override
            public void mouseReleased(MouseEvent mouseEvent) { }
            @Override
            public void mouseEntered(MouseEvent mouseEvent) { }
            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                // clear our transparent overlay to draw robots on.
                Graphics2D g2 = mouseLayer.createGraphics();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, mouseLayer.getWidth(), mouseLayer.getHeight());
                g2.dispose();
            }
        });

        fieldPanel.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) { }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                // clear our transparent overlay to draw robots on.
                Graphics2D g2 = mouseLayer.createGraphics();
                g2.setComposite(AlphaComposite.Clear);
                g2.fillRect(0, 0, mouseLayer.getWidth(), mouseLayer.getHeight());
                g2.dispose();
                if (showMouseCursor) {
                    float x = mouseEvent.getX();
                    float y = mouseEvent.getY();
                    double fieldInches = wgwSimCore.getFieldDimensionInches();
                    double relativeX = x * fieldInches / fieldPanel.getWidth() - fieldInches / 2.0;
                    double relativeY = -y * fieldInches / fieldPanel.getHeight() + fieldInches / 2.0;
                    String formatedStr = String.format(Locale.US, "moved x: %.2f   y: %.2f\n", relativeX, relativeY);
                    mouseInfo.setText(formatedStr);
                    Graphics g = mouseLayer.getGraphics();
                    int xi = (int) ((x * mouseLayer.getWidth()) / fieldPanel.getWidth());
                    int yi = (int) ((y * mouseLayer.getHeight()) / fieldPanel.getHeight());
                    g.setColor(Color.MAGENTA);
                    g.drawLine(xi, 0, xi, mouseLayer.getHeight());
                    g.drawLine(0, yi, mouseLayer.getWidth(), yi);
                }
            }
        });

        // frame size
        setSize(1024,650);
        setPreferredSize(getSize());

        // setting the title of Frame
        setTitle("BeepBeepWin");

        // add a menu
        JMenu submenu;
        JMenuBar mb=new JMenuBar();
        menu = new JMenu("Robot Paths");
        mb.add(Box.createHorizontalGlue()); // make the robot menu on the right side
        mb.add(menu);
        setJMenuBar(mb);

        // add the robots and their paths to the menu
        for (int robotNum = 0; robotNum< wgwSimCore.getNumRobots(); robotNum++) {   // all robots, clear telemetry
            String robotname = wgwSimCore.getRobotName(robotNum);
            submenu = new JMenu(robotname);
            ButtonGroup buttonGroup = new ButtonGroup();
            JRadioButtonMenuItem diabledItem = new JRadioButtonMenuItem("Disabled", !wgwSimCore.getRobotEnabled(robotNum));
            submenu.add(diabledItem);
            buttonGroup.add(diabledItem);

            List<String> names = wgwSimCore.getActionNames(robotNum);
            boolean firstPath = true;
            for (String name : names) {   // add all the names of the paths.
                JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(name, (wgwSimCore.getRobotEnabled(robotNum) && firstPath));
                firstPath = false;
                submenu.add(menuItem);
                buttonGroup.add(menuItem);
                menuItem.addActionListener(this::actionPathChange);
            }
            menu.add(submenu);
        }

        // now make frame visible
        pack();
        setVisible(true);

        // Create a timer tic to run the sim core
        new javax.swing.Timer((int)wgwSimCore.getTimerTickms(), new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timerTick();
            }
        }).start();
    }

    private void timerTick() {
        // clear our transparent overlay to draw robots on.
        Graphics2D g2 = robotlayer.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, robotlayer.getWidth(), robotlayer.getHeight());
        g2.dispose();

        if (startNewRun) {
            startNewRun = false;
            wgwSimCore.clearRobotActions();
            int max = menu.getItemCount();
            for(int i = 0; i<max;i++) {
                JMenu subMenu = (JMenu) menu.getItem(i);
                String robotName = subMenu.getText();
                for (int j = 0; j < subMenu.getItemCount(); j++) {
                    if (subMenu.getItem(j).isSelected()) {
                        String path = subMenu.getItem(j).getText();
                        wgwSimCore.setCurrentAction(robotName, path);
                    }
                }
            }
            wgwSimCore.startAction();
        }

        wgwSimCore.timerTick();
        getContentPane().repaint(); // don't forget to repaint the container
        double time = wgwSimCore.getCurrentTimeMs();
        slider.setValue((int) ( (double) slider.getMaximum() * time / wgwSimCore.getActionDuration() ));
    }

    // convert FTC field locaiton in inches to screen pixel locations
    Vector2d inch2Pixel (Vector2d inch) {
        int h = robotlayer.getHeight();
        int w = robotlayer.getWidth();
        double scale = Math.min(h, w) /  wgwSimCore.getFieldDimensionInches();
        int x = (int) (w / 2 + inch.x * scale);
        int y = (int) (h / 2 - inch.y * scale);
        return new Vector2d(x,y);
    }

    WGWsimCore wgwSimCore = new WGWsimCore() {
        @Override
        public void drawLineInches(Vector2d start, Vector2d end) {
            robotGraphic.drawLine((int)inch2Pixel(start).x, (int)inch2Pixel(start).y,
                    (int)inch2Pixel(end).x, (int)inch2Pixel(end).y);
        }

        @Override
        public void drawCircleInches(double xInches, double yInches, double rInches) {
            //todo
        }

        @Override
        public void setColor(String colorString) {
            Color color = Color.decode(colorString);
            robotGraphic.setColor(color);
        }

        @Override
        public void telemetryTextAddLine(String str) {
             textView.append(str);
        }

        @Override
        public void telemetryTextClear() {
            textView.setText("");  // got some new text so clear the display
        }
    };

    // user changes a path in the menu selection
    public void actionPathChange (ActionEvent e) {
        startNewRun = true;
    }


    // main method
    public static void main(String args[]) {
        // creating instance of Frame class
        //BeepBeepWin f = new BeepBeepWin();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BeepBeepWin f = new BeepBeepWin();
            }
        });
    }

}