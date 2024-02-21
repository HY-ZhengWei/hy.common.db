package org.hy.common.configfile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hy.common.Help;
import org.hy.common.StringHelp;
import org.hy.common.xml.log.Logger;





/**
 * 配置文件转为对象实例的集合
 * 
 * 在 ConfigFileReadBase 的基础之上，按 objectProtoType 原型和反射机制，
 * 将配置文件转变为对象实例，并保存在集合中。
 * 
 * 1. 有回写功能
 * 2. #号注释行
 * 3. 使用如下样式，将生成两个对象实例：name1 和 name2。
 *    name1.key1=value1
 *    name1.key2=value2
 * 
 *    name2.key1=value3
 *    name2.key2=value4
 *
 * @author   ZhengWei(HY)
 * @version  V1.0  2012-05-28
 */
public class ConfigFileObject extends ConfigFileReadBase
{

    private static final Logger $Logger = new Logger(ConfigFileObject.class ,true);
    
    /**
     * 配置文件存放格式为：name.key=value
     * 
     * Map.key 为配置文件中的 name
     */
    private Map<String ,Object>                                objectMap;
    
    /**
     * 用于保存一个完整<O>的位置
     * 
     * 配置文件存放格式为：name.key=value
     * 
     * List.value 为配置文件中的 name
     */
    private List<String>                                       objectNameList;
    
    /**
     * 用于保存基本行级配置信息
     * 
     * 配置文件存放格式为：name.key=value
     * 
     * Map.key          为配置文件中的 name
     * Map.value.key    为配置文件中的 key
     * Map.value.value  为 ConfigFileObjectRowInfo
     */
    private Map<String ,Map<String ,ConfigFileObjectRowInfo>>  objRowInfoMap;
    
    /**
     * objectMap.value 中存放元素的对象的原型。
     * 用它来创建更多新的对象实例
     */
    private Class<?>                                           objectProtoType;
    
    /**
     * 配置文件转为对象的安全接口
     */
    private ConfigFileSecurity                                 cfSecurity;
    
    
    
    public ConfigFileObject(String i_FileName ,Class<?> i_ObjectProtoType)
    {
        super(i_FileName);
        
        this.objectProtoType = i_ObjectProtoType;
    }
    
    
    
