/*
 * @(#)MovieMakerMain.java  
 * 
 * Copyright © 2010-2011 Werner Randelshofer, Goldau, Switzerland.
 * All rights reserved.
 * 
 * You may not use, copy or modify this file, except in compliance with the
 * license agreement you entered into with Werner Randelshofer.
 * For details see accompanying license terms.
 */
package org.monte.moviemaker;

import javax.swing.*;

import org.monte.media.math.Rational;
import org.monte.media.Buffer;
import org.monte.media.Format;
import org.monte.media.gui.datatransfer.FileTextFieldTransferHandler;
import org.monte.media.mp3.MP3AudioInputStream;
import org.monte.media.quicktime.QuickTimeWriter;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import static java.lang.Math.*;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.AudioFormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;
import org.monte.screenrecorder.ScreenRecorderMain;

/**
 * A demo for the {@link QuickTimeWriter} class.
 *
 * @author Werner Randelshofer
 * @version $Id: MovieMakerMain.java 301 2013-01-03 07:40:42Z werner $
 */
public class MovieMakerMain extends javax.swing.JFrame {

    private JFileChooser imageFolderChooser;
    private JFileChooser soundFileChooser;
    private JFileChooser movieFileChooser;
    private Preferences prefs;

    enum WhatToDo {

        REPEAT_SHORTER_TRACK,
        STRETCH_AND_SQUASH_VIDEO_TRACK,
        CUT_LONGER_TRACK,
        DONT_CARE
    }
    private WhatToDo whatToDo = WhatToDo.STRETCH_AND_SQUASH_VIDEO_TRACK;

