var getCpuTop = function(){
    $('#tbody_metric').empty();
    requestByJson({
        url:"admin/monitor/thread/cpu/top",
        method:"GET"
    },{
        success:function(req,data){
            var result = data.data || {};
            for( var i in result){
                var obj = result[i];
                var tplSysApiTr = $('#tpl_metric_tr').text();
                tplSysApiTr = tplSysApiTr.replaceAll("{{cpu}}",i);
                tplSysApiTr = tplSysApiTr.replaceAll("{{threadStack}}",obj);
                $('#tbody_metric').append(tplSysApiTr);
            }
        },fail: function(req,data){
            alert("请求出错");
            return;
        }
    })
}
getCpuTop();
