var getMetric = function(){
    $('#tbody_metric').empty();
    requestByJson({
        url:"admin/monitor/method-metric?orderType="+$('#orderType').val(),
        method:"GET"
    },{
        success:function(req,data){
            var result = data.data || {};
            for( var i in result){
                var obj = result[i];
                var tplSysApiTr = $('#tpl_metric_tr').text();
                tplSysApiTr = tplSysApiTr.replaceAll("{{sequence}}",i);
                tplSysApiTr = tplSysApiTr.replaceAll("{{refDate}}",obj.refDate);
                tplSysApiTr = tplSysApiTr.replaceAll("{{name}}",obj.name);
                tplSysApiTr = tplSysApiTr.replaceAll("{{cntRequest}}",obj.cntRequest);
                tplSysApiTr = tplSysApiTr.replaceAll("{{cntPassRequest}}",obj.cntPassRequest);
                tplSysApiTr = tplSysApiTr.replaceAll("{{cntSuccessRequest}}",obj.cntSuccessRequest);
                tplSysApiTr = tplSysApiTr.replaceAll("{{cntExceptionRequest}}",obj.cntExceptionRequest);
                tplSysApiTr = tplSysApiTr.replaceAll("{{cntBlockRequest}}",obj.cntBlockRequest);
                var tardiness = obj.tardiness /1000 ;
                if(tardiness >3 ){
                    tardiness = '<button type="button" class="am-btn am-radius am-btn-warning am-btn-xs">'+tardiness+'</button>';
                }else if(tardiness == 0){
                    tardiness = '<0.5s';
                }
                tplSysApiTr = tplSysApiTr.replaceAll("{{tardiness}}",tardiness);

                var avgRt = obj.avgRt /1000 ;
                avgRt = avgRt >3 ? '<button type="button" class="am-btn am-radius am-btn-warning am-btn-xs">'+avgRt+'</button>' : avgRt;
                tplSysApiTr = tplSysApiTr.replaceAll("{{avgRt}}",avgRt);
                $('#tbody_metric').append(tplSysApiTr);
            }
        },fail: function(req,data){
            alert("请求出错");
            return;
        }
    })
}
getMetric();

var manualControlMetric = function () {
    requestByJson({
        url:"admin/monitor/job",
        method:"GET"
    },{
        success:function(req,data){
            getMetric();
        },fail: function(req,data){
            alert("请求出错");
            return;
        }
    })
}