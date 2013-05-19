/** Copyright (c) 2005-2011, Johan Kotlinski

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE. */

package com.littlesounddj.lsdmanager;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import java.awt.*;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.*;

public class Frame extends JFrame {
    LSDSavFile m_file;
    //String m_latest_path = "c:\\dev\\lsdmanager\\";
    String m_latest_path = "\\";

    JButton addLsdSngButton = new JButton();
    JButton clearSlotButton = new JButton();
    JButton exportLsdSngButton = new JButton();
    JButton openSavButton = new JButton();
    JButton saveSavAsButton = new JButton();
    JProgressBar jRamUsageIndicator = new JProgressBar();
    JList jSongSlotList = new JList();
    JScrollPane jScrollPane1 = new JScrollPane(jSongSlotList);
    JButton importV2SavButton = new JButton();
    JButton exportV2SavButton = new JButton();
    JLabel workMemLabel = new JLabel();
    public Frame() {
        m_file = new LSDSavFile();

        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        addLsdSngButton.setBounds(new Rectangle(197, 195, 139, 31));
        addLsdSngButton.setEnabled(false);
        addLsdSngButton.setToolTipText(
            "Add compressed .lsdsng to file memory");
        addLsdSngButton.setText("Add .lsdsng...");
        addLsdSngButton.addActionListener(new
                                          Frame_addLsdSngButton_actionAdapter(this));
        clearSlotButton.setBounds(new Rectangle(197, 267, 139, 30));
        clearSlotButton.setEnabled(false);
        clearSlotButton.setToolTipText("Clear file memory slot");
        clearSlotButton.setText("Clear Slot");
        clearSlotButton.addActionListener(new
                                          Frame_clearSlotButton_actionAdapter(this));
        exportLsdSngButton.setBounds(new Rectangle(197, 231, 139, 31));
        exportLsdSngButton.setEnabled(false);
        exportLsdSngButton.setToolTipText(
            "Export compressed .lsdsng from file memory");
        exportLsdSngButton.setText("Export .lsdsng...");
        exportLsdSngButton.addActionListener(new
            Frame_exportLsdSngButton_actionAdapter(this));

        jSongSlotList.addListSelectionListener(new Frame_jSongSlotList_listSelectionListener(this));

        openSavButton.setBounds(new Rectangle(198, 11, 139, 36));
        openSavButton.setPreferredSize(new Dimension(99, 20));
        openSavButton.setText("Open V3+ .SAV...");
        openSavButton.addActionListener(new Frame_openSavButton_actionAdapter(this));
        saveSavAsButton.setBounds(new Rectangle(198, 52, 139, 36));
        saveSavAsButton.setEnabled(false);
        saveSavAsButton.setText("Save V3+ .SAV as...");
        saveSavAsButton.addActionListener(new
                                          Frame_saveSavAsButton_actionAdapter(this));
        jRamUsageIndicator.setString("");
        jRamUsageIndicator.setStringPainted(true);
        jRamUsageIndicator.setBounds(new Rectangle(11, 276, 178, 20));

        this.getContentPane().setLayout(null);
        this.setSize(new Dimension(352, 333));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle("LSDManager v0.9");

        jScrollPane1.setBounds(new Rectangle(11, 35, 178, 236));
        importV2SavButton.setBounds(new Rectangle(197, 108, 140, 31));
        importV2SavButton.setEnabled(false);
        importV2SavButton.setToolTipText(
            "Import 32 kByte V2 .SAV file to work memory (will overwrite what\'s " +
            "already there!!)");
        importV2SavButton.setActionCommand("Import V2 SAV...");
        importV2SavButton.setText("Import V2 .SAV...");
        importV2SavButton.addActionListener(new
            Frame_importV2SavButton_actionAdapter(this));
        exportV2SavButton.setBounds(new Rectangle(197, 144, 140, 31));
        exportV2SavButton.setEnabled(false);
        exportV2SavButton.setToolTipText(
            "Export work memory to 32 kByte V2 .SAV");
        exportV2SavButton.setActionCommand("Export to V2 SAV...");
        exportV2SavButton.setText("Export V2 .SAV...");
        exportV2SavButton.addActionListener(new
            Frame_exportV2SavButton_actionAdapter(this));
        workMemLabel.setText("Work memory empty.");
        workMemLabel.setBounds(new Rectangle(11, 11, 178, 21));
        this.getContentPane().add(jScrollPane1);
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

    public void openSavButton_actionPerformed(ActionEvent e)
    {
        JFileChooser l_file_chooser = new JFileChooser( m_latest_path );
        l_file_chooser.setFileFilter(new SAVFilter());
        l_file_chooser.setDialogTitle("Open 128kByte V3+ .sav");

        int l_ret_val = l_file_chooser.showOpenDialog(null);

        if ( JFileChooser.APPROVE_OPTION == l_ret_val )
        {
            m_latest_path = l_file_chooser.getSelectedFile().getAbsoluteFile().toString();

            boolean l_fileLoadedOk = this.m_file.loadFromSav(l_file_chooser.
                getSelectedFile().getAbsoluteFile().toString());

            if (true == l_fileLoadedOk)
            {
                m_file.populate_slot_list(jSongSlotList);
                workMemLabel.setText("Loaded work+file memory.");
                enable_all_buttons();
            }
            else
            {
                workMemLabel.setText("File is not valid 128kB .SAV!");
            }
        }
    }

    private void enable_all_buttons()
    {
        //clearSlotButton.setEnabled(true);
        exportV2SavButton.setEnabled(true);
        openSavButton.setEnabled(true);
        saveSavAsButton.setEnabled(true);
        //exportLsdSngButton.setEnabled(true);
        addLsdSngButton.setEnabled(true);
        importV2SavButton.setEnabled(true);

        update_ram_usage_indicator();
    }

    public void clearSlotButton_actionPerformed(ActionEvent e)
    {
        int l_slot = jSongSlotList.getSelectedIndex();
        if (l_slot < 0 || l_slot > 0x1f) {
            JOptionPane.showMessageDialog(null,
                    "Please select a song!",
                    "No song selected!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        m_file.clear_slot(l_slot);
        m_file.populate_slot_list(jSongSlotList);
        update_ram_usage_indicator();
    }

    private void update_ram_usage_indicator()
    {
        jRamUsageIndicator.setMaximum(m_file.getTotalBlockCount());
        jRamUsageIndicator.setValue(m_file.get_used_blocks());
        jRamUsageIndicator.setString("File mem. used: " +
                                     m_file.get_used_blocks() +
                                     "/" +
                                     m_file.getTotalBlockCount());
    }

    public void exportLsdSngButton_actionPerformed(ActionEvent e)
    {
        int l_slot = jSongSlotList.getSelectedIndex();
        if (l_slot < 0 || l_slot > 0x1f) {
            JOptionPane.showMessageDialog(null,
                    "Please select a song!",
                    "No song selected!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser l_file_chooser = new JFileChooser( m_latest_path );
        l_file_chooser.setFileFilter(new LsdSngFilter());
        l_file_chooser.setDialogTitle("Export selected slot to compressed .lsdsng file");
        int l_ret_val = l_file_chooser.showSaveDialog(null);

        if ( JFileChooser.APPROVE_OPTION == l_ret_val )
        {
            String l_file_name = l_file_chooser.getSelectedFile().getAbsoluteFile().toString();
            if(!l_file_name.toUpperCase().endsWith(".LSDSNG"))
            {
                l_file_name += ".lsdsng";
            }

            m_file.export_song_to_file(l_slot, l_file_name);
        }
    }

    public void addLsdSngButton_actionPerformed(ActionEvent e)
    {
        JFileChooser l_file_chooser = new JFileChooser( m_latest_path );
        l_file_chooser.setFileFilter(new LsdSngFilter());
        l_file_chooser.setDialogTitle("Add compressed .lsdsng to file memory");
        int l_ret_val = l_file_chooser.showOpenDialog(null);

        if ( JFileChooser.APPROVE_OPTION == l_ret_val )
        {
            m_file.add_song_from_file(
                l_file_chooser.getSelectedFile().getAbsoluteFile().toString()
                );
            m_file.populate_slot_list(jSongSlotList);
            update_ram_usage_indicator();
        }
    }

    public void importV2SavButton_actionPerformed(ActionEvent e)
    {
        JFileChooser l_file_chooser = new JFileChooser( m_latest_path );
        l_file_chooser.setFileFilter(new SAVFilter());
        l_file_chooser.setDialogTitle("Import 32kByte .sav file to work memory");
        int l_ret_val = l_file_chooser.showOpenDialog(null);

        if ( JFileChooser.APPROVE_OPTION == l_ret_val )
        {
            m_file.import_32kb_sav_to_work_ram(
                l_file_chooser.getSelectedFile().getAbsoluteFile().toString()
                        );
            workMemLabel.setText("Work memory updated.");
        }
    }

    public void jSongSlotList_valueChanged(ListSelectionEvent e)
    {
        if ( e.getFirstIndex() >= 0 && e.getFirstIndex() < 32 )
        {
            clearSlotButton.setEnabled(true);
            exportLsdSngButton.setEnabled(true);
        }
        else
        {
            clearSlotButton.setEnabled(false);
            exportLsdSngButton.setEnabled(false);
        }
    }

    public void saveSavAsButton_actionPerformed(ActionEvent e)
    {
        JFileChooser l_file_chooser = new JFileChooser( m_latest_path );
        l_file_chooser.setFileFilter(new SAVFilter());
        l_file_chooser.setDialogTitle("Save 128kByte V3+ .sav file");

        int l_ret_val = l_file_chooser.showSaveDialog(null);

        if ( JFileChooser.APPROVE_OPTION == l_ret_val )
        {
            String l_file_name = l_file_chooser.getSelectedFile().getAbsoluteFile().toString();
            if(!l_file_name.toUpperCase().endsWith(".SAV"))
            {
                l_file_name += ".sav";
            }
            m_file.save_as ( l_file_name );
        }
    }

    public void exportV2SavButton_actionPerformed(ActionEvent e)
    {
        JFileChooser l_file_chooser = new JFileChooser( m_latest_path );
        l_file_chooser.setFileFilter(new SAVFilter());
        l_file_chooser.setDialogTitle("Export work memory to 32kByte v2 .sav file");

        int l_ret_val = l_file_chooser.showSaveDialog(null);

        if ( JFileChooser.APPROVE_OPTION == l_ret_val )
        {
            String l_file_name = l_file_chooser.getSelectedFile().getAbsoluteFile().toString();
            if(!l_file_name.toUpperCase().endsWith(".SAV"))
            {
                l_file_name += ".sav";
            }
            m_file.save_work_memory_as ( l_file_name );
        }
    }
}


class Frame_exportV2SavButton_actionAdapter implements ActionListener
{
    private Frame adaptee;
    Frame_exportV2SavButton_actionAdapter(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.exportV2SavButton_actionPerformed(e);
    }
}


class Frame_saveSavAsButton_actionAdapter implements ActionListener
{
    private Frame adaptee;
    Frame_saveSavAsButton_actionAdapter(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.saveSavAsButton_actionPerformed(e);
    }
}


class Frame_importV2SavButton_actionAdapter implements ActionListener
{
    private Frame adaptee;
    Frame_importV2SavButton_actionAdapter(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.importV2SavButton_actionPerformed(e);
    }
}


class Frame_addLsdSngButton_actionAdapter implements ActionListener
{
    private Frame adaptee;
    Frame_addLsdSngButton_actionAdapter(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.addLsdSngButton_actionPerformed(e);
    }
}


class Frame_exportLsdSngButton_actionAdapter implements ActionListener
{
    private Frame adaptee;
    Frame_exportLsdSngButton_actionAdapter(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.exportLsdSngButton_actionPerformed(e);
    }
}


class Frame_clearSlotButton_actionAdapter implements ActionListener
{
    private Frame adaptee;
    Frame_clearSlotButton_actionAdapter(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.clearSlotButton_actionPerformed(e);
    }
}


class Frame_openSavButton_actionAdapter implements ActionListener
{
    private Frame adaptee;
    Frame_openSavButton_actionAdapter(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.openSavButton_actionPerformed(e);
    }
}

class Frame_jSongSlotList_listSelectionListener implements ListSelectionListener
{
    private Frame adaptee;
    Frame_jSongSlotList_listSelectionListener(Frame adaptee)
    {
        this.adaptee = adaptee;
    }

    public void valueChanged(ListSelectionEvent e)
    {
        adaptee.jSongSlotList_valueChanged(e);
    }
}
