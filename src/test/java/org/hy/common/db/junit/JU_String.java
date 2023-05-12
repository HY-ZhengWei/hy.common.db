package org.hy.common.db.junit;

import org.hy.common.xml.log.Logger;
import org.junit.Test;





/**
 * 测试单元：字符串相关的测试
 *
 * @author      ZhengWei(HY)
 * @createDate  2023-05-11
 * @version     v1.0
 */
public class JU_String
{
    
    private static final Logger $Logger = new Logger(JU_String.class ,true);
    
    
    
    @Test
    public void test_String()
    {
        String v_A = "ABCDEFG";
        String v_B = v_A;
        
        v_B = v_B.substring(2);
        $Logger.info(v_B);
        $Logger.info(v_A);
        
    }
    
}
