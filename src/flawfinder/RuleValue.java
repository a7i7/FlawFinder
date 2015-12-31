/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

import java.util.Map;

/**
 *
 * @author afif
 */
public class RuleValue {
    private String hook;
    private int level;
    private String warning;
    private String suggestion;
    private String category;
    private String url;
    private Map<String,Integer> other;
    
    public RuleValue(String hook,int level,String warning,String suggestion,String category,String unknown,Map<String,Integer> other)
    {
       this.hook = hook;
       this.level = level;
       this.warning = warning;
       this.suggestion = suggestion;
       this.category = category;
       this.url = unknown;
       this.other = other;
    }
    /**
     * @return the hook
     */
    public String getHook() {
        return hook;
    }

    /**
     * @param hook the hook to set
     */
    public void setHook(String hook) {
        this.hook = hook;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the warning
     */
    public String getWarning() {
        return warning;
    }

    /**
     * @param warning the warning to set
     */
    public void setWarning(String warning) {
        this.warning = warning;
    }

    /**
     * @return the suggestion
     */
    public String getSuggestion() {
        return suggestion;
    }

    /**
     * @param suggestion the suggestion to set
     */
    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the other
     */
    public Map<String,Integer> getOther() {
        return other;
    }

    /**
     * @param other the other to set
     */
    public void setOther(Map<String,Integer> other) {
        this.other = other;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
}
