

import ij.IJ;
import ij.plugin.PlugIn;
import ij.plugin.RoiEnlarger;
import ij.plugin.filter.RankFilters;


import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;

import java.io.IOException;
import java.io.InputStream;

import ij.gui.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import ij.io.FileSaver;
import java.util.List;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.io.SaveDialog;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import java.util.Arrays;
import java.io.File;

import ij.io.DirectoryChooser;
import ij.io.FileSaver;
import ij.plugin.FolderOpener;
import ij.plugin.frame.RoiManager;
import ij.plugin.frame.Recorder;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import ij.process.ImageStatistics;
import ij.process.StackConverter;

import java.util.stream.IntStream;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.image.ColorModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ij.WindowManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;


import ij.ImagePlus;
import ij.plugin.frame.PlugInFrame;
import ij.plugin.FolderOpener;







public class AR_IA implements PlugIn {
    private static int sValue = 255; // Default saturation value
    private static boolean counterClockwise = true; // Default direction for hue adjustment
    private static boolean hueBarInVideo = true;
    private static boolean oneFrame = false;
    private static boolean allFrame = false;
    private static float minZoom = -0.2f;
    private static float maxZoom = 0.4f;
    private static float theoreticalMinimum = 0.0f;
    private static float theoreticalMaximum = 0.0f;
    private static float hueStartColour = 0.3f;
    private static float hueEndColour = 0.86f;
    private static float MedianFilterRadius = 3.0f;
    private static float zoomMin = 0f;
    private static float zoomMax = 1f;
    private static float brightnessMinimum = 0f;
    private static float brightnessMaximum = 1f;
    private static float brightnessMultiplier = 1.0f;
    private static float brightnessExponent = 1.0f;
    private static float maxHistoValue = 0.2f;
    private String sampleName = "";
    private JTextField sampleNameField;   // IMPORTANT: also global
    private static String savePath;
    private static String inputCellpose;
    private static ImagePlus pixvidone;
    private static ImagePlus pixvidtwo;
    private static ImagePlus imp1;
    private static ImagePlus imp2;
    private static ImagePlus parallelImp; // Image for the parallel channel
    private static ImagePlus cellposeImp;
    private static ImagePlus perpendicularImp; // Image for the perpendicular channel
    private static ImagePlus anisotropyResult;
    private static ImagePlus denominatorResult;
    private static ImagePlus totalIntensityResult;
    private static ImagePlus ratioResult;
    private static ImagePlus imp1Storage;
    private static ImagePlus imp2Storage;
    private static ImagePlus anisotropyStack;
    private static ImagePlus denominatorStack;
    private static ImagePlus donorImp;
    private static ImagePlus toFilterImp;
    private static ImagePlus acceptorImp;
    private static ImagePlus impToTrack;
    
    private static final String PYTHON_PREF_KEY = "fretanalyzer.pythonPath";
    private String userProvidedPath = null;


    private static ImagePlus primaryVideo;
    private static ImagePlus secondaryVideo;
    private static float lowerTreshold = 0.0f;
    private static float upperTreshold = Float.MAX_VALUE;
    private static ImagePlus imp1ToUse;
    private static ImagePlus imp2ToUse;
    private static float theoMin = 0.0f;
    private static float theoMax = 1.0f;
  private static ImagePlus pixelValueImp;
  private static ImagePlus pixelImp;
  private static ImagePlus treshholdImp;
    private static float brightnessValue = 0.0f;
    private String filePathDenominator; // To store the path for the denominator result
    private String filePathAnisotropy;  // To s
    
    private String outputDir;  // Instance variable for output directory
    private String diameterValue;  // Instance variable for diameter
    private volatile boolean running = true; // To control the running status of CellPose
    
   private static double gFactor = 1;
   private static double bgParallel = 0;
   private static double bgPerpendicular = 0;
   private static double fCorr = 1;
   

    


    @Override
    
    public void run(String arg) {
        createMainMenu();
    }

