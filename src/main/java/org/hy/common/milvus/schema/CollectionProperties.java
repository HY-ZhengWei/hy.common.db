package org.hy.common.milvus.schema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;





/**
 * 表属性的Json结构
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-08-07
 * @version     v1.0
 */
public class CollectionProperties
{
    
    /** 时区 */
    private ZoneId timezone;
    
    
    
    public CollectionProperties()
    {
        
    }
    
    
    
    public CollectionProperties(String i_Timezone)
    {
        this.setTimezone(i_Timezone);
    }

    
    
    /**
     * 获取：时区
     */
    public String getTimezone()
    {
        if ( this.timezone != null )
        {
            return this.timezone.getId();
        }
        else
        {
            return null;
        }
    }

    
    /**
     * 设置：时区
     * 
     * @param i_Timezone 时区
     */
    public void setTimezone(String i_Timezone)
    {
        this.timezone = ZoneId.of(i_Timezone);
    }
    
    
    /**
     * 获取：时区
     */
    public String getTimezoneOffset()
    {
        if ( this.timezone != null )
        {
            ZoneOffset v_Offset = this.timezone.getRules().getOffset(Instant.now());
            return "UTC" + v_Offset.getId();
        }
        else
        {
            return null;
        }
    }
    
}
