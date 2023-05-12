package org.hy.common.db;

import org.hy.common.Date;
import org.hy.common.Help;





/**
 * 数据库信息
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0
 * @createDate  2009-08-06
 */
public class DatabaseInfo
{
    private static int SerialNo = 0;
    
    
    
    /** 配置ID */
    private String configID;
    
    /** 数据库连接驱动 */
    private String driver;
    
    /** 数据库连接具体数据库时的 url */
    private String url;
    
    /** 连接数据库用户名 */
    private String user;
    
    /** 连接数据库密码 */
    private String password;
    
    /** 最大池对象 */
    private String maxPool;
    
    /** 最大数据库连接数 */
    private String maxConn;
    
    /** 终结 */
    private String expiry;
    
    /** 初始化大小 */
    private String init;
    
    /** 提交时，验证 */
    private String validator;
    
    /** 是否启用高速缓存 */
    private String cache;
    
    /** 是否开启Debug模式 */
    private String debug;
    
    /** 安全接口 */
    private DBSecurity dbSecurity;
    
    
    
    /**
     * 构造器
     */
    public DatabaseInfo()
    {
        this.configID  = getSerialNo() + "_" + new Date().getFull();
    }
    
    
    /**
     * 带安全接口的构造器
     * 
     * @param i_DBSecurity
     */
    public DatabaseInfo(DBSecurity i_DBSecurity)
    {
        this();
        
        this.dbSecurity = i_DBSecurity;
    }
    
    
    /**
     * 注意：本方法可能在多个实例、多个线程中执行，所以要用 static synchronized
     * 
     * @return
     */
    protected static synchronized int getSerialNo()
    {
        return SerialNo++;
    }
    
    
    /**
     * 获取XML元素对象惟一标示
     */
    public String getObjectID()
    {
        return "" + this.configID;
    }
    
    
    public String getDriver()
    {
        return driver;
    }

    
    public void setDriver(String driver)
    {
        this.driver = driver;
    }

    
    public String getPassword()
    {
        if ( this.dbSecurity != null && !Help.isNull(this.user) )
        {
            if ( Help.isNull(this.password) )
            {
                return "";
            }
            else
            {
                return this.dbSecurity.getDecrypt(this.password, this.user);
            }
        }
        else
        {
            return this.password;
        }
    }
    
    
    public String getPasswordValue()
    {
        return this.password;
    }

    
    public void setPassword(String i_Password)
    {
        if (  this.dbSecurity != null && !Help.isNull(this.user) )
        {
            if ( i_Password != null )
            {
                if ( i_Password.trim().length() == this.dbSecurity.getSecurityLen())
                {
                    this.password  = i_Password;
                }
                else
                {
                    this.password  = this.dbSecurity.getEncrypt(i_Password ,this.user);
                }
            }
            else
            {
                this.password = i_Password;
            }
        }
        else
        {
            this.password = i_Password;
        }
    }

    
    public String getUrl()
    {
        return url;
    }

    
    public void setUrl(String url)
    {
        this.url = url;
    }

    
    public String getUser()
    {
        return user;
    }

    
    public void setUser(String user)
    {
        this.user = user;
    }


    public String getCache()
    {
        return cache;
    }


    public void setCache(String cache)
    {
        this.cache = cache;
    }


    public String getDebug()
    {
        return debug;
    }


    public void setDebug(String debug)
    {
        this.debug = debug;
    }


    public String getExpiry()
    {
        return expiry;
    }


    public void setExpiry(String expiry)
    {
        this.expiry = expiry;
    }


    public String getInit()
    {
        return init;
    }


    public void setInit(String init)
    {
        this.init = init;
    }


    public String getMaxConn()
    {
        return maxConn;
    }


    public void setMaxConn(String maxConn)
    {
        this.maxConn = maxConn;
    }


    public String getMaxPool()
    {
        return maxPool;
    }


    public void setMaxPool(String maxPool)
    {
        this.maxPool = maxPool;
    }


    public String getValidator()
    {
        return validator;
    }


    public void setValidator(String validator)
    {
        this.validator = validator;
    }


    public DBSecurity getDbSecurity()
    {
        return dbSecurity;
    }


    public void setDbSecurity(DBSecurity dbSecurity)
    {
        this.dbSecurity = dbSecurity;
    }
    
}
