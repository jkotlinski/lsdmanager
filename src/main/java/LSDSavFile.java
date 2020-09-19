import java.io.RandomAccessFile;
import javax.swing.*;

public class LSDSavFile
{
    final int blockSize = 0x200;
    final int bankSize = 0x8000;
    final int bankCount = 4;
    final int savFileSize = bankSize * bankCount;
    final int slotCount = 0x20;
    final int fileNameLength = 8;

    final int fileNameStartPtr = 0x8000;
    final int fileVersionStartPtr = 0x8100;
    final int blockAllocTableStartPtr = 0x8141;
    final int blockStartPtr = 0x8200;
    final int activeFileSlot = 0x8140;
    final char emptySlotValue = (char) 0xff;

    boolean is64kb = false;
    boolean is64kbHasBeenSet = false;

    byte[] workRam;
    boolean fileIsLoaded = false;

    public LSDSavFile()
    {
        workRam = new byte[savFileSize];
    }

    private boolean isSixtyFourKbRam()
    {
        if (!fileIsLoaded) return false;
        if (is64kbHasBeenSet) return is64kb;

        for (int i = 0; i < 0x10000; ++i)
        {
            if (workRam[i] != workRam[0x10000 + i])
            {
                is64kb = false;
                is64kbHasBeenSet = true;
                return false;
            }
        }
        is64kb = true;
        is64kbHasBeenSet = true;
        return true;
    }

    public int totalBlockCount()
    {
        // FAT takes one block.
        return isSixtyFourKbRam() ? 0xbf - 0x80 : 0xbf;
    }

