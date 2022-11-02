package org.hy.common.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hy.common.Help;





/**
 * 通过对占位符SQL分析后的分段SQL信息
 * 
 * @author      ZhengWei(HY)
 * @version     v1.0  
 * @createDate  2012-10-30
 */
@Deprecated
public class DBSQL_Split_V1 
{
	
	/** sqlSplit 在完整SQL中匹配时的序号。下标从零开始 */
	private int    sqlIndex; 
	
	/** 匹配信息前面的SQL分段 */
	private String sqlSplit;
	
	/** 匹配信息前面的SQL分段的最小有效部分 */
	private String sqlSplit_Small;
	
	/** 匹配信息前面的SQL分段的相对于上一个SQL分段的结束部分 */
	private String sqlSplit_UpEnd;
	
	/** 匹配的信息 */
	private String matcheInfo;
	
	/** 匹配的开始位置。下标从零开始 */
	private int    matcheStartIndex;
	
	/** 匹配的结束位置。下标从零开始 */
	private int    matcheEndIndex;

	
	
	/**
	 * 生成匹配信息前面的SQL分段的最小有效部分
	 * 
	 * 即，当 DBSQL.getSQL() 方法时，对应的占位符没有对应的填充信息时，生成可执行SQL的最小有效部分。
	 * 
	 * 1. 如果 this.sqlSplit 为 "... WHERE ..." 时，则返回 "... WHERE 1 = 1 "。
	 * 
	 * 2. 如果 this.sqlSplit 为 "... AND ..."   时，则返回 " AND ..."。
	 * 
	 * 3. 如果 this.sqlSplit 为 "... OR ..."    时，则返回 " OR ..."。
	 * 
	 * @param i_SQLSplit
	 * @return
	 */
	public static String getSmall(String i_SQLSplit)
	{
		if ( Help.isNull(i_SQLSplit) )
		{
			return "";
		}
		
		
		Pattern v_Pattern    = Pattern.compile(" (([Aa][Nn][Dd])|([Oo][Rr])|([Ww][Hh][Ee][Rr][Ee])) ");
		Matcher v_Matcher    = v_Pattern.matcher(i_SQLSplit);
		int     v_StartIndex = 0;
		int     v_EndIndex   = 0;    
		String  v_Group      = "";
		
		
		// 查找最后一个匹配信息
		while ( v_Matcher.find() )
		{
			v_StartIndex = v_Matcher.start(); 
			v_EndIndex   = v_Matcher.end();
			v_Group      = v_Matcher.group();
		}
		
		
		if ( v_EndIndex > 0 )
		{
			if ( "WHERE".equalsIgnoreCase(v_Group.trim()) )
			{
				return i_SQLSplit.substring(0 ,v_EndIndex) + " 1 = 1 ";
			}
			else
			{
				return i_SQLSplit.substring(v_StartIndex);
			}
		}
		else
		{
			return i_SQLSplit;
		}
	}
	
	
	
	/**
	 * 生成匹配信息前面的SQL分段的相对于上一个SQL分段的结束部分
	 * 
	 * 即，当 DBSQL.getSQL() 方法时，出现 v_UpUpIsExist && !v_UpIsExist == true 的情况下，添加上上一次SQL分段结束信息。
	 * 
	 * 1. 如果 this.sqlSplit 为 "... WHERE ..." 时，则返回 ""。
	 * 
	 * 2. 如果 this.sqlSplit 为 "%' AND ..."    时，则返回 "%'"。
	 * 
	 * 3. 如果 this.sqlSplit 为 "%' OR ..."     时，则返回 "%'"。
	 * 
	 * @param i_SQLSplit
	 * @return
	 */
	public static String getUpEnd(String i_SQLSplit)
	{
		if ( Help.isNull(i_SQLSplit) )
		{
			return "";
		}
		
		
		Pattern v_Pattern    = Pattern.compile(" (([Aa][Nn][Dd])|[Oo][Rr]|([Ww][Hh][Ee][Rr][Ee])) ");
		Matcher v_Matcher    = v_Pattern.matcher(i_SQLSplit);
		int     v_StartIndex = 0;
		int     v_EndIndex   = 0; 
		String  v_Group      = "";
		
		
		// 查找首个匹配信息
		if ( v_Matcher.find() )
		{
			v_StartIndex = v_Matcher.start(); 
			v_EndIndex   = v_Matcher.end();
			v_Group      = v_Matcher.group();
		}
		
		
		if ( v_EndIndex > 0 )
		{
			if ( "WHERE".equalsIgnoreCase(v_Group.trim()) )
			{
				return "";
			}
			else
			{
				return i_SQLSplit.substring(0 ,v_StartIndex);
			}
		}
		else
		{
			return "";
		}
	}
	
	
	