    /** Creates new form MovieMakerMain */
    public MovieMakerMain() {
        try {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    } else {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                e.printStackTrace();
                System.err.println("Nimbus look and feel not found on this Java system.");
            }

        initComponents();

        String version = getClass().getPackage().getImplementationVersion();
        if (version != null) {
            setTitle(getTitle() + " " + version);
        }

        ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 18, 18, 18));
        imageFolderField.setTransferHandler(new FileTextFieldTransferHandler(JFileChooser.DIRECTORIES_ONLY));
        soundFileField.setTransferHandler(new FileTextFieldTransferHandler());

        JComponent[] smallComponents = {compressionBox,
            compressionLabel,
            fpsField,
            fpsLabel,
            heightField,
            heightLabel,
            widthField,
            widthLabel,
            passThroughCheckBox,
            noPreparationRadio,
            fastStartCompressedRadio,
            fastStartRadio};
        for (JComponent c : smallComponents) {
            c.putClientProperty("JComponent.sizeVariant", "small");
        }

        // Get Preferences
        prefs = Preferences.userNodeForPackage(MovieMakerMain.class);
        imageFolderField.setText(prefs.get("movie.imageFolder", ""));
        soundFileField.setText(prefs.get("movie.soundFile", ""));
        widthField.setText("" + prefs.getInt("movie.width", 320));
        heightField.setText("" + prefs.getInt("movie.height", 240));
        passThroughCheckBox.setSelected(prefs.getBoolean("movie.passThrough", false));
        String fps = "" + prefs.getDouble("movie.fps", 30);
        if (fps.endsWith(".0")) {
            fps = fps.substring(0, fps.length() - 2);
        }
        fpsField.setText(fps);
        compressionBox.setSelectedIndex(Math.max(0, Math.min(compressionBox.getItemCount() - 1, prefs.getInt("movie.compression", 1))));

        //
        String streaming = prefs.get("movie.streaming", "fastStartCompressed");
        for (Enumeration<AbstractButton> i = streamingGroup.getElements(); i.hasMoreElements();) {
            AbstractButton btn = i.nextElement();
            if (btn.getActionCommand().equals(streaming)) {
                btn.setSelected(true);
                break;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        streamingGroup = new javax.swing.ButtonGroup();
        aboutLabel = new javax.swing.JLabel();
        imageFolderHelpLabel = new javax.swing.JLabel();
        imageFolderField = new javax.swing.JTextField();
        chooseImageFolderButton = new javax.swing.JButton();
        soundFileHelpLable = new javax.swing.JLabel();
        soundFileField = new javax.swing.JTextField();
        chooseSoundFileButton = new javax.swing.JButton();
        createMovieButton = new javax.swing.JButton();
        widthLabel = new javax.swing.JLabel();
        widthField = new javax.swing.JTextField();
        heightLabel = new javax.swing.JLabel();
        heightField = new javax.swing.JTextField();
        compressionLabel = new javax.swing.JLabel();
        compressionBox = new javax.swing.JComboBox();
        fpsLabel = new javax.swing.JLabel();
        fpsField = new javax.swing.JTextField();
        passThroughCheckBox = new javax.swing.JCheckBox();
        streamingLabel = new javax.swing.JLabel();
        noPreparationRadio = new javax.swing.JRadioButton();
        fastStartRadio = new javax.swing.JRadioButton();
        fastStartCompressedRadio = new javax.swing.JRadioButton();

        FormListener formListener = new FormListener();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("QuickTime Movie Maker");
        setIconImage(new ImageIcon("resource/icons/monte/cam.png").getImage());
        addWindowListener(formListener);

        aboutLabel.setText("<html><b>This is a demo of the Monte Media library.</b><br>Copyright © 2010-2012 Werner Randelshofer. All rights reserved.<br> This software can be licensed under Creative Commons Atribution 3.0.");

        imageFolderHelpLabel.setText("Drag a folder with image files into the field below:");

        chooseImageFolderButton.setText("Choose...");
        chooseImageFolderButton.addActionListener(formListener);

        soundFileHelpLable.setText("Drag a sound file into the field below (.au, .aiff, .wav, .mp3):");

        chooseSoundFileButton.setText("Choose...");
        chooseSoundFileButton.addActionListener(formListener);

        createMovieButton.setText("Create QuickTime Movie...");
        createMovieButton.addActionListener(formListener);

        widthLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        widthLabel.setText("Width:");

        widthField.setColumns(4);
        widthField.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        widthField.setText("320");

        heightLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        heightLabel.setText("Height:");

        heightField.setColumns(4);
        heightField.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        heightField.setText("240");

        compressionLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        compressionLabel.setText("Compression:");

        compressionBox.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        compressionBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None", "Animation", "JPEG", "PNG" }));

        fpsLabel.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        fpsLabel.setText("FPS:");

        fpsField.setColumns(4);
        fpsField.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        fpsField.setText("30");

        passThroughCheckBox.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        passThroughCheckBox.setText("Pass through");
        passThroughCheckBox.setToolTipText("Check this box if the folder contains already encoded video frames in the desired size.");

        streamingLabel.setText("Prepare for Internet Streaming");

        streamingGroup.add(noPreparationRadio);
        noPreparationRadio.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        noPreparationRadio.setSelected(true);
        noPreparationRadio.setText("No preparation");
        noPreparationRadio.setActionCommand("none");
        noPreparationRadio.addActionListener(formListener);

        streamingGroup.add(fastStartRadio);
        fastStartRadio.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        fastStartRadio.setText("Fast Start");
        fastStartRadio.setActionCommand("fastStart");
        fastStartRadio.addActionListener(formListener);

        streamingGroup.add(fastStartCompressedRadio);
        fastStartCompressedRadio.setFont(new java.awt.Font("Lucida Grande", 0, 11)); // NOI18N
        fastStartCompressedRadio.setText("Fast Start - Compressed Header");
        fastStartCompressedRadio.setActionCommand("fastStartCompressed");
        fastStartCompressedRadio.addActionListener(formListener);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(aboutLabel)
                            .addComponent(imageFolderHelpLabel)
                            .addComponent(soundFileHelpLable)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(soundFileField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseSoundFileButton))
                            .addComponent(createMovieButton, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(imageFolderField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(chooseImageFolderButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(61, 61, 61)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(widthLabel)
                                    .addComponent(fpsLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(fpsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(compressionLabel)
                                        .addGap(1, 1, 1)
                                        .addComponent(compressionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(passThroughCheckBox))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(widthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(heightLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(heightField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(streamingLabel)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(fastStartRadio)
                                            .addComponent(noPreparationRadio)
                                            .addComponent(fastStartCompressedRadio))))))
                        .addGap(41, 41, 41)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(aboutLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(imageFolderHelpLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(imageFolderField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseImageFolderButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(widthLabel)
                    .addComponent(widthField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightLabel)
                    .addComponent(heightField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(compressionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fpsLabel)
                    .addComponent(fpsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(compressionLabel)
                    .addComponent(passThroughCheckBox))
                .addGap(18, 18, 18)
                .addComponent(soundFileHelpLable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(soundFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chooseSoundFileButton))
                .addGap(18, 18, 18)
                .addComponent(streamingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(noPreparationRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fastStartRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fastStartCompressedRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(createMovieButton)
                .addContainerGap())
        );

        pack();
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener, java.awt.event.WindowListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == chooseImageFolderButton) {
                MovieMakerMain.this.chooseImageFolder(evt);
            }
            else if (evt.getSource() == chooseSoundFileButton) {
                MovieMakerMain.this.chooseSoundFile(evt);
            }
            else if (evt.getSource() == createMovieButton) {
                MovieMakerMain.this.createMovie(evt);
            }
            else if (evt.getSource() == noPreparationRadio) {
                MovieMakerMain.this.streamingRadioPerformed(evt);
            }
            else if (evt.getSource() == fastStartRadio) {
                MovieMakerMain.this.streamingRadioPerformed(evt);
            }
            else if (evt.getSource() == fastStartCompressedRadio) {
                MovieMakerMain.this.streamingRadioPerformed(evt);
            }
        }

        public void windowActivated(java.awt.event.WindowEvent evt) {
        }

        public void windowClosed(java.awt.event.WindowEvent evt) {
        }

        public void windowClosing(java.awt.event.WindowEvent evt) {
            if (evt.getSource() == MovieMakerMain.this) {
                MovieMakerMain.this.formWindowClosing(evt);
            }
        }

        public void windowDeactivated(java.awt.event.WindowEvent evt) {
        }

        public void windowDeiconified(java.awt.event.WindowEvent evt) {
        }

        public void windowIconified(java.awt.event.WindowEvent evt) {
        }

        public void windowOpened(java.awt.event.WindowEvent evt) {
        }
    }// </editor-fold>//GEN-END:initComponents

    private void chooseImageFolder(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseImageFolder
        if (imageFolderChooser == null) {
            imageFolderChooser = new JFileChooser();
            imageFolderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (imageFolderField.getText().length() > 0) {
                imageFolderChooser.setSelectedFile(new File(imageFolderField.getText()));
            } else if (soundFileField.getText().length() > 0) {
                imageFolderChooser.setCurrentDirectory(new File(soundFileField.getText()).getParentFile());
            }
        }
        if (JFileChooser.APPROVE_OPTION == imageFolderChooser.showOpenDialog(this)) {
            imageFolderField.setText(imageFolderChooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_chooseImageFolder

    private void chooseSoundFile(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseSoundFile
        if (soundFileChooser == null) {
            soundFileChooser = new JFileChooser();
            if (soundFileField.getText().length() > 0) {
                soundFileChooser.setSelectedFile(new File(soundFileField.getText()));
            } else if (imageFolderField.getText().length() > 0) {
                soundFileChooser.setCurrentDirectory(new File(imageFolderField.getText()));
            }
        }
        if (JFileChooser.APPROVE_OPTION == soundFileChooser.showOpenDialog(this)) {
            soundFileField.setText(soundFileChooser.getSelectedFile().getPath());
        }
    }//GEN-LAST:event_chooseSoundFile

    private void createMovie(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMovie
        // ---------------------------------
        // Check input
        // ---------------------------------
        final File soundFile = soundFileField.getText().trim().length() == 0 ? null : new File(soundFileField.getText().trim());
        final File imageFolder = imageFolderField.getText().trim().length() == 0 ? null : new File(imageFolderField.getText().trim());
        final String streaming = prefs.get("movie.streaming", "fastStartCompressed");
        if (soundFile == null && imageFolder == null) {
            JOptionPane.showMessageDialog(this, "<html>You need to specify a folder with<br>image files and/or a sound file.");
            return;
        }

        final int width, height;
        final double fps;
        try {
            width = Integer.parseInt(widthField.getText());
            height = Integer.parseInt(heightField.getText());
            fps = Double.parseDouble(fpsField.getText());
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(this, "<html>Width, Height and FPS must be numeric.");
            return;
        }
        if (fps!=fps) {
            JOptionPane.showMessageDialog(this, "<html>FPS must be a real number greater than zero.");
            return;
        }
        if (width <= 0 || height <= 0 || fps <= 0.0) {
            JOptionPane.showMessageDialog(this, "<html>Width, Height and FPS must be greater than zero.");
            return;
        }

        final Format videoFormat;
        switch (compressionBox.getSelectedIndex()) {
            case 0:
                videoFormat = QuickTimeWriter.VIDEO_RAW;
                break;
            case 1:
                videoFormat = QuickTimeWriter.VIDEO_ANIMATION;
                break;
            case 2:
                videoFormat = QuickTimeWriter.VIDEO_JPEG;
                break;
            case 3:
            default:
                videoFormat = QuickTimeWriter.VIDEO_PNG;
                break;
        }

        // ---------------------------------
        // Update Preferences
        // ---------------------------------
        prefs.put("movie.imageFolder", imageFolderField.getText());
        prefs.put("movie.soundFile", soundFileField.getText());
        prefs.putInt("movie.width", width);
        prefs.putInt("movie.height", height);
        prefs.putDouble("movie.fps", fps);
        prefs.putInt("movie.compression", compressionBox.getSelectedIndex());
        prefs.putBoolean("movie.passThrough", passThroughCheckBox.isSelected());


        // ---------------------------------
        // Choose an output file
        // ---------------------------------
        if (movieFileChooser == null) {
            movieFileChooser = new JFileChooser();
            if (prefs.get("movie.outputFile", null) != null) {
                movieFileChooser.setSelectedFile(new File(prefs.get("movie.outputFile", null)));
            } else {
                if (imageFolderField.getText().length() > 0) {
                    movieFileChooser.setCurrentDirectory(new File(imageFolderField.getText()).getParentFile());
                } else if (soundFileField.getText().length() > 0) {
                    movieFileChooser.setCurrentDirectory(new File(soundFileField.getText()).getParentFile());
                }
            }
        }
        if (JFileChooser.APPROVE_OPTION != movieFileChooser.showSaveDialog(this)) {
            return;
        }

        final File movieFile = movieFileChooser.getSelectedFile().getPath().toLowerCase().endsWith(".mov")//
                ? movieFileChooser.getSelectedFile()
                : new File(movieFileChooser.getSelectedFile().getPath() + ".mov");
        prefs.put("movie.outputFile", movieFile.getPath());
        createMovieButton.setEnabled(false);

        final boolean passThrough = passThroughCheckBox.isSelected();

        // ---------------------------------
        // Create the QuickTime movie
        // ---------------------------------
        SwingWorker w = new SwingWorker() {

            @Override
            protected Object doInBackground() {
                try {

                    // Read image files
                    File[] imgFiles = null;
                    if (imageFolder != null) {
                        imgFiles = imageFolder.listFiles(new FileFilter() {

                            FileSystemView fsv = FileSystemView.getFileSystemView();

                            @Override
                            public boolean accept(File f) {
                                return f.isFile() && !fsv.isHiddenFile(f) && !f.getName().equals("Thumbs.db");
                            }
                        });
                        if (imgFiles != null) {
                            Arrays.sort(imgFiles);
                        }
                    }

                    // FIXME Check on first image, if we can actually do pass through
                    if (passThrough) {
                    }

                    // Delete movie file if it already exists.
                    if (movieFile.exists()) {
                        movieFile.delete();
                    }

                    if (imgFiles != null && soundFile != null && imgFiles.length > 0) {
                        writeVideoAndAudio(movieFile, imgFiles, soundFile, width, height, fps, videoFormat, passThrough, streaming);
                    } else if (imgFiles != null && imgFiles.length > 0) {
                        writeVideoOnlyVFR(movieFile, imgFiles, width, height, fps, videoFormat, passThrough, streaming);
                    } else if (soundFile != null) {
                        writeAudioOnly(movieFile, soundFile, streaming);

                    }
                    return null;
                } catch (Throwable t) {
                    return t;
                }
            }

            @Override
            protected void done() {
                Object o;
                try {
                    o = get();
                } catch (Exception ex) {
                    o = ex;
                }
                if (o instanceof Throwable) {
                    Throwable t = (Throwable) o;
                    t.printStackTrace();
                    JOptionPane.showMessageDialog(MovieMakerMain.this, "<html>Creating the QuickTime Movie failed.<br>" + (t.getMessage() == null ? t.toString() : t.getMessage()), "Sorry", JOptionPane.ERROR_MESSAGE);
                }
                createMovieButton.setEnabled(true);
            }
        };
        w.execute();


    }//GEN-LAST:event_createMovie

    private void streamingRadioPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_streamingRadioPerformed
        prefs.put("movie.streaming", evt.getActionCommand());
    }//GEN-LAST:event_streamingRadioPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
                ScreenRecorderMain.movie_maker_running = false;
            
    }//GEN-LAST:event_formWindowClosing

    
    
    /** variable frame rate. */
    private void writeVideoOnlyVFR(File movieFile, File[] imgFiles, int width, int height, double fps, Format videoFormat, boolean passThrough, String streaming) throws IOException {
        File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");
        ProgressMonitor p = new ProgressMonitor(MovieMakerMain.this, "Creating " + movieFile.getName(), "Creating Output File...", 0, imgFiles.length);
        Graphics2D g = null;
        BufferedImage img = null;
        BufferedImage prevImg = null;
        int[] data = null;
        int[] prevData = null;
        QuickTimeWriter qtOut = null;
        try {
            int timeScale = (int) (fps * 100.0);
            int duration = 100;

            qtOut = new QuickTimeWriter(tmpFile);
            int vt = qtOut.addVideoTrack(videoFormat, timeScale, width, height);
            qtOut.setSyncInterval(0, 30);

            if (!passThrough) {
                img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                data = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
                prevImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                prevData = ((DataBufferInt) prevImg.getRaster().getDataBuffer()).getData();
                g = img.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            }
            int prevImgDuration = 0;
            Buffer buf = new Buffer();
            for (int i = 0; i < imgFiles.length && !p.isCanceled(); i++) {
                File f = imgFiles[i];
                p.setNote("Processing " + f.getName());
                p.setProgress(i);

                if (passThrough) {
                    qtOut.writeSample(vt, f, duration, true);
                } else {
                    BufferedImage fImg = ImageIO.read(f);
                    g.drawImage(fImg, 0, 0, width, height, null);
                    if (i != 0 && Arrays.equals(data, prevData)) {
                        prevImgDuration += duration;
                    } else {
                        if (prevImgDuration != 0) {
                            qtOut.write(vt, prevImg, prevImgDuration);
                        }
                        prevImgDuration = duration;
                        System.arraycopy(data, 0, prevData, 0, data.length);
                    }
                }
            }
            if (prevImgDuration != 0) {
                qtOut.write(vt, prevImg, prevImgDuration);
            }
            if (streaming.equals("fastStart")) {
                qtOut.toWebOptimizedMovie(movieFile, false);
                tmpFile.delete();
            } else if (streaming.equals("fastStartCompressed")) {
                qtOut.toWebOptimizedMovie(movieFile, true);
                tmpFile.delete();
            }
            qtOut.close();
            qtOut = null;
        } finally {
            p.close();
            if (g != null) {
                g.dispose();
            }
            if (img != null) {
                img.flush();
            }
            if (qtOut != null) {
                qtOut.close();
            }
        }
    }

    /** fixed framerate. */
    private void writeVideoOnlyFFR(File movieFile, File[] imgFiles, int width, int height, double fps, Format videoFormat, boolean passThrough, String streaming) throws IOException {
        File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");
        ProgressMonitor p = new ProgressMonitor(MovieMakerMain.this, "Creating " + movieFile.getName(), "Creating Output File...", 0, imgFiles.length);
        Graphics2D g = null;
        BufferedImage imgBuffer = null;
        QuickTimeWriter qtOut = null;

        try {
            Rational duration = new Rational(1000,(int)(fps*1000));
            qtOut = new QuickTimeWriter(tmpFile);
            int vt = qtOut.addTrack(videoFormat.append(WidthKey,width, HeightKey,height));
            //qtOut.setSyncInterval(0,0);
            if (!passThrough) {
                imgBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                g = imgBuffer.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            }
            Buffer buf = new Buffer();
            for (int i = 0; i < imgFiles.length && !p.isCanceled(); i++) {
                File f = imgFiles[i];
                p.setNote("Processing " + f.getName());
                p.setProgress(i);

                if (passThrough) {
                    qtOut.writeSample(vt, f, duration.floor(qtOut.getMediaTimeScale(vt)).getNumerator(), true);
                } else {
                    BufferedImage fImg = ImageIO.read(f);
                    if (fImg == null) {
                        continue;
                    }
                    g.drawImage(fImg, 0, 0, width, height, null);
                    buf.data = imgBuffer;
                    buf.sampleDuration = duration;
                    qtOut.write(vt, buf);
                }
            }
            if (streaming.equals("fastStart")) {
                qtOut.toWebOptimizedMovie(movieFile, false);
                tmpFile.delete();
            } else if (streaming.equals("fastStartCompressed")) {
                qtOut.toWebOptimizedMovie(movieFile, true);
                tmpFile.delete();
            }
            qtOut.close();
            qtOut = null;
        } finally {
            p.close();
            if (g != null) {
                g.dispose();
            }
            if (imgBuffer != null) {
                imgBuffer.flush();
            }
            if (qtOut != null) {
                qtOut.close();
            }
        }
    }

    private void writeAudioOnly(File movieFile, File audioFile, String streaming) throws IOException {
        File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");

        int length = (int) Math.min(Integer.MAX_VALUE, audioFile.length()); // file length is used for a rough progress estimate. This will only work for uncompressed audio.
        ProgressMonitor p = new ProgressMonitor(MovieMakerMain.this, "Creating " + movieFile.getName(), "Initializing...", 0, length);
        AudioInputStream audioIn = null;
        QuickTimeWriter qtOut = null;

        try {
            qtOut = new QuickTimeWriter(tmpFile);
            if (audioFile.getName().toLowerCase().endsWith(".mp3")) {
                audioIn = new MP3AudioInputStream(audioFile);
            } else {
                audioIn = AudioSystem.getAudioInputStream(audioFile);
            }
            AudioFormat audioFormat = audioIn.getFormat();
            //System.out.println("MovieMakerMain " + audioFormat);
            int at = qtOut.addAudioTrack(audioFormat);
            boolean isVBR = audioFormat.getProperty("vbr") != null && ((Boolean) audioFormat.getProperty("vbr")).booleanValue();
            int asSize = audioFormat.getFrameSize();
            int nbOfFramesInBuffer = isVBR ? 1 : Math.max(1, 1024 / asSize);
            int asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
            //System.out.println("  frameDuration=" + asDuration);
            long count = 0;
            byte[] audioBuffer = new byte[asSize * nbOfFramesInBuffer];
            for (int bytesRead = audioIn.read(audioBuffer); bytesRead
                    != -1; bytesRead = audioIn.read(audioBuffer)) {
                if (bytesRead != 0) {
                    int framesRead = bytesRead / asSize;
                    qtOut.writeSamples(at, framesRead, audioBuffer, 0, bytesRead, asDuration);
                    count += bytesRead;
                    p.setProgress((int) count);
                }
                if (isVBR) {
                    audioFormat = audioIn.getFormat();
                    if (audioFormat == null) {
                        break;
                    }
                    asSize = audioFormat.getFrameSize();
                    asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
                    if (audioBuffer.length < asSize) {
                        audioBuffer = new byte[asSize];
                    }
                }
            }
            audioIn.close();
            audioIn = null;
            if (streaming.equals("fastStart")) {
                qtOut.toWebOptimizedMovie(movieFile, false);
                tmpFile.delete();
            } else if (streaming.equals("fastStartCompressed")) {
                qtOut.toWebOptimizedMovie(movieFile, true);
                tmpFile.delete();
            }
            qtOut.close();
            qtOut = null;
        } catch (UnsupportedAudioFileException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } finally {
            p.close();
            if (audioIn != null) {
                audioIn.close();
            }
            if (qtOut != null) {
                qtOut.close();
            }
        }
    }

    private void writeVideoAndAudio(File movieFile, File[] imgFiles, File audioFile, int width, int height, double fps, Format videoFormat, boolean passThrough, String streaming) throws IOException {
        File tmpFile = streaming.equals("none") ? movieFile : new File(movieFile.getPath() + ".tmp");
        ProgressMonitor p = new ProgressMonitor(MovieMakerMain.this, "Creating " + movieFile.getName(), "Creating Output File...", 0, imgFiles.length);
        AudioInputStream audioIn = null;
        QuickTimeWriter qtOut = null;
        BufferedImage imgBuffer = null;
        Graphics2D g = null;

        try {
            // Determine audio format
            if (audioFile.getName().toLowerCase().endsWith(".mp3")) {
                audioIn = new MP3AudioInputStream(audioFile);
            } else {
                audioIn = AudioSystem.getAudioInputStream(audioFile);
            }
            AudioFormat audioFormat = audioIn.getFormat();
            boolean isVBR = audioFormat.getProperty("vbr") != null && ((Boolean) audioFormat.getProperty("vbr")).booleanValue();

            // Determine sampleDuration of a single sample
            int asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
            int vsDuration = 100;
            // Create writer
            qtOut = new QuickTimeWriter(tmpFile);
            int at = qtOut.addAudioTrack(audioFormat); // audio in track 0
            int vt = qtOut.addVideoTrack(videoFormat, (int) (fps * vsDuration), width, height);  // video in track 1
            qtOut.setCompressionQuality(vt, 0.95f);

            // Create audio buffer
            int asSize;
            byte[] audioBuffer;
            if (isVBR) {
                // => variable bit rate: create audio buffer for a single frame
                asSize = audioFormat.getFrameSize();
                audioBuffer = new byte[asSize];
            } else {
                // => fixed bit rate: create audio buffer for half a second
                asSize = audioFormat.getChannels() * audioFormat.getSampleSizeInBits() / 8;
                audioBuffer = new byte[(int) (qtOut.getMediaTimeScale(0) / 2 * asSize)];
            }

            // Create video buffer
            if (!passThrough) {
                imgBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                g = imgBuffer.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            } // Main loop
            int movieTime = 0;
            int imgIndex = 0;
            boolean isAudioDone = false;
            Buffer buf = new Buffer();
            while ((imgIndex < imgFiles.length || !isAudioDone) && !p.isCanceled()) {
                // Advance movie time by half a second (we interleave twice per second)
                movieTime += qtOut.getMovieTimeScale() / 2;

                // Advance audio to movie time + 1 second (audio must be ahead of video by 1 second)
                while (!isAudioDone && qtOut.getTrackDuration(0) < movieTime + qtOut.getMovieTimeScale()) {
                    int len = audioIn.read(audioBuffer);
                    if (len == -1) {
                        isAudioDone = true;
                    } else {
                        qtOut.writeSamples(at, len / asSize, audioBuffer, 0, len, asDuration);
                    }
                    if (isVBR) {
                        // => variable bit rate: format can change at any time
                        audioFormat = audioIn.getFormat();
                        if (audioFormat == null) {
                            break;
                        }
                        asSize = audioFormat.getFrameSize();
                        asDuration = (int) (audioFormat.getSampleRate() / audioFormat.getFrameRate());
                        if (audioBuffer.length < asSize) {
                            audioBuffer = new byte[asSize];
                        }
                    }
                }

                // Advance video to movie time
                while (imgIndex < imgFiles.length && qtOut.getTrackDuration(1) < movieTime) {
                    // catch up with video time
                    p.setProgress(imgIndex);
                    p.setNote("Processing " + imgFiles[imgIndex].getName());
                    if (passThrough) {
                        qtOut.writeSample(vt, imgFiles[imgIndex], vsDuration, true);
                    } else {
                        BufferedImage fImg = ImageIO.read(imgFiles[imgIndex]);
                        if (fImg == null) {
                            continue;
                        }
                        g.drawImage(fImg, 0, 0, width, height, null);
                        fImg.flush();
                        qtOut.write(vt, imgBuffer, vsDuration);
                    }
                    ++imgIndex;
                }
            }

            switch (whatToDo) {
                case CUT_LONGER_TRACK: {
                    long d0 = qtOut.getTrackDuration(at);
                    long d1 = qtOut.getTrackDuration(vt);
                    int longerTrack = -1;
                    int shorterDuration = -1;
                    if (d0 != 0 && d1 != 0) {
                        if (d0 > d1) {
                            longerTrack = 0;
                            shorterDuration = (int) d1;
                        } else if (d1 > d0) {
                            longerTrack = 1;
                            shorterDuration = (int) d0;
                        }
                    }
                    if (longerTrack != -1) {
                        LinkedList<QuickTimeWriter.Edit> l = new LinkedList<QuickTimeWriter.Edit>();
                        l.add(new QuickTimeWriter.Edit(shorterDuration, 0, 1.0));       // sampleDuration, media time, media rate
                        qtOut.setEditList(longerTrack, l.toArray(new QuickTimeWriter.Edit[l.size()]));
                    }
                }
                break;
                case DONT_CARE:
                    break;
                case REPEAT_SHORTER_TRACK: {
                    long d0 = qtOut.getTrackDuration(at);
                    long d1 = qtOut.getTrackDuration(vt);
                    int shorterTrack = -1;
                    int longerTrack = -1;
                    int shorterDuration = -1;
                    int longerDuration = -1;
                    if (d0 != 0 && d1 != 0) {
                        if (d0 > d1) {
                            shorterTrack = 1;
                            longerTrack = 0;
                            shorterDuration = (int) d1;
                            longerDuration = (int) d0;
                        } else if (d1 > d0) {
                            longerTrack = 1;
                            shorterTrack = 1;
                            shorterDuration = (int) d0;
                            longerDuration = (int) d1;
                        }
                    }
                    if (longerTrack != -1) {
                        LinkedList<QuickTimeWriter.Edit> l = new LinkedList<QuickTimeWriter.Edit>();
                        for (; longerDuration > 0; longerDuration -= shorterDuration) {
                            l.add(new QuickTimeWriter.Edit(min(shorterDuration, longerDuration), 0, 1.0));       // sampleDuration, media time, media rate
                        }
                        qtOut.setEditList(shorterTrack, l.toArray(new QuickTimeWriter.Edit[l.size()]));
                    }
                }
                break;
                case STRETCH_AND_SQUASH_VIDEO_TRACK: {
                    long d0 = qtOut.getTrackDuration(at);
                    long d1 = qtOut.getTrackDuration(vt);
                    if (d0 != d1 && d0 != 0 && d1 != 0) {
                        LinkedList<QuickTimeWriter.Edit> l = new LinkedList<QuickTimeWriter.Edit>();
                        l.add(new QuickTimeWriter.Edit((int) d0, 0, d1 / (float) d0));       // sampleDuration, media time, media rate
                        qtOut.setEditList(1, l.toArray(new QuickTimeWriter.Edit[l.size()]));
                    }

                }
                break;
            }

            if (streaming.equals("fastStart")) {
                qtOut.toWebOptimizedMovie(movieFile, false);
                tmpFile.delete();
            } else if (streaming.equals("fastStartCompressed")) {
                qtOut.toWebOptimizedMovie(movieFile, true);
                tmpFile.delete();
            }
            qtOut.close();
            qtOut = null;
        } catch (UnsupportedAudioFileException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        } finally {
            p.close();
            if (qtOut != null) {
                qtOut.close();
            }
            if (audioIn != null) {
                audioIn.close();

            }
            if (g != null) {
                g.dispose();


            }
            if (imgBuffer != null) {
                imgBuffer.flush();


            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                //ignore
            }
            MovieMakerMain m = new MovieMakerMain();
            m.setVisible(true);
            m.pack();
        });


    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel aboutLabel;
    private javax.swing.JButton chooseImageFolderButton;
    private javax.swing.JButton chooseSoundFileButton;
    private javax.swing.JComboBox compressionBox;
    private javax.swing.JLabel compressionLabel;
    private javax.swing.JButton createMovieButton;
    private javax.swing.JRadioButton fastStartCompressedRadio;
    private javax.swing.JRadioButton fastStartRadio;
    private javax.swing.JTextField fpsField;
    private javax.swing.JLabel fpsLabel;
    private javax.swing.JTextField heightField;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JTextField imageFolderField;
    private javax.swing.JLabel imageFolderHelpLabel;
    private javax.swing.JRadioButton noPreparationRadio;
    private javax.swing.JCheckBox passThroughCheckBox;
    private javax.swing.JTextField soundFileField;
    private javax.swing.JLabel soundFileHelpLable;
    private javax.swing.ButtonGroup streamingGroup;
    private javax.swing.JLabel streamingLabel;
    private javax.swing.JTextField widthField;
    private javax.swing.JLabel widthLabel;
    // End of variables declaration//GEN-END:variables
}