    public void saveAs(String filePath )
    {
        try
        {
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            if (isSixtyFourKbRam())
            {
                System.arraycopy(workRam, 0, workRam, 65536, 0x10000);
            }
            file.write(workRam);
            file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void saveWorkMemoryAs(String filePath)
    {
        try
        {
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            file.write(workRam, 0, bankSize);
            file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void clearSlot(int index)
    {
        int ramPtr = blockAllocTableStartPtr;
        int block = 0;

        while (block < totalBlockCount() )
        {
            int tableValue = workRam[ramPtr];
            if (index == tableValue)
            {
                workRam[ramPtr] = (byte) emptySlotValue;
            }
            ramPtr++;
            block++;
        }

        clearFileName(index);
        clearFileVersion(index);

        if (index == getActiveFileSlot())
        {
            clearActiveFileSlot();
        }
    }

    public int getBlocksUsed(int slot)
    {
        int ramPtr = blockAllocTableStartPtr;
        int block = 0;
        int blockCount = 0;

        while (block++ < totalBlockCount())
        {
            if (slot == workRam[ramPtr++])
            {
                blockCount++;
            }
        }
        return blockCount;
    }

    private void clearFileName(int index)
    {
        workRam[fileNameStartPtr + fileNameLength * index] = (byte) 0;
    }

    private void clearFileVersion(int index)
    {
        workRam[fileVersionStartPtr + index] = (byte) 0;
    }

    public int usedBlockCount()
    {
        return totalBlockCount() - freeBlockCount();
    }

    public boolean has_free_slot()
    {
        int fileNamePtr = fileNameStartPtr;
        for (int slot = 0; slot < slotCount; slot++ )
        {
            if (0 == workRam[fileNamePtr])
            {
                return true;
            }
            fileNamePtr += fileNameLength;
        }
        System.out.println("no free slot:(");
        return false;
    }

    public byte get_free_slot() throws Exception {
        for (byte slot = 0; slot < slotCount; slot++) {
            if (0 == getBlocksUsed(slot)) {
                return slot;
            }
        }
        throw new Exception("No free slot found");
    }

    public int getBlockIdOfFirstFreeBlock() throws Exception
    {
        int blockAllocTableStartPtr = this.blockAllocTableStartPtr;
        int block = 0;

        while (block < totalBlockCount())
        {
            int tableValue = workRam[blockAllocTableStartPtr++];
            if (tableValue < 0 || tableValue > 0x1f)
            {
                return block;
            }
            block++;
        }
        throw new Exception("No free block found");
    }

    /*
    public void debug_dump_fat()
    {
        int l_ram_ptr = g_block_alloc_table_start_ptr;
        int l_block = 0;

        while (l_block < getTotalBlockCount())
        {
            int l_table_value = m_work_ram[l_ram_ptr++];
            System.out.print(l_table_value + " " );
            l_block++;
        }
        System.out.println();
    }
    */

    public int freeBlockCount()
    {
        int ramPtr = blockAllocTableStartPtr;
        int block = 0;
        int freeBlockCount = 0;

        while (block < totalBlockCount())
        {
            int tableValue = workRam[ramPtr++];
            if (tableValue < 0 || tableValue > 0x1f)
            {
                freeBlockCount++;
            }
            block++;
        }
        return freeBlockCount;
    }

    public boolean loadFromSav(String filePath)
    {
        RandomAccessFile savFile;
        int readBytes;

        try
        {
            savFile = new RandomAccessFile(filePath, "r");
            readBytes = savFile.read(workRam);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        if (readBytes > savFileSize)
        {
            return false;
        }

        is64kbHasBeenSet = false;
        fileIsLoaded = true;
        return true;
    }

    public void populateSlotList(JList<String> slotList) {
        String[] slotStringList = new String[slotCount];
        slotList.removeAll();

        for (int slot = 0; slot < slotCount; slot++) {
            int blocksUsed = getBlocksUsed(slot);
            String slotString = slot + 1 + ". ";

            if (blocksUsed > 0) {
                slotString += getFileName(slot);
                slotString += "." + version(slot);
                slotString += " " + blocksUsed;
            }

            slotStringList[slot] = slotString;
        }

        slotList.setListData(slotStringList);
    }

    private static int convertLsdCharToAscii(int ch)
    {
        if (ch >= 65 && ch <= (65+25))
        {
            //char
            return 'A' + ch - 65;
        }
        if (ch >= 48 && ch < 58)
        {
            //decimal number
            return '0' + ch - 48;
        }
        return 0 == ch ? 0 : ' ';
    }

    public String getFileName(int slot)
    {
        StringBuilder sb = new StringBuilder();
        int ramPtr = fileNameStartPtr + fileNameLength * slot;
        boolean endOfFileName = false;
        for (int fileNamePos = 0;
                fileNamePos < 8;
                fileNamePos++)
        {
            if (!endOfFileName)
            {
                char ch = (char) convertLsdCharToAscii((char)
                        workRam[ramPtr]);
                if (0 == ch)
                {
                    endOfFileName = true;
                }
                else
                {
                    sb.append(ch);
                }
            }
            ramPtr++;
        }
        return sb.toString();
    }

    public String version(int slot)
    {
        int ramPtr = fileVersionStartPtr + slot;
        String version = Integer.toHexString(workRam[ramPtr]);
        return version.substring(Math.max(version.length() - 2, 0)).toUpperCase();
    }

    public void exportSongToFile(int slot, String filePath)
    {
        if (slot < 0 || slot > 0x1f) {
            return;
        }
        RandomAccessFile file;
        try
        {
            file = new RandomAccessFile(filePath, "rw");

            int fileNamePtr = fileNameStartPtr + slot * fileNameLength;
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr++]);
            file.writeByte(workRam[fileNamePtr]);

            int fileVersionPtr = fileVersionStartPtr + slot;
            file.writeByte(workRam[fileVersionPtr]);

            int blockId = 0;
            int blockAllocTablePtr = blockAllocTableStartPtr;

            while (blockId < totalBlockCount())
            {
                if (slot == workRam[blockAllocTablePtr++])
                {
                    int blockPtr = blockStartPtr + blockId * blockSize;
                    for (int byteIndex = 0; byteIndex < blockSize; byteIndex++)
                    {
                        file.writeByte(workRam[blockPtr++]);
                    }
                }
                blockId++;
            }
            file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean addSongFromFile(String filePath)
    {
        RandomAccessFile file;
        try
        {
            file = new RandomAccessFile(filePath, "r");

            byte[] fileName = new byte[8];
            file.read(fileName);
            byte fileVersion = file.readByte();

            byte[] buffer = new byte[0x8000*4];
            int bytesRead = file.read(buffer);
            int blocksRead = bytesRead / blockSize;

            if (blocksRead > freeBlockCount() || !has_free_slot())
            {
                JOptionPane.showMessageDialog(null,
                        "Not enough free blocks or song slots!",
                        "Error adding song(s)!",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            byte freeSlot = get_free_slot();
            int fileNamePtr = fileNameStartPtr + freeSlot * fileNameLength;
            workRam[fileNamePtr++] = fileName[0];
            workRam[fileNamePtr++] = fileName[1];
            workRam[fileNamePtr++] = fileName[2];
            workRam[fileNamePtr++] = fileName[3];
            workRam[fileNamePtr++] = fileName[4];
            workRam[fileNamePtr++] = fileName[5];
            workRam[fileNamePtr++] = fileName[6];
            workRam[fileNamePtr] = fileName[7];

            int fileVersionPtr = fileVersionStartPtr + freeSlot;
            workRam[fileVersionPtr] = fileVersion;

            int blocksToWrite = blocksRead;
            int bufferIndex = 0;

            int nextBlockIdPtr = 0;
            while ( blocksToWrite-- > 0 )
            {
                int blockId = getBlockIdOfFirstFreeBlock();

                if (0 != nextBlockIdPtr)
                {
                    //add one to compensate for unused FAT block
                    workRam[nextBlockIdPtr] = (byte)(blockId+1);
                }
                workRam[blockAllocTableStartPtr + blockId] = freeSlot;
                int blockPtr = blockStartPtr + blockId * blockSize;
                for (int block = 0; block < blockSize; block++)
                {
                    workRam[blockPtr++] = buffer[bufferIndex++];
                }
                nextBlockIdPtr = getNextBlockIdPtr(blockId);
            }
            file.close();
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null,
                    e.getLocalizedMessage(),
                    "File open failed!",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void clearActiveFileSlot()
    {
        workRam[activeFileSlot] = (byte)0xff;
    }

    private byte getActiveFileSlot()
    {
        return workRam[activeFileSlot];
    }

    private int getNextBlockIdPtr(int a_block )
    {
        int ramPtr = blockStartPtr + blockSize * a_block;
        int byteCounter = 0;

        while (byteCounter < blockSize)
        {
            if (workRam[ramPtr] == (byte)0xc0)
            {
                ramPtr++;
                byteCounter++;
                if (workRam[ramPtr] != (byte) 0xc0) {
                    //rle
                    ramPtr++;
                    byteCounter++;
                }
            }
            else if (workRam[ramPtr] == (byte)0xe0)
            {
                switch (workRam[ramPtr + 1])
                {
                    case (byte)0xe0:
                    case (byte)0xff:
                        ramPtr++;
                        byteCounter++;
                        break;
                    case (byte)0xf0: //wave
                    case (byte)0xf1: //instr
                        ramPtr += 2;
                        byteCounter += 2;
                        break;
                    default:
                        return ramPtr + 1;
                }
            }
            ramPtr++;
            byteCounter++;
        }
        System.out.println("get_next_block_id_ptr returns 0");
        return 0;
    }

    public void import32KbSavToWorkRam(String a_file_path)
    {
        RandomAccessFile file;
        try
        {
            file = new RandomAccessFile(a_file_path, "r");

            int bytesRead = file.read(workRam, 0, bankSize);

            if (bytesRead < bankSize)
            {
                return;
            }
            file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        clearActiveFileSlot();
    }
    
}