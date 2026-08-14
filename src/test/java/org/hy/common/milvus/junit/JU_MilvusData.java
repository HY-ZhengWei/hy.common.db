package org.hy.common.milvus.junit;

import java.util.List;




/**
 * 测试单元：Milvus向量库的数据对象
 *
 * @author      ZhengWei(HY)
 * @createDate  2026-07-28
 * @version     v1.0
 */
public class JU_MilvusData
{
    
    /** 主键 */
    private String      id;
    
    /** 向量数据 */
    private List<Float> vector;
    
    /** 向量数据 */
    private List<Float> vectorElse;
    
    /** 注释 */
    private String      comment;

    
    
    /**
     * 获取：主键
     */
    public String getId()
    {
        return id;
    }

    
    /**
     * 设置：主键
     * 
     * @param i_Id 主键
     */
    public void setId(String i_Id)
    {
        this.id = i_Id;
    }


    /**
     * 获取：向量数据
     */
    public List<Float> getVector()
    {
        return vector;
    }

    
    /**
     * 设置：向量数据
     * 
     * @param i_Vector 向量数据
     */
    public void setVector(List<Float> i_Vector)
    {
        this.vector = i_Vector;
    }
    
    
    /**
     * 获取：向量数据
     */
    public List<Float> getVectorElse()
    {
        return vectorElse;
    }

    
    /**
     * 设置：向量数据
     * 
     * @param i_VectorElse 向量数据
     */
    public void setVectorElse(List<Float> i_VectorElse)
    {
        this.vectorElse = i_VectorElse;
    }


    /**
     * 获取：注释
     */
    public String getComment()
    {
        return comment;
    }

    
    /**
     * 设置：注释
     * 
     * @param i_Comment 注释
     */
    public void setComment(String i_Comment)
    {
        this.comment = i_Comment;
    }
    
}