    private void createMainMenu() {    	 // Create a frame to contain the buttons
    	
    	userProvidedPath = Prefs.get(PYTHON_PREF_KEY, null);

        JFrame frame = new JFrame("Select Video Processing Option");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLayout(new BorderLayout());

        // Create a panel to hold the components and center them
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // Stack components vertically

        // Create a horizontal panel for "Sample Name" label and text field
        JPanel sampleNamePanel = new JPanel();
        sampleNamePanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Center the label and text field

        // Create a label and text field for "Sample Name"
        JLabel sampleNameLabel = new JLabel("Sample Name:");
        sampleNameField = new JTextField(30);
        sampleNameField.setText(sampleName);   // restore previous name
        sampleNameField.setPreferredSize(new Dimension(10, 20)); // Set preferred size (width, height)

        // Add the label and text field to the horizontal panel
        sampleNamePanel.add(sampleNameLabel);
        sampleNamePanel.add(sampleNameField);

        // Add the horizontal panel to the main panel
        panel.add(sampleNamePanel);

        // Create "Create HSB Video" button
        JButton hsbButton = new JButton("Create Hue-Brightness image");
        hsbButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        hsbButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        hsbButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        hsbButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	sampleName = sampleNameField.getText().trim();
                frame.dispose(); // Close the window
                select_H_and_B(); // Call the HSB video creation method
            }
        });

        // Create "Create anisotropy Video" button
        JButton anisotropyButton = new JButton("Create anisotropy and total brightness image");
        anisotropyButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        anisotropyButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        anisotropyButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        anisotropyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                createGrayscaleVideo(); // Call the anisotropy video creation method
            }
       
        });
        // Create "Create ratio Video" button
        JButton ratioButton = new JButton("Create ratio and total brightness image");
        ratioButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        ratioButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        ratioButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        ratioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                createFretRatioVideo(); // Call the anisotropy video creation method
            }
       
        });


        JButton cellposeButton = new JButton("Create ROI's with cellpose and apply for graph of pixelvalue");
        cellposeButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        cellposeButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        cellposeButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        cellposeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                CellPoseSegmentation(); // Call the anisotropy video creation method
            }
       
        });
        
        JButton graphButton = new JButton("Create pixel value graph by selecting stored ROI's");
        graphButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        graphButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        graphButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        graphButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                applyROIsToVideo(); // Call the anisotropy video creation method
            }
       
        });
        
        JButton ManualGraphButton = new JButton("Create pixel value graph by manual drawn ROI");
        ManualGraphButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        ManualGraphButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        ManualGraphButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        ManualGraphButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window

                DisplayAnisoGraph(); // Call the anisotropy video creation method
            }
       
        });
        // Create "Create median filtered video" button
        JButton filterButton = new JButton("Create median filtered images");
        filterButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        filterButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        filterButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                createMedianFilteredImage(); // Call the filter video creation method
            }
       
        });
        
        // Create "turn tiffs into csv" button
        JButton csvButton = new JButton("Turn your tif graphs into csv (batch)");
        csvButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        csvButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        csvButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        csvButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                TifToCsv(); // Call the anisotropy video creation method
            }
       
        });
        
        // Create "Track Roi with maths" button
        JButton RoiTrackerButton = new JButton("Track one ROI with math instead of AI");
        RoiTrackerButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        RoiTrackerButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        RoiTrackerButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        RoiTrackerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                RoiTracker(); // Call the anisotropy video creation method
            }
       
        });
        
        // Create pixel,value,value list
        JButton valueperpixelButton = new JButton("get value of each pixel of one frame");
        valueperpixelButton.setPreferredSize(new Dimension(200, 40)); // Set preferred button size
        valueperpixelButton.setMinimumSize(new Dimension(200, 40)); // Set minimum button size
        valueperpixelButton.setMaximumSize(new Dimension(400, 40)); // Set maximum button size
        valueperpixelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sampleName = sampleNameField.getText().trim(); // Retrieve and store the sample name
                frame.dispose(); // Close the window
                valueperpixel(); // Call the pixel value method
            }
       
        });
        // Create a panel to center the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS)); // Stack buttons vertically
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center the button panel

        // Add buttons to the button panel
  
        buttonPanel.add(anisotropyButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(ratioButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(hsbButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between buttons
        
        buttonPanel.add(cellposeButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(graphButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(ManualGraphButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(filterButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(csvButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(RoiTrackerButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add space between button
        
        buttonPanel.add(valueperpixelButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Add the button panel to the main panel
        panel.add(buttonPanel);

        // Add the main panel to the frame
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null); // Center the frame on the screen
        frame.setVisible(true);
    }
    private JFrame buttonPanelFrame; 
    
    private void valueperpixel() {
    	
    	DirectoryChooser dc = new DirectoryChooser("Select folder to save csv with pixel values");
        String folderPath = dc.getDirectory();
        if (folderPath == null) {
            IJ.showMessage("No folder selected.");
            return;
        }

        File folder = new File(folderPath);
    	
    	
        ImagePlus pixvalimpone = null;
    	ImagePlus pixvalimptwo = null;

    	int[] windowIDs = WindowManager.getIDList();

    	// If no images are open, present file selection dialogs
    	if (windowIDs == null || windowIDs.length == 0) {
    	    OpenDialog pvio = new OpenDialog("Select the donor channel", "");
    	    String filePathpvio = pvio.getPath();
    	    if (filePathpvio == null) {
    	        createMainMenu();
    	        return;
    	    }

    	    OpenDialog pvit = new OpenDialog("Select the acceptor channel", "");
    	    String filePathpvit = pvit.getPath();
    	    if (filePathpvit == null) {
    	        createMainMenu();
    	        return;
    	    }

    	    // Open the selected images and assign them to variables
    	    pixvalimpone = IJ.openImage(filePathpvio); // First movie (parallel channel)
    	    pixvalimptwo = IJ.openImage(filePathpvit); // Second movie (perpendicular channel)

    	    if (pixvalimptwo == null || pixvalimpone == null) {
    	        IJ.error("Error", "Could not open one or both of the selected images.");
    	      
    	            createMainMenu();
    	            
    	        return;
    	    }

    	    // Now add these images to the WindowManager so they can be selected in the dialog
    	    pixvalimpone.show();
    	    pixvalimptwo.show();
    	}


   	    // At this point, whether we opened new files or images were already open, we show the parameter dialog
   	    windowIDs = WindowManager.getIDList(); // Re-fetch the window IDs to include any newly opened images
   	    GenericDialog gd = new GenericDialog("Select Images");

   	    // Populate dropdown lists with titles of open images
   	    String[] imageTitles = new String[windowIDs.length];
   	    for (int i = 0; i < windowIDs.length; i++) {
   	        ImagePlus img = WindowManager.getImage(windowIDs[i]);
   	        imageTitles[i] = img != null ? img.getTitle() : "";
   	    }

   	    // Allow the user to select from open images
   	    gd.addChoice("Select first image Channel:", imageTitles, imageTitles.length > 0 ? imageTitles[0] : "");
   	    gd.addChoice("Select second image Channel:", imageTitles, imageTitles.length > 1 ? imageTitles[1] : "");

  
   	    gd.addNumericField("Background image one:", 0.0, 2);
   	    gd.addNumericField("Background image two:", 0.0, 2);


   	    gd.showDialog();
   	    if (gd.wasCanceled()) {
   	     createMainMenu();
   	        return;
   	    }

   	// Get selected choices and parameters
   	 String selectedpiximpone = gd.getNextChoice();
   	 String selectedpiximptwo = gd.getNextChoice();

   	 double bgDonor = gd.getNextNumber();
   	 double bgAcceptor = gd.getNextNumber();

   	 // Get the selected images
   	 ImagePlus pixvalimgone = WindowManager.getImage(selectedpiximpone);
   	 ImagePlus pixvalimgtwo = WindowManager.getImage(selectedpiximptwo);

   	 if (pixvalimgone == null || pixvalimgtwo == null) {
   	     IJ.error("Error loading images.");
   	     return;
   	 }

   	 ImageStack stack1 = pixvalimgone.getStack();
   	 ImageStack stack2 = pixvalimgtwo.getStack();

        int frames = stack1.getSize();

        File csvFile = new File(folderPath + File.separator + "pixel_values_video.csv");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {

            writer.write("frame,pixel_index,image1,image2\n");

            for (int f = 1; f <= frames; f++) {

                IJ.showProgress(f, frames);

                float[] pixels1 = (float[]) stack1.getProcessor(f).getPixels();
                float[] pixels2 = (float[]) stack2.getProcessor(f).getPixels();

                int pixelCount = pixels1.length;

                StringBuilder buffer = new StringBuilder(pixelCount * 30);

                for (int i = 0; i < pixelCount; i++) {

                    buffer.append(f)
                          .append(',')
                          .append(i)
                          .append(',')
                          .append(pixels1[i])
                          .append(',')
                          .append(pixels2[i])
                          .append('\n');
                }

                writer.write(buffer.toString());
            }

            IJ.showProgress(1.0);
            IJ.showMessage("CSV saved at:\n" + csvFile.getAbsolutePath());

        } catch (IOException ex) {
            IJ.showMessage("Error writing CSV: " + ex.getMessage());
        }

        createMainMenu();
    }




    private void TifToCsv() {
    	   DirectoryChooser dc = new DirectoryChooser("Select folder with TIFF files");
           String folderPath = dc.getDirectory();
           if (folderPath == null) {
               IJ.showMessage("No folder selected.");
               return;
           }

           File folder = new File(folderPath);
           File[] files = folder.listFiles((dir, name) ->
                   name.toLowerCase().endsWith(".tif") || name.toLowerCase().endsWith(".tiff")
           );

           if (files == null || files.length == 0) {
               IJ.showMessage("No TIFF files found in the selected folder.");
           	createMainMenu();

               return;
           }

           List<float[]> allYData = new ArrayList<>();
           List<String> fileNames = new ArrayList<>();
           int maxLength = 0;

           for (File file : files) {
               try {
                   ImagePlus imp = IJ.openImage(file.getAbsolutePath());
                   if (imp == null) {
                       IJ.log("Could not open: " + file.getName());
                       continue;
                   }

                   imp.show();
                   
                   // Check if there's a plot window associated with this image
                   // or if the image itself contains plot data
                   Plot plot = null;
                   
                   // Try to get plot from the current window
                   if (WindowManager.getCurrentWindow() instanceof ij.gui.PlotWindow) {
                       ij.gui.PlotWindow pw = (ij.gui.PlotWindow) WindowManager.getCurrentWindow();
                       plot = pw.getPlot();
                   } else {
                       // If the TIFF contains embedded plot data, we need to extract it differently
                       // This is a simplified approach - you may need to adjust based on your specific TIFF format
                       IJ.log("No plot data found in: " + file.getName());
                       imp.close();
                       continue;
                   }

                   if (plot != null) {
                       // Get the plot values
                       float[] xValues = plot.getXValues();
                       float[] yValues = plot.getYValues();
                       
                       if (yValues != null && yValues.length > 0) {
                           if (yValues.length > maxLength) {
                               maxLength = yValues.length;
                           }
                           allYData.add(yValues);
                           fileNames.add(file.getName());
                           IJ.log("Processed: " + file.getName() + " (" + yValues.length + " points)");
                       } else {
                           IJ.log("No Y values found in: " + file.getName());
                       }
                   }
                   
                   imp.close();
                   
               } catch (Exception e) {
                   IJ.log("Error processing " + file.getName() + ": " + e.getMessage());
               }
           }

           if (allYData.isEmpty()) {
               IJ.showMessage("No plot data found in any TIFF files.");
               return;

           }

           // Save all Y data to CSV
           File csvFile = new File(folderPath + File.separator + "all_y_data.csv");
           try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
               // Write header with file names
               for (int i = 0; i < fileNames.size(); i++) {
                   writer.write(fileNames.get(i));
                   if (i < fileNames.size() - 1) writer.write(",");
               }
               writer.newLine();
               
               // Write data rows
               for (int row = 0; row < maxLength; row++) {
                   for (int col = 0; col < allYData.size(); col++) {
                       float[] yValues = allYData.get(col);
                       if (row < yValues.length) {
                           writer.write(String.valueOf(yValues[row]));
                       }
                       if (col < allYData.size() - 1) writer.write(",");
                   }
                   writer.newLine();
               }
               IJ.showMessage("CSV saved at: " + csvFile.getAbsolutePath() + 
                             "\nProcessed " + allYData.size() + " files.");
           } catch (IOException e) {
               IJ.showMessage("Error writing CSV: " + e.getMessage());
           }
    	createMainMenu();
    }
    
    private void RoiTracker() {
    	   int[] windowIDs = WindowManager.getIDList();

           // If no images are open, let the user pick one
           if (windowIDs == null || windowIDs.length == 0) {
               OpenDialog od1 = new OpenDialog("Select the donor channel", "");
               String filePathdonor = od1.getPath();
               if (filePathdonor == null) return;

               impToTrack = IJ.openImage(filePathdonor);
               if (impToTrack == null) return;

               impToTrack.show();
           } else {
        	   impToTrack = IJ.getImage();
           }

           // Open a side panel with inputs and Continue button
           SwingUtilities.invokeLater(() -> showControlPanel());
       }

       // ----------------- Side panel -----------------
       private void showControlPanel() {
           JDialog dialog = new JDialog((Frame) null, "ROI Control", false);
           dialog.setLayout(new FlowLayout());

           JLabel label1 = new JLabel("Draw an ROI, then set pixel radius:");
           JTextField radiusField = new JTextField("5", 5); // default radius

           JLabel label2 = new JLabel("Set threshold for optimal ROI:");
           JTextField thresholdField = new JTextField("0", 5); // default threshold

           JButton continueButton = new JButton("Continue");
           JButton cancelButton = new JButton("Cancel");

           continueButton.addActionListener(e -> {
               try {
                   int pixelRadius = Integer.parseInt(radiusField.getText());
                   double threshold = Double.parseDouble(thresholdField.getText());
                   processROI(pixelRadius, threshold);
                   dialog.dispose();
               } catch (NumberFormatException ex) {
                   IJ.showMessage("Please enter valid numeric values for radius and threshold.");
               }
           });

           cancelButton.addActionListener(e -> dialog.dispose());

           dialog.add(label1);
           dialog.add(radiusField);
           dialog.add(label2);
           dialog.add(thresholdField);
           dialog.add(continueButton);
           dialog.add(cancelButton);

           dialog.pack();
           dialog.setLocation(100, 100);
           dialog.setVisible(true);
       }

       // ----------------- First frame processing -----------------
       private void processROI(int pixelRadius, double threshold) {
           if (impToTrack == null) {
               IJ.showMessage("No image available.");
               return;
           }

           Roi roi = impToTrack.getRoi();
           if (roi == null) {
               IJ.showMessage("Please draw an ROI before pressing Continue.");
               return;
           }

           // Enlarge ROI using user-defined pixel radius
           Roi enlargedRoi = RoiEnlarger.enlarge(roi, pixelRadius);
           if (enlargedRoi == null) {
               IJ.showMessage("Unable to enlarge ROI.");
               return;
           }

           ShapeRoi shapeOriginal = new ShapeRoi(roi);
           ShapeRoi shapeEnlarged = new ShapeRoi(enlargedRoi);
           ShapeRoi ringRoi = shapeEnlarged.not(shapeOriginal);

           RoiManager rm = RoiManager.getInstance();
           if (rm == null) rm = new RoiManager();

           rm.addRoi(roi);          // ROI #1
           rm.addRoi(enlargedRoi);  // ROI #2
           rm.addRoi(ringRoi);      // ROI #3

           // ----- Optimization with threshold -----
           Roi optimal = findOptimalRoiWithThreshold(impToTrack, roi, enlargedRoi, ringRoi, threshold);

           if (optimal != null) {
        	   impToTrack.setRoi(optimal);
        	   impToTrack.updateAndDraw();
               rm.addRoi(optimal); // ROI #4
           } else {
               // If threshold not exceeded, ROI stays at start
        	   impToTrack.setRoi(roi);
               rm.addRoi(roi); // still add as ROI #4
               IJ.log("No optimal ROI exceeded threshold; keeping initial position.");
           }

           // ----------------- Track rest of stack -----------------
           trackStack(pixelRadius, threshold);
       }

       // ----------------- Frame-by-frame tracking -----------------
       private void trackStack(int pixelRadius, double threshold) {
           if (impToTrack == null || impToTrack.getStackSize() < 2) return;

           RoiManager rm = RoiManager.getInstance();
           if (rm == null) rm = new RoiManager();

           Roi previousOptimal = impToTrack.getRoi(); // ROI #4 from first frame
           if (previousOptimal == null) {
               IJ.showMessage("Please run processROI() on the first frame to generate ROI #4.");
               return;
           }

           int nFrames = impToTrack.getStackSize();

           for (int frame = 2; frame <= nFrames; frame++) {
        	   impToTrack.setSlice(frame);

               Roi smallRoi = previousOptimal;

               // Enlarge using user-defined pixel radius
               Roi enlargedRoi = RoiEnlarger.enlarge(smallRoi, pixelRadius);
               ShapeRoi shapeOriginal = new ShapeRoi(smallRoi);
               ShapeRoi shapeEnlarged = new ShapeRoi(enlargedRoi);
               ShapeRoi ringRoi = shapeEnlarged.not(shapeOriginal);

               // Optimize with threshold
               Roi optimal = findOptimalRoiWithThreshold(impToTrack, smallRoi, enlargedRoi, ringRoi, threshold);

               Roi roiToAdd;
               if (optimal != null) {
                   roiToAdd = optimal;
                   previousOptimal = optimal; // update for next frame
                   IJ.log("Frame " + frame + ": Optimal ROI calculated");
               } else {
                   // If threshold not exceeded, keep small ROI at current frame
                   roiToAdd = (Roi) smallRoi.clone(); // clone to avoid referencing previous frame
                   IJ.log("Frame " + frame + ": Threshold not exceeded, keeping initial ROI position.");
               }

               // Add to ROI Manager for this frame
               impToTrack.setRoi(roiToAdd);
               rm.addRoi(roiToAdd);
           }

           runMultiMeasureAndSaveCSV();

       }

       // ----------------- Helper function: optimization with threshold -----------------
       private Roi findOptimalRoiWithThreshold(ImagePlus imp, Roi smallRoi, Roi enlargedRoi, ShapeRoi ringRoi, double threshold) {
           Rectangle bounds = enlargedRoi.getBounds();
           Rectangle smallBounds = smallRoi.getBounds();

           int maxDx = bounds.width - smallBounds.width;
           int maxDy = bounds.height - smallBounds.height;

           double bestDiff = Double.NEGATIVE_INFINITY;
           Roi bestRoi = null;

           for (int dx = 0; dx <= maxDx; dx++) {
               for (int dy = 0; dy <= maxDy; dy++) {
                   // clone the original ROI shape
                   Roi shifted = (Roi) smallRoi.clone();
                   shifted.setLocation(bounds.x + dx, bounds.y + dy);

                   // Average pixel in small ROI
                   imp.setRoi(shifted);
                   ImageStatistics statsSmall = imp.getStatistics();
                   double meanSmall = statsSmall.mean;

                   // Average pixel in ring ROI
                   imp.setRoi(ringRoi);
                   ImageStatistics statsRing = imp.getStatistics();
                   double meanRing = statsRing.mean;

                   double diff = Math.abs(meanSmall - meanRing);

                   if (diff > bestDiff) {
                       bestDiff = diff;
                       bestRoi = shifted;
                   }
               }
           }

           if (bestDiff >= threshold) {
               IJ.log("Optimal ROI difference: " + bestDiff);
               return bestRoi;
           } else {
               return null;
           }
       }

       
       

       private void runMultiMeasureAndSaveCSV() {
           if (impToTrack == null) {
               IJ.showMessage("No image open.");
               return;
           }

           RoiManager rm = RoiManager.getInstance();
           if (rm == null || rm.getRoisAsArray().length == 0) {
               IJ.showMessage("No ROIs in the ROI Manager.");
               return;
           }

           // Run Multi Measure on all ROIs
           ResultsTable rt = new ResultsTable();
           IJ.runPlugIn("ij.plugin.MultipleMeasurement", ""); // optional alternative
           rm.runCommand("Measure");

           // Ask user for CSV save location
           JFileChooser fileChooser = new JFileChooser();
           fileChooser.setDialogTitle("Save ROI measurements as CSV");
           int userSelection = fileChooser.showSaveDialog(null);
           if (userSelection != JFileChooser.APPROVE_OPTION) return;

           File file = fileChooser.getSelectedFile();
           String path = file.getAbsolutePath();
           if (!path.toLowerCase().endsWith(".csv")) path += ".csv";

           // Save ResultsTable to CSV
           ResultsTable rtSaved = ResultsTable.getResultsTable();
           if (rtSaved == null) {
               IJ.showMessage("No measurements were made.");
               return;
           }

           try {
               rtSaved.save(path);
               IJ.showMessage("CSV saved to: " + path);
           } catch (Exception e) {
               IJ.showMessage("Error saving CSV: " + e.getMessage());
               e.printStackTrace();
           }
           createMainMenu();
    	
    }
    
    
    private void createMedianFilteredImage() {
        ImagePlus tempImp = null;

        int[] windowIDs = WindowManager.getIDList();

        // If no images are open, present file selection dialog
        if (windowIDs == null || windowIDs.length == 0) {
            OpenDialog odf = new OpenDialog("Select the file you want to median filter", "");
            String filePathFilter = odf.getPath();
            if (filePathFilter == null) {
            	createMainMenu();
            	return;
            }

            tempImp = IJ.openImage(filePathFilter);
            if (tempImp == null) {
                IJ.error("Error", "Could not open the selected image.");
                createMainMenu();
                return;
            }

            tempImp.show();
        }

        windowIDs = WindowManager.getIDList();
        String[] imageTitles = new String[windowIDs.length];
        for (int i = 0; i < windowIDs.length; i++) {
            ImagePlus img = WindowManager.getImage(windowIDs[i]);
            imageTitles[i] = img != null ? img.getTitle() : "";
        }

        GenericDialog gd = new GenericDialog("Select Image to Filter");
        gd.addChoice("Select file to filter:", imageTitles, imageTitles[0]);
        gd.addNumericField("Median Filter Radius:", 2.0, 1); // default radius
        gd.showDialog();
        if (gd.wasCanceled()) {
            createMainMenu();
            return;
        }


        String selectedTitle = gd.getNextChoice();
        double MedianFilterRadius = gd.getNextNumber();

        // Find the selected image by matching title
        ImagePlus toFilterImp = null;
        for (int id : windowIDs) {
            ImagePlus imp = WindowManager.getImage(id);
            if (imp != null && imp.getTitle().equals(selectedTitle)) {
                toFilterImp = imp;
                break;
            }
        }

        if (toFilterImp == null) {
            IJ.error("Error", "Image not found.");
            createMainMenu();
            return;
        }

        final ImagePlus finalToFilterImp = toFilterImp;  // effectively final for lambda

        int width = finalToFilterImp.getWidth();
        int height = finalToFilterImp.getHeight();
        int nFrames = finalToFilterImp.getStackSize();

        ImageStack filteredStack = new ImageStack(width, height);
        ConcurrentHashMap<Integer, ImageProcessor> filteredframeMap = new ConcurrentHashMap<>();

        IntStream.rangeClosed(1, nFrames).parallel().forEach(i -> {
            ImageProcessor ip = finalToFilterImp.getStack().getProcessor(i);
            FloatProcessor floatIp = (ip instanceof FloatProcessor) ? (FloatProcessor) ip : ip.convertToFloatProcessor();

            FloatProcessor filtered = (FloatProcessor) floatIp.duplicate();
            new RankFilters().rank(filtered, MedianFilterRadius, RankFilters.MEDIAN);

            filteredframeMap.put(i, filtered);
        });

        for (int i = 1; i <= nFrames; i++) {
            filteredStack.addSlice(filteredframeMap.get(i));
        }

        ImagePlus filteredOutput = new ImagePlus(finalToFilterImp.getTitle() + sampleName+ " - Median Filtered", filteredStack);
        filteredOutput.show();
        createMainMenu();
    }



   

    

    	
    
    private void createFretRatioVideo() {
    	ImagePlus donorImp = null;
    	ImagePlus acceptorImp = null;

    	int[] windowIDs = WindowManager.getIDList();

    	// If no images are open, present file selection dialogs
    	if (windowIDs == null || windowIDs.length == 0) {
    	    OpenDialog od1 = new OpenDialog("Select the donor channel", "");
    	    String filePathdonor = od1.getPath();
    	    if (filePathdonor == null) {
    	        createMainMenu();
    	        return;
    	    }

    	    OpenDialog od2 = new OpenDialog("Select the acceptor channel", "");
    	    String filePathacceptor = od2.getPath();
    	    if (filePathacceptor == null) {
    	        createMainMenu();
    	        return;
    	    }

    	    // Open the selected images and assign them to variables
    	    donorImp = IJ.openImage(filePathdonor); // First movie (parallel channel)
    	    acceptorImp = IJ.openImage(filePathacceptor); // Second movie (perpendicular channel)

    	    if (donorImp == null || acceptorImp == null) {
    	        IJ.error("Error", "Could not open one or both of the selected images.");
    	      
    	            createMainMenu();
    	            
    	        return;
    	    }

    	    // Now add these images to the WindowManager so they can be selected in the dialog
    	    donorImp.show();
    	    acceptorImp.show();
    	}


   	    // At this point, whether we opened new files or images were already open, we show the parameter dialog
   	    windowIDs = WindowManager.getIDList(); // Re-fetch the window IDs to include any newly opened images
   	    GenericDialog gd = new GenericDialog("Select Images");

   	    // Populate dropdown lists with titles of open images
   	    String[] imageTitles = new String[windowIDs.length];
   	    for (int i = 0; i < windowIDs.length; i++) {
   	        ImagePlus img = WindowManager.getImage(windowIDs[i]);
   	        imageTitles[i] = img != null ? img.getTitle() : "";
   	    }

   	    // Allow the user to select from open images
   	    gd.addChoice("Select donor Channel:", imageTitles, imageTitles.length > 0 ? imageTitles[0] : "");
   	    gd.addChoice("Select acceptor Channel:", imageTitles, imageTitles.length > 1 ? imageTitles[1] : "");

  
   	    gd.addNumericField("Background donor:", 0.0, 2);
   	    gd.addNumericField("Background acceptor:", 0.0, 2);


   	    gd.showDialog();
   	    if (gd.wasCanceled()) {
   	     createMainMenu();
   	        return;
   	    }

   	    // Get selected choices and parameters
   	    String selectedDonor = gd.getNextChoice();
   	    String selectedAcceptor = gd.getNextChoice();

   	    double bgDonor = gd.getNextNumber();
   	    double bgAcceptor = gd.getNextNumber();

   	    // Assign selected images based on the dialog
   	    donorImp = WindowManager.getImage(selectedDonor);    	            
   	    acceptorImp = WindowManager.getImage(selectedAcceptor);

   	    if (donorImp == null || acceptorImp == null) {
   	        IJ.error("Error loading images.");
 
   	// Convert to 32-bit grayscale if RGB
   	 if (donorImp.getType() == ImagePlus.COLOR_RGB) {
   	    if (donorImp.getStackSize() > 1) {
   	        new StackConverter(donorImp).convertToGray32();
   	    } else {
   	        new ImageConverter(donorImp).convertToGray32();
   	    }
   	}

   	if (acceptorImp.getType() == ImagePlus.COLOR_RGB) {
   	    if (acceptorImp.getStackSize() > 1) {
   	        new StackConverter(acceptorImp).convertToGray32();
   	    } else {
   	        new ImageConverter(acceptorImp).convertToGray32();
   	    }
   	}



   	    
             createMainMenu();
    
   	        return;
   	    }

   	    // Ensure both videos have the same dimensions and number of frames
   	    int width = donorImp.getWidth();
   	    int height = donorImp.getHeight();
   	    int nFrames = donorImp.getStackSize();

   	    if (width != acceptorImp.getWidth() || height != acceptorImp.getHeight() || nFrames != acceptorImp.getStackSize()) {
   	        IJ.error("The two videos must have the same dimensions and number of frames.");

            createMainMenu();
   	        return;
   	    }
   

        // Create new stacks for the ratio result and the denominator
        ImageStack ratioStack = new ImageStack(width, height);
        ImageStack totalIntensityStack = new ImageStack(width, height);

        // Loop through each frame to calculate ratio
        for (int frame = 1; frame <= nFrames; frame++) {
            IJ.showStatus("Processing frame " + frame + " of " + nFrames);

            // Get the current frames from each video
            ImageProcessor donorIp = donorImp.getStack().getProcessor(frame);
            ImageProcessor acceptorIp = acceptorImp.getStack().getProcessor(frame);

            // Create a new image processor for the anisotropy result
            FloatProcessor ratioIp = new FloatProcessor(width, height);
            FloatProcessor totalIntensityIp = new FloatProcessor(width, height);

            // Loop through each pixel
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Get the intensity values for parallel and perpendicular
                    float I_donor = donorIp.getf(x, y) - (float) bgDonor;
                    float I_acceptor = acceptorIp.getf(x, y) - (float) bgAcceptor;

                    // Calculate the total intensity
                    float totalIntensity = I_donor + I_acceptor;
                    totalIntensityIp.setf(x, y, totalIntensity);

                    // Calculate the ratio
                    float ratio = (I_donor != 0) ? I_acceptor / I_donor : 0;  // Set ratio to 0 if I_donor is 0
                    ratioIp.setf(x, y, ratio);
                }
            }

            // Add the image processors to their respective stacks
            ratioStack.addSlice(ratioIp);
            totalIntensityStack.addSlice(totalIntensityIp);
        }



        // Create a new ImagePlus to show the denominator result
        totalIntensityResult = new ImagePlus(sampleName + "_total_intensity", totalIntensityStack);
        totalIntensityResult.show();

   

    

    
        // Create a new ImagePlus to show the anisotropy result
        ratioResult = new ImagePlus(sampleName + "ratio", ratioStack);
        ratioResult.show();

        theoMin = (float) ratioResult.getDisplayRangeMin();
        theoMax = (float) ratioResult.getDisplayRangeMax();

        createMainMenu();
   }
   	    
  
     private void createGrayscaleVideo() {
    	 ImagePlus parallelImp = null;
    	 ImagePlus perpendicularImp = null;

    	 int[] windowIDs = WindowManager.getIDList();

    	 // If no images are open, present file selection dialogs
    	 if (windowIDs == null || windowIDs.length == 0) {
    	     OpenDialog od1 = new OpenDialog("Select the parallel channel", "");
    	     String filePathparallel = od1.getPath();
    	     if (filePathparallel == null) {
    	         createMainMenu();
    	         return;
    	     }

    	     OpenDialog od2 = new OpenDialog("Select the perpendicular channel", "");
    	     String filePathperpendicular = od2.getPath();
    	     if (filePathperpendicular == null) {
    	         createMainMenu();
    	         return;
    	     }

    	     // Open the selected images and assign them to variables
    	     parallelImp = IJ.openImage(filePathparallel);
    	     perpendicularImp = IJ.openImage(filePathperpendicular);

    	     if (parallelImp == null || perpendicularImp == null) {
    	         IJ.error("Error", "Could not open one or both of the selected images.");

                 createMainMenu();
    	         return;
    	     }

    	     // Add these images to the WindowManager so they can be selected later
    	     parallelImp.show();
    	     perpendicularImp.show();
    	 }


    	    // At this point, whether we opened new files or images were already open, we show the parameter dialog
    	    windowIDs = WindowManager.getIDList(); // Re-fetch the window IDs to include any newly opened images
    	    GenericDialog gd = new GenericDialog("Select Images");

    	    // Populate dropdown lists with titles of open images
    	    String[] imageTitles = new String[windowIDs.length];
    	    for (int i = 0; i < windowIDs.length; i++) {
    	        ImagePlus img = WindowManager.getImage(windowIDs[i]);
    	        imageTitles[i] = img != null ? img.getTitle() : "";
    	    }

    	    // Allow the user to select from open images
    	    gd.addChoice("Select Parallel Channel:", imageTitles, imageTitles.length > 0 ? imageTitles[0] : "");
    	    gd.addChoice("Select Perpendicular Channel:", imageTitles, imageTitles.length > 1 ? imageTitles[1] : "");

    	    // Add fields for additional parameters
    	    gd.addNumericField("G-factor:", gFactor, 2);
    	    gd.addNumericField("Background Parallel:", bgParallel, 2);
    	    gd.addNumericField("Background Perpendicular:", bgPerpendicular, 2);
    	    gd.addNumericField("F_corr:", fCorr, 2);

    	    gd.showDialog();
    	    if (gd.wasCanceled()) {
    	    	createMainMenu();
    	        return;
    	    }

    	    // Get selected choices and parameters
    	    String selectedParallel = gd.getNextChoice();
    	    String selectedPerpendicular = gd.getNextChoice();
    	    gFactor = gd.getNextNumber();
    	    bgParallel = gd.getNextNumber();
    	    bgPerpendicular = gd.getNextNumber();
    	    fCorr = gd.getNextNumber();

    	    // Assign selected images based on the dialog
    	    parallelImp = WindowManager.getImage(selectedParallel);    	            
    	    perpendicularImp = WindowManager.getImage(selectedPerpendicular);

    	    if (parallelImp == null || perpendicularImp == null) {
    	        IJ.error("Error loading images.");
    	        createMainMenu();
    	        return;
    	    }

    	    // Ensure both videos have the same dimensions and number of frames
    	    int width = parallelImp.getWidth();
    	    int height = parallelImp.getHeight();
    	    int nFrames = parallelImp.getStackSize();

    	    if (width != perpendicularImp.getWidth() || height != perpendicularImp.getHeight() || nFrames != perpendicularImp.getStackSize()) {
    	        IJ.error("The two videos must have the same dimensions and number of frames.");
    	        createMainMenu();
    	        return;
    	    }
    

         // Create new stacks for the anisotropy result and the denominator
         ImageStack anisotropyStack = new ImageStack(width, height);
         ImageStack denominatorStack = new ImageStack(width, height);

         // Loop through each frame to calculate anisotropy
         for (int frame = 1; frame <= nFrames; frame++) {
             IJ.showStatus("Processing frame " + frame + " of " + nFrames);

             // Get the current frames from each video
             ImageProcessor parallelIp = parallelImp.getStack().getProcessor(frame);
             ImageProcessor perpendicularIp = perpendicularImp.getStack().getProcessor(frame);

             // Create a new image processor for the anisotropy result
             FloatProcessor anisotropyIp = new FloatProcessor(width, height);
             FloatProcessor denominatorIp = new FloatProcessor(width, height);

             // Loop through each pixel
             // Loop through each pixel
             for (int y = 0; y < height; y++) {
                 for (int x = 0; x < width; x++) {
                     // Get the intensity values for parallel and perpendicular
                     float I_parallel = parallelIp.getf(x, y) - (float) bgParallel;
                     float I_perpendicular = perpendicularIp.getf(x, y) - (float) bgPerpendicular;

                     // Calculate the denominator
                     float denominator = I_parallel + 2 * (float) gFactor * I_perpendicular;
                     denominatorIp.setf(x, y, denominator);

                     // Calculate the anisotropy
                     float rnul = (denominator != 0) ? (I_parallel - (float) (gFactor * I_perpendicular)) / denominator : 0;
                     float r = (float) (rnul *fCorr);
                     anisotropyIp.setf(x, y, r);
                 }
             }

             // Add the image processors to their respective stacks
             anisotropyStack.addSlice(anisotropyIp);
             denominatorStack.addSlice(denominatorIp);
         }



         // Create a new ImagePlus to show the denominator result
         denominatorResult = new ImagePlus(sampleName + "_total_intensity", denominatorStack);
         denominatorResult.show();

    

     

     
         // Create a new ImagePlus to show the anisotropy result
         anisotropyResult = new ImagePlus(sampleName + "_Anisotropy", anisotropyStack);
         anisotropyResult.show();

         theoMin = (float) anisotropyResult.getDisplayRangeMin();
         theoMax = (float) anisotropyResult.getDisplayRangeMax();


         createMainMenu();
     }
     

    	public void select_H_and_B () {
    

    		    int[] windowIDs = WindowManager.getIDList();
    		 // If no images are open, present file selection dialogs
    		    if (windowIDs == null || windowIDs.length == 0) {
    		        OpenDialog od1 = new OpenDialog("Select the H channel", "");
    		        String filePathAnisotropy = od1.getPath();
    		        if (filePathAnisotropy == null) {
    		            createMainMenu();
    		            return;
    		        }

    		        OpenDialog od2 = new OpenDialog("Select the B channel", "");
    		        String filePathDenominator = od2.getPath();
    		        if (filePathDenominator == null) {
    		            createMainMenu();
    		            return;
    		        }

    		        // Open the selected images and assign them to variables
    		        imp1 = IJ.openImage(filePathAnisotropy); // First movie (H channel)
    		        imp2 = IJ.openImage(filePathDenominator); // Second movie (B channel)

    		        if (imp1 == null || imp2 == null) {
    		            IJ.error("Error", "Could not open one or both of the selected images.");

    		             createMainMenu();
    		            return;
    		        }
    		    
    		        // Show the opened images in ImageJ
    		        imp1.show();
    		        imp2.show();
    		        imp1Storage = imp1.duplicate();
                    imp2Storage = imp2.duplicate();
    		    }

    		    // At this point, whether we opened new files or images were already open, we show the parameter dialog
    		    windowIDs = WindowManager.getIDList(); // Re-fetch the window IDs to include any newly opened images
    		    GenericDialog gd = new GenericDialog("Select H and B Channels");

    		    // Populate dropdown lists with titles of open images
    		    String[] imageTitles = new String[windowIDs.length];
    		    for (int i = 0; i < windowIDs.length; i++) {
    		        ImagePlus img = WindowManager.getImage(windowIDs[i]);
    		        imageTitles[i] = img != null ? img.getTitle() : "";
    		    }

    		    // Allow the user to select from open images
    		    gd.addChoice("Select H Channel:", imageTitles, imageTitles.length > 0 ? imageTitles[0] : "");
    		    gd.addChoice("Select B Channel:", imageTitles, imageTitles.length > 1 ? imageTitles[1] : "");

    		    // Show the dialog
    		    gd.showDialog();
    		    if (gd.wasCanceled()) {
    		        return;
    		    }

    		    // Get selected choices
    		    String selectedHChannel = gd.getNextChoice();
    		    String selectedBChannel = gd.getNextChoice();

    		    // Assign selected images based on the dialog
    		    imp1 = WindowManager.getImage(selectedHChannel);
    		    imp2 = WindowManager.getImage(selectedBChannel);

    		    if (imp1 == null || imp2 == null) {
    		        IJ.error("Error loading images.");
    		        createMainMenu();
    		        return;
    		    }

    		    // Now you can proceed with the next steps, such as creating the HSB video
    		    create_HSB_video();
    		}

    	
    	
    	private void recreate_HSB_video () {
            // Assign selected images


         imp1 = imp1Storage;
         imp2 = imp2Storage;
    
      
      

            if (imp1 == null || imp2 == null) {
                IJ.error("Error", "Could not open one or both of the selected images.");
                createMainMenu();
                return;
            }
            create_HSB_video();
    	}

        private void create_HSB_video() {
        	
              
        // Step 3: Get the current display range (brightness/contrast range)

           
       minZoom = theoreticalMinimum = theoMin;
       maxZoom = theoreticalMaximum = theoMax;
       
       float BMin = (float) imp2.getDisplayRangeMin();
       float BMax = (float) imp2.getDisplayRangeMax();

      brightnessMinimum = BMin;
      brightnessMaximum = BMax;
      
        	        
        // Step 2: Create and show the custom Swing dialog for scaling
        JFrame frame = new JFrame("Adjust H and B");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setSize(1000, 1000); // Increased height to accommodate the new fields

        // Main panel to hold sliders, labels, text fields, and checkbox
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Padding around components
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        
        // Brightness slider
        // Brightness Multiplier Input
        JLabel multiplierLabel = new JLabel("Brightness Multiplier:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(multiplierLabel, gbc);

        JTextField multiplierField = new JTextField(String.valueOf(brightnessMultiplier));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(multiplierField, gbc);

        // Brightness Exponent Input
        JLabel exponentLabel = new JLabel("Brightness Exponent:");
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(exponentLabel, gbc);

        JTextField exponentField = new JTextField(String.valueOf(brightnessExponent));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(exponentField, gbc);
     // Add label for brightness visualization with subscript formatting
        JLabel visualizeLabel = new JLabel("<html>(B<sub>8-bit format </sub> * M)<sup>E</sup></html>");
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(visualizeLabel, gbc);

        
        JCheckBox visualizeCheckBox = new JCheckBox("visualize", false);
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(visualizeCheckBox, gbc);
        
        JLabel histoLabel = new JLabel("y-axis limit:");
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainPanel.add(histoLabel, gbc);
        
        JTextField histoField = new JTextField(String.valueOf(maxHistoValue));
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        mainPanel.add(histoField, gbc);

        // Min Zoom field
        JLabel minZoomLabel = new JLabel("Min hue value:");
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        mainPanel.add(minZoomLabel, gbc);

        JTextField minZoomField = new JTextField(String.valueOf(minZoom));
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        mainPanel.add(minZoomField, gbc);

        // Max Zoom field
        JLabel maxZoomLabel = new JLabel("Max hue value:");
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 1;
        mainPanel.add(maxZoomLabel, gbc);

        JTextField maxZoomField = new JTextField(String.valueOf(maxZoom));
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        mainPanel.add(maxZoomField, gbc);
        
     // Add label for color presets
        JLabel presetLabel = new JLabel("Select Hue preset:");
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        mainPanel.add(presetLabel, gbc);

        // Add checkboxes for color presets
        JCheckBox firstCheckBox = new JCheckBox("green-yellow-red-magenta", false);
        JCheckBox secondCheckBox = new JCheckBox("blue-magenta", false);
        JCheckBox fourthCheckBox = new JCheckBox("magenta-red-yellow-green", false); // New checkbox for opposite direction
        JCheckBox fifthCheckBox = new JCheckBox("magenta-blue", false); // New checkbox for opposite direction
        JCheckBox thirdCheckBox = new JCheckBox("show advanced settings", false);

        // Button group to ensure only one selection is possible
        ButtonGroup hueGroup = new ButtonGroup();
        hueGroup.add(firstCheckBox);
        hueGroup.add(secondCheckBox);
        hueGroup.add(fourthCheckBox); 
        hueGroup.add(fifthCheckBox); 
        hueGroup.add(thirdCheckBox);

        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 1;
        mainPanel.add(firstCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 9;
        mainPanel.add(secondCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        mainPanel.add(fourthCheckBox, gbc); // Position for new checkbox

        gbc.gridx = 1;
        gbc.gridy = 10;
        mainPanel.add(fifthCheckBox, gbc); // Position for new checkbox

        gbc.gridx = 2;
        gbc.gridy = 10;
        mainPanel.add(thirdCheckBox, gbc);

        
        // hue Start colour field
        JLabel hueStartLabel = new JLabel("Hue starting colour:");
        gbc.gridx = 0;
        gbc.gridy = 11;
        gbc.gridwidth = 1;
        mainPanel.add(hueStartLabel, gbc);

        JTextField hueStartField = new JTextField(String.valueOf(hueStartColour));
        gbc.gridx = 0;
        gbc.gridy = 12;
        gbc.gridwidth = 1;
        mainPanel.add(hueStartField, gbc);

        // hue end colour field
        JLabel hueEndLabel = new JLabel("hue ending colour:");
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.gridwidth = 1;
        mainPanel.add(hueEndLabel, gbc);

        JTextField hueEndField = new JTextField(String.valueOf(hueEndColour));
        gbc.gridx = 1;
        gbc.gridy = 12;
        gbc.gridwidth = 1;
        mainPanel.add(hueEndField, gbc);
        
        // Add checkbox for direction selection
        JLabel directionLabel = new JLabel("Select Hue direction in the colour circle:");
        gbc.gridx = 0;
        gbc.gridy = 13;
        gbc.gridwidth = 2;
        mainPanel.add(directionLabel, gbc);

        JCheckBox clockwiseCheckBox = new JCheckBox("Clockwise", false);
        JCheckBox counterClockwiseCheckBox = new JCheckBox("Counterclockwise", true);

        // Button group to ensure only one selection is possible
        ButtonGroup directionGroup = new ButtonGroup();
        directionGroup.add(clockwiseCheckBox);
        directionGroup.add(counterClockwiseCheckBox);

        gbc.gridx = 0;
        gbc.gridy = 14;
        gbc.gridwidth = 1;
        mainPanel.add(clockwiseCheckBox, gbc);

        gbc.gridx = 1;
        gbc.gridy = 14;
        mainPanel.add(counterClockwiseCheckBox, gbc);
        
        hueStartLabel.setVisible(false);
        hueStartField.setVisible(false);
        hueEndLabel.setVisible(false);
        hueEndField.setVisible(false);
        clockwiseCheckBox.setVisible(false);
        counterClockwiseCheckBox.setVisible(false);
        directionLabel.setVisible(false);
        histoLabel.setVisible(false);
        histoField.setVisible(false);
        multiplierLabel.setVisible(false);
        multiplierField.setVisible(false);
        exponentLabel.setVisible(false);
        exponentField.setVisible(false);
        visualizeLabel.setVisible(false);
        visualizeCheckBox.setVisible(false);

        // add checkbox etc for the aniso graph
        JButton graphCheckBox = new JButton ("manual ROI-pixelvalue graph");
        gbc.gridx = 0;
        gbc.gridy = 15;
        gbc.gridwidth = 1;
        mainPanel.add(graphCheckBox, gbc);
        
        
        //hue bar check box
        JCheckBox hueBarInVideoCheckBox = new JCheckBox("select for hue-bar appended to video", true);

        gbc.gridx = 2;
        gbc.gridy = 15;
        gbc.gridwidth = 1;
        mainPanel.add(hueBarInVideoCheckBox, gbc);

      
        JCheckBox hideCheckBox  = new JCheckBox("hide advanced settings", false);

        gbc.gridx = 2;
        gbc.gridy = 11;
        gbc.gridwidth = 1;
        mainPanel.add(hideCheckBox, gbc);
        
        hideCheckBox.setVisible(false);

        // Add main panel to frame
        frame.add(mainPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
   
// visualize checkbox action listeners
        
   
        
        visualizeCheckBox.addActionListener(e -> {
        	visualizeCheckBox.setSelected(false);
            histoLabel.setVisible(true);
            histoField.setVisible(true);
         
            maxHistoValue = Float.parseFloat(histoField.getText());
        	brightnessMultiplier = Float.parseFloat(multiplierField.getText());
        	brightnessExponent = Float.parseFloat(exponentField.getText());
            mainPanel.revalidate();
            mainPanel.repaint();
            frame.pack();
            // Display histogram for imp2 (B channel)
            displayHistogram();
        
        });
        
       // Update ActionListeners for checkboxes
        firstCheckBox.addActionListener(e -> {
            hueStartField.setText(String.valueOf(0.3f));
            hueEndField.setText(String.valueOf(0.86f));
            clockwiseCheckBox.setSelected(false);
            counterClockwiseCheckBox.setSelected(true); 
        });
        
  


        secondCheckBox.addActionListener(e -> {
            hueStartField.setText(String.valueOf(0.63f));
            hueEndField.setText(String.valueOf(0.9f));
            clockwiseCheckBox.setSelected(true);
            counterClockwiseCheckBox.setSelected(false); 
        });

        // ActionListener for fourthCheckBox (reverse of first preset)
        fourthCheckBox.addActionListener(e -> {
            hueStartField.setText(String.valueOf(0.86f));
            hueEndField.setText(String.valueOf(0.3f));
            clockwiseCheckBox.setSelected(true);
            counterClockwiseCheckBox.setSelected(false); 
        });

        // ActionListener for fifthCheckBox (reverse of second preset)
        fifthCheckBox.addActionListener(e -> {
            hueStartField.setText(String.valueOf(0.9f));
            hueEndField.setText(String.valueOf(0.63f));
            clockwiseCheckBox.setSelected(false);
            counterClockwiseCheckBox.setSelected(true); 
        });

        thirdCheckBox.addActionListener(e -> {
            boolean isManualSelected = thirdCheckBox.isSelected();
            hueStartLabel.setVisible(isManualSelected);
            thirdCheckBox.setVisible(!isManualSelected);
            hideCheckBox.setVisible(isManualSelected);
            hideCheckBox.setSelected(!isManualSelected);
            thirdCheckBox.setSelected(!isManualSelected);

           
            hueStartField.setVisible(isManualSelected);
            hueEndLabel.setVisible(isManualSelected);
            hueEndField.setVisible(isManualSelected);
            clockwiseCheckBox.setVisible(isManualSelected);
            counterClockwiseCheckBox.setVisible(isManualSelected);
            directionLabel.setVisible(isManualSelected);
            multiplierLabel.setVisible(isManualSelected);
            multiplierField.setVisible(isManualSelected);
            exponentLabel.setVisible(isManualSelected);
            exponentField.setVisible(isManualSelected);
            visualizeLabel.setVisible(isManualSelected);
            visualizeCheckBox.setVisible(isManualSelected);
            mainPanel.revalidate();
            mainPanel.repaint();
            frame.pack();
      
            
            
        
        
        int newwidth = 512;  // Width of the image
        int newheight = 512; // Height of the image
        int radius = Math.min(newwidth, newheight) / 3; // Circle radius

        // Create a new image
        ImagePlus hueCircle = IJ.createImage("Hue Circle", "RGB", newwidth, newheight, 1);
        ImageProcessor hueProcessor = hueCircle.getProcessor();

        // Center of the circle
        int centerX = newwidth / 2;
        int centerY = newheight / 2;

        // Loop through each pixel to determine its hue
        for (int y = 0; y < newheight; y++) {
            for (int x = 0; x < newwidth; x++) {
                // Calculate distance from the center
                int dx = x - centerX;
                int dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= radius) { // Check if the pixel is within the circle
                    // Calculate the angle (hue) and rotate by 90 degrees counterclockwise
                    double angle = Math.atan2(dy, dx) + Math.PI / 2; // Subtract 90 degrees to rotate
                    if (angle < 0) {
                        angle += 2 * Math.PI; // Ensure the angle is in the range [0, 2π]
                    }
                    float hue = (float) (angle / (2 * Math.PI)); // Normalize the angle to [0,1]
                    
                    // Convert hue to RGB color
                    Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
                    hueProcessor.setColor(color);
                    hueProcessor.drawPixel(x, y);
                }
                
            }
        }
        
     // Define the specific hue positions for markers
        float[] hueMarkers = {0.0f, 0.2f, 0.4f, 0.6f, 0.8f};

        // Draw markers at the specified hue positions
        for (float hueMarker : hueMarkers) {
            // Calculate angle for the current hue marker and rotate by 90 degrees counterclockwise
            double angle = hueMarker * 2 * Math.PI - Math.PI / 2; // Subtract 90 degrees to rotate

            // Determine the position on the circle's edge for the marker
            int markerX = centerX + (int) (radius * Math.cos(angle));
            int markerY = centerY + (int) (radius * Math.sin(angle));
            
            // Draw a small marker at this position (e.g., a black dot)
            hueProcessor.setColor(Color.BLACK);
            hueProcessor.drawLine(centerX, centerY, markerX, markerY);  // Draw a line from center to the marker

            // Draw a small circle to mark the exact point
            int markerSize = 4; // Size of the marker
            hueProcessor.fillOval(markerX - markerSize / 2, markerY - markerSize / 2, markerSize, markerSize);

            // Draw the number next to each marker
            int textOffsetX = (int) (30 * Math.cos(angle));  // Offset to avoid overlapping with the marker
            int textOffsetY = (int) (30 * Math.sin(angle));  // Offset to avoid overlapping with the marker
            String hueText = String.format("%.2f", hueMarker);  // Format the hue value to two decimal places
            hueProcessor.drawString(hueText, markerX + textOffsetX, markerY + textOffsetY); // Draw the text next to the marker
        }
        frame.pack();

        // Display the hue color circle image
        hueCircle.show();
        });
        applyButton.addActionListener(e -> {
        
            counterClockwise = counterClockwiseCheckBox.isSelected();
        
            try {
            	brightnessMultiplier = Float.parseFloat(multiplierField.getText());
            	brightnessExponent = Float.parseFloat(exponentField.getText());
               theoMin = minZoom = Float.parseFloat(minZoomField.getText());
               theoMax = maxZoom= Float.parseFloat(maxZoomField.getText());
                hueStartColour = Float.parseFloat(hueStartField.getText());
                hueEndColour = Float.parseFloat(hueEndField.getText());
            } catch (NumberFormatException ex) {
                IJ.error("Invalid input for values. Check if notation is point and not comma");
                return;
            }
            frame.dispose();
            processImages();

        });
        hideCheckBox.addActionListener(e -> {
            boolean isManualSelected = !hideCheckBox.isSelected();
            hueStartLabel.setVisible(isManualSelected);
            thirdCheckBox.setVisible(!isManualSelected);
            hideCheckBox.setVisible(isManualSelected);
            hideCheckBox.setSelected(!isManualSelected);
            thirdCheckBox.setSelected(!isManualSelected);
            hueStartField.setVisible(isManualSelected);
            hueEndLabel.setVisible(isManualSelected);
            hueEndField.setVisible(isManualSelected);
            clockwiseCheckBox.setVisible(isManualSelected);
            counterClockwiseCheckBox.setVisible(isManualSelected);
            directionLabel.setVisible(isManualSelected);
            multiplierLabel.setVisible(isManualSelected);
            multiplierField.setVisible(isManualSelected);
            exponentLabel.setVisible(isManualSelected);
            exponentField.setVisible(isManualSelected);
            visualizeLabel.setVisible(isManualSelected);
            visualizeCheckBox.setVisible(isManualSelected);
            mainPanel.revalidate();
            mainPanel.repaint();
            frame.pack();
        });
        //anisotropy graph check box
        graphCheckBox.addActionListener(e -> {
           
            graphCheckBox.setSelected(false);
  
            DisplayAnisoGraph();
            
        });
        hueBarInVideoCheckBox.addActionListener(e -> {
            hueBarInVideo = hueBarInVideoCheckBox.isSelected();
        });
        cancelButton.addActionListener(e -> {
        	
        createMainMenu();
        frame.dispose();
        });

    }

    	// Function to display histogram for all frames
    	private void displayHistogram() {
    		  if (imp2 == null) {
    		        IJ.error("Error", "Image imp2 is not initialized.");
    		        return;
    		    }
    			 int width = imp2.getWidth();
    		        int height = imp2.getHeight();
    		        int frames = (imp2.getStackSize());
    			
    	    // Total number of pixels in one frame
    	    int totalPixels = width * height;
    	    int bins = 255; // Standard number of bins for grayscale images
    	    double[] histogram = new double[bins];
    	    double binSize = (255 - 0) / (double) bins; // Bin size

    	    // Initialize histogram array
    	    Arrays.fill(histogram, 0);

    	    // Process all frames in parallel
    	    IntStream.range(1, frames + 1).parallel().forEach(frameIndex -> {
    	        ImageProcessor ip2 = imp2.getStack().getProcessor(frameIndex); // Current frame
    	        FloatProcessor brightnessProcessor = (ip2 instanceof FloatProcessor) ? (FloatProcessor) ip2 : ip2.convertToFloatProcessor();
    	        float[] brightnessPixels = (float[]) brightnessProcessor.getPixels();

    	        // Local histogram for the current frame to avoid contention
    	        double[] localHistogram = new double[bins];

    	        // Process each pixel in parallel within the current frame
    	        IntStream.range(0, totalPixels).parallel().forEach(index -> {
    	            // Get and scale the pixel value
    	            brightnessValue = brightnessPixels[index];
    	            brightnessValue = (brightnessValue - brightnessMinimum) / (brightnessMaximum - brightnessMinimum);
    	            brightnessValue = brightnessValue * 255f;
    	            brightnessValue = brightnessValue * brightnessMultiplier;
    	            brightnessValue = (float) Math.pow(brightnessValue, brightnessExponent);

    	            // Map pixel value to histogram bins
    	            int binIndex = (int) ((brightnessValue - 0) / binSize);
    	            binIndex = Math.min(Math.max(binIndex, 0), bins - 1); // Ensure binIndex is within bounds

    	            // Update local histogram bin count
    	            localHistogram[binIndex]++;
    	        });

    	        // Merge local histogram into the global histogram
    	        synchronized (histogram) {
    	            for (int i = 0; i < bins; i++) {
    	                histogram[i] += localHistogram[i];
    	            }
    	        }
    	    });

    	    // Normalize the histogram
    	    double totalFramesPixels = totalPixels * frames; // Total number of pixels across all frames
    	    double[] normalizedHistogram = new double[bins];
    	    for (int i = 0; i < bins; i++) {
    	        normalizedHistogram[i] = histogram[i] / totalFramesPixels;
    	    }

    	    // Create and show the histogram plot
    	    Plot plot = new Plot("Brightness", "Gray Value 8bit", "Frequency", 
    	        generateXValues(bins, 0, 255), normalizedHistogram);
    	    plot.setLimits(0, 255, 0, maxHistoValue);
    	    plot.setColor(Color.BLUE);
    	    plot.show();
    	    
    	}

    	// Helper method to generate x-values for the plot
    	private double[] generateXValues(int bins, double min, double max) {
    	    double[] xValues = new double[bins];
    	    double binSize = (max - min) / bins;
    	    for (int i = 0; i < bins; i++) {
    	        xValues[i] = min + i * binSize;
    	    }
    	    return xValues;
    	}

    
    	// to create a graph that shows anisotropy values based on intensity selecction (ROI)

    	private void DisplayAnisoGraph() {
    	    int[] imageIDs = WindowManager.getIDList();
    	    if (imageIDs == null || imageIDs.length == 0) {
    	        OpenDialog od = new OpenDialog("No open images found. Please select an image file", "");
    	        String filePath = od.getPath();

    	        if (filePath == null) {
    	            createMainMenu();
    	            return;
    	        }

    	        ImagePlus imp = IJ.openImage(filePath);
    	        if (imp == null) {
    	            IJ.showMessage("Error", "Could not open the selected image.");
    	            createMainMenu();
    	            return;
    	        }
    	        imp.show();
    	        imageIDs = WindowManager.getIDList();
    	    }

    	    String[] imageTitles = new String[imageIDs.length];
    	    for (int i = 0; i < imageIDs.length; i++) {
    	        imageTitles[i] = WindowManager.getImage(imageIDs[i]).getTitle();
    	    }

    	    JDialog dialog = new JDialog((Frame) null, "Select Images", false);
    	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    	    dialog.setLayout(new BorderLayout());

    	    JPanel panel = new JPanel(new GridBagLayout());
    	    GridBagConstraints gbc = new GridBagConstraints();
    	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	    gbc.gridx = 0;

    	    // Instructions label
    	    JLabel label = new JLabel("<html><center>Any ROI on any file will be pasted onto the chosen video to generate the pixelvalue/frame graph.<br>"
    	            + "Please draw a ROI before running the script.<br>"
    	            + "Select the video to extract pixel values from.</center></html>");
    	    gbc.gridy = 0;
    	    gbc.insets = new Insets(10, 10, 20, 10);
    	    gbc.weightx = 1.0;
    	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	    gbc.gridwidth = GridBagConstraints.REMAINDER;
    	    panel.add(label, gbc);

    	    gbc.insets = new Insets(5, 10, 5, 10);
    	    gbc.weightx = 0;
    	    gbc.gridwidth = 1;
    	    gbc.fill = GridBagConstraints.NONE;

    	    // Pixel value video selector (always visible)
    	    gbc.gridy = 1;
    	    panel.add(new JLabel("Select Pixel Value Image:"), gbc);
    	    JComboBox<String> pixelValueComboBox = new JComboBox<>(imageTitles);
    	    gbc.gridy = 2;
    	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	    gbc.weightx = 1.0;
    	    gbc.gridwidth = GridBagConstraints.REMAINDER;
    	    panel.add(pixelValueComboBox, gbc);

    	    // Percentile checkboxes (visible when advanced is OFF)
    	    JPanel percentilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    	    percentilePanel.add(new JLabel("Use middle:"));
    	    JCheckBox cb99 = new JCheckBox("99%");
    	    JCheckBox cb95 = new JCheckBox("95%");
    	    JCheckBox cb80 = new JCheckBox("80%");

    	    cb99.addActionListener(e -> { if (cb99.isSelected()) { cb95.setSelected(false); cb80.setSelected(false); } });
    	    cb95.addActionListener(e -> { if (cb95.isSelected()) { cb99.setSelected(false); cb80.setSelected(false); } });
    	    cb80.addActionListener(e -> { if (cb80.isSelected()) { cb99.setSelected(false); cb95.setSelected(false); } });

    	    percentilePanel.add(cb99);
    	    percentilePanel.add(cb95);
    	    percentilePanel.add(cb80);

    	    gbc.gridy = 3;
    	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	    gbc.weightx = 1.0;
    	    gbc.gridwidth = GridBagConstraints.REMAINDER;
    	    gbc.insets = new Insets(10, 10, 5, 10);
    	    panel.add(percentilePanel, gbc);
    	    gbc.insets = new Insets(5, 10, 5, 10);

    	    // Advanced settings checkbox
    	    gbc.gridy = 4;
    	    gbc.fill = GridBagConstraints.NONE;
    	    gbc.weightx = 0;
    	    gbc.gridwidth = GridBagConstraints.REMAINDER;
    	    gbc.insets = new Insets(15, 10, 5, 10);
    	    JCheckBox advancedCheckBox = new JCheckBox("Advanced settings");
    	    advancedCheckBox.setSelected(false);
    	    panel.add(advancedCheckBox, gbc);
    	    gbc.insets = new Insets(5, 10, 5, 10);

    	    // ── CHANGED: advanced panel now uses BoxLayout to hold dynamic threshold rows
    	    JPanel advancedPanel = new JPanel();
    	    advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
    	    advancedPanel.setVisible(false);

    	    // ROI video selector row (always inside advanced)
    	    JPanel roiSelectorPanel = new JPanel(new GridBagLayout());
    	    GridBagConstraints rgbc = new GridBagConstraints();
    	    rgbc.insets = new Insets(4, 8, 4, 8);
    	    rgbc.fill = GridBagConstraints.HORIZONTAL;
    	    rgbc.gridx = 0;

    	    rgbc.gridy = 0; rgbc.weightx = 0; rgbc.gridwidth = 1; rgbc.fill = GridBagConstraints.NONE;
    	    roiSelectorPanel.add(new JLabel("Select Image with ROI:"), rgbc);
    	    JComboBox<String> roiComboBox = new JComboBox<>(imageTitles);
    	    rgbc.gridy = 1; rgbc.fill = GridBagConstraints.HORIZONTAL;
    	    rgbc.weightx = 1.0; rgbc.gridwidth = GridBagConstraints.REMAINDER;
    	    roiSelectorPanel.add(roiComboBox, rgbc);
    	    advancedPanel.add(roiSelectorPanel);

    	    // ── CHANGED: dynamic threshold video entries ──────────────────────────
    	    List<JComboBox<String>> thresholdDropdowns = new ArrayList<>();
    	    List<JTextField> thresholdMinFields = new ArrayList<>();
    	    List<JTextField> thresholdMaxFields = new ArrayList<>();

    	    JPanel thresholdEntriesPanel = new JPanel();
    	    thresholdEntriesPanel.setLayout(new BoxLayout(thresholdEntriesPanel, BoxLayout.Y_AXIS));

    	    Runnable addThresholdRow = () -> {
    	        JPanel rowPanel = new JPanel(new GridBagLayout());
    	        rowPanel.setBorder(BorderFactory.createTitledBorder(
    	            "Threshold video " + (thresholdDropdowns.size() + 1)));
    	        GridBagConstraints tgbc = new GridBagConstraints();
    	        tgbc.insets = new Insets(4, 8, 4, 8);
    	        tgbc.fill = GridBagConstraints.HORIZONTAL;
    	        tgbc.gridx = 0;

    	        tgbc.gridy = 0; tgbc.weightx = 0; tgbc.gridwidth = 1; tgbc.fill = GridBagConstraints.NONE;
    	        rowPanel.add(new JLabel("Video:"), tgbc);
    	        JComboBox<String> dropdown = new JComboBox<>(imageTitles);
    	        tgbc.gridx = 1; tgbc.weightx = 1.0; tgbc.gridwidth = GridBagConstraints.REMAINDER;
    	        tgbc.fill = GridBagConstraints.HORIZONTAL;
    	        rowPanel.add(dropdown, tgbc);
    	        thresholdDropdowns.add(dropdown);

    	        tgbc.gridx = 0; tgbc.gridy = 1; tgbc.gridwidth = 1; tgbc.weightx = 0;
    	        tgbc.fill = GridBagConstraints.NONE;
    	        rowPanel.add(new JLabel("Min:"), tgbc);
    	        JTextField minField = new JTextField("", 8);
    	        tgbc.gridx = 1; tgbc.weightx = 1.0; tgbc.gridwidth = GridBagConstraints.REMAINDER;
    	        tgbc.fill = GridBagConstraints.HORIZONTAL;
    	        rowPanel.add(minField, tgbc);
    	        thresholdMinFields.add(minField);

    	        tgbc.gridx = 0; tgbc.gridy = 2; tgbc.gridwidth = 1; tgbc.weightx = 0;
    	        tgbc.fill = GridBagConstraints.NONE;
    	        rowPanel.add(new JLabel("Max:"), tgbc);
    	        JTextField maxField = new JTextField("", 8);
    	        tgbc.gridx = 1; tgbc.weightx = 1.0; tgbc.gridwidth = GridBagConstraints.REMAINDER;
    	        tgbc.fill = GridBagConstraints.HORIZONTAL;
    	        rowPanel.add(maxField, tgbc);
    	        thresholdMaxFields.add(maxField);
    	        thresholdEntriesPanel.add(rowPanel);
    	        thresholdEntriesPanel.revalidate();
    	        thresholdEntriesPanel.repaint();
    	        advancedPanel.revalidate();
    	        advancedPanel.repaint();
    	        dialog.pack(); // ── CHANGED: restore this so dialog grows with each new row
    	    
    	        // ─────────────────────────────────────────────────────────────────────
    	    };

    	    // Add the first threshold row by default
    	    addThresholdRow.run();

    	    JButton addThresholdButton = new JButton("+ Add threshold video");
    	    addThresholdButton.addActionListener(e -> addThresholdRow.run());

    	    advancedPanel.add(thresholdEntriesPanel);
    	    JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	    addButtonPanel.add(addThresholdButton);
    	    advancedPanel.add(addButtonPanel);
    	    // ─────────────────────────────────────────────────────────────────────

    	 // ── CHANGED: add advancedPanel directly, no scroll pane ──────────────
    	    gbc.gridy = 5;
    	    gbc.fill = GridBagConstraints.HORIZONTAL;
    	    gbc.weightx = 1.0;
    	    gbc.gridwidth = GridBagConstraints.REMAINDER;
    	    gbc.insets = new Insets(0, 0, 0, 0);
    	    panel.add(advancedPanel, gbc);
    	    gbc.insets = new Insets(5, 10, 5, 10);

    	    // Toggle advanced panel and percentile panel
    	    advancedCheckBox.addActionListener(e -> {
    	        boolean advanced = advancedCheckBox.isSelected();
    	        advancedPanel.setVisible(advanced);
    	        percentilePanel.setVisible(!advanced);
    	        if (advanced) {
    	            cb99.setSelected(false);
    	            cb95.setSelected(false);
    	            cb80.setSelected(false);
    	        }
    	        panel.revalidate();
    	        panel.repaint();
    	        dialog.pack();  // dialog resizes to fit content naturally
    	    });
    	    // ─────────────────────────────────────────────────────────────────────
    	    // Buttons
    	    JPanel buttonPanel = new JPanel();
    	    JButton okButton = new JButton("OK");
    	    JButton cancelButton = new JButton("Cancel");

    	    okButton.addActionListener(e -> {
    	        String pixelValueImageTitle = (String) pixelValueComboBox.getSelectedItem();
    	        pixelValueImp = WindowManager.getImage(pixelValueImageTitle);

    	        // Determine percentile
    	        double percentile = -1;
    	        if (!advancedCheckBox.isSelected()) {
    	            if      (cb99.isSelected()) percentile = 0.99;
    	            else if (cb95.isSelected()) percentile = 0.95;
    	            else if (cb80.isSelected()) percentile = 0.80;
    	        }

    	        ImagePlus roiVideo;
    	        // ── CHANGED: build threshold video list ───────────────────────────
    	        List<ImagePlus> thresholdVideos = new ArrayList<>();
    	        List<Double> thresholdMins = new ArrayList<>();
    	        List<Double> thresholdMaxs = new ArrayList<>();

    	        if (advancedCheckBox.isSelected()) {
    	            String selectedImageTitle = (String) roiComboBox.getSelectedItem();
    	            roiVideo = WindowManager.getImage(selectedImageTitle);

    	            for (int t = 0; t < thresholdDropdowns.size(); t++) {
    	                String title = (String) thresholdDropdowns.get(t).getSelectedItem();
    	                ImagePlus tv = WindowManager.getImage(title);
    	                if (tv == null) {
    	                    IJ.showMessage("Please select a valid threshold video for entry " + (t + 1) + ".");
    	                    return;
    	                }
    	                double tMin = thresholdMinFields.get(t).getText().trim().isEmpty()
    	                    ? -Double.MAX_VALUE
    	                    : Double.parseDouble(thresholdMinFields.get(t).getText().trim());
    	                double tMax = thresholdMaxFields.get(t).getText().trim().isEmpty()
    	                    ? Double.MAX_VALUE
    	                    : Double.parseDouble(thresholdMaxFields.get(t).getText().trim());
    	                thresholdVideos.add(tv);
    	                thresholdMins.add(tMin);
    	                thresholdMaxs.add(tMax);
    	            }
    	        } else {
    	            roiVideo = pixelValueImp;
    	        }
    	        // ─────────────────────────────────────────────────────────────────

    	        if (roiVideo == null) {
    	            IJ.showMessage("Error", "Could not find the selected ROI video.");
    	            return;
    	        }

    	        Roi roi = roiVideo.getRoi();
    	        if (roi == null) {
    	            IJ.showMessage("Error", "Please draw an ROI on the selected image before running the script.");
    	            return;
    	        }

    	        generateGraphUsingROI(roiVideo, roi, thresholdVideos, thresholdMins, thresholdMaxs, percentile);
    	        dialog.dispose();
    	        createMainMenu();
    	    });

    	    cancelButton.addActionListener(e -> {
    	        dialog.dispose();
    	        createMainMenu();
    	    });

    	    buttonPanel.add(okButton);
    	    buttonPanel.add(cancelButton);

    	    dialog.add(panel, BorderLayout.CENTER);
    	    dialog.add(buttonPanel, BorderLayout.SOUTH);
    	    dialog.setLocationRelativeTo(null);
    	    dialog.pack();
    	    dialog.setVisible(true);
    	}


    	private void generateGraphUsingROI(ImagePlus imageWithROI, Roi roi,
    	        List<ImagePlus> thresholdVideos, List<Double> thresholdMins, List<Double> thresholdMaxs,
    	        double percentile) {

    	    if (pixelValueImp == null) {
    	        OpenDialog od = new OpenDialog("Select the pixel value video", "");
    	        String filePath = od.getPath();
    	        if (filePath == null) { createMainMenu(); return; }
    	        pixelValueImp = IJ.openImage(filePath);
    	        if (pixelValueImp == null) {
    	            IJ.showMessage("Error", "Could not open the selected pixel value video.");
    	            createMainMenu(); return;
    	        }
    	        pixelValueImp.show();
    	    }

    	    ImagePlus imp1Copy = pixelValueImp.duplicate();
    	    ImagePlus roiVideoCopy = imageWithROI.duplicate();

    	    imp1Copy.setRoi(roi);
    	    roiVideoCopy.setRoi(roi);

    	    int numFrames = imp1Copy.getStackSize();

    	    ArrayList<Double> frameNumbers = new ArrayList<>();
    	    ArrayList<Double> averageValues = new ArrayList<>();

    	    for (int frame = 1; frame <= numFrames; frame++) {
    	        imp1Copy.setSlice(frame);
    	        roiVideoCopy.setSlice(frame);

    	        ImageProcessor pixelIP = imp1Copy.getProcessor();
    	        ImageProcessor roiIP   = roiVideoCopy.getProcessor();

    	        // ── CHANGED: collect threshold processors for this frame ──────────
    	        List<ImageProcessor> thresholdProcessors = new ArrayList<>();
    	        for (ImagePlus tv : thresholdVideos) {
    	            tv.setSlice(Math.min(frame, tv.getStackSize()));
    	            thresholdProcessors.add(tv.getProcessor());
    	        }
    	        // ─────────────────────────────────────────────────────────────────

    	        Rectangle bounds = roi.getBounds();

    	        double frameMin = -Double.MAX_VALUE;
    	        double frameMax = Double.MAX_VALUE;

    	        if (percentile > 0) {
    	            ArrayList<Double> roiPixels = new ArrayList<>();
    	            for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
    	                for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
    	                    if (roi.contains(x, y))
    	                        roiPixels.add((double) roiIP.getPixelValue(x, y));
    	                }
    	            }
    	            if (!roiPixels.isEmpty()) {
    	                Collections.sort(roiPixels);
    	                int total = roiPixels.size();
    	                double trim = (1.0 - percentile) / 2.0;
    	                int lo = (int) Math.floor(trim * total);
    	                int hi = Math.min((int) Math.ceil((1.0 - trim) * total) - 1, total - 1);
    	                frameMin = roiPixels.get(lo);
    	                frameMax = roiPixels.get(hi);
    	            }
    	        }

    	        double sumPixelValues = 0.0;
    	        int validPixelCount = 0;

    	        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
    	            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
    	                if (roi.contains(x, y)) {
    	                    boolean passes;
    	                    if (percentile > 0) {
    	                        double v = roiIP.getPixelValue(x, y);
    	                        passes = (v >= frameMin && v <= frameMax);
    	                    } else if (!thresholdVideos.isEmpty()) {
    	                        // ── CHANGED: must pass ALL threshold videos ───────
    	                        passes = passesAllThresholds(x, y, thresholdProcessors, thresholdMins, thresholdMaxs);
    	                    } else {
    	                        passes = true; // no filtering at all
    	                    }

    	                    if (passes) {
    	                        sumPixelValues += pixelIP.getPixelValue(x, y);
    	                        validPixelCount++;
    	                    }
    	                }
    	            }
    	        }

    	        double averageValue = validPixelCount > 0 ? sumPixelValues / validPixelCount : 0.0;
    	        frameNumbers.add((double) frame);
    	        averageValues.add(averageValue);
    	    }

    	    double[] xValues = frameNumbers.stream().mapToDouble(Double::doubleValue).toArray();
    	    double[] yValues = averageValues.stream().mapToDouble(Double::doubleValue).toArray();
    	    double minY = Collections.min(averageValues);
    	    double maxY = Collections.max(averageValues);

    	    Plot plot = new Plot(sampleName + "ROI", "Frame", "Average pixel value", xValues, yValues);
    	    plot.setLimits(0, numFrames, minY, maxY);
    	    plot.setColor(java.awt.Color.BLUE);
    	    plot.show();
    	}


    


    	private void processImages() {
    	    int width = imp1.getWidth();
    	    int height = imp1.getHeight();
    	    int hueBarHeight = height/7;  // Set the height of the hue bar
    	    int frames = Math.min(imp1.getStackSize(), imp2.getStackSize());

    	    // New stack with extra space for the hue bar (conditionally)
    	    ImageStack hsbStackWithHueBar = new ImageStack(width, height + hueBarHeight);
    	    ImageStack hsbStack = new ImageStack(width, height);
    	   imp1ToUse = imp1.duplicate();
    	    imp2ToUse = imp2.duplicate();

    	    // Pre-calculate constants
    	    float theoreticalTotal = theoreticalMaximum - theoreticalMinimum;
    	    float zoomMin = (minZoom - theoreticalMinimum) / theoreticalTotal;
    	    float zoomMax = (maxZoom - theoreticalMinimum) / theoreticalTotal;
    	    float zoomTotal = zoomMax - zoomMin;
    	    float brightnessRange = brightnessMaximum - brightnessMinimum;

    	    // Thread-safe maps for processed frames
    	    ConcurrentHashMap<Integer, ColorProcessor> frameMap = new ConcurrentHashMap<>();

    	    // Frame-level parallel processing
    	    IntStream.rangeClosed(1, frames).parallel().forEach(i -> {
    	        ImageProcessor ip1 = imp1ToUse.getStack().getProcessor(i); // H channel
    	        ImageProcessor ip2 = imp2ToUse.getStack().getProcessor(i); // B channel

    	        FloatProcessor hueProcessor = (ip1 instanceof FloatProcessor) ? (FloatProcessor) ip1 : ip1.convertToFloatProcessor();
    	        FloatProcessor brightnessProcessor = (ip2 instanceof FloatProcessor) ? (FloatProcessor) ip2 : ip2.convertToFloatProcessor();
    	        FloatProcessor saturationProcessor = new FloatProcessor(width, height);
    	        saturationProcessor.setValue(sValue);
    	        saturationProcessor.fill();

    	        float[] huePixels = (float[]) hueProcessor.getPixels();
    	        float[] brightnessPixels = (float[]) brightnessProcessor.getPixels();
    	        float[] saturationPixels = (float[]) saturationProcessor.getPixels();

    	        // Process pixels
    	        for (int index = 0; index < huePixels.length; index++) {
    	            float brightnessUnits = brightnessPixels[index];
    	            float brightnessValue = (brightnessUnits - brightnessMinimum) / brightnessRange;

    	            float I_anisotropy = huePixels[index];
    	            float hueValue = (I_anisotropy - theoreticalMinimum) / theoreticalTotal;

    	            // Apply zoom and clipping
    	            if (hueValue < zoomMin) {
    	                hueValue = 0;
    	            } else if (hueValue > zoomMax) {
    	                hueValue = 1;
    	            } else {
    	                hueValue = (hueValue - zoomMin) / zoomTotal;
    	            }

    	            // Adjust hue based on direction
    	            if (!counterClockwise) {  // Clockwise direction
    	                if (hueEndColour > hueStartColour) {
    	                    hueValue = hueValue * (hueEndColour - hueStartColour) + hueStartColour;
    	                } else {
    	                    hueValue = (hueValue * ((1.0f - hueStartColour) + hueEndColour) + hueStartColour) % 1.0f;
    	                }
    	            } else {  // Counterclockwise direction
    	                if (hueEndColour > hueStartColour) {
    	                    hueValue = ((1.0f - (hueValue * (1 - (hueEndColour - hueStartColour)))) + hueStartColour) % 1.0f;
    	                } else {
    	                    hueValue = ((1.0f - (1.0f - hueStartColour)) - (hueValue * (hueStartColour - hueEndColour))) % 1.0f;
    	                }
    	            }

    	            // Update hue and brightness
    	            huePixels[index] = hueValue * 255;
    	            brightnessValue = (float) Math.pow(brightnessValue * 255 * brightnessMultiplier, brightnessExponent);
    	            brightnessPixels[index] = brightnessValue;
    	        }

    	
    	        // Convert all channels to byte arrays
    	        byte[] hueByteArray = convertToByteArray(huePixels);
    	        byte[] saturationByteArray = convertToByteArray(saturationPixels);
    	        byte[] brightnessByteArray = convertToByteArray(brightnessPixels);

    	        // Generate RGB processors
    	        ColorProcessor rgbProcessor = new ColorProcessor(width, height);
    	        rgbProcessor.setHSB(hueByteArray, saturationByteArray, brightnessByteArray);

    	 
    	        if (hueBarInVideo) {
    	            // Create the hue bar as a BufferedImage
    	            BufferedImage hueBar = createHueBar(width, hueBarHeight, counterClockwise, hueStartColour, hueEndColour, minZoom, maxZoom);

    	            // Convert BufferedImage to ImageProcessor for ImageJ
    	            ImageProcessor hueBarProcessor = new ColorProcessor(hueBar);

    	            // Create a combined frame (video + hue bar)
    	            ColorProcessor combinedFrame = new ColorProcessor(width, height + hueBarHeight);
    	            combinedFrame.insert(rgbProcessor, 0, 0);  // Insert the video frame at the top
    	            combinedFrame.insert(hueBarProcessor, 0, height);  // Insert the hue bar below the video frame
    	            
    	  
    	            frameMap.put(i, combinedFrame);
    	        } else {
    	            frameMap.put(i, rgbProcessor);
    	        }
    	    });

    	    // Assemble and show final video(s)
    	    if (hueBarInVideo) {
    	        for (int i = 1; i <= frames; i++) {
    	            hsbStackWithHueBar.addSlice(frameMap.get(i));
    	        }
    	        ImagePlus combinedVideo = new ImagePlus(sampleName + "RGB Movie", hsbStackWithHueBar);
    	        combinedVideo.show();
    	    } else {
    	        for (int i = 1; i <= frames; i++) {
    	            hsbStack.addSlice(frameMap.get(i));
    	        }

    	        ImagePlus originalRGB = new ImagePlus(sampleName + "Combined RGB Movie", hsbStack);
    	        originalRGB.show();

    	        SwingUtilities.invokeLater(() -> new HueBarWindow().setVisible(true));
    	        ImagePlus IWAD = new ImagePlus("HSB Stack", hsbStack);
    	    }


            createMainMenu();
    	    }
    	

    		

    		  


    		private void CellPoseSegmentation() {
    		    // Fetch window IDs again after ensuring images are open
    		    int[] windowIDs = WindowManager.getIDList();

    		    if (windowIDs == null || windowIDs.length == 0) {
    		        OpenDialog od1 = new OpenDialog("Select the template video for ROI determination", "");
    		        String filePathCellpose = od1.getPath();
    		        if (filePathCellpose == null) {
    		            createMainMenu();
    		            return;
    		        }

    		        // Open the selected images and assign them to variables
    		        cellposeImp = IJ.openImage(filePathCellpose); // First movie (parallel channel)

    		        if (cellposeImp == null) {
    		            IJ.error("Error", "Could not open the selected video.");
    		            createMainMenu();
    		            return;
    		        }

    		        // Now add these images to the WindowManager so they can be selected in the dialog
    		        cellposeImp.show();
    		    }

    		    // Re-fetch the window IDs to include any newly opened images
    		    final int[] finalWindowIDs = WindowManager.getIDList();  // Assign to final variable

    		    // Step 1: Create the JDialog
    		    JDialog dialog = new JDialog((Frame) null, "Select ROI Video and Set Diameter", true);
    		    dialog.setLayout(new BorderLayout());
    		    dialog.setSize(650, 300);  // Adjust the size to fit your needs

    		    // Step 2: Main panel to hold components with GridBagLayout
    		    JPanel mainPanel = new JPanel();
    		    mainPanel.setLayout(new GridBagLayout());
    		    GridBagConstraints gbc = new GridBagConstraints();
    		    gbc.insets = new Insets(10, 10, 10, 10);  // Padding around components
    		    gbc.fill = GridBagConstraints.HORIZONTAL;
    		    gbc.anchor = GridBagConstraints.CENTER;

    		    // Video Selection Dropdown
    		    JTextArea videoLabel = new JTextArea(
    		        "Select the template video for ROI determination:\n" +
    		        "We recommend total brightness video."
    		    );
    		    videoLabel.setLineWrap(true);
    		    videoLabel.setWrapStyleWord(true);
    		    videoLabel.setEditable(false);
    		    videoLabel.setBackground(mainPanel.getBackground());
    		    videoLabel.setOpaque(false);
    		    videoLabel.setBorder(null);

    		    gbc.gridx = 0;
    		    gbc.gridy = 0;
    		    gbc.gridwidth = 1;
    		    mainPanel.add(videoLabel, gbc);

    		    String[] videoTitles = new String[finalWindowIDs.length];
    		    for (int i = 0; i < finalWindowIDs.length; i++) {
    		        ImagePlus img = WindowManager.getImage(finalWindowIDs[i]);
    		        videoTitles[i] = img != null ? img.getTitle() : "";
    		    }

    		 // JComboBox and other components setup (unchanged)
    		    JComboBox<String> videoDropdown = new JComboBox<>(videoTitles);
    		    gbc.gridx = 1;
    		    gbc.gridy = 0;
    		    gbc.gridwidth = 3;
    		    mainPanel.add(videoDropdown, gbc);

    		    // Diameter Input Label and Field
    		    JLabel diameterLabel = new JLabel("Diameter of Average Cell (in Pixels):");
    		    gbc.gridx = 0;
    		    gbc.gridy = 1;
    		    gbc.gridwidth = 1;
    		    mainPanel.add(diameterLabel, gbc);

    		    JTextField diameterField = new JTextField("80", 10);  // Pre-filled with default value
    		    gbc.gridx = 1;
    		    gbc.gridy = 1;
    		    gbc.gridwidth = 2;
    		    mainPanel.add(diameterField, gbc);

    		    // Instructions below the diameter input
    		    JTextArea instructions = new JTextArea(
    		        "Only parts of the cells are outlined? Increase diameter.\n" +
    		        "Cells clumped together in one outline? Decrease diameter.\n" +
    		        "Set to zero for automatic calibration per image."
    		    );
    		    instructions.setLineWrap(true);
    		    instructions.setWrapStyleWord(true);
    		    instructions.setEditable(false);
    		    instructions.setBackground(mainPanel.getBackground());

    		    gbc.gridx = 0;
    		    gbc.gridy = 2;
    		    gbc.gridwidth = 3;
    		    mainPanel.add(instructions, gbc);
    		    
    		    JLabel frameLabel = new JLabel("Generate ROI based on the first frame (fast) or all the frames (slow)");
    	        gbc.gridx = 0;
    	        gbc.gridy = 3;
    	        gbc.gridwidth = 1;
    	        mainPanel.add(frameLabel, gbc);

    	        
    	        JCheckBox frameOneCheckBox = new JCheckBox("first frame", false);
    	        gbc.gridx = 1;
    	        gbc.gridy = 3;
    	        gbc.gridwidth = 1;
    	        mainPanel.add(frameOneCheckBox, gbc);
    	        
    	        JCheckBox frameAllCheckBox = new JCheckBox("all frames", false);
    	        gbc.gridx = 1;
    	        gbc.gridy = 4;
    	        gbc.gridwidth = 1;
    	        mainPanel.add(frameAllCheckBox, gbc);

    		    // Add "OK" button at the bottom to confirm
    		    JButton okButton = new JButton("OK");
    		    gbc.gridx = 1;
    		    gbc.gridy = 5;
    		    gbc.gridwidth = 1;
    		    mainPanel.add(okButton, gbc);
    		    
    		        frameAllCheckBox.addActionListener(e -> {
    		        	frameAllCheckBox.setSelected(true);
    		        	frameOneCheckBox.setSelected(false);
    		            mainPanel.revalidate();
    		            mainPanel.repaint();
    		            dialog.pack();
    		            allFrame = true;
    		            oneFrame = false;
    		        });
    		        frameOneCheckBox.addActionListener(e -> {
    		        	frameOneCheckBox.setSelected(true);
    		        	frameAllCheckBox.setSelected(false);
    		            mainPanel.revalidate();
    		            mainPanel.repaint();
    		            dialog.pack();
    		            oneFrame = true;
    		            allFrame = false;
    		        });// Action listener for the OK button
    		        okButton.addActionListener(e -> {
    		            String selectedVideo = (String) videoDropdown.getSelectedItem();
    		            diameterValue = diameterField.getText();  // Store diameter in instance variable

    		            // Find the ImagePlus corresponding to the selected video
    		            for (int id : finalWindowIDs) {
    		                ImagePlus imp = WindowManager.getImage(id);
    		                if (imp != null && imp.getTitle().equals(selectedVideo)) {
    		                    cellposeImp = imp;  // Update cellposeImp to the selected video
    		                    break;
    		                }
    		            }

    		            if (cellposeImp == null) {
    		                IJ.error("Error", "Could not find the selected video.");
    		                createMainMenu();
    		                return;
    		            }

    		            System.out.println("Selected Video: " + selectedVideo);
    		            System.out.println("Diameter Value: " + diameterValue);

    		            dialog.dispose();  // Close the dialog

    		            // Additional pop-up to ask where to save the video frames as an image sequence
    		            DirectoryChooser dc = new DirectoryChooser("Video needs to be converted to images. Select a folder to save them.");
    		            outputDir = dc.getDirectory();  // Store outputDir in instance variable
    		            inputCellpose = outputDir;

    		            // Check if the user selected a valid directory
    		            if (outputDir != null) {
    		                // Ensure the output directory ends with a separator
    		                if (!outputDir.endsWith(File.separator)) {
    		                    outputDir += File.separator;
    		                }

    		                // ✅ Fix: Ensure even single-frame images are treated as stacks
    		        

    		                if (frameOneCheckBox.isSelected()) {
    		                    // Save only the first frame
    		                    cellposeImp.setSlice(1);  // Set the current frame to the first one
    		                    ImageProcessor ip = cellposeImp.getProcessor();  // Get the processor for the first slice
    		                    String frameFilename = outputDir + "frame_0001.tif";

    		                    // Create a new ImagePlus for the first frame to save
    		                    ImagePlus firstFrame = new ImagePlus("Frame 1", ip.duplicate());  // Duplicate to avoid changes in the original image
    		                    new FileSaver(firstFrame).saveAsTiff(frameFilename);  // Save the first frame

    		                    // Log the result about saving the frame
    		                    IJ.log("First frame of the video saved to: " + frameFilename);
    		                    IJ.log("Now turning stack into outlines.");

    		                } else if (frameAllCheckBox.isSelected()) {
    		                    // Save all frames
    		                    int frameCount = cellposeImp.getStackSize();
    		                    System.out.println("Stack size: " + frameCount);  // For debugging
    		                    System.out.println("Is stack: " + cellposeImp.isStack());  // For debugging

    		                    for (int i = 1; i <= frameCount; i++) {
    		                        cellposeImp.setSlice(i);  // Set the current frame
    		                        ImageProcessor ip = cellposeImp.getProcessor();  // Get the processor for the current slice
    		                        String frameFilename = outputDir + "frame_" + String.format("%04d", i) + ".tif";

    		                        // Create a new ImagePlus for the current frame to save
    		                        ImagePlus currentFrame = new ImagePlus("Frame " + i, ip.duplicate());  // Duplicate to avoid changes in the original image
    		                        new FileSaver(currentFrame).saveAsTiff(frameFilename);  // Save the current frame
    		                    }

    		                    // Log the result about saving the frames
    		                    IJ.log("Video converted to image sequence and saved to: " + outputDir);
    		                    IJ.log("Video has been converted to stacks. Now turning stacks into outlines.");
    		                }
    		            } else {
    		                // Log an error message if no directory was selected
    		                IJ.log("No directory selected for saving the image sequence.");
    		            }

    		            // Now you can run CellPose
    		            runCellpose();  // Run CellPose
    		        });

    		        // Add the main panel to the dialog
    		        dialog.add(mainPanel, BorderLayout.CENTER);
    		        dialog.setVisible(true);  // Display the dialog
    		}
    		     
    		     
    		private String findPythonInterpreter() {
    			
    			// Load saved path if not already loaded
    			if (userProvidedPath == null || userProvidedPath.isEmpty()) {
    			    userProvidedPath = Prefs.get(PYTHON_PREF_KEY, null);
    			}
    			if (userProvidedPath != null && !userProvidedPath.isEmpty()) {
    		        File savedPython = new File(userProvidedPath);
    		        if (savedPython.exists() && savedPython.canExecute()) {
    		            return savedPython.getAbsolutePath();
    		        }
    		    }

    		    String[] possiblePaths = {
    		        "/usr/bin/python3",           // Linux, macOS
    		        "/usr/local/bin/python3",     // macOS (homebrew)
    		        "C:\\Python39\\python.exe",   // Common Windows installation path
    		        "C:\\Python38\\python.exe",   // Another common Windows path
    		        "C:\\Program Files\\Python311\\python.exe",  // Newer installation location
    		        "C:\\Program Files\\Python310\\python.exe",  // Newer installation location
    		        "python"                      // Fallback to system PATH
    		        
    		    };

    		    // Try to find the Python interpreter in the predefined paths
    		    for (String path : possiblePaths) {
    		        File pythonFile = new File(path);
    		        if (pythonFile.exists() && pythonFile.canExecute()) {
    		            return pythonFile.getAbsolutePath(); // Return the found path
    		        }
    		    }

    		    userProvidedPath = JOptionPane.showInputDialog(
    		            null,
    		            "Python interpreter not found.\nPlease enter full path to python:"
    		    );

    		    if (userProvidedPath != null && !userProvidedPath.trim().isEmpty()) {

    		        File userPythonFile = new File(userProvidedPath);
    		        if (userPythonFile.exists() && userPythonFile.canExecute()) {

    		            // SAVE IT PERMANENTLY
    		            Prefs.set(PYTHON_PREF_KEY, userProvidedPath);
    		            Prefs.savePreferences();

    		            return userPythonFile.getAbsolutePath();

    		        } else {
    		            JOptionPane.showMessageDialog(
    		                    null,
    		                    "The provided path is not a valid Python interpreter.",
    		                    "Invalid Path",
    		                    JOptionPane.ERROR_MESSAGE
    		            );
    		        }
    		    }


    		    return null; // Return null if no valid path is found
    		  

    		}

    	    private File extractPythonScript(String scriptName) {
    	        try {
    	            // Get the Python script as a resource from the JAR
    	            InputStream scriptStream = getClass().getResourceAsStream("/" + scriptName);
    	            if (scriptStream == null) {
    	                throw new IOException("Python script not found in the JAR.");
    	            }

    	            // Create a temporary file to store the extracted script
    	            File tempScriptFile = File.createTempFile(scriptName, ".py");
    	            tempScriptFile.deleteOnExit();  // Delete the file when the JVM exits

    	            // Write the content of the script to the temporary file
    	            try (FileOutputStream out = new FileOutputStream(tempScriptFile)) {
    	                byte[] buffer = new byte[1024];
    	                int bytesRead;
    	                while ((bytesRead = scriptStream.read(buffer)) != -1) {
    	                    out.write(buffer, 0, bytesRead);
    	                }
    	            }

    	            return tempScriptFile;
    	        } catch (IOException e) {
    	            e.printStackTrace();
    	            return null;
    	        }
    	    }
    	    private void runCellpose() {
    	        // Run CellPose on a background thread
    	        new Thread(() -> {
    	            // Extract the necessary Python scripts from the JAR
    	            File checkPackagesScriptFile = extractPythonScript("check_and_install_packages.py");
    	            File pythonScriptFile = extractPythonScript("run_cellpose.py");
    	            
    	            // Check if the package check and install script was extracted successfully
    	            if (checkPackagesScriptFile == null) {
    	                IJ.log("Package check script not found in JAR.");
    	                return;
    	            }

    	            // Check if the CellPose script was extracted successfully
    	            if (pythonScriptFile == null) {
    	                IJ.log("CellPose script not found in JAR.");
    	                return;
    	            }

    	            // Find the Python interpreter
    	            String pythonInterpreter = findPythonInterpreter();
    	            if (pythonInterpreter == null) {
    	                IJ.log("Python interpreter not found. Please install Python or specify its location.");
    	                return;
    	            }

    	            // Build the command to run the package check script
    	            ProcessBuilder checkPackagesBuilder = new ProcessBuilder(
    	                    pythonInterpreter,
    	                    checkPackagesScriptFile.getAbsolutePath()
    	            );

    	            // Redirect error and output streams for package check
    	            checkPackagesBuilder.redirectErrorStream(true);

    	            try {
    	                // Start the package installation process
    	                Process checkProcess = checkPackagesBuilder.start();
    	                IJ.log("Checking and installing required Python packages... If this is your first time installing dependencies, please restart the plugin after installing.");

    	                // Read the output of the package check process
    	                try (BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()))) {
    	                    String line;
    	                    while ((line = reader.readLine()) != null) {
    	                        IJ.log(line); // Log output to the console
    	                    }
    	                }

    	                // Wait for the package check process to complete
    	                int checkExitCode = checkProcess.waitFor();
    	                if (checkExitCode != 0) {
    	                    IJ.log("Package check script failed with exit code: " + checkExitCode + "Common issues are incompatible python version (8-11 are compatible), or in windows the absence of C++ environment in visual studio");
    	                    return; // Exit if package installation failed
    	                }

    	                // Now, run the CellPose script
    	                ProcessBuilder processBuilder = new ProcessBuilder(
    	                        pythonInterpreter,
    	                        pythonScriptFile.getAbsolutePath(), // Use the main script path
    	                        "--dir", inputCellpose,
    	                        "--diameter", diameterValue
    	                );
    	                processBuilder.redirectErrorStream(true);  // Add this


    	                // Start the CellPose process
    	                Process process = processBuilder.start();
    	                IJ.log("If you see this message, CellPose is running."
    	                        + " This may take some time, so please be patient.");

    	                // Read the output of the CellPose process
    	                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
    	                    String line;
    	                    while ((line = reader.readLine()) != null) {
    	                        IJ.log(line); // Log output to the console
    	                    }
    	                }

    	                // Wait for the process to complete
    	                int exitCode = process.waitFor();
    	                if (exitCode == 0) {
    	                    IJ.log("CellPose script executed successfully.");
    	                } else {
    	                    IJ.log("CellPose script execution failed with exit code: " + exitCode);
    	                }
    	            } catch (IOException | InterruptedException e) {
    	                e.printStackTrace();
    	            } finally {
    	                // Log the completion message
    	                IJ.log("CellPose processing completed. Stacks have been converted to outlines. Use outlines as ROI for aniso graph per cell.");
    	                applyROIsToVideo();
    	            }
    	        }).start();

    	        IJ.getInstance().repaint(); // Force the UI to repaint
    	    }




    
    	    private void applyROIsToVideo() {
    	        String[] openImages = getOpenImageTitles();

    	        if (openImages == null || openImages.length == 0) {
    	            OpenDialog od1 = new OpenDialog("Select the graph video (pixel values)", "");
    	            String filePathPrimary = od1.getPath();
    	            if (filePathPrimary == null) { createMainMenu(); return; }
    	            ImagePlus primaryVideo = IJ.openImage(filePathPrimary);
    	            if (primaryVideo == null) {
    	                IJ.showMessage("Error", "Could not open the selected graph video.");
    	                createMainMenu(); return;
    	            }
    	            primaryVideo.show();
    	            openImages = getOpenImageTitles();
    	        }

    	        final String[] openImagesFinal = openImages;

    	        JPanel mainPanel = new JPanel();
    	        mainPanel.setLayout(new GridBagLayout());
    	        GridBagConstraints gbc = new GridBagConstraints();
    	        gbc.insets = new Insets(10, 10, 10, 10);
    	        gbc.fill = GridBagConstraints.HORIZONTAL;
    	        gbc.anchor = GridBagConstraints.WEST;

    	        // Primary video label + dropdown (always visible)
    	        JTextArea primaryVideoLabel = new JTextArea(
    	            "Select the graph video (the pixel values of this video will be used to plot the graphs):"
    	        );
    	        primaryVideoLabel.setLineWrap(true);
    	        primaryVideoLabel.setWrapStyleWord(true);
    	        primaryVideoLabel.setEditable(false);
    	        primaryVideoLabel.setBackground(mainPanel.getBackground());
    	        primaryVideoLabel.setOpaque(false);
    	        primaryVideoLabel.setBorder(null);
    	        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
    	        mainPanel.add(primaryVideoLabel, gbc);

    	        JComboBox<String> primaryVideoDropdown = new JComboBox<>(openImages);
    	        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3;
    	        mainPanel.add(primaryVideoDropdown, gbc);

    	        // Percentile checkboxes (visible when advanced is OFF)
    	        JPanel percentilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    	        percentilePanel.add(new JLabel("Use middle:"));
    	        JCheckBox cb99 = new JCheckBox("99%");
    	        JCheckBox cb95 = new JCheckBox("95%");
    	        JCheckBox cb80 = new JCheckBox("80%");

    	        cb99.addActionListener(e -> { if (cb99.isSelected()) { cb95.setSelected(false); cb80.setSelected(false); } });
    	        cb95.addActionListener(e -> { if (cb95.isSelected()) { cb99.setSelected(false); cb80.setSelected(false); } });
    	        cb80.addActionListener(e -> { if (cb80.isSelected()) { cb99.setSelected(false); cb95.setSelected(false); } });

    	        percentilePanel.add(cb99);
    	        percentilePanel.add(cb95);
    	        percentilePanel.add(cb80);

    	        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4;
    	        gbc.insets = new Insets(5, 10, 5, 10);
    	        mainPanel.add(percentilePanel, gbc);
    	        gbc.insets = new Insets(10, 10, 10, 10);

    	        // Advanced settings checkbox
    	        JCheckBox advancedCheckBox = new JCheckBox("Advanced settings");
    	        advancedCheckBox.setSelected(false);
    	        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
    	        mainPanel.add(advancedCheckBox, gbc);

    	        // ── Advanced panel ────────────────────────────────────────────────────
    	        JPanel advancedPanel = new JPanel();
    	        advancedPanel.setLayout(new BoxLayout(advancedPanel, BoxLayout.Y_AXIS));
    	        advancedPanel.setVisible(false);

    	        // ── CHANGED: each threshold entry is a small panel with dropdown + min/max ──
    	        // We keep a list of these panels so we can read them on OK
    	        List<JComboBox<String>> thresholdDropdowns = new ArrayList<>();
    	        List<JTextField> thresholdMinFields = new ArrayList<>();
    	        List<JTextField> thresholdMaxFields = new ArrayList<>();

    	        // Helper to build one threshold entry panel
    	        // We use an array wrapper so the lambda can reference the container
    	        JPanel thresholdEntriesPanel = new JPanel();
    	        thresholdEntriesPanel.setLayout(new BoxLayout(thresholdEntriesPanel, BoxLayout.Y_AXIS));

    	        // Method-like block to add a new threshold row
    	        Runnable addThresholdRow = () -> {
    	            JPanel rowPanel = new JPanel(new GridBagLayout());
    	            rowPanel.setBorder(BorderFactory.createTitledBorder(
    	                "Threshold video " + (thresholdDropdowns.size() + 1)));
    	            GridBagConstraints rgbc = new GridBagConstraints();
    	            rgbc.insets = new Insets(4, 8, 4, 8);
    	            rgbc.fill = GridBagConstraints.HORIZONTAL;
    	            rgbc.gridx = 0;

    	            // Dropdown
    	            rgbc.gridy = 0; rgbc.gridwidth = 1; rgbc.weightx = 0;
    	            rowPanel.add(new JLabel("Video:"), rgbc);
    	            JComboBox<String> dropdown = new JComboBox<>(openImagesFinal);
    	            rgbc.gridx = 1; rgbc.weightx = 1.0; rgbc.gridwidth = GridBagConstraints.REMAINDER;
    	            rowPanel.add(dropdown, rgbc);
    	            thresholdDropdowns.add(dropdown);

    	            // Min field
    	            rgbc.gridx = 0; rgbc.gridy = 1; rgbc.gridwidth = 1; rgbc.weightx = 0;
    	            rowPanel.add(new JLabel("Min:"), rgbc);
    	            JTextField minField = new JTextField("", 8);
    	            rgbc.gridx = 1; rgbc.weightx = 1.0; rgbc.gridwidth = GridBagConstraints.REMAINDER;
    	            rowPanel.add(minField, rgbc);
    	            thresholdMinFields.add(minField);

    	            // Max field
    	            rgbc.gridx = 0; rgbc.gridy = 2; rgbc.gridwidth = 1; rgbc.weightx = 0;
    	            rowPanel.add(new JLabel("Max:"), rgbc);
    	            JTextField maxField = new JTextField("", 8);
    	            rgbc.gridx = 1; rgbc.weightx = 1.0; rgbc.gridwidth = GridBagConstraints.REMAINDER;
    	            rowPanel.add(maxField, rgbc);
    	            thresholdMaxFields.add(maxField);

    	            thresholdEntriesPanel.add(rowPanel);
    	            thresholdEntriesPanel.revalidate();
    	            thresholdEntriesPanel.repaint();
    	        };

    	        // Add the first threshold row by default
    	        addThresholdRow.run();

    	        // "Add threshold video" button
    	        JButton addThresholdButton = new JButton("+ Add threshold video");
    	        addThresholdButton.addActionListener(e -> {
    	            addThresholdRow.run();
    	        });

    	        advancedPanel.add(thresholdEntriesPanel);
    	        JPanel addButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    	        addButtonPanel.add(addThresholdButton);
    	        advancedPanel.add(addButtonPanel);
    	        // ─────────────────────────────────────────────────────────────────────

    	        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
    	        mainPanel.add(advancedPanel, gbc);

    	        // Toggle advanced panel and percentile panel
    	        advancedCheckBox.addActionListener(e -> {
    	            boolean advanced = advancedCheckBox.isSelected();
    	            advancedPanel.setVisible(advanced);
    	            percentilePanel.setVisible(!advanced);
    	            if (advanced) {
    	                cb99.setSelected(false);
    	                cb95.setSelected(false);
    	                cb80.setSelected(false);
    	            }
    	        });

    	        // Buttons
    	        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    	        JButton okButton = new JButton("OK");
    	        JButton cancelButton = new JButton("Cancel");
    	        buttonPanel.add(okButton);
    	        buttonPanel.add(cancelButton);
    	        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
    	        mainPanel.add(buttonPanel, gbc);

    	        final boolean[] confirmed = {false};

    	        JDialog dialog = new JDialog();
    	        dialog.setTitle("Video and Threshold Selection");
    	        dialog.setModal(true);

    	        // ── CHANGED: wrap in a scroll pane so it stays manageable with many rows ──
    	        JScrollPane scrollPane = new JScrollPane(mainPanel);
    	        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    	        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    	        dialog.getContentPane().add(scrollPane);
    	        dialog.setSize(680, 420);
    	        dialog.setLocationRelativeTo(null);
    	        // ─────────────────────────────────────────────────────────────────────

    	        okButton.addActionListener(e -> { confirmed[0] = true; dialog.dispose(); });
    	        cancelButton.addActionListener(e -> { confirmed[0] = false; dialog.dispose(); createMainMenu(); });
    	        dialog.setVisible(true);

    	        if (confirmed[0]) {
    	            String primaryVideoTitle = (String) primaryVideoDropdown.getSelectedItem();

    	            // Determine percentile
    	            double percentile = -1;
    	            if (!advancedCheckBox.isSelected()) {
    	                if      (cb99.isSelected()) percentile = 0.99;
    	                else if (cb95.isSelected()) percentile = 0.95;
    	                else if (cb80.isSelected()) percentile = 0.80;
    	            }

    	            // ── CHANGED: build list of threshold videos and their min/max ────
    	            List<ImagePlus> thresholdVideos = new ArrayList<>();
    	            List<Double> thresholdMins = new ArrayList<>();
    	            List<Double> thresholdMaxs = new ArrayList<>();

    	            if (advancedCheckBox.isSelected()) {
    	                for (int t = 0; t < thresholdDropdowns.size(); t++) {
    	                    String title = (String) thresholdDropdowns.get(t).getSelectedItem();
    	                    ImagePlus tv = WindowManager.getImage(title);
    	                    if (tv == null) {
    	                        IJ.showMessage("Please select a valid threshold video for entry " + (t + 1) + ".");
    	                        return;
    	                    }
    	                    double tMin = thresholdMinFields.get(t).getText().trim().isEmpty()
    	                        ? -Double.MAX_VALUE
    	                        : Double.parseDouble(thresholdMinFields.get(t).getText().trim());
    	                    double tMax = thresholdMaxFields.get(t).getText().trim().isEmpty()
    	                        ? Double.MAX_VALUE
    	                        : Double.parseDouble(thresholdMaxFields.get(t).getText().trim());
    	                    thresholdVideos.add(ensureStack(tv));
    	                    thresholdMins.add(tMin);
    	                    thresholdMaxs.add(tMax);
    	                }
    	            }
    	            // ─────────────────────────────────────────────────────────────────

    	            ImagePlus primaryVideo = WindowManager.getImage(primaryVideoTitle);
    	            if (primaryVideo == null) {
    	                IJ.showMessage("Please select a valid graph video.");
    	                return;
    	            }
    	            primaryVideo = ensureStack(primaryVideo);

    	            DirectoryChooser dc = new DirectoryChooser("Select ROI folder");
    	            String roiFolderPath = dc.getDirectory();
    	            if (roiFolderPath == null) { IJ.showMessage("No ROI folder selected."); return; }

    	            if (!oneFrame && !allFrame) {
    	                String[] options = {"Apply to One Frame", "Apply to All Frames", "Cancel"};
    	                int choice = JOptionPane.showOptionDialog(null,
    	                    "No frame mode selected.\nWould you like to apply ROIs to one frame or all frames?",
    	                    "Select ROI Application Mode",
    	                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
    	                    null, options, options[0]);
    	                if      (choice == 0) oneFrame = true;
    	                else if (choice == 1) allFrame = true;
    	                else { IJ.showMessage("Operation cancelled."); return; }
    	            }

    	            if (oneFrame) {
    	                applyROIsToFirstFrame(primaryVideo, roiFolderPath, thresholdVideos, thresholdMins, thresholdMaxs, percentile);
    	            } else if (allFrame) {
    	                applyROIsToFramesAndCalculateAverages(primaryVideo, roiFolderPath, thresholdVideos, thresholdMins, thresholdMaxs, percentile);
    	            }
    	        } else {
    	            createMainMenu();
    	        }
    	    }


    	    // ── CHANGED: helper — returns true if a pixel passes ALL threshold videos ──
    	    private boolean passesAllThresholds(int px, int py,
    	            List<ImageProcessor> thresholdProcessors,
    	            List<Double> thresholdMins, List<Double> thresholdMaxs) {
    	        for (int t = 0; t < thresholdProcessors.size(); t++) {
    	            double val = thresholdProcessors.get(t).getPixelValue(px, py);
    	            if (val < thresholdMins.get(t) || val > thresholdMaxs.get(t)) return false;
    	        }
    	        return true;
    	    }
    	    // ─────────────────────────────────────────────────────────────────────────


    	    private void applyROIsToFirstFrame(ImagePlus primaryVideo, String roiFolderPath,
    	            List<ImagePlus> thresholdVideos, List<Double> thresholdMins, List<Double> thresholdMaxs,
    	            double percentile) {

    	        File roiFolder = new File(roiFolderPath);
    	        File[] zipFiles = roiFolder.listFiles((dir, name) -> name.endsWith(".zip"));

    	        if (zipFiles == null || zipFiles.length == 0) {
    	            IJ.showMessage("No ROI zip files found in the selected folder.");
    	            return;
    	        }

    	        RoiManager roiManager = new RoiManager(false);
    	        List<Roi> initialROIs = new ArrayList<>();

    	        roiManager.reset();
    	        try {
    	            roiManager.runCommand("Open", zipFiles[0].getAbsolutePath());
    	            Roi[] rois = roiManager.getRoisAsArray();
    	            for (int i = 0; i < rois.length; i++) {
    	                if (rois[i] != null) {
    	                    rois[i].setName("ROI " + (i + 1));
    	                    initialROIs.add(rois[i]);
    	                }
    	            }
    	        } catch (Exception e) {
    	            IJ.log("Failed to load ROI from: " + zipFiles[0].getName() + " - " + e.getMessage());
    	            return;
    	        }

    	        if (initialROIs.isEmpty()) { IJ.showMessage("No ROIs found in the zip file."); return; }

    	        Map<String, List<Double>> roiPixelValues = new HashMap<>();
    	        for (Roi roi : initialROIs) roiPixelValues.put(roi.getName(), new ArrayList<>());

    	        for (int frameIndex = 1; frameIndex <= primaryVideo.getStackSize(); frameIndex++) {
    	            primaryVideo.setSlice(frameIndex);
    	            ImageProcessor primaryProcessor = primaryVideo.getProcessor();

    	            // ── CHANGED: collect all threshold processors for this frame ──────
    	            List<ImageProcessor> thresholdProcessors = new ArrayList<>();
    	            for (ImagePlus tv : thresholdVideos) {
    	                tv.setSlice(Math.min(frameIndex, tv.getStackSize()));
    	                thresholdProcessors.add(tv.getProcessor());
    	            }
    	            // ─────────────────────────────────────────────────────────────────

    	            for (Roi roi : initialROIs) {
    	                Rectangle bounds = roi.getBounds();
    	                ImageProcessor mask = roi.getMask();

    	                double frameMin = -Double.MAX_VALUE;
    	                double frameMax = Double.MAX_VALUE;

    	                if (percentile > 0) {
    	                    ArrayList<Double> roiPixels = new ArrayList<>();
    	                    for (int y = 0; y < bounds.height; y++) {
    	                        for (int x = 0; x < bounds.width; x++) {
    	                            int px = bounds.x + x;
    	                            int py = bounds.y + y;
    	                            if (roi.contains(px, py))
    	                                roiPixels.add((double) primaryProcessor.getPixelValue(px, py));
    	                        }
    	                    }
    	                    if (!roiPixels.isEmpty()) {
    	                        Collections.sort(roiPixels);
    	                        int total = roiPixels.size();
    	                        double trim = (1.0 - percentile) / 2.0;
    	                        int lo = (int) Math.floor(trim * total);
    	                        int hi = Math.min((int) Math.ceil((1.0 - trim) * total) - 1, total - 1);
    	                        frameMin = roiPixels.get(lo);
    	                        frameMax = roiPixels.get(hi);
    	                    }
    	                }

    	                int keptPixelCount = 0;
    	                double sumPixelValues = 0.0;

    	                for (int y = 0; y < bounds.height; y++) {
    	                    for (int x = 0; x < bounds.width; x++) {
    	                        int px = bounds.x + x;
    	                        int py = bounds.y + y;
    	                        if (roi.contains(px, py)) {
    	                            boolean passes;
    	                            if (percentile > 0) {
    	                                float v = primaryProcessor.getPixelValue(px, py);
    	                                passes = (v >= frameMin && v <= frameMax);
    	                            } else {
    	                                // ── CHANGED: must pass ALL threshold videos ───
    	                                passes = passesAllThresholds(px, py, thresholdProcessors, thresholdMins, thresholdMaxs);
    	                            }

    	                            if (passes) {
    	                                sumPixelValues += primaryProcessor.getPixelValue(px, py);
    	                                keptPixelCount++;
    	                            } else {
    	                                if (mask != null) mask.putPixelValue(x, y, 0);
    	                            }
    	                            // ─────────────────────────────────────────────────
    	                        }
    	                    }
    	                }

    	                roiPixelValues.get(roi.getName()).add(
    	                    keptPixelCount > 0 ? sumPixelValues / keptPixelCount : 0.0);

    	                primaryProcessor.setRoi(roi);
    	                roi.drawPixels(primaryProcessor);
    	                primaryProcessor.setLineWidth(2);
    	                primaryProcessor.setColor(Color.RED);
    	                primaryProcessor.drawString(roi.getName(), bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    	            }

    	            primaryVideo.updateAndDraw();
    	        }

    	        createGraph(roiPixelValues);
    	    }


    	    private void applyROIsToFramesAndCalculateAverages(ImagePlus primaryVideo, String roiFolderPath,
    	            List<ImagePlus> thresholdVideos, List<Double> thresholdMins, List<Double> thresholdMaxs,
    	            double percentile) {
    	        IJ.log("Applying ROI's to frames and calculating pixel values.");

    	        int frameCount = primaryVideo.getStackSize();
    	        File roiFolder = new File(roiFolderPath);
    	        File[] zipFiles = roiFolder.listFiles((dir, name) -> name.endsWith(".zip"));

    	        if (zipFiles == null || zipFiles.length == 0) {
    	            IJ.showMessage("No ROI zip files found in the selected folder.");
    	            return;
    	        }
    	        if (zipFiles.length != frameCount) {
    	            IJ.showMessage("Number of ROI files does not match the number of video frames.");
    	            return;
    	        }

    	        RoiManager roiManager = new RoiManager(false);
    	        HashMap<Integer, ArrayList<Double>> roiPixelValuesMap = new HashMap<>();

    	        for (int i = 0; i < frameCount; i++) {
    	            primaryVideo.setSlice(i + 1);

    	            // ── CHANGED: collect all threshold processors for this frame ──────
    	            List<ImageProcessor> thresholdProcessors = new ArrayList<>();
    	            for (ImagePlus tv : thresholdVideos) {
    	                tv.setSlice(Math.min(i + 1, tv.getStackSize()));
    	                thresholdProcessors.add(tv.getProcessor());
    	            }
    	            // ─────────────────────────────────────────────────────────────────

    	            File roiZip = zipFiles[i];
    	            roiManager.reset();

    	            try {
    	                roiManager.runCommand("Open", roiZip.getAbsolutePath());
    	                Roi[] currentFrameROIs = roiManager.getRoisAsArray();

    	                for (int j = 0; j < currentFrameROIs.length; j++) {
    	                    Roi roi = currentFrameROIs[j];
    	                    primaryVideo.setRoi(roi);

    	                    ImageProcessor primaryIP = primaryVideo.getProcessor();
    	                    Rectangle bounds = roi.getBounds();

    	                    double frameMin = -Double.MAX_VALUE;
    	                    double frameMax = Double.MAX_VALUE;

    	                    if (percentile > 0) {
    	                        ArrayList<Double> roiPixels = new ArrayList<>();
    	                        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
    	                            for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
    	                                if (roi.contains(x, y))
    	                                    roiPixels.add((double) primaryIP.getPixelValue(x, y));
    	                            }
    	                        }
    	                        if (!roiPixels.isEmpty()) {
    	                            Collections.sort(roiPixels);
    	                            int total = roiPixels.size();
    	                            double trim = (1.0 - percentile) / 2.0;
    	                            int lo = (int) Math.floor(trim * total);
    	                            int hi = Math.min((int) Math.ceil((1.0 - trim) * total) - 1, total - 1);
    	                            frameMin = roiPixels.get(lo);
    	                            frameMax = roiPixels.get(hi);
    	                        }
    	                    }

    	                    double sumPixelValues = 0.0;
    	                    int validPixelCount = 0;

    	                    for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
    	                        for (int x = bounds.x; x < bounds.x + bounds.width; x++) {
    	                            if (roi.contains(x, y)) {
    	                                boolean passes;
    	                                if (percentile > 0) {
    	                                    double v = primaryIP.getPixelValue(x, y);
    	                                    passes = (v >= frameMin && v <= frameMax);
    	                                } else {
    	                                    // ── CHANGED: must pass ALL threshold videos
    	                                    passes = passesAllThresholds(x, y, thresholdProcessors, thresholdMins, thresholdMaxs);
    	                                }

    	                                if (passes) {
    	                                    sumPixelValues += primaryIP.getPixelValue(x, y);
    	                                    validPixelCount++;
    	                                }
    	                            }
    	                        }
    	                    }

    	                    double meanPixelValue = validPixelCount > 0 ? sumPixelValues / validPixelCount : 0;
    	                    roiPixelValuesMap.computeIfAbsent(j, k -> new ArrayList<>()).add(meanPixelValue);

    	                    primaryIP.setLineWidth(2);
    	                    primaryIP.setColor(Color.RED);
    	                    roi.drawPixels(primaryIP);
    	                    primaryIP.setFont(new Font("SansSerif", Font.PLAIN, 14));
    	                    primaryIP.setColor(Color.WHITE);
    	                    primaryIP.drawString("" + (j + 1), bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);
    	                }

    	                primaryVideo.updateAndDraw();
    	            } catch (Exception e) {
    	                IJ.log("Failed to load ROI from: " + roiZip.getName() + " - " + e.getMessage());
    	            }
    	        }

    	        for (Map.Entry<Integer, ArrayList<Double>> entry : roiPixelValuesMap.entrySet()) {
    	            int roiLabel = entry.getKey() + 1;
    	            ArrayList<Double> pixelValuesList = entry.getValue();

    	            double[] frameIndices = new double[pixelValuesList.size()];
    	            double[] pixelValues  = new double[pixelValuesList.size()];

    	            for (int i = 0; i < pixelValuesList.size(); i++) {
    	                frameIndices[i] = i + 1;
    	                pixelValues[i]  = pixelValuesList.get(i);
    	            }

    	            plotPixelValuesForROI(sampleName + "ROI " + roiLabel, frameIndices, pixelValues);
    	        }
    	    }
    			// Plot function
    			private void plotPixelValuesForROI(String title, double[] frameIndices, double[] pixelValues) {
    			    Plot plot = new Plot(title, "Frame", "Average Pixel Value");
    			    plot.addPoints(frameIndices, pixelValues, Plot.LINE);
    			    plot.show();
    			}
    			

    			private void createGraph(Map<String, List<Double>> roiPixelValues) {
    				
    			    // Create a plot for each ROI
    			    for (String roiName : roiPixelValues.keySet()) {
    			        List<Double> values = roiPixelValues.get(roiName);
    			        double[] xValues = new double[values.size()];
    			        double[] yValues = new double[values.size()];

    			        // Prepare x (frame number) and y (pixel value) arrays
    			        for (int i = 0; i < values.size(); i++) {
    			            xValues[i] = i + 1;  // Frame number starts from 1
    			            yValues[i] = values.get(i);
    			        }

    			        // Create a plot for this ROI
    			        Plot plot = new Plot(sampleName + "Average Pixel Value for " + roiName,
    			                "Frame", "Average Pixel Value", xValues, yValues);

    			        // Display the plot
    			        plot.show();
    			    }

    		        createMainMenu();
    			}

    		

    	    	private ImagePlus ensureStack(ImagePlus imp) {
    	    	    if (imp.getStackSize() == 1 && !imp.isStack()) {
    	    	        ImageProcessor ip = imp.getProcessor();
    	    	        ImageStack stack = new ImageStack(ip.getWidth(), ip.getHeight());
    	    	        stack.addSlice(ip);
    	    	        ImagePlus stackImp = new ImagePlus(imp.getTitle(), stack);
    	    	        stackImp.setCalibration(imp.getCalibration());
    	    	        return stackImp;
    	    	    }
    	    	    return imp;
    	    	}

    	    

      		    // Get open image titles
    		    private String[] getOpenImageTitles() {
    		        int[] imageIds = WindowManager.getIDList();
    		        if (imageIds == null) return null;

    		        String[] imageTitles = new String[imageIds.length];
    		        for (int i = 0; i < imageIds.length; i++) {
    		            imageTitles[i] = WindowManager.getImage(imageIds[i]).getTitle();
    		        }
    		        return imageTitles;
    		    }


    		    private BufferedImage createHueBar(int width, int height, boolean counterClockwise, float hueStartColour, float hueEndColour, float minZoom, float maxZoom) {
    		    	 BufferedImage hueBar = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    		    	    Graphics2D g2d = hueBar.createGraphics();

    		    	    // Smooth rendering
    		    	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    		    	    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    		    	    // ---------------- SCALE ----------------
    		    	 // Font size = 1/20 of image width
    		    	    float fontSize = width / 50f;

    		    	    Font baseFont = g2d.getFont();
    		    	    Font scaledFont = baseFont.deriveFont(fontSize);
    		    	    g2d.setFont(scaledFont);



    		    	    FontMetrics fm = g2d.getFontMetrics();

    		    	    // ---------------- SAFE BUFFER (PREVENT EDGE CLIPPING) ----------------
    		    	    String minLabel = String.format("%.2f", minZoom);
    		    	    String maxLabel = String.format("%.2f", maxZoom);
    		    	    int maxLabelWidth = Math.max(fm.stringWidth(minLabel), fm.stringWidth(maxLabel));

    		    	    int padding = Math.round(width / 80f);
    		    	    int bufferSize = maxLabelWidth / 2 + padding;

    		    	    // Fill buffer zones
    		    	    g2d.setColor(Color.GRAY);
    		    	    g2d.fillRect(0, 0, bufferSize, height);
    		    	    g2d.fillRect(width - bufferSize, 0, bufferSize, height);

    		    	    // ---------------- GRADIENT ----------------
    		    	    int gradientWidth = width - 2 * bufferSize;

    		    	    for (int x = 0; x < gradientWidth; x++) {

    		    	        float hue = (float) x / gradientWidth;

    		    	        if (!counterClockwise)
    		    	            hue = adjustHueClockwise(hue, hueStartColour, hueEndColour);
    		    	        else
    		    	            hue = adjustHueCounterClockwise(hue, hueStartColour, hueEndColour);

    		    	        Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
    		    	        g2d.setColor(color);

    		    	        g2d.drawLine(x + bufferSize, 0, x + bufferSize, height);
    		    	    }

    		    	    // ---------------- MARKERS ----------------
    		    	    g2d.setColor(Color.BLACK);
    		    	    
    		    	 
    		    	    int numMarkers = 7;
    		    	    float markerStep = (maxZoom - minZoom) / (numMarkers - 1);

    		    	    int markerHeight = Math.round(height * 0.25f);
    		    	    int labelY = Math.round(height * 0.75f);
    		    	    
    		    	    float thickness = Math.max(1f, width / 400f);
    		    	    g2d.setStroke(new BasicStroke(thickness));


    		    	    for (int i = 0; i < numMarkers; i++) {

    		    	        float marker = minZoom + i * markerStep;

    		    	        int xPosition = Math.round((marker - minZoom) / (maxZoom - minZoom) * gradientWidth) + bufferSize;
    		    	        xPosition = Math.max(bufferSize, Math.min(xPosition, width - bufferSize - 1));

    		    	        // Marker line
    		    	        g2d.drawLine(xPosition, 0, xPosition, markerHeight);

    		    	        // Centered label
    		    	        String label = String.format("%.2f", marker);
    		    	        int textWidth = fm.stringWidth(label);

    		    	        g2d.drawString(label, xPosition - textWidth / 2, labelY);
    		    	    }

    		    	    g2d.dispose();
    		    	    return hueBar;
    		    	}


    		// Adjust hue in clockwise direction
    		private float adjustHueClockwise(float hue, float hueStart, float hueEnd) {
    		    if (hueEnd > hueStart) {
    		        return hue * (hueEnd - hueStart) + hueStart;
    		    } else {
    		        return (hue * ((1.0f - hueStart) + hueEnd) + hueStart) % 1.0f;
    		    }
    		}

    		// Adjust hue in counterclockwise direction
    		private float adjustHueCounterClockwise(float hue, float hueStart, float hueEnd) {
    		    if (hueEnd > hueStart) {
    		        return ((1.0f - (hue * (1 - (hueEnd - hueStart)))) + hueStart) % 1.0f;
    		    } else {
    		        return ((1.0f - (1.0f - hueStart)) - (hue * (hueStart - hueEnd))) % 1.0f;
    		    }
    		}

    

    	// Helper method to convert float array to byte array
    	private byte[] convertToByteArray(float[] floatArray) {
    	    byte[] byteArray = new byte[floatArray.length];
    	    for (int i = 0; i < floatArray.length; i++) {
    	        byteArray[i] = (byte) Math.min(255, Math.max(0, (int) (floatArray[i])));
    	    }
    	    return byteArray;
    	}
    	
  



    

    	private class HueBarPanel extends JPanel {
    	    private static final int WIDTH = 500;
    	    private static final int HEIGHT = 50;
    	    private static final int MARKER_HEIGHT = 20; // Height of the markers
    	    private static final int GRAY_PADDING = 50; // Width of gray padding on both sides

    	    @Override
    	    protected void paintComponent(Graphics g) {
    	        super.paintComponent(g);
    	        Graphics2D g2d = (Graphics2D) g;

    	        BufferedImage hueBar = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    	        Graphics2D hueGraphics = hueBar.createGraphics();

    	        // Fill the whole bar with gray initially
    	        hueGraphics.setColor(Color.GRAY);
    	        hueGraphics.fillRect(0, 0, WIDTH, HEIGHT);

    	        // Calculate the width of the hue gradient
    	        int gradientWidth = WIDTH - 2 * GRAY_PADDING;

    	        // Draw the hue gradient in the middle
    	        for (int x = GRAY_PADDING; x < GRAY_PADDING + gradientWidth; x++) {
    	            // Calculate normalized hue value
    	            float hue = (x - GRAY_PADDING) / (float) gradientWidth;

    	            // Adjust hue for direction
    	            if (!counterClockwise) { // Clockwise direction selected
    	                if (hueEndColour > hueStartColour) {
    	                    hue = hue * (hueEndColour - hueStartColour) + hueStartColour;
    	                } else {
    	                    hue = (hue * ((1.0f - hueStartColour) + hueEndColour) + hueStartColour) % 1.0f;
    	                }
    	            } else { // Counterclockwise direction selected
    	                if (hueEndColour > hueStartColour) {
    	                    hue = ((1.0f - (hue * (1 - (hueEndColour - hueStartColour)))) + hueStartColour) % 1.0f;
    	                } else {
    	                    hue = ((1.0f - (1.0f - hueStartColour)) - (hue * (hueStartColour - hueEndColour))) % 1.0f;
    	                }
    	            }

    	            // Convert to RGB color
    	            Color color = Color.getHSBColor(hue, 1.0f, 1.0f);
    	            hueGraphics.setColor(color);
    	            hueGraphics.drawLine(x, 0, x, HEIGHT);
    	        }

    	        hueGraphics.dispose();
    	        g2d.drawImage(hueBar, 0, 0, null);

    	        // Drawing markers on the hue bar
    	        g2d.setColor(Color.BLACK);

    	        // Calculate the number of markers
    	        int numMarkers = 7;
    	        float markerStep = (maxZoom - minZoom) / (numMarkers - 1);

    	        for (int i = 0; i < numMarkers; i++) {
    	            float marker = minZoom + i * markerStep;

    	            // Calculate the position of the marker on the hue bar
    	            int xPosition = GRAY_PADDING + Math.round((marker - minZoom) / (maxZoom - minZoom) * gradientWidth);

    	            // Clamp xPosition to stay within bounds
    	            xPosition = Math.max(GRAY_PADDING, Math.min(xPosition, GRAY_PADDING + gradientWidth - 1));

    	            // Draw the vertical marker line
    	            g2d.drawLine(xPosition, 0, xPosition, MARKER_HEIGHT);

    	            // Draw the marker label
    	            g2d.drawString(String.format("%.3f", marker), xPosition - 10, MARKER_HEIGHT + 15);
    	        }
    	    }
    	}

    	// Inner class to create the Hue Bar Window
    	private class HueBarWindow extends JFrame {
    	    public HueBarWindow() {
    	        setTitle("Hue Bar");
    	        setSize(500, 100);
    	        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	        setLayout(new BorderLayout());

    	        HueBarPanel hueBarPanel = new HueBarPanel();
    	        add(hueBarPanel, BorderLayout.CENTER);
    	    }
    	}

    	

    public static void main(String[] args) {
        // Launch ImageJ
        new ij.ImageJ();
        

        // Run the plugin
        IJ.runPlugIn(AR_IA.class.getName(), "");
    }
} 


