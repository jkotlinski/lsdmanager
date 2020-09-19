import java.awt.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;
import java.util.prefs.*;

public class Frame extends JFrame implements ActionListener, ListSelectionListener {
    
    LSDSavFile file;
    String latestSavPath = "\\";
    String latestSngPath = "\\";
    
    JButton addLsdSngButton = new JButton();
    JButton clearSlotButton = new JButton();
    JButton exportLsdSngButton = new JButton();
    JButton openSavButton = new JButton();
    JButton saveSavAsButton = new JButton();
    JProgressBar jRamUsageIndicator = new JProgressBar();
    JList<String> songList = new JList<>();
    JScrollPane songs = new JScrollPane(songList);
    JButton importV2SavButton = new JButton();
    JButton exportV2SavButton = new JButton();
    JLabel workMemLabel = new JLabel();
    
    Preferences preferences;
    private static final String LATEST_SAV_PATH = "latest_sav_path";
    private static final String LATEST_SNG_PATH = "latest_sng_path";
    private static final long serialVersionUID = 1279298060794170168L;

    public Frame() {
        file = new LSDSavFile();

        preferences = Preferences.userNodeForPackage(Frame.class);
        latestSavPath = preferences.get(LATEST_SAV_PATH, latestSavPath);
        latestSngPath = preferences.get(LATEST_SNG_PATH, latestSngPath);
        
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() {
        addLsdSngButton.setBounds(new Rectangle(197, 108, 140, 31));
        addLsdSngButton.setEnabled(false);
        addLsdSngButton.setToolTipText(
                "Add compressed .lsdsng to file memory");
        addLsdSngButton.setText("Add .lsdsng...");
        addLsdSngButton.addActionListener(this);
        clearSlotButton.setBounds(new Rectangle(197, 267, 139, 30));
        clearSlotButton.setEnabled(false);
        clearSlotButton.setToolTipText("Clear file memory slot");
        clearSlotButton.setText("Clear Slot");
        clearSlotButton.addActionListener(this);
        exportLsdSngButton.setBounds(new Rectangle(197, 144, 140, 31));
        exportLsdSngButton.setEnabled(false);
        exportLsdSngButton.setToolTipText(
                "Export compressed .lsdsng from file memory");
        exportLsdSngButton.setText("Export .lsdsng...");
        exportLsdSngButton.addActionListener(this);
        songList.addListSelectionListener(this);

        openSavButton.setBounds(new Rectangle(198, 11, 139, 36));
        openSavButton.setPreferredSize(new Dimension(99, 20));
        openSavButton.setText("Open V3+ .SAV...");
        openSavButton.addActionListener(this);
        saveSavAsButton.setBounds(new Rectangle(198, 52, 139, 36));
        saveSavAsButton.setEnabled(false);
        saveSavAsButton.setText("Save V3+ .SAV as...");
        saveSavAsButton.addActionListener(this);
        jRamUsageIndicator.setString("");
        jRamUsageIndicator.setStringPainted(true);
        jRamUsageIndicator.setBounds(new Rectangle(11, 276, 178, 20));

        this.getContentPane().setLayout(null);
        this.setSize(new Dimension(352, 333));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle("LSDManager v1.0");

        songs.setBounds(new Rectangle(11, 35, 178, 236));
        importV2SavButton.setBounds(new Rectangle(197, 195, 139, 31));
        importV2SavButton.setEnabled(false);
        importV2SavButton.setToolTipText(
                "Import 32 kByte V2 .SAV file to work memory "
                        + "(will overwrite what's already there!!)");
        importV2SavButton.setActionCommand("Import V2 SAV...");
        importV2SavButton.setText("Import V2 .SAV...");
        importV2SavButton.addActionListener(this);
        exportV2SavButton.setBounds(new Rectangle(197, 231, 139, 31));
        exportV2SavButton.setEnabled(false);
        exportV2SavButton.setToolTipText(
                "Export work memory to 32 kByte V2 .SAV");
        exportV2SavButton.setActionCommand("Export to V2 SAV...");
        exportV2SavButton.setText("Export V2 .SAV...");
        exportV2SavButton.addActionListener(this);
        workMemLabel.setText("Work memory empty.");
        workMemLabel.setBounds(new Rectangle(11, 11, 178, 21));
        this.getContentPane().add(songs);
        this.getContentPane().add(openSavButton);
        this.getContentPane().add(saveSavAsButton);
        this.getContentPane().add(addLsdSngButton);
        this.getContentPane().add(exportLsdSngButton);
        this.getContentPane().add(clearSlotButton);
        this.getContentPane().add(importV2SavButton);
        this.getContentPane().add(exportV2SavButton);
        this.getContentPane().add(jRamUsageIndicator);
        this.getContentPane().add(workMemLabel);
    }

    public void openSavButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
                "Open 128kByte V3+ .sav",
                FileDialog.LOAD);
        fileDialog.setDirectory(latestSavPath);
        fileDialog.setFile("*.sav");
        fileDialog.setVisible(true);

        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return;
        }

        latestSavPath = fileDialog.getDirectory();
        if (latestSngPath.equals("\\")) {
            latestSngPath = latestSavPath;
        }

        if (this.file.loadFromSav(latestSavPath + fileName)) {
            file.populateSlotList(songList);
            workMemLabel.setText("Loaded work+file memory.");
            enableAllButtons();
            savePreferences();
        } else {
            workMemLabel.setText("File is not valid 128kB .SAV!");
        }
    }

    private void enableAllButtons() {
        exportV2SavButton.setEnabled(true);
        openSavButton.setEnabled(true);
        saveSavAsButton.setEnabled(true);
        addLsdSngButton.setEnabled(true);
        importV2SavButton.setEnabled(true);

        updateRamUsageIndicator();
    }

    public void clearSlotButton_actionPerformed() {

        if (songList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a song!",
                    "No song selected!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int[] slots = songList.getSelectedIndices();
        
        for (int slot : slots)
            file.clearSlot(slot);
        file.populateSlotList(songList);
        updateRamUsageIndicator();
    }

    private void updateRamUsageIndicator() {
        jRamUsageIndicator.setMaximum(file.totalBlockCount());
        jRamUsageIndicator.setValue(file.usedBlockCount());
        jRamUsageIndicator.setString("File mem. used: "
                + file.usedBlockCount() + "/" + file.totalBlockCount());
    }

    public void exportLsdSngButton_actionPerformed() {
        if (songList.isSelectionEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a song!",
                    "No song selected!", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int[] slots = songList.getSelectedIndices();

        JFileChooser fileChooser = new JFileChooser(latestSngPath);
        if (slots.length == 1) {
            fileChooser.setFileFilter(new LsdSngFilter());
            fileChooser.setDialogTitle(
                    "Export selected slot to compressed .lsdsng file");
            int ret = fileChooser.showSaveDialog(null);

            if (JFileChooser.APPROVE_OPTION == ret) {
                latestSngPath = fileChooser.getSelectedFile()
                        .getAbsoluteFile().getParent();
                String fileName = fileChooser.getSelectedFile()
                        .getAbsoluteFile().toString();
                if (!fileName.toUpperCase().endsWith(".LSDSNG")) {
                    fileName += ".lsdsng";
                }

                file.exportSongToFile(slots[0], fileName);
                savePreferences();
            }
        } else if (slots.length > 1) {
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle(
                    "Batch export selected slots to compressed .lsdsng files");
            int ret_val = fileChooser.showDialog(null, "Choose Directory");
            
            if (JFileChooser.APPROVE_OPTION == ret_val) {
                latestSngPath = fileChooser.getSelectedFile()
                        .getAbsolutePath();
                savePreferences();

                for (int slot : slots) {
                    String filename = file.getFileName(slot).toLowerCase()
                            + "-" + file.version(slot) + ".lsdsng";
                    String path = latestSngPath + File.separator + filename;
                    String[] options = { "Yes", "No", "Cancel" };
                    File f = new File(path);
                    if (f.exists()) {
                        int overWrite = JOptionPane.showOptionDialog(
                                this, "File \"" 
                                + filename
                                + "\" already exists.\n"
                                + "Overwrite existing file?", "Warning",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE, null, options,
                                options[1]);

                        if (overWrite == JOptionPane.YES_OPTION) {
                            boolean deleted;
                            try {
                                deleted = f.delete();
                            } catch (Exception fileInUse) {
                                deleted = false;
                            }
                            if (!deleted) {
                                JOptionPane.showMessageDialog(this,
                                        "Could not delete file.");
                                continue;
                            }
                        } else if (overWrite == JOptionPane.NO_OPTION) {
                            continue;
                        } else if (overWrite == JOptionPane.CANCEL_OPTION)
                            return;
                    }
                    if (file.getBlocksUsed(slot) > 0) {
                        file.exportSongToFile(slot, path);
                    }
                }
            }
        }
    }

    public void addLsdSngButton_actionPerformed() {
        JFileChooser fileChooser = new JFileChooser(latestSngPath);
        fileChooser.setFileFilter(new LsdSngFilter());
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setDialogTitle("Add compressed .lsdsng to file memory");

        if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(null)) {
            boolean success = true;
            for (File f : fileChooser.getSelectedFiles()) {
                success &= file.addSongFromFile(
                        f.getAbsoluteFile().toString());
                file.populateSlotList(songList);
            }
            latestSngPath = fileChooser.getSelectedFiles()[0]
                    .getAbsoluteFile().getParent();
            updateRamUsageIndicator();
            if (success) {
                savePreferences();
            }
        }
    }

    public void importV2SavButton_actionPerformed() {
        JFileChooser fileChooser = new JFileChooser(latestSavPath);
        fileChooser.setFileFilter(new SAVFilter());
        fileChooser.setDialogTitle(
                "Import 32kByte .sav file to work memory");
        int retVal = fileChooser.showOpenDialog(null);

        if (JFileChooser.APPROVE_OPTION == retVal) {
            file.import_32kb_sav_to_work_ram(
                    fileChooser.getSelectedFile().getAbsoluteFile().toString());
            workMemLabel.setText("Work memory updated.");
        }
    }

    public void saveSavAsButton_actionPerformed() {
        JFileChooser fileChooser = new JFileChooser(latestSavPath);
        fileChooser.setFileFilter(new SAVFilter());
        fileChooser.setDialogTitle("Save 128kByte V3+ .sav file");

        int retVal = fileChooser.showSaveDialog(null);

        if (JFileChooser.APPROVE_OPTION == retVal) {
            String fileName = fileChooser.getSelectedFile()
                    .getAbsoluteFile().toString();
            if (!fileName.toUpperCase().endsWith(".SAV")) {
                fileName += ".sav";
            }
            file.saveAs(fileName);
        }
    }

    public void exportV2SavButton_actionPerformed() {
        FileDialog fileDialog = new FileDialog(this,
            "Export work memory to 32kByte v2 .sav file",
            FileDialog.SAVE);
        fileDialog.setDirectory(latestSavPath);
        fileDialog.setFile("*.sav");
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName == null) {
            return;
        }
        String filePath = fileDialog.getDirectory() + fileName;
        if (!filePath.toUpperCase().endsWith(".SAV")) {
            filePath += ".sav";
        }
        file.saveWorkMemoryAs(filePath);
    }

    public void savePreferences() {
        preferences.put(LATEST_SAV_PATH, latestSavPath);
        preferences.put(LATEST_SNG_PATH, latestSngPath);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exportV2SavButton)
            exportV2SavButton_actionPerformed();
        else if (e.getSource() == saveSavAsButton)
            saveSavAsButton_actionPerformed();
        else if (e.getSource() == importV2SavButton)
            importV2SavButton_actionPerformed();
        else if (e.getSource() == addLsdSngButton)
            addLsdSngButton_actionPerformed();
        else if (e.getSource() == exportLsdSngButton)
            exportLsdSngButton_actionPerformed();
        else if (e.getSource() == clearSlotButton)
            clearSlotButton_actionPerformed();
        else if (e.getSource() == openSavButton)
            openSavButton_actionPerformed();
        else
            assert false : "unknown action event";
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        boolean enable = !songList.isSelectionEmpty();
        clearSlotButton.setEnabled(enable);
        exportLsdSngButton.setEnabled(enable);
    }
}