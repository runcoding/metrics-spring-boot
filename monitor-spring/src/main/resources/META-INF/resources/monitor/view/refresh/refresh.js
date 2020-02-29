var successHostStatus = ' <button type="button" class="am-btn-sm am-btn-success">更新成功</button>';
var failHostStatus    = ' <button type="button" class="am-btn-sm am-btn-warning">更新失败</button>';
var runStatus = false;

var refresh = function(){
    if(runStatus){
       alert("请求正在处理中……");
       return;
    }
    runStatus = true;
    $('#tbody_metric').empty();
     requestByJson({
        url:"admin/monitor/refresh",
        method:"PUT"
    },{
        success:function(req,res){
            runStatus = false;
            var data = res.data || {};
            for(var i in data){
                var dataInfo = data[i];
                var isSuccess = (i == 0);
                for( var hostPort in dataInfo){
                    var successKey = dataInfo[hostPort];
                        successKey = successKey && successKey.length == 0 ? "无更新" :"<textarea rows='3' cols='50'>"+JSON.stringify(successKey)+"</textarea>";
                    var tplSysApiTr = $('#tpl_metric_tr').text();
                    tplSysApiTr = tplSysApiTr.replaceAll("{{hostPort}}",hostPort);
                    tplSysApiTr = tplSysApiTr.replaceAll("{{hostStatus}}",isSuccess?successHostStatus:failHostStatus);

                    tplSysApiTr = tplSysApiTr.replaceAll("{{changeKey}}",successKey);
                    $('#tbody_metric').append(tplSysApiTr);
                }
            }
        },fail: function(req,data){
            runStatus = false;
            alert("请求出错");
            return;
        }
    })
}