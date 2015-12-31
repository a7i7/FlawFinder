/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package flawfinder;

/**
 *
 * @author afif
 */
public class Arguments {
    
    private boolean showContext;
    private boolean showInputs;
    private boolean allowLink;
    private boolean skipDotDir;
    private boolean omitTime;
    
    private boolean outputFormat;
    private int minimumLevel;
    private boolean showImmediately;
    private boolean singleLine;
    
    private boolean requiredRegex;
    //private boolean required_regex_compiled; //datatype to be decided
    
    private boolean falsePositive;
    
    private boolean showColumns;
    
    private boolean neverIgnore;
    private boolean quiet;
    private boolean showHeading;
    private boolean listRules;
    
    private String loadHitList; //probably String
    private String saveHitList; //probably ,,
    private String diffHitList; //probably ,,
    private String patchFile; //probably ,,

    /**
     * @return the showContext
     */
    public boolean isShowContext() {
        return showContext;
    }

    /**
     * @param showContext the showContext to set
     */
    public void setShowContext(boolean showContext) {
        this.showContext = showContext;
    }

    /**
     * @return the showInputs
     */
    public boolean isShowInputs() {
        return showInputs;
    }

    /**
     * @param showInputs the showInputs to set
     */
    public void setShowInputs(boolean showInputs) {
        this.showInputs = showInputs;
    }

    /**
     * @return the allowLink
     */
    public boolean isAllowLink() {
        return allowLink;
    }

    /**
     * @param allowLink the allowLink to set
     */
    public void setAllowLink(boolean allowLink) {
        this.allowLink = allowLink;
    }

    /**
     * @return the skipDotDir
     */
    public boolean isSkipDotDir() {
        return skipDotDir;
    }

    /**
     * @param skipDotDir the skipDotDir to set
     */
    public void setSkipDotDir(boolean skipDotDir) {
        this.skipDotDir = skipDotDir;
    }

    /**
     * @return the omitTime
     */
    public boolean isOmitTime() {
        return omitTime;
    }

    /**
     * @param omitTime the omitTime to set
     */
    public void setOmitTime(boolean omitTime) {
        this.omitTime = omitTime;
    }

    /**
     * @return the outputFormat
     */
    public boolean isOutputFormat() {
        return outputFormat;
    }

    /**
     * @param outputFormat the outputFormat to set
     */
    public void setOutputFormat(boolean outputFormat) {
        this.outputFormat = outputFormat;
    }

    /**
     * @return the minimumLevel
     */
    public int getMinimumLevel() {
        return minimumLevel;
    }

    /**
     * @param minimumLevel the minimumLevel to set
     */
    public void setMinimumLevel(int minimumLevel) {
        this.minimumLevel = minimumLevel;
    }

    /**
     * @return the showImmediately
     */
    public boolean isShowImmediately() {
        return showImmediately;
    }

    /**
     * @param showImmediately the showImmediately to set
     */
    public void setShowImmediately(boolean showImmediately) {
        this.showImmediately = showImmediately;
    }

    /**
     * @return the singleLine
     */
    public boolean isSingleLine() {
        return singleLine;
    }

    /**
     * @param singleLine the singleLine to set
     */
    public void setSingleLine(boolean singleLine) {
        this.singleLine = singleLine;
    }

    /**
     * @return the requiredRegex
     */
    public boolean isRequiredRegex() {
        return requiredRegex;
    }

    /**
     * @param requiredRegex the requiredRegex to set
     */
    public void setRequiredRegex(boolean requiredRegex) {
        this.requiredRegex = requiredRegex;
    }

    /**
     * @return the falsePositive
     */
    public boolean isFalsePositive() {
        return falsePositive;
    }

    /**
     * @param falsePositive the falsePositive to set
     */
    public void setFalsePositive(boolean falsePositive) {
        this.falsePositive = falsePositive;
    }

    /**
     * @return the showColumns
     */
    public boolean isShowColumns() {
        return showColumns;
    }

    /**
     * @param showColumns the showColumns to set
     */
    public void setShowColumns(boolean showColumns) {
        this.showColumns = showColumns;
    }

    /**
     * @return the neverIgnore
     */
    public boolean isNeverIgnore() {
        return neverIgnore;
    }

    /**
     * @param neverIgnore the neverIgnore to set
     */
    public void setNeverIgnore(boolean neverIgnore) {
        this.neverIgnore = neverIgnore;
    }

    /**
     * @return the quiet
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * @param quiet the quiet to set
     */
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    /**
     * @return the showHeading
     */
    public boolean isShowHeading() {
        return showHeading;
    }

    /**
     * @param showHeading the showHeading to set
     */
    public void setShowHeading(boolean showHeading) {
        this.showHeading = showHeading;
    }

    /**
     * @return the listRules
     */
    public boolean isListRules() {
        return listRules;
    }

    /**
     * @param listRules the listRules to set
     */
    public void setListRules(boolean listRules) {
        this.listRules = listRules;
    }

    /**
     * @return the loadHitList
     */
    public String getLoadHitList() {
        return loadHitList;
    }

    /**
     * @param loadHitList the loadHitList to set
     */
    public void setLoadHitList(String loadHitList) {
        this.loadHitList = loadHitList;
    }

    /**
     * @return the saveHitList
     */
    public String getSaveHitList() {
        return saveHitList;
    }

    /**
     * @param saveHitList the saveHitList to set
     */
    public void setSaveHitList(String saveHitList) {
        this.saveHitList = saveHitList;
    }

    /**
     * @return the diffHitList
     */
    public String getDiffHitList() {
        return diffHitList;
    }

    /**
     * @param diffHitList the diffHitList to set
     */
    public void setDiffHitList(String diffHitList) {
        this.diffHitList = diffHitList;
    }

    /**
     * @return the patchFile
     */
    public String getPatchFile() {
        return patchFile;
    }

    /**
     * @param patchFile the patchFile to set
     */
    public void setPatchFile(String patchFile) {
        this.patchFile = patchFile;
    }
    
    
}