    /**
     * 读取一行数据
     * 
     * @param i_LineNo      行号
     * @param i_LineString  一行数据
     * @throws Exception
     * @throws IOException
     */
    @Override
    public void readLineString(int i_LineNo, String i_LineString)
    {
        if ( i_LineString != null )
        {
            String v_LineStr       = i_LineString.trim();
            String [] v_LineStrArr = v_LineStr.split("=");
            
            if ( v_LineStrArr.length != 2 )
            {
                return;
            }
            
            String    v_LeftVallue = v_LineStrArr[0];
            String [] v_NameAndKey = v_LeftVallue.replace('.', ',').split(",");
            
            if ( v_NameAndKey.length != 2 )
            {
                $Logger.info(ConfigFile_I18N.getHintRigthOSConfig() + i_LineNo + " = " + i_LineString);
                this.setStopRead(true);
            }
            
            
            if ( Help.isNull(this.objectMap) )
            {
                this.objectMap      = new Hashtable<String ,Object>();
                this.objectNameList = new ArrayList<String>();
                this.objRowInfoMap  = new Hashtable<String ,Map<String ,ConfigFileObjectRowInfo>>();
            }
            
            
            ConfigFileObjectRowInfo v_CFORowInfo = new ConfigFileObjectRowInfo(i_LineNo
                                                                              ,v_NameAndKey[0]
                                                                              ,v_NameAndKey[1]
                                                                              ,v_LineStrArr[1].trim());
            
            Object v_Object = null;
            if ( this.objectMap.containsKey(v_CFORowInfo.getName()) )
            {
                v_Object = this.objectMap.get(v_CFORowInfo.getName());
                
                // 记录基本行级配置信息
                Map<String ,ConfigFileObjectRowInfo> v_CFORMap = this.objRowInfoMap.get(v_CFORowInfo.getName());
                if ( v_CFORMap.containsKey(v_CFORowInfo.getKey()) )
                {
                    v_CFORMap.remove(v_CFORowInfo.getKey());
                }
                v_CFORMap.put(v_CFORowInfo.getKey() ,v_CFORowInfo);
            }
            else
            {
                try
                {
                    if ( this.cfSecurity != null )
                    {
                        v_Object = this.cfSecurity.newObject();
                    }
                    else
                    {
                        v_Object = this.objectProtoType.getDeclaredConstructor().newInstance();
                    }
                    
                    this.objectMap.put(v_CFORowInfo.getName(), v_Object);
                    this.objectNameList.add(v_CFORowInfo.getName());
                    
                    // 记录基本行级配置信息
                    Map<String ,ConfigFileObjectRowInfo> v_CFORMap = new Hashtable<String ,ConfigFileObjectRowInfo>();
                    v_CFORMap.put(v_CFORowInfo.getKey() ,v_CFORowInfo);
                    this.objRowInfoMap.put(v_CFORowInfo.getName() ,v_CFORMap);
                }
                catch (Exception exce)
                {
                    this.setStopRead(true);
                    $Logger.error(exce);
                }
            }
            
            
            Method v_SetMethod = this.getSetMethod(v_CFORowInfo.getKey());
            if ( v_SetMethod != null )
            {
                Class<?> v_ParamType = v_SetMethod.getParameterTypes()[0];
                
                try
                {
                    if ( String.class == v_ParamType )
                    {
                        v_SetMethod.invoke(v_Object ,v_CFORowInfo.getValue());
                    }
                    else if ( int.class == v_ParamType )
                    {
                        v_SetMethod.invoke(v_Object ,Integer.parseInt(v_CFORowInfo.getValue()));
                    }
                    else if ( boolean.class == v_ParamType )
                    {
                        v_SetMethod.invoke(v_Object ,Boolean.parseBoolean(v_CFORowInfo.getValue()));
                    }
                }
                catch (Exception exce)
                {
                    $Logger.error(exce);
                }
            }
            
            
            // 配置文件处理每一行时，在配置数据转为对象之后，将调用此方法。
            // 将一些特殊的处理动交给使用者自行定义。
            if ( this.cfSecurity != null )
            {
                try
                {
                    this.cfSecurity.dispose(this ,v_Object,v_CFORowInfo);
                    
                    if ( v_CFORowInfo.isValueChange() )
                    {
                        this.writeByLineData(v_CFORowInfo.getLineNo() ,v_CFORowInfo.toString());
                        v_CFORowInfo.valueChangeInit();
                    }
                }
                catch (Exception exce)
                {
                    this.setStopRead(true);
                    throw new SecurityException("Config file security is exception of [" + v_CFORowInfo.toString() + "].");
                }
            }
            
        }
    }
    
    
    
    /**
     * 获取 Setter 方法
     * 
     * @param i_SetMethodName
     * @return
     */
    private Method getSetMethod(String i_SetMethodName)
    {
        String v_SetMethodName = i_SetMethodName.trim();
        v_SetMethodName        = "set" + StringHelp.toUpperCaseByFirst(v_SetMethodName);
        
        Method [] v_Methods = this.objectProtoType.getMethods();
        
        
        for (int i=0; i<v_Methods.length; i++)
        {
            if ( v_Methods[i].getName().equals(v_SetMethodName) )
            {
                if ( v_Methods[i].getParameterTypes().length == 1 )
                {
                    return v_Methods[i];
                }
            }
        }
        
        return null;
    }
    
    
    
    /**
     * 获取对象的数量
     * 
     * @return
     */
    public int size()
    {
        if ( this.objectMap == null )
        {
            return 0;
        }
        
        return this.objectMap.size();
    }
    
    
    
    /**
     * 获取对象实例
     * 
     * @param i_Index
     * @return
     */
    public Object getObject(int i_Index)
    {
        if ( Help.isNull(this.objectMap) )
        {
            return null;
        }
        
        if ( i_Index < 0  || i_Index >= this.objectNameList.size() )
        {
            return null;
        }
        
        return this.objectMap.get(this.objectNameList.get(i_Index));
    }
    
    
    
