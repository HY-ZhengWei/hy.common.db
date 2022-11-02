package org.hy.common.db.junit;

import java.util.Hashtable;
import java.util.Map;

import org.hy.common.Help;





public class JU_MapOrder
{
    
    public static void main(String [] args)
    {
        Map<String ,Object> v_Map = new Hashtable<String ,Object>();
        
        v_Map.put("A"   ,"1");
        v_Map.put("AA"  ,"2");
        v_Map.put("AAA" ,"3");
        
        System.out.println(Help.toReverse(v_Map));
    }
    
}
