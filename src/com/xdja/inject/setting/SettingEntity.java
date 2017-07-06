package com.xdja.inject.setting;

import com.xdja.inject.util.Util;

import java.util.List;

/**
 * Created by zlw on 2017/6/26.
 */
public class SettingEntity {

    /**
     * isInject : true
     * showLog : true
     * InjectSettings : [{"className":"*Activity","classExclude":["com.android.*","android.support.*"],"InjectMethod":[{"methodName":"onCreate","methodDesc":"(Landroid/os/Bundle;)V","methodExclude":[],"InjectContent":[{"InjectClass":"com.xdja.monitor.AppMonitor","InjectMethodName":"monitorPageStart","InjectMethodDesc":"(Ljava/lang/String;)V","InjectPosition":""}]},{"methodName":"onWindowFocusChanged","methodDesc":"(Z)V","methodExclude":[],"InjectContent":[{"InjectClass":"com.xdja.monitor.AppMonitor","InjectMethodName":"monitorPageLoaded","InjectMethodDesc":"(JLjava/lang/String;)V","InjectPosition":""}]}],"InjectField":[{"InjectMethodParams":[],"InjectFieldName":"","InjectFieldClassType":""}]}]
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
         * classExclude : ["com.android.*","android.support.*"]
         * InjectMethod : [{"methodName":"onCreate","methodDesc":"(Landroid/os/Bundle;)V","methodExclude":[],"InjectContent":[{"InjectClass":"com.xdja.monitor.AppMonitor","InjectMethodName":"monitorPageStart","InjectMethodDesc":"(Ljava/lang/String;)V","InjectPosition":""}]},{"methodName":"onWindowFocusChanged","methodDesc":"(Z)V","methodExclude":[],"InjectContent":[{"InjectClass":"com.xdja.monitor.AppMonitor","InjectMethodName":"monitorPageLoaded","InjectMethodDesc":"(JLjava/lang/String;)V","InjectPosition":""}]}]
         * InjectField : [{"InjectMethodParams":[],"InjectFieldName":"","InjectFieldClassType":""}]
         */

        private String className;
        private List<String> classExclude;
        private List<InjectMethodBean> InjectMethod;
        private List<InjectFieldBean> InjectField;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<String> getClassExclude() {
            return classExclude;
        }

        public void setClassExclude(List<String> classExclude) {
            this.classExclude = classExclude;
        }

        public List<InjectMethodBean> getInjectMethod() {
            return InjectMethod;
        }

        public void setInjectMethod(List<InjectMethodBean> InjectMethod) {
            this.InjectMethod = InjectMethod;
        }

        public List<InjectFieldBean> getInjectField() {
            return InjectField;
        }

        public void setInjectField(List<InjectFieldBean> InjectField) {
            this.InjectField = InjectField;
        }

        public static class InjectMethodBean {
            /**
             * methodName : onCreate
             * methodDesc : (Landroid/os/Bundle;)V
             * methodExclude : []
             * InjectContent : [{"InjectClass":"com.xdja.monitor.AppMonitor","InjectMethodName":"monitorPageStart","InjectMethodDesc":"(Ljava/lang/String;)V","InjectPosition":""}]
             */

            private String methodName;
            private String methodDesc;
            private List<?> methodExclude;
            private List<InjectContentBean> InjectContent;

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

            public List<?> getMethodExclude() {
                return methodExclude;
            }

            public void setMethodExclude(List<?> methodExclude) {
                this.methodExclude = methodExclude;
            }

            public List<InjectContentBean> getInjectContent() {
                return InjectContent;
            }

            public void setInjectContent(List<InjectContentBean> InjectContent) {
                this.InjectContent = InjectContent;
            }

            public static class InjectContentBean {
                /**
                 * InjectClass : com.xdja.monitor.AppMonitor
                 * InjectMethodName : monitorPageStart
                 * InjectMethodDesc : (Ljava/lang/String;)V
                 * InjectPosition :
                 */

                private String InjectClass;
                private String InjectMethodName;
                private String InjectMethodDesc;
                private String InjectPosition;

                public String getInjectClass() {
                    return InjectClass;
                }

                public void setInjectClass(String InjectClass) {
                    this.InjectClass = InjectClass;
                }

                public String getInjectMethodName() {
                    return InjectMethodName;
                }

                public void setInjectMethodName(String InjectMethodName) {
                    this.InjectMethodName = InjectMethodName;
                }

                public String getInjectMethodDesc() {
                    return InjectMethodDesc;
                }

                public void setInjectMethodDesc(String InjectMethodDesc) {
                    this.InjectMethodDesc = InjectMethodDesc;
                }

                public String getInjectPosition() {
                    return InjectPosition;
                }

                public void setInjectPosition(String InjectPosition) {
                    this.InjectPosition = InjectPosition;
                }
            }
        }

        public static class InjectFieldBean {
            /**
             * InjectMethodParams : []
             * InjectFieldName :
             * InjectFieldClassType :
             */

            private String InjectFieldName;
            private String InjectFieldClassType;
            private List<?> InjectMethodParams;

            public String getInjectFieldName() {
                return InjectFieldName;
            }

            public void setInjectFieldName(String InjectFieldName) {
                this.InjectFieldName = InjectFieldName;
            }

            public String getInjectFieldClassType() {
                return InjectFieldClassType;
            }

            public void setInjectFieldClassType(String InjectFieldClassType) {
                this.InjectFieldClassType = InjectFieldClassType;
            }

            public List<?> getInjectMethodParams() {
                return InjectMethodParams;
            }

            public void setInjectMethodParams(List<?> InjectMethodParams) {
                this.InjectMethodParams = InjectMethodParams;
            }
        }
    }
}
