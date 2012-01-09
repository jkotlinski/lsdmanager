/** Copyright (c) 2010, Johan Kotlinski

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

import java.io.RandomAccessFile;
import javax.swing.JList;
import javax.swing.JTable;

public class LSDSavFile
{
    final int g_block_size = 0x200;
    final int g_bank_size = 0x8000;
    final int g_bank_count = 4;
    final int g_sav_file_size = g_bank_size * g_bank_count;
    final int g_slot_count = 0x20;
    final int g_file_name_length = 8;

    final int g_file_name_start_ptr = 0x8000;
    final int g_file_version_start_ptr = 0x8100;
    final int g_block_alloc_table_start_ptr = 0x8141;
    final int g_block_start_ptr = 0x8200;
    final int g_active_file_slot = 0x8140;
    final char g_empty_slot_value = (char) 0xff;

    boolean g_is_64_kb = false;
    boolean g_is_64_kb_has_been_set = false;

    byte m_work_ram[];
    boolean m_file_is_loaded = false;

    public LSDSavFile()
    {
        m_work_ram = new byte[g_sav_file_size];
    }

    private boolean isSixtyfourKbRam()
    {
        if ( !m_file_is_loaded ) return false;
        if ( g_is_64_kb_has_been_set ) return g_is_64_kb;

        for ( int i = 0; i < 0x10000; ++i )
        {
            if ( m_work_ram[i] != m_work_ram[0x10000+i] )
            {
                g_is_64_kb = false;
                g_is_64_kb_has_been_set = true;
                return false;
            }
        }
        g_is_64_kb = true;
        g_is_64_kb_has_been_set = true;
        return true;
    }

    public int getTotalBlockCount()
    {
        if ( isSixtyfourKbRam() )
        {
            return 0xbf - 0x80;
        }
        else
        {
            return 0xbf;  // Almost 0xc0, except first block for FAT.
        }
    }

    public boolean save_as ( String a_file_path )
    {
        try
        {
            RandomAccessFile l_file = new RandomAccessFile ( a_file_path, "rw" );
            if ( isSixtyfourKbRam() )
            {
                for ( int i = 0; i < 0x10000; ++i )
                {
                    m_work_ram[i+0x10000] = m_work_ram[i];
                }
            }
            l_file.write(m_work_ram);
            l_file.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean save_work_memory_as ( String a_file_path )
    {
        try
        {
            RandomAccessFile l_file = new RandomAccessFile ( a_file_path, "rw" );
            l_file.write(m_work_ram, 0, g_bank_size );
            l_file.close();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void clear_slot(int a_index)
    {
        int l_ram_ptr = g_block_alloc_table_start_ptr;
        int l_block = 0;

        while (l_block < getTotalBlockCount() )
        {
            int l_table_value = m_work_ram[l_ram_ptr];
            if (a_index == l_table_value)
            {
                m_work_ram[l_ram_ptr] = (byte) g_empty_slot_value;
            }
            l_ram_ptr++;
            l_block++;
        }

        clear_file_name(a_index);
        clear_file_version(a_index);

        if ( a_index == get_active_file_slot() )
        {
            clear_active_file_slot();
        }
    }

    private int get_blocks_used ( int a_slot )
    {
        int l_ram_ptr = g_block_alloc_table_start_ptr;
        int l_block = 0;
        int l_block_count = 0;

        while (l_block < getTotalBlockCount())
        {
            int l_table_value = m_work_ram[l_ram_ptr];
            if ( a_slot == l_table_value)
            {
                l_block_count++;
            }
            l_ram_ptr++;
            l_block++;
        }
        return l_block_count;
    }

    private void clear_file_name(int a_index)
    {
        m_work_ram[g_file_name_start_ptr +
            g_file_name_length * a_index] = (byte) 0;
    }

    private void clear_file_version(int a_index)
    {
        m_work_ram[g_file_version_start_ptr + a_index] = (byte) 0;
    }

    public int get_used_blocks()
    {
        return getTotalBlockCount() - get_free_blocks();
    }

    public boolean has_free_slot()
    {
        int l_file_name_ptr = g_file_name_start_ptr;
        for ( int l_slot = 0; l_slot < g_slot_count; l_slot++ )
        {
            if ( 0 == m_work_ram[l_file_name_ptr] )
            {
                return true;
            }
            l_file_name_ptr += g_file_name_length;
        }
        System.out.println("no free slot:(");
        return false;
    }

    public byte get_free_slot() throws Exception
    {
        int l_file_name_ptr = g_file_name_start_ptr;
        for ( byte l_slot = 0; l_slot < g_slot_count; l_slot++ )
        {
            if ( 0 == m_work_ram[l_file_name_ptr] )
            {
                return l_slot;
            }
            l_file_name_ptr += g_file_name_length;
        }
        throw new Exception("no free slot found");
    }

    public int get_block_id_of_first_free_block() throws Exception
    {
        int l_block_alloc_table_ptr = g_block_alloc_table_start_ptr;
        int l_block = 0;

        while (l_block < getTotalBlockCount())
        {
            int l_table_value = m_work_ram[l_block_alloc_table_ptr++];
            if (l_table_value < 0 || l_table_value > 0x1f)
            {
                return l_block;
            }
            l_block++;
        }
        throw new Exception ("no free block found");
    }

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

    public int get_free_blocks()
    {
        int l_ram_ptr = g_block_alloc_table_start_ptr;
        int l_block = 0;
        int l_free_block_count = 0;

        while (l_block < getTotalBlockCount())
        {
            int l_table_value = m_work_ram[l_ram_ptr++];
            if (l_table_value < 0 || l_table_value > 0x1f)
            {
                l_free_block_count++;
            }
            l_block++;
        }
        return l_free_block_count;
    }

    public boolean loadFromSav(String a_file_path)
    {
        RandomAccessFile l_sav_file = null;
        int l_read_bytes = 0;

        try
        {
            l_sav_file = new RandomAccessFile(a_file_path, "r");
            l_read_bytes = l_sav_file.read(m_work_ram);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        if (l_read_bytes > g_sav_file_size)
        {
            return false;
        }

        g_is_64_kb_has_been_set = false;
        m_file_is_loaded = true;
        return true;
    }

    public void populate_slot_list(JList a_slot_list)
    {
        String l_slot_string_list[] = new String[g_slot_count];
        a_slot_list.removeAll();

        for (int l_slot = 0; l_slot < g_slot_count; l_slot++)
        {
            String l_slot_string = l_slot + ". ";

            l_slot_string += get_file_name(l_slot);
            if (0 != get_version(l_slot))
            {
                l_slot_string += "\t." + get_version(l_slot);
                l_slot_string += " " + get_blocks_used(l_slot);
            }

            l_slot_string_list[l_slot] = l_slot_string;
        }

        a_slot_list.setListData(l_slot_string_list);
    }

    private String get_file_name(int l_slot)
    {
        String l_string = "";
        int l_ram_ptr = g_file_name_start_ptr + g_file_name_length * l_slot;
        boolean l_end_of_file_name = false;
        for (int l_file_name_pos = 0;
                l_file_name_pos < 8;
                l_file_name_pos++)
        {
            if (!l_end_of_file_name)
            {
                char l_char = (char) CharConv.convertLsdCharToAscii((char)
                        m_work_ram[l_ram_ptr]);
                if (0 == l_char)
                {
                    l_end_of_file_name = true;
                }
                else
                {
                    l_string += l_char;
                }
            }
            l_ram_ptr++;
        }
        return l_string;
    }

    private int get_version(int l_slot)
    {
        int l_ram_ptr = g_file_version_start_ptr + l_slot;
        int l_version = m_work_ram[l_ram_ptr];
        return l_version;
    }

    public boolean export_song_to_file(int a_slot, String a_file_path)
    {
        if (a_slot < 0 || a_slot > 0x1f) {
            return;
        }
        RandomAccessFile m_file = null;
        try
        {
            m_file = new RandomAccessFile(a_file_path, "rw");

            int l_file_name_ptr = g_file_name_start_ptr + a_slot * g_file_name_length;
            m_file.writeByte(m_work_ram[l_file_name_ptr++]);
            m_file.writeByte(m_work_ram[l_file_name_ptr++]);
            m_file.writeByte(m_work_ram[l_file_name_ptr++]);
            m_file.writeByte(m_work_ram[l_file_name_ptr++]);
            m_file.writeByte(m_work_ram[l_file_name_ptr++]);
            m_file.writeByte(m_work_ram[l_file_name_ptr++]);
            m_file.writeByte(m_work_ram[l_file_name_ptr++]);
            m_file.writeByte(m_work_ram[l_file_name_ptr]);

            int l_file_version_ptr = g_file_version_start_ptr + a_slot;
            m_file.writeByte(m_work_ram[l_file_version_ptr]);

            int l_block_id = 0;
            int l_block_alloc_table_ptr = g_block_alloc_table_start_ptr;

            while ( l_block_id < getTotalBlockCount() )
            {
                if ( a_slot == m_work_ram[l_block_alloc_table_ptr++] )
                {
                    int l_block_ptr = g_block_start_ptr + l_block_id * g_block_size;
                    for ( int l_byte_index = 0; l_byte_index < g_block_size; l_byte_index++ )
                    {
                        m_file.writeByte(m_work_ram[l_block_ptr++]);
                    }
                }
                l_block_id++;
            }
            m_file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean add_song_from_file(String a_file_path)
    {
        RandomAccessFile m_file = null;
        try
        {
            m_file = new RandomAccessFile(a_file_path, "r");

            byte l_file_name[] = new byte[8];
            m_file.read(l_file_name);
            byte l_file_version = m_file.readByte();

            byte l_buffer[] = new byte[0x8000*4];
            int l_bytes_read = m_file.read(l_buffer);
            int l_blocks_read = l_bytes_read / g_block_size;

            if ( l_blocks_read > get_free_blocks() || !has_free_slot() )
            {
                return false;
            }

            byte l_free_slot = get_free_slot();
            int l_file_name_ptr = g_file_name_start_ptr + l_free_slot * g_file_name_length;
            m_work_ram[l_file_name_ptr++] = l_file_name[0];
            m_work_ram[l_file_name_ptr++] = l_file_name[1];
            m_work_ram[l_file_name_ptr++] = l_file_name[2];
            m_work_ram[l_file_name_ptr++] = l_file_name[3];
            m_work_ram[l_file_name_ptr++] = l_file_name[4];
            m_work_ram[l_file_name_ptr++] = l_file_name[5];
            m_work_ram[l_file_name_ptr++] = l_file_name[6];
            m_work_ram[l_file_name_ptr++] = l_file_name[7];

            int l_file_version_ptr = g_file_version_start_ptr + l_free_slot;
            m_work_ram[l_file_version_ptr] = l_file_version;

            int l_blocks_to_write = l_blocks_read;
            int l_buffer_index = 0;

            int l_next_block_id_ptr = 0;
            while ( l_blocks_to_write-- > 0 )
            {
                int l_block_id = get_block_id_of_first_free_block();

                if ( 0 != l_next_block_id_ptr )
                {
                    //add one to compensate for unused FAT block
                    m_work_ram[l_next_block_id_ptr] = (byte)(l_block_id+1);
                }
                m_work_ram[g_block_alloc_table_start_ptr + l_block_id] = l_free_slot;
                int l_block_ptr = g_block_start_ptr + l_block_id * g_block_size;
                for ( int l_byte = 0; l_byte < g_block_size; l_byte++ )
                {
                    m_work_ram[l_block_ptr++] = l_buffer[l_buffer_index++];
                }
                l_next_block_id_ptr = get_next_block_id_ptr ( l_block_id );
            }
            m_file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void clear_active_file_slot()
    {
        m_work_ram[g_active_file_slot] = (byte)0xff;
    }

    private byte get_active_file_slot()
    {
        return m_work_ram[g_active_file_slot];
    }

    private int get_next_block_id_ptr ( int a_block )
    {
        int l_ram_ptr = g_block_start_ptr + g_block_size * a_block;
        int l_byte_counter = 0;

        while ( l_byte_counter < g_block_size )
        {
            if ( m_work_ram[l_ram_ptr] == (byte)0xc0 )
            {
                byte arg = m_work_ram[l_ram_ptr+1];
                if ( arg == (byte) 0xc0 )
                {
                    l_ram_ptr++;
                    l_byte_counter++;
                }
                else
                {
                    //rle
                    l_ram_ptr++;
                    l_ram_ptr++;
                    l_byte_counter++;
                    l_byte_counter++;
                }
            }
            else if ( m_work_ram[l_ram_ptr] == (byte)0xe0 )
            {
                switch ( m_work_ram[l_ram_ptr + 1] )
                {
                    case (byte)0xe0:
                    case (byte)0xff:
                        l_ram_ptr++;
                        l_byte_counter++;
                        break;
                    case (byte)0xf0: //wave
                    case (byte)0xf1: //instr
                        l_ram_ptr += 2;
                        l_byte_counter += 2;
                        break;
                    default:
                        return l_ram_ptr + 1;
                }
            }
            l_ram_ptr++;
            l_byte_counter++;
        }
        System.out.println("get_next_block_id_ptr returns 0");
        return 0;
    }

    public boolean import_32kb_sav_to_work_ram(String a_file_path)
    {
        RandomAccessFile m_file = null;
        try
        {
            m_file = new RandomAccessFile(a_file_path, "r");

            int l_bytes_read = m_file.read(m_work_ram, 0, g_bank_size);

            if ( l_bytes_read < g_bank_size )
            {
                return false;
            }
            m_file.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        clear_active_file_slot();
        return true;
    }
}
