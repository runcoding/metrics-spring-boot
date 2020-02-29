var jsonEditor = null;
var autoRule = true;
/**获取规则*/
var getSentinelRules = function(){
    var sentinelRule = getQueryParam().sentinelRule || 'flowRules';
    requestByJson({
        url:"admin/monitor/sentinel/rules",
        method:"GET"
    }, {
        success: function (req, data) {
            var json = [{}];
            var sentinelTitle = "";
            if(sentinelRule == "flowRules"){
                json = data.data.flowRules || [{}];
                sentinelTitle = "流控规则管理";
            }else if(sentinelRule == "degradeRules"){
                json = data.data.degradeRules || [{}];
                sentinelTitle = "降级规则管理";
            }else if(sentinelRule == "systemRules"){
                json = data.data.systemRules || [{}];
                sentinelTitle = "系统规则管理";
            }else if(sentinelRule == "authorityWhite"){
                json = data.data.authorityWhite || {};
                for (var key in json) {
                    if(json[key] == -1 ){
                        continue;
                    }
                    json[key] = moment(json[key]).format('YYYY-MM-DD HH:mm:ss');
                }
                sentinelTitle = "方法白名单管理";
            }else if(sentinelRule == "authorityBlock"){
                json = data.data.authorityBlock || {};
                for (var key in json) {
                    if(json[key] == -1 ){
                        continue;
                    }
                    json[key] = moment(json[key]).format('YYYY-MM-DD HH:mm:ss');
                }
                sentinelTitle = "方法黑名单管理";
            }
            autoRule = data.data.autoRule;
            changeAutoRule();
            $("#sentinel-title").text(sentinelTitle);
            jsonEditor = new JSONEditor(document.getElementById("jsonEditor"), {mode: 'code'},json);
            $('.jsoneditor-menu').remove();
        },
        fail: function(req,data){  alert("请求出错");  return;  }
})

}
getSentinelRules();

/**编辑规则*/
var updateSentinelRules = function(){
    var sentinelRule = getQueryParam().sentinelRule || 'flowRules';
    var body = {};
    if(sentinelRule == "flowRules"){
        body.flowRules =  jsonEditor.get();
    }else if(sentinelRule == "degradeRules"){
        body.degradeRules =  jsonEditor.get();
    }else if(sentinelRule == "systemRules"){
        body.systemRules =  jsonEditor.get();
    }else if(sentinelRule == "authorityWhite"){
        var json =  jsonEditor.get();
        for (var key in json) {
            if(json[key] == -1 ){
               continue;
            }
            json[key] = moment(json[key]).format("x");
        }
        body.authorityWhite = json;
    }else if(sentinelRule == "authorityBlock"){
        var json =  jsonEditor.get();
        for (var key in json) {
            if(json[key] == -1 ){
                continue;
            }
            json[key] = moment(json[key]).format("x");
        }
        body.authorityBlock = json;
    }
    requestByJson({
        url:"admin/monitor/sentinel/rule?ruleType="+sentinelRule,
        method:"PUT",
        body: body
    }, {
        success: function (req, data) {
            if(data.status == 200 ){
                var show = "更新成功服务:"+data.data[0];
                if(data.data[1].length != 0){
                    show += ",更新失败服务:"+data.data[1];
                }
                alert(show);
            }else{
                alert("更新出错");
            }
        },
        fail: function(req,data){  alert("请求出错");  return;  }
    })
}



/**清除所有(系统、流控、降级)配置规则*/
var clearAllSentinelRules = function () {
    if(!window.confirm("是否清除所有(系统、流控、降级)配置规则")){return;}

    var body = {
        "flowRules":[],
        "degradeRules":[],
        "systemRules":[]
    };
    requestByJson({
        url:"admin/monitor/sentinel/rule?ruleType=all",
        method:"PUT",
        body: body
    }, {
        success: function (req, data) {
            if(data.status == 200 ){
                var show = "成功清除服务:"+data.data[0];
                if(data.data[1].length != 0){
                    show += ",失败清除服务:"+data.data[1];
                }
                alert(show);
            }else{
                alert("更新出错");
            }
        },
        fail: function(req,data){  alert("请求出错");  return;  }
    })
}

/**查看规则说明*/
var sentinelRulesWiki = function () {
    window.location.href = baseDomain + "monitor/index.html?#/monitor/wiki/monitor/sentinel_config";
}

var changeAutoRule = function () {
    if(autoRule){
        $('#autoRule').addClass('am-btn-success');
        $('#autoRule').removeClass('am-btn-default');
    }else{
        $('#autoRule').addClass('am-btn-default');
        $('#autoRule').removeClass('am-btn-success');
    }
}

var editAutoRule = function () {
    autoRule = autoRule == true ?  false : true;
    requestByJson({
        url:"admin/monitor/sentinel/rule?ruleType=autoRule",
        method:"PUT",
        body: {
            autoRule: autoRule
        }
    }, {
        success: function (req, data) {
            if(data.status == 200 ){
                var show = "成功编辑服务:"+data.data[0];
                if(data.data[1].length != 0){
                    show += ",失败编辑服务:"+data.data[1];
                }
                changeAutoRule();
                alert(show);
            }else{
                autoRule = autoRule & false;
                alert("更新出错");
            }
        },
        fail: function(req,data){
            autoRule = autoRule & false;
            alert("请求出错");
            return;
        }
    })
}