    /**
     * 按配置文件中的 name.key 获取对象实例
     * 
     * 配置文件存放格式为：name.key=value
     * 
     * @param i_Name
     * @return
     */
    public Object getObject(String i_Name)
    {
        if ( Help.isNull(this.objectMap) )
        {
            return null;
        }
        
        if ( i_Name == null )
        {
            return null;
        }
        
        if ( this.objectMap.containsKey(i_Name) )
        {
            return this.objectMap.get(i_Name);
        }
        else
        {
            return null;
        }
    }
    
    
    
    /**
     * 获取对象配置 name 集合的迭代器
     * 
     * @return
     */
    public Iterator<String> getObjectNameList()
    {
        if ( this.objectNameList == null || this.objectNameList.size() == 0 )
        {
            return null;
        }
        
        return this.objectNameList.iterator();
    }
    
    
    
    /**
     * 设置对象配置文件某一行（某一属性）的数据，并先写入缓存中
     * 
     * 配置文件存放格式为：name.key=value
     * 
     * @param i_Name
     * @param i_Key
     * @param i_NewValue
     */
    public synchronized void setValue(String i_Name ,String i_Key ,String i_NewValue)
    {
        if ( this.objRowInfoMap == null || this.objRowInfoMap.size() == 0 )
        {
            return;
        }
        
        if ( Help.isNull(i_Name) || Help.isNull(i_Key) )
        {
            return;
        }
        
        
        if ( this.objRowInfoMap.containsKey(i_Name) )
        {
            Map<String ,ConfigFileObjectRowInfo> v_CFORMap = this.objRowInfoMap.get(i_Name);
            
            if ( v_CFORMap.containsKey(i_Key) )
            {
                ConfigFileObjectRowInfo v_CFORowInfo = v_CFORMap.get(i_Key);
                
                if ( !Help.NVL(i_NewValue ,"").equals(v_CFORowInfo.getValue()) )
                {
                    v_CFORowInfo.setValue(i_NewValue);
                
                    this.writeByLineData(v_CFORowInfo.getLineNo() ,v_CFORowInfo.toString());
                }
            }
        }
    }
    
    
    
    /**
     * 设置对象配置文件某一行（某一属性）的数据，并先写入缓存中
     * 
     * 配置文件存放格式为：name.key=value
     * 
     * @param i_CFORowInfo
     */
    public synchronized void setValue(ConfigFileObjectRowInfo i_CFORowInfo)
    {
        if ( this.objRowInfoMap == null || this.objRowInfoMap.size() == 0 )
        {
            return;
        }
        
        if ( i_CFORowInfo == null || Help.isNull(i_CFORowInfo.getName()) || Help.isNull(i_CFORowInfo.getKey()) )
        {
            return;
        }
        
        
        if ( this.objRowInfoMap.containsKey(i_CFORowInfo.getName()) )
        {
            Map<String ,ConfigFileObjectRowInfo> v_CFORMap = this.objRowInfoMap.get(i_CFORowInfo.getName());
            
            if ( v_CFORMap.containsKey(i_CFORowInfo.getKey()) )
            {
                ConfigFileObjectRowInfo v_CFORowInfo = v_CFORMap.get(i_CFORowInfo.getKey());
                
                if ( !i_CFORowInfo.getValue().equals(v_CFORowInfo.getValue()) )
                {
                    v_CFORowInfo.setValue(i_CFORowInfo.getValue());
                    
                    this.writeByLineData(v_CFORowInfo.getLineNo() ,v_CFORowInfo.toString());
                }
            }
        }
    }
    
    
    
    public ConfigFileSecurity getCfSecurity()
    {
        return cfSecurity;
    }


    
    /**
     * 配置文件转为对象的安全接口
     * 
     * @param i_CFSecurity
     */
    public void setCfSecurity(ConfigFileSecurity i_CFSecurity)
    {
        this.cfSecurity = i_CFSecurity;
    }
    
}
