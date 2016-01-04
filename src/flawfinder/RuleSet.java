/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author afif
 */
public class RuleSet {
    
    private Map<String,RuleValue> ruleSetMap;
    
    public RuleSet()
    {
        ruleSetMap = new HashMap<String,RuleValue>();
    }
    
    public void addRule(String functionName,RuleValue ruleValue)
    {
        ruleSetMap.put(functionName,ruleValue);
    }
    
    public RuleValue getRule(String functionName)
    {
        return ruleSetMap.get(functionName);
    }
    
    public Iterator<String> getKeyIterator()
    {
        return ruleSetMap.keySet().iterator();
    }
    
    public void expandRuleSet()
    {
        Iterator<String> it = getKeyIterator();
        List<String> keys = new ArrayList<String>();
        while(it.hasNext())
            keys.add(it.next());
            
        for(String rule:keys)
        {
            if(rule.indexOf('|')!=-1)
            {
                for(String newRule: rule.split("\\|"))
                {
//                    System.out.println(newRule);
                    if(ruleSetMap.get(newRule)!=null)
                    {
                        System.out.println("Error: Rule "+rule+" when expanded overlaps "+newRule);
                        System.exit(1);
                    }
                    RuleValue copyRuleValue = new RuleValue(ruleSetMap.get(rule)); 
                    ruleSetMap.put(newRule, copyRuleValue);
//                    ruleSetMap.put(newRule, ruleSetMap.get(rule));
//                    System.out.println("Put "+newRule);
                }
                ruleSetMap.remove(rule);
//                System.out.println("Removed "+rule);


            }
        }
    }
    
    @Override
    public String toString()
    {
        String res = "";
        for(String key:ruleSetMap.keySet())
        {
            res += key+" : "+"\n"
                    +
                    ruleSetMap.get(key);
            res+="\n";
        }
        return res;
    }

    public boolean hasKey(String word) {
        return ruleSetMap.containsKey(word);
    }
}
