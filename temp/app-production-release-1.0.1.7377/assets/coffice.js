!
function(win){
    "use strict";
    var callback_success = {};
    var callback_fail = {};
    var callback_event = {};

    var addEventListener = function(event, callback) {
        callNative({
            method:"addEventListener",
            params:event,
            success:function(data) {
                callback_event[event] = function(result) {
                    callback(result);
                }
                console.log(callback_event[event]);
            },
            error:function(error) {
//                alert(error.message);
            }
        });
    }

    //调用android本地方法
    var callNative = function(param) {

        var timestamp = new Date().getTime();
        var successCallback = "callback_success_" + timestamp;
        var failCallback =  "callback_fail_" + timestamp;
        var tempFuncSuccess, tempFuncFail;

        if(param.success) {
            tempFuncSuccess = param.success;
            param.success = successCallback;
            callback_success[successCallback] = function(result) {
                tempFuncSuccess(result);
                delete callback_fail[failCallback];
                delete callback_success[successCallback];
            }
        }

        if(param.error) {
            tempFuncFail = param.error;
            param.error = failCallback;
            callback_fail[failCallback] = function(result) {
                tempFuncFail(result);
                delete callback_success[successCallback];
                delete callback_fail[failCallback];
            }
        }

        if(win.cofficeBridge) {
            cofficeBridge.callNative(JSON.stringify(param));
        }
    }

    //android本地回调js的方法
    var callBackJs = function(result) {
        console.log(result, result);
        var resultObj = JSON.parse(result);
        if(resultObj.type === 1) {
            //事件
            if(resultObj.callback && callback_event[resultObj.callback]) {
                callback_event[resultObj.callback](resultObj.data);
            }
        } else if(resultObj.type === 2) {
            //成功
            if(resultObj.callback && callback_success[resultObj.callback]) {
                //此处应该只给data就行
                callback_success[resultObj.callback](resultObj.data);
            }
        } else {
            if(resultObj.callback && callback_fail[resultObj.callback]) {
                callback_fail[resultObj.callback](resultObj);
            } else {
                console.log("此页面未提供error回调方法，结果是：" + result, result);
//                alert("执行错误，code=" + resultObj.code + ", message=\"" + resultObj.message + "\"");
            }
        }
    }

    var Coffice = win.Coffice = {
        addEventListener:addEventListener,
        callNative:callNative,
        callBackJs:callBackJs
    }

}(this);