	/**
	 * 生成匹配信息前面的SQL分段的最小有效部分
	 * 
	 * 即，当 DBSQL.getSQL() 方法时，对应的占位符没有对应的填充信息时，生成可执行SQL的最小有效部分。
	 * 
	 * 1. 如果 this.sqlSplit 为 "... WHERE ..." 时，则返回 "... WHERE 1 = 1 "。
	 * 
	 * 2. 如果 this.sqlSplit 为 "xxx AND yyy"   时，则返回 "xxx "。
	 * 
	 * 3. 如果 this.sqlSplit 为 "xxx OR yyy"    时，则返回 "xxx "。
	 * 
	 * @param i_SQLSplit
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String getGoto(DBSQL_Split_V1 i_DBSQL_Split)
	{
		if ( i_DBSQL_Split == null )
		{
			return "";
		}
		
		
		String  v_Value      = i_DBSQL_Split.getSqlSplit();
		Pattern v_Pattern    = Pattern.compile(" (([Aa][Nn][Dd])|([Oo][Rr])|([Ww][Hh][Ee][Rr][Ee])) ");
		Matcher v_Matcher    = v_Pattern.matcher(v_Value);
		int     v_StartIndex = 0;
		int     v_EndIndex   = 0;    
		String  v_Group      = "";
		
		
		// 查找最后一个匹配信息
		while ( v_Matcher.find() )
		{
			v_StartIndex = v_Matcher.start(); 
			v_EndIndex   = v_Matcher.end();
			v_Group      = v_Matcher.group();
		}
		
		
		if ( v_EndIndex > 0 )
		{
			if ( "WHERE".equalsIgnoreCase(v_Group.trim()) )
			{
				return v_Value.substring(0 ,v_EndIndex) + " 1 = 1 ";
			}
			else
			{
				return v_Value.substring(0 ,v_StartIndex - 1);
			}
		}
		else
		{
			return v_Value;
		}
	}
	
	
	
	public DBSQL_Split_V1(int i_SQLIndex ,String i_SQLSplit ,String i_MatcheInfo ,int i_MatcheStartIndex ,int i_MatcheEndIndex)
	{
		if ( Help.isNull(i_SQLSplit) )
		{
			throw new NullPointerException("SQL split info is null.");
		}
		
		this.sqlIndex         = i_SQLIndex;
		this.sqlSplit         = i_SQLSplit;
		this.sqlSplit_Small   = getSmall(this.sqlSplit);
		this.sqlSplit_UpEnd   = getUpEnd(this.sqlSplit);
		this.matcheInfo       = i_MatcheInfo;
		this.matcheStartIndex = i_MatcheStartIndex;
		this.matcheEndIndex   = i_MatcheEndIndex;
	}
	
	
	
	public int getMatcheEndIndex() 
	{
		return matcheEndIndex;
	}

	
	
	public String getMatcheInfo() 
	{
		return matcheInfo;
	}

	
	
	public int getMatcheStartIndex() 
	{
		return matcheStartIndex;
	}

	
	
	public String getSqlSplit() 
	{
		return sqlSplit;
	}
	
	
	
	public int getSqlIndex() 
	{
		return sqlIndex;
	}
	
	
	
	public String getSqlSplit_Small() 
	{
		return sqlSplit_Small;
	}
	
	
	
	public String getSqlSplit_UpEnd()
	{
		return this.sqlSplit_UpEnd;
	}
	
	

	public String toString()
	{
	    StringBuilder v_Buffer = new StringBuilder();
		
		v_Buffer.append(this.sqlIndex).append("  [");
		v_Buffer.append(this.matcheInfo).append("] (");
		v_Buffer.append(this.matcheStartIndex).append(" ,");
		v_Buffer.append(this.matcheEndIndex).append(")  ");
		v_Buffer.append(this.sqlSplit);
		
		return v_Buffer.toString();
	}
	
}
