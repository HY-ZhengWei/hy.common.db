package org.hy.common.db.junit;

import java.util.Hashtable;
import java.util.Map;

import org.hy.common.Date;
import org.hy.common.db.DBSQL;





/**
 * 测试：数据库占位符SQL的信息
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-10-31
 */
public class DBSQLTest 
{
	private Date           beginTime;
	
	private Date           endTime;
	
	private int            statue;
	
	private String         userName;
	
	private String         datas;
	
	private String         lists;
	
	
	
	public Date getBeginTime() 
	{
		return beginTime;
	}

	
	public void setBeginTime(Date beginTime) 
	{
		this.beginTime = beginTime;
	}

	
	public Date getEndTime() 
	{
		return endTime;
	}

	
	public void setEndTime(Date endTime) 
	{
		this.endTime = endTime;
	}

	
	public int getStatue() 
	{
		return statue;
	}

	
	public void setStatue(int statue) 
	{
		this.statue = statue;
	}

	
	public String getUserName() 
	{
		return userName;
	}

	
	public void setUserName(String userName) 
	{
		this.userName = userName;
	}
	
	
    public String getDatas()
    {
        return datas;
    }


    public void setDatas(String datas)
    {
        this.datas = datas;
    }
    
    
    public String getLists()
    {
        return lists;
    }

    
    public void setLists(String lists)
    {
        this.lists = lists;
    }
    


    public static void main(String [] args)
	{
		// 按属性类生成可执行SQL
	    StringBuilder v_SQL = new StringBuilder();
		
		v_SQL.append("SELECT  * ");
		v_SQL.append("  FROM  Dual");
		v_SQL.append(" WHERE  BeginTime = TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')");
		v_SQL.append("   AND  EndTime   = TO_DATE(':EndTime'   ,'YYYY-MM-DD HH24:MI:SS')");
		v_SQL.append("   AND  Statue    = :Statue").append("\n");
		v_SQL.append("   AND  UserName  = ':UserName'");
		v_SQL.append("   AND  CardNo    = ':CardNo'");
		v_SQL.append("   AND  CityCode  = 0910");
		v_SQL.append("   AND  CityCode IN (:datas)");
		v_SQL.append("   AND  CityCode in (:lists)");
		
		DBSQL        v_DBSQL = new DBSQL(v_SQL.toString());
		DBSQLTest    v_Test  = new DBSQLTest();
		
		v_Test.setBeginTime(new Date());
		v_Test.setEndTime(  new Date());
		v_Test.setStatue(   2012);
		v_Test.setUserName("HY");
		v_Test.setDatas("'A' ,'B' ,'C' ,'Z'");
		v_Test.setLists("A' ,'B' ,'C' ,'Z");
		
		System.out.println(v_DBSQL.getSQL(v_Test ,null));
		System.out.println(v_DBSQL.getSQLType());
		
		
		
		
		// 按Map<String ,Object>生成可执行SQL
		Map<String ,Object> v_Values = new Hashtable<String ,Object>();
		v_SQL = new StringBuilder();
		
		v_SQL.append("SELECT  /*+ LEADING(A) */");
		v_SQL.append("        A.CallID");                                                                 // 呼叫标示
		v_SQL.append("       ,A.ContactID");                                                              // 接触编号
		v_SQL.append("       ,A.CallerNo");                                                               // 主叫号码
		v_SQL.append("       ,A.CalledNo");                                                               // 被叫号码
		v_SQL.append("       ,A.SubSNumber                                         AS AcceptNo");         // 受理号码
		v_SQL.append("       ,A.StaffID                                            AS AgentID");          // 员工账号
		v_SQL.append("       ,TO_CHAR(A.ContactStartTime ,'YYYY-MM-DD HH24:MI:SS') AS ContactStartTime"); // 接触开始时间
		v_SQL.append("       ,A.ContactDuration");                                                        // 接触时长
		v_SQL.append("       ,REPLACE(B.RecordFilePath ,'\\' ,'/')                 AS FileName");         // 语音文件
		v_SQL.append("  FROM  ICDWF.T_CCT_ContactDetail  A");
		v_SQL.append("       ,ICDWF.T_CCT_CallaffixInfo  B");
		v_SQL.append(" WHERE  B.ContactID         = A.ContactID");
		v_SQL.append("   AND  B.ID               >= ':PartBegin'");
		v_SQL.append("   AND  B.ID               <  ':PartEnd'");
		v_SQL.append("   AND  A.ContactStartTime >= TO_DATE(':BeginTime' ,'YYYY-MM-DD HH24:MI:SS')");
		v_SQL.append("   AND  A.ContactStartTime <  TO_DATE(':EndTime  ' ,'YYYY-MM-DD HH24:MI:SS')");
		v_SQL.append("   AND  A.SerialNo         >= ':PartBegin'");
		v_SQL.append("   AND  A.SerialNo         <  ':PartEnd'");
		v_SQL.append("   AND  A.ContactID         = ':ContactID'");
		v_SQL.append("   AND  A.CallerNo || '' LIKE '%:CallerNo%'");
		v_SQL.append("   AND  A.CalledNo || '' LIKE '%:CalleeNo%'");
		v_SQL.append("   AND  A.StaffID           = ':AgentID'");
		v_SQL.append("   AND  A.ContactDuration  >= :CallTimeMin");
		v_SQL.append("   AND  A.ContactDuration  <= :CallTimeMax");
		v_SQL.append("   AND  1 = 1");
		v_SQL.append("   AND  A.SubCCNo           = ':CenterID'");
		v_SQL.append("   AND  A.VDNID             = ':VirtualID'");
		
		v_DBSQL.setSqlText(v_SQL.toString());
		
		v_Values.put("PartBegin" ,"121031");
		v_Values.put("PartEnd"   ,"121101");
		v_Values.put("BeginTime" ,new Date());
		v_Values.put("EndTime"   ,new Date());
		v_Values.put("CallerNo"  ,"13669224517");
		//v_Values.put("ContactID" ,"201210310001");
		
		System.out.println();
		System.out.println(v_DBSQL.getSQL(v_Values ,null));
		System.out.println("无填充项的情况：" + v_DBSQL.getSQL());
	}
	
}
