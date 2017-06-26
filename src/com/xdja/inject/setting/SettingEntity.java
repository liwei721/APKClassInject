package com.xdja.inject.setting;

import java.util.List;

/**
 * Created by zlw on 2017/6/26.
 */
public class SettingEntity {

    /**
     * isInject : true
     * showLog : true
     * InjectSettings : [{"className":"*Activity","classExclude":["com.android.*"],"modifyMethods":"","methodName":"onCreate","methodDesc":"","methodExclude":[],"InjectClass":"com.xdja.inject.MontiorTrace","InjectMethod":"log","InjectMethodDesc":"()V","InjectMethodParams":[]}]
     */

    private boolean isInject;
    private boolean showLog;
    private List<InjectSettingsBean> InjectSettings;

    public boolean isIsInject() {
        return isInject;
    }

    public void setIsInject(boolean isInject) {
        this.isInject = isInject;
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setShowLog(boolean showLog) {
        this.showLog = showLog;
    }

    public List<InjectSettingsBean> getInjectSettings() {
        return InjectSettings;
    }

    public void setInjectSettings(List<InjectSettingsBean> InjectSettings) {
        this.InjectSettings = InjectSettings;
    }

    public static class InjectSettingsBean {
        /**
         * className : *Activity
         * classExclude : ["com.android.*"]
         * modifyMethods :
         * methodName : onCreate
         * methodDesc :
         * methodExclude : []
         * InjectClass : com.xdja.inject.MontiorTrace
         * InjectMethod : log
         * InjectMethodDesc : ()V
         * InjectMethodParams : []
         */

        private String className;
        private String modifyMethods;
        private String methodName;
        private String methodDesc;
        private String InjectClass;
        private String InjectMethod;
        private String InjectMethodDesc;
        private List<String> classExclude;
        private List<?> methodExclude;
        private List<?> InjectMethodParams;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String getModifyMethods() {
            return modifyMethods;
        }

        public void setModifyMethods(String modifyMethods) {
            this.modifyMethods = modifyMethods;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getMethodDesc() {
            return methodDesc;
        }

        public void setMethodDesc(String methodDesc) {
            this.methodDesc = methodDesc;
        }

        public String getInjectClass() {
            return InjectClass;
        }

        public void setInjectClass(String InjectClass) {
            this.InjectClass = InjectClass;
        }

        public String getInjectMethod() {
            return InjectMethod;
        }

        public void setInjectMethod(String InjectMethod) {
            this.InjectMethod = InjectMethod;
        }

        public String getInjectMethodDesc() {
            return InjectMethodDesc;
        }

        public void setInjectMethodDesc(String InjectMethodDesc) {
            this.InjectMethodDesc = InjectMethodDesc;
        }

        public List<String> getClassExclude() {
            return classExclude;
        }

        public void setClassExclude(List<String> classExclude) {
            this.classExclude = classExclude;
        }

        public List<?> getMethodExclude() {
            return methodExclude;
        }

        public void setMethodExclude(List<?> methodExclude) {
            this.methodExclude = methodExclude;
        }

        public List<?> getInjectMethodParams() {
            return InjectMethodParams;
        }

        public void setInjectMethodParams(List<?> InjectMethodParams) {
            this.InjectMethodParams = InjectMethodParams;
        }
    }
}
