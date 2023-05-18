package org.hy.common.db;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.MethodReflect;
import org.hy.common.StringHelp;
import org.hy.common.xml.log.Logger;





/**
 * JDBC原生态的"预解释的SQL"
 *
 * @author      ZhengWei(HY)
 * @createDate  2016-08-03
 * @version     v1.0
 *              v2.0  2023-05-17  添加：有能力获取，输出具体数值的SQL伪代码，主要用于日志的显示，而不是显示一大堆?问号
 */
public class DBPreparedSQL implements Serializable
{
    private static final long   serialVersionUID = 1823923050291047420L;
    
    private static final Logger $Logger          = new Logger(DBPreparedSQL.class);
    
    

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
     * 对象转字符串，并按类型添加单引号。空指针返回 NULL 字符串
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-05-18
     * @version     v1.0
     *
     * @param i_Value
     * @return
     */
    private String objectToString(Object i_Value)
    {
        String v_ValueString = "NULL";
        
        if ( i_Value != null )
        {
            if ( i_Value.getClass().equals(String.class) )
            {
                v_ValueString = "'" + i_Value.toString() + "'";
            }
            else if ( i_Value.getClass().equals(Date.class) )
            {
                v_ValueString = "'" + ((Date)i_Value).getFull() + "'";
            }
            else if ( i_Value.getClass().equals(java.util.Date.class) )
            {
                v_ValueString = "'" + new Date((java.util.Date)i_Value).getFull() + "'";
            }
            else if ( i_Value.getClass().equals(java.sql.Date.class) )
            {
                v_ValueString = "'" + new Date((java.sql.Date)i_Value).getFull() + "'";
            }
            else if ( i_Value.getClass().equals(Timestamp.class) )
            {
                v_ValueString = "'" + new Date((Timestamp)i_Value).getFullMilli() + "'";
            }
            else if ( i_Value.getClass().equals(Character.class) )
            {
                v_ValueString = "'" + i_Value.toString() + "'";
            }
            else if ( i_Value.getClass().equals(char.class) )
            {
                v_ValueString = "'" + i_Value.toString() + "'";
            }
            else
            {
                v_ValueString = i_Value.toString();
            }
        }
        
        return v_ValueString;
    }
    
    
    
    /**
     * 获取：预解释SQL。如 Insert Into ... Values(? ,? ,?)
     *      并将 ? 号替换成具体的数值
     * 
     * @author      ZhengWei(HY)
     * @createDate  2023-05-17
     * @version     v1.0
     *
     * @param i_Obj
     * @return
     */
    @SuppressWarnings("unchecked")
    public String getSQL(Object i_Obj)
    {
        String v_SQL = this.sql;
        
        if ( i_Obj == null )
        {
            return v_SQL;
        }
        
        if ( MethodReflect.isExtendImplement(i_Obj ,Map.class) )
        {
            for (String v_PlaceHolder : this.placeholders)
            {
                Object v_Value = MethodReflect.getMapValue((Map<String ,?>)i_Obj ,v_PlaceHolder);
                v_SQL = StringHelp.replaceFirst(v_SQL ,"?" ,objectToString(v_Value));
            }
        }
        else
        {
            for (String v_PlaceHolder : this.placeholders)
            {
                MethodReflect v_MethodReflect = null;
                try
                {
                    v_MethodReflect = new MethodReflect(i_Obj ,v_PlaceHolder ,true ,MethodReflect.$NormType_Getter);
                    
                    Object v_Value = v_MethodReflect.invoke();
                    v_SQL = StringHelp.replaceFirst(v_SQL ,"?" ,objectToString(v_Value));
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                }
                finally
                {
                    if ( v_MethodReflect != null )
                    {
                        v_MethodReflect.clearDestroy();
                        v_MethodReflect = null;
                    }
                }
            }
        }
        
        return v_SQL;
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
