package org.hy.common.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;





/**
 * JDBC原生态的"预解释的SQL"
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-08-03
 * @version     v1.0
 */
public class DBPreparedSQL implements Serializable 
{
    private static final long serialVersionUID = 1823923050291047420L;
    
    

    /** 预解释SQL。如 Insert Into ... Values(? ,? ,?) */
    private String         sql;
    
    /** 预解释SQL中?号对应的占位符字符 */
    private List<String>   placeholders;
    
    
    
    public DBPreparedSQL()
    {
        this(null ,new ArrayList<String>());
    }
    
    
    
    public DBPreparedSQL(String i_SQL)
    {
        this(i_SQL ,new ArrayList<String>());
    }
    
    
    
    public DBPreparedSQL(String i_SQL ,List<String> i_PlaceHolders)
    {
        this.sql          = i_SQL;
        this.placeholders = i_PlaceHolders;
    }

    
    
    /**
     * 获取：预解释SQL。如 Insert Into ... Values(? ,? ,?)
     */
    public String getSQL()
    {
        return sql;
    }

    
    
    /**
     * 设置：预解释SQL。如 Insert Into ... Values(? ,? ,?)
     * 
     * @param sql 
     */
    public void setSQL(String sql)
    {
        this.sql = sql;
    }


    
    /**
     * 获取：预解释SQL中?号对应的占位符字符
     */
    public List<String> getPlaceholders()
    {
        return placeholders;
    }

    
    
    /**
     * 设置：预解释SQL中?号对应的占位符字符
     * 
     * @param placeholders 
     */
    public void setPlaceholders(List<String> placeholders)
    {
        this.placeholders = placeholders;
    }

}
