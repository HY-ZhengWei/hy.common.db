package org.hy.common.configfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.hy.common.Help;





/**
 * 对配置文件读操作的相关基础(公用)方法
 * 
 * 1. 有回写功能
 * 2. #号注释行
 * 3. 使用=号分隔key与value
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2011-04-17
 */
public abstract class ConfigFileReadBase
{
    // 读取的文件名称(含路径)
    private String              fileName;
    
    // 控制方法--停止读取
    private boolean             stopRead;
    
    // 文件全部内容。文件中的一行数据，存放在集合的一元素中
    private ConfigFileWriteBase configWriteBase;
    
    // 是否回写。即回写信息在文件中
    private boolean             isToWrite;
    
    
    
    /**
     * 构造器
     * 
     * @param i_FileName  读取的文件名称(含路径)
     */
    public ConfigFileReadBase(String i_FileName)
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
        
        this.configWriteBase = new ConfigFileWriteBase(this.fileName);
        this.isToWrite       = false;
    }
    
    
    /**
     * 读取文件
     * 
     * 对于注释行不向 readLineString() 抽象方法发起消息
     * 
     * @throws Exception
     */
    public void read() throws Exception
    {
        File           v_File   = new File(this.fileName);
        BufferedReader v_Reader = null;
        
        if ( !v_File.exists() )
        {
            throw new IOException(ConfigFile_I18N.getHintFileNotExist() + this.fileName);
        }
        
        if ( !v_File.isFile() )
        {
            throw new IOException(ConfigFile_I18N.getHintIsNotFile() + this.fileName);
        }
        
        if ( !v_File.canRead() )
        {
            throw new IOException(ConfigFile_I18N.getHintFileNotRead() + this.fileName);
        }
        
        
        v_Reader = new BufferedReader(new InputStreamReader(new FileInputStream(v_File)));
        
        try
        {
            int    v_LineNo   = 1;
            String v_LineData = v_Reader.readLine();
            this.stopRead     = false;
            this.configWriteBase.clearBuffer();
            
            while ( v_LineData != null && !this.isStopRead() )
            {
                try
                {
                    this.configWriteBase.appendToBuffer(v_LineData);
                    
                    // #号表示注释行
                    if ( !v_LineData.startsWith("#") )
                    {
                        readLineString(v_LineNo ,v_LineData);
                    }
                }
                catch (Exception exce)
                {
                    // Nothing.
                }
                
                v_LineData = v_Reader.readLine();
                v_LineNo++;
            }
        }
        catch (Exception exce)
        {
            throw exce;
        }
        finally
        {
            try
            {
                if ( v_Reader != null )
                {
                    v_Reader.close();
                    v_Reader = null;
                }
            }
            catch (Exception exce2)
            {
                v_Reader = null;
            }
            
            if ( v_File != null )
            {
                v_File = null;
            }
            
        }
        
    }
    
    
    /**
     * 重写文件中的所有内容
     * 
     * @return
     */
    public boolean write()
    {
        if ( !this.isToWrite )
        {
            return false;
        }
        
        if ( this.configWriteBase == null || this.configWriteBase.getBufferRowSize()<= 0 )
        {
            return false;
        }
        
        
        return this.configWriteBase.write();
    }
    
    
    /**
     * 重写某一行上的数据到缓存中
     * 
     * @param i_LineNo    行号，从 1 开始计数
     * @param i_LineData  一行的数据
     */
    public boolean writeByLineData(int i_LineNo ,String i_LineData)
    {
        if ( this.configWriteBase != null )
        {
            this.configWriteBase.writeToBuffer(i_LineNo, i_LineData);
            
            this.isToWrite = true;
            
            return true;
        }
        
        return false;
    }
    
    
    /**
     * 读取一行数据
     * 
     * @param i_LineNo      行号
     * @param i_LineString  一行数据
     */
    public abstract void readLineString(int i_LineNo ,String i_LineString);


    public String getFileName()
    {
        return fileName;
    }


    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }


    public boolean isStopRead()
    {
        return stopRead;
    }


    public void setStopRead(boolean stopRead)
    {
        this.stopRead = stopRead;
    }


    public boolean isToWrite()
    {
        return isToWrite;
    }


    public void setToWrite(boolean isToWrite)
    {
        this.isToWrite = isToWrite;
    }
    
    
    /*
    ZhengWei(HY) Del 2016-07-30
    不能实现这个方法。首先JDK中的Hashtable、ArrayList中也没有实现此方法。
    它会在元素还有用，但集合对象本身没有用时，释放元素对象
    
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了
    public void finalize()
    {
        if ( this.configWriteBase != null )
        {
            this.configWriteBase = null;
        }
    }
    */
    
}
