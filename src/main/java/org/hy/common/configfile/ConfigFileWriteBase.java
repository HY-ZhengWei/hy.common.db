package org.hy.common.configfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Vector;

import org.hy.common.Help;





/**
 * 对文件写操作的相关基础(公用)方法
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2011-06-05
 */
public class ConfigFileWriteBase
{
    
    // 读取的文件名称(含路径)
    private String       fileName;
    
    // 是否向文件中追加信息
    private boolean      isAppend;
    
    // 文件全部内容。文件中的一行数据，存放在集合的一元素中
    private List<String> contentList;
    
    
    
    /**
     * 构造器
     * 
     * @param i_FileName  文件路径+名称
     */
    public ConfigFileWriteBase(String i_FileName)
    {
        this(i_FileName ,false);
    }
    
    
    /**
     * 构造器
     * 
     * @param i_FileName  文件路径+名称
     * @param i_IsAppend  是否向文件中追加信息
     */
    public ConfigFileWriteBase(String i_FileName ,boolean i_IsAppend)
    {
        if ( Help.isNull(i_FileName) )
        {
            throw new java.lang.NullPointerException("File Name is null.");
        }
        
        if ( i_FileName.startsWith(".") )
        {
            this.fileName = ConfigFile_I18N.getSysCurrentPath() + i_FileName.substring(1);
        }
        else
        {
            this.fileName = i_FileName.trim();
        }
        
        this.isAppend    = i_IsAppend;
        this.contentList = new Vector<String>();
    }
    
    
    /**
     * 写缓存中的信息到文件中
     * 
     * 如果写入成功，则缓存清空
     * 
     * @return
     */
    public synchronized boolean write()
    {
        
        if ( this.contentList == null || this.contentList.isEmpty() )
        {
            return false;
        }
        
        
        File v_File = new File(this.fileName);
        
        if ( v_File.exists() )
        {
            if ( !v_File.isFile() )
            {
                return false;
            }
            
            if ( !v_File.canWrite() )
            {
                return false;
            }
        }

        
        BufferedWriter v_Writer = null;
        
        try
        {
            v_Writer = new BufferedWriter(new OutputStreamWriter((new FileOutputStream(v_File ,this.isAppend))));
            
            for (int v_RowIndex=0; v_RowIndex<this.contentList.size(); v_RowIndex++)
            {
                v_Writer.write(this.contentList.get(v_RowIndex));
                v_Writer.newLine();
            }
            
            v_Writer.flush();
            
            this.clearBuffer();
        }
        catch (Exception e)
        {
            return false;
        }
        finally
        {
            if ( v_Writer != null )
            {
                try
                {
                    v_Writer.close();
                }
                catch (Exception e)
                {
                    // Nothing.
                }
                
                v_Writer = null;
            }
        }
        
        return true;
    }
    
    
    /**
     * 写信息到文件的某一行上，并先写在缓存中
     * 
     * @param i_LineNo    行号，从 1 开始计数
     * @param i_LineData  一行的数据
     */
    public synchronized boolean writeToBuffer(int i_LineNo ,String i_LineData)
    {
        if ( this.contentList != null && this.contentList.size() >= i_LineNo && i_LineNo > 0 )
        {
            this.contentList.set(i_LineNo - 1, i_LineData);
            
            return true;
        }
        
        return false;
    }
    
    
    /**
     * 写信息到文件的最后一行，并先写在缓存中
     * 
     * @param i_LineData  一行的数据
     */
    public synchronized boolean appendToBuffer(String i_LineData)
    {
        if ( this.contentList != null )
        {
            this.contentList.add(i_LineData);
            
            return true;
        }
        
        return false;
    }

    
    /**
     * 清空缓存
     */
    public synchronized void clearBuffer()
    {
        if ( !Help.isNull(this.contentList) )
        {
            this.contentList.clear();
        }
        
        this.contentList = new Vector<String>();
    }
    
    
    /**
     * 返回缓存的大小
     * 
     * @return
     */
    public int getBufferRowSize()
    {
        if ( this.contentList == null )
        {
            return 0;
        }
        else
        {
            return this.contentList.size();
        }
    }
    

    public String getFileName()
    {
        return fileName;
    }


    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }


    public boolean isAppend()
    {
        return isAppend;
    }


    public void setAppend(boolean isAppend)
    {
        this.isAppend = isAppend;
    }
    
    
    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
    public void finalize()
    {
        this.clearBuffer();
        
        this.contentList = null;
    }
    */
    
}
