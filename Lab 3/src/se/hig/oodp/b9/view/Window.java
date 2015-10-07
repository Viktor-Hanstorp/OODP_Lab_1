/**
 * 
 */
package se.hig.oodp.b9.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import se.hig.oodp.Vertex2D;
import se.hig.oodp.b9.data.PolyShape;
import se.hig.oodp.b9.data.Shape;
import se.hig.oodp.b9.model.PrimitivesPainter;
import se.hig.oodp.b9.model.ShapeControl;
import se.hig.oodp.b9.view.Pair.PairConstraintTypes;

public class Window extends JFrame
{
    private ShapeControl shapeControl;

    private abstract class Canvas extends JPanel implements PrimitivesPainter
    {

    }

    private JPanel contentPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    Window frame = new Window();
                    frame.setVisible(true);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showLoadShapesDialog()
    {
        try
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Load shapes");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Shapes list", "shp");
            fileChooser.setFileFilter(filter);
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileHidingEnabled(false);

            if (fileChooser.showOpenDialog(contentPane.getParent()) == JFileChooser.APPROVE_OPTION)
            {
                File fileToLoad = fileChooser.getSelectedFile();
                if (!fileToLoad.exists() && !fileToLoad.getName().endsWith(".shp"))
                {
                    JOptionPane.showMessageDialog(contentPane.getParent(), "You have to load an .shp file!", "Wrong file type!", JOptionPane.WARNING_MESSAGE);
                }
                else
                {
                    loadShapes(fileToLoad);
                }
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(contentPane.getParent(), "Error while loading file!\n\n" + e.getMessage(), "Error on loading!", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void loadShapes(File fileToLoad) throws Exception
    {
        FileInputStream fileIn = new FileInputStream(fileToLoad);
        ObjectInputStream in = new ObjectInputStream(fileIn);
        Shape[] newShapes = (Shape[]) in.readObject();
        in.close();
        fileIn.close();
        shapeControl.loadShapes(newShapes);
    }

    public void showSaveShapesDialog()
    {
        try
        {
            File file = saveFileDialog("Save shapes", "shp");
            saveShapes(file);
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(contentPane.getParent(), "Error on saving!\n\n" + e.getMessage(), "Error!", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void saveShapes(File file) throws Exception
    {
        // http://www.tutorialspoint.com/java/java_serialization.htm

        if (file == null)
            throw new Exception("No file chosen");
        file.createNewFile();
        FileOutputStream fileOut = new FileOutputStream(file);
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(shapeControl.getShapes());
        out.close();
        fileOut.close();
    }

    /**
     * Create the frame.
     */
    public Window()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 800);
        setLocationRelativeTo(null);

        contentPane = new Canvas()
        {
            private Graphics2D g;

            @Override
            public void paint(Graphics g)
            {
                super.paint(g);
                this.g = (Graphics2D) g;

                System.out.println("DRAW!");

                for (Shape s : shapeControl.getShapes())
                {
                    g.setColor(Color.RED);
                    AffineTransform transform = this.g.getTransform();
                    s.draw(this);
                    this.g.setTransform(transform);
                }
            }

            @Override
            public void paintPolygon(Vertex2D[] nodes)
            {
                System.out.println("DRAW poly");

                int[] xPoints = new int[nodes.length];
                int[] yPoints = new int[nodes.length];

                for (int i = 0; i < nodes.length; i++)
                {
                    xPoints[i] = (int) nodes[i].getX();
                    yPoints[i] = (int) nodes[i].getY();
                }

                g.fillPolygon(xPoints, yPoints, nodes.length);
            }

            @Override
            public void paintEllipse(Vertex2D center, double width, double height, double rotation)
            {
                g.rotate(Math.toRadians(rotation), center.getX(), center.getY());
                g.fillOval((int) center.getX() - (int) width / 2, (int) center.getY() - (int) height / 2, (int) width, (int) height);
            }

            @Override
            public void paintLine(Vertex2D a, Vertex2D b)
            {
                g.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
            }
        };

        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));

        shapeControl = new ShapeControl()
        {
            @Override
            public void onChange()
            {
                contentPane.repaint();
            }
        };

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmLoad = new JMenuItem("Load");
        mntmLoad.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                showLoadShapesDialog();
            }
        });
        mnFile.add(mntmLoad);

        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                showSaveShapesDialog();
            }
        });
        mnFile.add(mntmSave);

        JMenuItem mntmExport = new JMenuItem("Export");
        mntmExport.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                // http://stackoverflow.com/a/1349264

                int w = contentPane.getWidth();
                int h = contentPane.getHeight();
                BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = bi.createGraphics();
                contentPane.paint(g);

                try
                {
                    File file = saveFileDialog("Export to image", "png");
                    ImageIO.write(bi, "png", file);
                }
                catch (Exception exception)
                {
                    JOptionPane.showMessageDialog(contentPane.getParent(), "Could not export to image!\n\n" + exception.getMessage(), "IOException!", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        mnFile.add(mntmExport);

        JMenu mnCreate = new JMenu("Create");
        menuBar.add(mnCreate);

        JMenuItem mntmPoint = new JMenuItem("Point");
        mntmPoint.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> center = new Pair<Vertex2D>("Center", Pair.PairDataTypes.VERTEX2D);
                arr.add(center);

                if (InitParamsDialog.showDialog("Create point", arr))
                {
                    shapeControl.createPoint(center.value.getX(), center.value.getY());
                }
            }
        });
        mnCreate.add(mntmPoint);

        JMenuItem mntmLine = new JMenuItem("Line");
        mntmLine.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> nodeA = new Pair<Vertex2D>("Node A", Pair.PairDataTypes.VERTEX2D);
                arr.add(nodeA);

                Pair<Vertex2D> nodeB = new Pair<Vertex2D>("Node B", Pair.PairDataTypes.VERTEX2D);
                arr.add(nodeB);

                if (InitParamsDialog.showDialog("Create line", arr))
                {
                    shapeControl.createLine(nodeA.value.getX(), nodeA.value.getY(), nodeB.value.getX(), nodeB.value.getY());
                }
            }
        });
        mnCreate.add(mntmLine);

        JMenuItem mntmTriangle = new JMenuItem("Triangle");
        mntmTriangle.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> nodeA = new Pair<Vertex2D>("Node A", Pair.PairDataTypes.VERTEX2D);
                arr.add(nodeA);

                Pair<Vertex2D> nodeB = new Pair<Vertex2D>("Node B", Pair.PairDataTypes.VERTEX2D);
                arr.add(nodeB);

                Pair<Vertex2D> nodeC = new Pair<Vertex2D>("Node C", Pair.PairDataTypes.VERTEX2D);
                arr.add(nodeC);

                if (InitParamsDialog.showDialog("Create triangle", arr))
                {
                    shapeControl.createTriangle(nodeA.value.getX(), nodeA.value.getY(), nodeB.value.getX(), nodeB.value.getY(), nodeC.value.getX(), nodeC.value.getY());
                }
            }
        });
        mnCreate.add(mntmTriangle);

        JMenuItem mntmRectangle = new JMenuItem("Rectangle");
        mntmRectangle.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> center = new Pair<Vertex2D>("Center", Pair.PairDataTypes.VERTEX2D);
                arr.add(center);

                Pair<Integer> width = new Pair<Integer>("Width", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(width);

                Pair<Integer> height = new Pair<Integer>("Height", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(height);

                Pair<Integer> rotation = new Pair<Integer>("Rotation", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(rotation);

                if (InitParamsDialog.showDialog("Create rectangle", arr))
                {
                    shapeControl.createRectangle(center.value.getX(), center.value.getY(), width.value, height.value, rotation.value);
                }
            }
        });
        mnCreate.add(mntmRectangle);

        JMenuItem mntmSquare = new JMenuItem("Square");
        mntmSquare.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> center = new Pair<Vertex2D>("Center", Pair.PairDataTypes.VERTEX2D);
                arr.add(center);

                Pair<Integer> size = new Pair<Integer>("Size", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(size);

                Pair<Integer> rotation = new Pair<Integer>("Rotation", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(rotation);

                if (InitParamsDialog.showDialog("Create square", arr))
                {
                    shapeControl.createSquare(center.value.getX(), center.value.getY(), size.value, rotation.value);
                }
            }
        });
        mnCreate.add(mntmSquare);

        JMenuItem mntmEllipse = new JMenuItem("Ellipse");
        mntmEllipse.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> center = new Pair<Vertex2D>("Center", Pair.PairDataTypes.VERTEX2D);
                arr.add(center);

                Pair<Integer> width = new Pair<Integer>("Width", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(width);

                Pair<Integer> height = new Pair<Integer>("Height", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(height);

                Pair<Integer> rotation = new Pair<Integer>("Rotation", Pair.PairDataTypes.INTEGER, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(rotation);

                if (InitParamsDialog.showDialog("Create ellipse", arr))
                {
                    shapeControl.createEllipse(center.value.getX(), center.value.getY(), width.value, height.value, rotation.value);
                }
            }
        });
        mnCreate.add(mntmEllipse);

        JMenuItem mntmCircle = new JMenuItem("Circle");
        mntmCircle.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> center = new Pair<Vertex2D>("Center", Pair.PairDataTypes.VERTEX2D);
                arr.add(center);

                Pair<Integer> size = new Pair<Integer>("Size", Pair.PairDataTypes.INTEGER, Pair.PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(size);

                if (InitParamsDialog.showDialog("Create circle", arr))
                {
                    shapeControl.createCircle(center.value.getX(), center.value.getY(), size.value);
                }
            }
        });
        mnCreate.add(mntmCircle);

        JMenu mnAction = new JMenu("Action");
        menuBar.add(mnAction);

        JMenuItem mntmPrintAll = new JMenuItem("Print All");
        mnAction.add(mntmPrintAll);

        JMenuItem mntmMoveAll = new JMenuItem("Move All");
        mntmMoveAll.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Vertex2D> center = new Pair<Vertex2D>("Offset", Pair.PairDataTypes.VERTEX2D);
                arr.add(center);

                if (InitParamsDialog.showDialog("Move all", arr))
                {
                    shapeControl.moveAll(center.value.getX(), center.value.getY());
                }
            }
        });
        mnAction.add(mntmMoveAll);

        JMenuItem mntmScaleAll = new JMenuItem("Scale All");
        mntmScaleAll.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Double> scaleX = new Pair<Double>("Offset x", Pair.PairDataTypes.DOUBLE, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(scaleX);

                Pair<Double> scaleY = new Pair<Double>("Offset y", Pair.PairDataTypes.DOUBLE, PairConstraintTypes.POSITIVE_NOT_ZERO);
                arr.add(scaleY);

                if (InitParamsDialog.showDialog("Scale all", arr))
                {
                    shapeControl.scaleAll(scaleX.value, scaleY.value);
                }
            }
        });
        mnAction.add(mntmScaleAll);

        JMenuItem mntmRotateAll = new JMenuItem("Rotate All");
        mntmRotateAll.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                ArrayList<Pair<?>> arr = new ArrayList<>();

                Pair<Integer> angle = new Pair<Integer>("Angle", Pair.PairDataTypes.INTEGER);
                arr.add(angle);

                if (InitParamsDialog.showDialog("Rotate all", arr))
                {
                    shapeControl.rotateAll(angle.value);
                }
            }
        });
        mnAction.add(mntmRotateAll);

        JMenuItem mntmRemoveAll = new JMenuItem("Remove All");
        mntmRemoveAll.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                shapeControl.removeAll();
            }
        });
        mnAction.add(mntmRemoveAll);

        setContentPane(contentPane);

        if (false)
        {
            shapeControl.createLine(0, 0, 200, 200);
            shapeControl.createLine(200, 0, 0, 200);
            shapeControl.createEllipse(100, 100, 100, 20);

            shapeControl.rotateAll(45);

            try
            {
                saveShapes(new File("H:/test.shp"));
            }
            catch (Exception e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
        else
        {
            try
            {
                loadShapes(new File("H:/test.shp"));
            }
            catch (Exception e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private File saveFileDialog(String title, String fileEnding) throws Exception
    {
        // http://www.codejava.net/java-se/swing/show-save-file-dialog-using-jfilechooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(fileEnding, fileEnding);
        fileChooser.setFileFilter(filter);
        fileChooser.addChoosableFileFilter(filter);

        if (fileChooser.showSaveDialog(contentPane.getParent()) == JFileChooser.APPROVE_OPTION)
        {
            File fileToSave = fileChooser.getSelectedFile();
            if (fileToSave.exists() && !fileToSave.getName().endsWith("." + fileEnding))
            {
                throw new Exception("You have to save as ." + fileEnding + " !");
            }
            else
            {
                String fileName = fileToSave.getAbsolutePath();
                if (!fileName.endsWith("." + fileEnding))
                    fileName = fileName + "." + fileEnding;

                fileToSave = new File(fileName);
                fileToSave.createNewFile();
                return fileToSave;
            }
        }
        else
            return null;
    }
}
