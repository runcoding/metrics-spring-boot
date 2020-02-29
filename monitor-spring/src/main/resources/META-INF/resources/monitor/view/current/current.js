/**系统请求信息*/
var globalInfo = {
    dataCount        :  0 ,
    isDumpAllThread  : false,
    autoGetSysStatus : true,
    selectServerIpOption     : {},
    selectMethodOption       : {},
}

/**视图颜色配置*/
window.chartColors = {red: 'rgb(255, 99, 132)', orange: 'rgb(255, 159, 64)', yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)', blue: 'rgb(54, 162, 235)', purple: 'rgb(153, 102, 255)', grey: 'rgb(201, 203, 207)'};

var panelColor = {
   "NEW":"",
   "RUNNABLE":"am-panel-success",
   "BLOCKED":"am-panel-primary",
   "WAITING":"am-panel-secondary",
   "TIMED_WAITING":"am-panel-warning",
   "TERMINATED":"am-panel-danger"
}
/**自动刷新按钮控制*/
var autoGetSysInfo = function () {
    globalInfo.autoGetSysStatus = !globalInfo.autoGetSysStatus;
    $('.autoGetSysInfo').toggleClass('am-btn-default');
    $('.autoGetSysInfo').toggleClass('am-btn-success');
}

/**操作项-服务ip*/
var selectPrependServerIp = function (ip) {
    if(globalInfo.selectServerIpOption[ip]){return;}
    $("#select_server_ip").prepend("<option value='"+ip+"'>内网("+ip+")</option>");
    globalInfo.selectServerIpOption[ip] = ip;
}

/**操作项-方法名称*/
var selectPrependMethodName = function (metrics) {
    var  methodNames = metrics.methodNames;
    if(!methodNames){return;}
    for( var key in methodNames){
        var  methodName = methodNames[key];
        if(!globalInfo.selectMethodOption[methodName]){
            $("#select_sentinel_monitor").prepend("<option value='"+methodName+"'>"+methodName+"</option>");
            globalInfo.selectMethodOption[methodName] = methodName;
        }
    }
    $("#select_sentinel_monitor").val(metrics.proposalMethodName);
}

/***/
$("#select_sentinel_monitor").change(function(){
   var sentinelMethodName = $("#select_sentinel_monitor").val();
   if(sentinelMethodName == 'all'){
       sentinelMethodName = '';
   }
   window.location.href = baseDomain+"monitor/index.html?module=current/current&sentinelMethodName="+sentinelMethodName
})

/**是否显示全部线程状态*/
var dumpAllThreads = function () {
    if(!globalInfo.isDumpAllThread){if(!window.confirm("开启DumpAllThread,获取线程状态将影响服务运行性能")){return;}}
    globalInfo.isDumpAllThread = !globalInfo.isDumpAllThread;
    $('.dumpAllThreads').toggleClass('am-btn-default');
    $('.dumpAllThreads').toggleClass('am-btn-success');
}

/**控制视图显示*/
var toggleCanvasShow = function (target,t) {
    $('#'+target).toggleClass('am-hide');
    $(t).toggleClass('am-btn-default');
    $(t).toggleClass('am-btn-success');
}

/**获取随机颜色*/
var randomColor = function(){return '#'+('00000'+(Math.random()*0x1000000<<0).toString(16)).slice(-6);}

/**排序*/
var quickSort = function(arr) {
    if (arr.length <= 1) { return arr; }
    var pivotIndex = Math.floor(arr.length / 2);
    var pivot = arr.splice(pivotIndex, 1)[0];
    var left = [];
    var right = [];
    for (var i = 0; i < arr.length; i++){
        if (arr[i] > pivot) {
            left.push(arr[i]);
        } else {
            right.push(arr[i]);
        }
    }
    return quickSort(left).concat([pivot], quickSort(right));
}

/**canvas配置*/
var canvasConfig = function (config) {
    return {
        type: 'line',
        data: {
            labels: [],
            datasets: [],
            datasetIndexs:{}
        },
        options: {
            onClick: function(e){
                //console.log(this);
            },
            responsive: true,
            legend: {
                position: 'bottom',
            },
            title:{
                display:true,
                text: config.title || '运行情况'
            },
            tooltips: {
                mode: 'index',
                intersect: false,
            },
            hover: {
                mode: 'nearest',
                intersect: true
            },
            scales: {
                xAxes: [{
                    display: true,
                    scaleLabel: {
                        display: true,
                        labelString: config.xAxeLabel || ''
                    }
                }],
                yAxes: [{
                    display: true,
                    scaleLabel: {
                        display: true,
                        labelString: config.yAxeLabel || ''
                    }
                }]
            }
        }
    };
}

/**创建或更新canvas图表*/
var canvasShowChart = function (canvasInfo) {
    var canvasConfig = canvasInfo.canvasConfig ;
    if(!canvasInfo.isLabeled){
        canvasConfig.data.labels.push(moment().format('HH:mm:ss'));
    }
    if(canvasConfig.canvasChart ){
        canvasConfig.canvasChart.update();
    }else {
        canvasConfig.canvasChart = new Chart(canvasInfo.canvasId, canvasConfig);
    }

}

/**设置canvas图表数据*/
var setCanvasData = function (canvasInfo,key,data,threshold) {
    var canvasConfig = canvasInfo.canvasConfig ;
    var canvasColors = canvasInfo.canvasColors;
    var headColor    = canvasInfo.canvasColors[key];

    if(!headColor ){
        if(data >= threshold ){
            if(Object.keys(canvasColors).length > 8  ){
                return ;
            }
            canvasColors[key] = randomColor();
        }else{
            return ;
        }
    }
    var dataset = {
        label: key,
        backgroundColor: canvasColors[key],
        borderColor: canvasColors[key],
        data: [ data],
        fill: false,
    }
    if(canvasConfig.data.datasetIndexs[key]){
        canvasConfig.data.datasets.forEach(function(dataset) {
            if(dataset.label == key){
                dataset.data.push(data);
            }
        });
    }else {
        canvasConfig.data.datasetIndexs[key] = true;
        if(globalInfo.dataCount >1){
            var arr = [];
            for(var i = 0 ; i< globalInfo.dataCount ; i++){
                arr[i]=0;
            }
            /**以前未显示的数据补充0*/
            Array.prototype.push.apply(arr, dataset.data);
            dataset.data = arr;
        }
        canvasConfig.data.datasets.push(dataset);
    }
}

/**-------------------  显示线程图--------------------*/
var threadCanvasInfo = {
    canvasId     : document.getElementById("canvas_thread").getContext("2d"),
    canvasConfig : canvasConfig({
        "title":'当前服务线程(个数top10)运行情况',
        "xAxeLabel":"",
        "yAxeLabel":'线程数(个)'
    }),
    canvasColors   : {}
}

var showThreadGroupChart = function(threadsInfo) {
    /**线程总数*/
    setCanvasData(threadCanvasInfo,"线程总数",threadsInfo.totalStartedThreadCount,-1);
    /**仍活动的线程总数*/
    setCanvasData(threadCanvasInfo,"活跃线程总数",threadsInfo.threadCount,-1);
    var threadGroupInfo = threadsInfo.threadGroupInfo;

    var arr = new Array(threadGroupInfo.length)
    var i=0;
    for( var key in threadGroupInfo){
        arr[i] = threadGroupInfo[key];
        i++;
    }
    /**排序，取最高10条*/
    arr = quickSort(arr);
    var threshold =  arr.length <=5 ? arr.length : arr[4].length;
    for( var key in threadGroupInfo){
        setCanvasData(threadCanvasInfo,key,threadGroupInfo[key].length,threshold);
    }
    canvasShowChart(threadCanvasInfo);
};

/**------------------- 显示接口线程运行数量图--------------------*/

var methodThreadCanvasInfo = {
    canvasId     : document.getElementById("canvas_method_thread").getContext("2d"),
    canvasConfig : canvasConfig({
        "title":'实时方法线程运行总数',
        "xAxeLabel":"",
        "yAxeLabel":'方法正在被线程处理数(个)'
    }),
    canvasColors   : {}
}

var showMethodThreadRunningCntChart = function(methodThreadMetrics) {
    var arr = new Array(methodThreadMetrics.length)
    var i=0;
    for( var key in methodThreadMetrics){
        arr[i] = methodThreadMetrics[key];
        i++;
    }
    /**排序，取最高10条*/
    arr = quickSort(arr);
    var threshold =  arr.length <=6 ? -1 : arr[5];
    for( var key in methodThreadMetrics){
        setCanvasData(methodThreadCanvasInfo,key,methodThreadMetrics[key],threshold);
    }
    canvasShowChart(methodThreadCanvasInfo);
};

/**------------------ 方法被哨兵监控指标图-----------*/
var methodThreadSentinelCanvasInfo = {
    canvasId     : document.getElementById('canvas_method_metric').getContext("2d"),
    canvasConfig   : canvasConfig({
        "title":'实时方法监控指标',
        "xAxeLabel":"",
        "yAxeLabel":'监控指标(个)'
    }),
    canvasColors   : {},
    isLabeled      : true
}
var list = [];
var showMethodMetricChart = function(metrics) {

    var proposalMetricList = metrics.proposalMetricList;
    for( var key in proposalMetricList){
        var proposalMetric = proposalMetricList[key];
        if(list[proposalMetric.refDate]){
           continue;
        }
        list[proposalMetric.refDate]=true;
        setCanvasData(methodThreadSentinelCanvasInfo,"请求数",proposalMetric.cntRequest,-1);
        setCanvasData(methodThreadSentinelCanvasInfo,"通过数",proposalMetric.cntPassRequest,-1);
        setCanvasData(methodThreadSentinelCanvasInfo,"成功数",proposalMetric.cntSuccessRequest,-1);
        setCanvasData(methodThreadSentinelCanvasInfo,"异常数",proposalMetric.cntExceptionRequest,-1);
        setCanvasData(methodThreadSentinelCanvasInfo,"阻塞数",proposalMetric.cntBlockRequest,-1);
        setCanvasData(methodThreadSentinelCanvasInfo,"RT(秒)",proposalMetric.avgRt/1000,-1);
        methodThreadSentinelCanvasInfo.canvasConfig.data.labels.push(moment(proposalMetric.refDate).format('HH:mm:ss'));
    }
    canvasShowChart(methodThreadSentinelCanvasInfo);
    /**切换显示哨兵监控方法*/
    selectPrependMethodName(metrics);
};

/**------------------- 显示内存信息--------------------*/

var memoryCanvasInfo = {
    canvasId     : document.getElementById("canvas_memory").getContext("2d"),
    canvasConfig : canvasConfig({
        "title":'当前服务内存运行情况',
        "xAxeLabel":"",
        "yAxeLabel":'已使用内存数(M)'
    }),
    canvasColors   : {}
}

var showMemoryChart = function(headsInfo) {
    for( var key in headsInfo){
        setCanvasData(memoryCanvasInfo,headsInfo[key].name,headsInfo[key].used,-1);
    }
    canvasShowChart(memoryCanvasInfo);
};

/**-------------------end 显示内存信息--------------------*/

/**-------------------start 显示系统信息--------------------*/
var showSysInfo = function (data) {
    var  sysData   = data.operatingSystemInfo || {};
    var memoryData = data.containerMemoryInfo || {};
    var threadData = data.containerThreadInfo || {};

    var tmp = sysTmpInfo("系统信息",sysData.name+"("+sysData.arch+sysData.version+",核数"+sysData.availableProcessors+")");
    tmp += sysTmpInfo("系统ID",sysData.runName+"<br>[ip:"+sysData.ip+" & pid="+sysData.runPid+"]");
    tmp += sysTmpInfo("近一分钟系统平均负载",sysData.systemLoadAverage);
  /*  tmp += sysTmpInfo("总物理内存",sysData.totalPhysicalMemory+"(M)");
    tmp += sysTmpInfo("已用物理内存",sysData.usedPhysicalMemorySize+"(M)");
    tmp += sysTmpInfo("剩余物理内存",sysData.freePhysicalMemory+"(M)");
    tmp += sysTmpInfo("总交换空间",sysData.totalSwapSpaceSize+"(M)");
    tmp += sysTmpInfo("已用交换空间",sysData.usedSwapSpaceSize+"(M)");
    tmp += sysTmpInfo("剩余交换空间",sysData.freeSwapSpaceSize+"(M)");*/

    tmp += sysTmpInfo("仍活跃线程数",threadData.threadCount);
    tmp += sysTmpInfo("线程峰值",threadData.peakThreadCount);
    if(Object.keys(threadData.threadGroupInfo).length > 0){
        tmp += sysTmpInfo("线程状态",sysThreadState(threadData.threadGroupInfo));
    }

    /**GC 信息*/
    if(data.garbageCollectorInfoList){
         data.garbageCollectorInfoList.forEach(function (gc) {
             tmp += sysTmpInfo("GC",gc.gcName+",总次数"+gc.gcCount+",总时间"+gc.gcTime/1000+"(秒)")
         });
    }

    var memoryHtml = "";
    for(var i in memoryData){
        var memory = memoryData[i];
        memoryHtml += "<tr>";
        memoryHtml += memoryTmpInfo(memory.name);
        memoryHtml += memoryTmpInfo(memory.init);
        memoryHtml += memoryTmpInfo(memory.max);
        memoryHtml += memoryTmpInfo(memory.used);
        memoryHtml += memoryTmpInfo(memory.committed);
        memoryHtml += memoryTmpInfo(memory.usedRate);
        memoryHtml += "</tr>";
    }
    if(sysData.totalMemory){
        memoryHtml += "<tr>";
        memoryHtml += memoryTmpInfo("虚拟机内存");
        memoryHtml += memoryTmpInfo(sysData.totalMemory);
        memoryHtml += memoryTmpInfo(sysData.maxMemory);
        memoryHtml += memoryTmpInfo(sysData.freeMemory);
        memoryHtml += memoryTmpInfo(sysData.usedMemory+"(used M)");
        memoryHtml += memoryTmpInfo(sysData.availableMemory+"(available M)");
        memoryHtml += "</tr>";
    }
    $('#tbody_memory_info').html(memoryHtml);

    $('#tbody_sys_info').html(tmp);
}

var sysTmpInfo = function (name,info) {
    return"<tr><td>"+name+":"+info+"</td></tr>";
}

var memoryTmpInfo = function (info) {
    return"<td>"+info+"</td>";
}

var sysThreadState = function (threadStates) {
    var threadStateMap = {};
    for( var key in threadStates){
        for (var i =0 ; i< threadStates[key].length ; i++) {
            var threadStateInfo = threadStates[key][i];
            var tState = threadStateMap[threadStateInfo.threadState] || [];
            tState.push(threadStateInfo.threadName+"("+threadStateInfo.threadId+")");
            threadStateMap[threadStateInfo.threadState] = tState;
        };
    }

    var threadStatePanel = "";
    for (var threadStateName in threadStateMap) {
        var threadPanel = '';
        for (var threadStateNum in threadStateMap[threadStateName]){
            threadPanel += threadStateMap[threadStateName][threadStateNum]+'<br>';
        }
        threadStatePanel += '<div class="am-panel '+panelColor[threadStateName]+'">'+
                            '<div class="am-panel-hd">'+threadStateName+ '</div><div class="am-panel-bd">'+threadPanel+'</div></div>';
    }
    return threadStatePanel;
}

/**-------------------end 显示系统信息--------------------*/


/**获取当前系统运行指标*/
var getCurrentMetricsInfo = function () {
    if(!globalInfo.autoGetSysStatus){return;}
    globalInfo.autoGetSysStatus = false;
    requestByJson({
        url: "admin/monitor/current_metrics?isDumpAllThread="+
        globalInfo.isDumpAllThread+"&proposalMethodName="+(getQueryParam().sentinelMethodName || '')
        ,method:"GET" },{
        success:function(req,data){
            globalInfo.dataCount = globalInfo.dataCount+1;
            var result = data.data || {};
            var ip  = result.operatingSystemInfo.ip;
            if( $("#select_server_ip").val() == ip || $("#select_server_ip").val() == "all"  ){
                /**显示sentinel哨兵方法监控指标信息*/
                showMethodMetricChart(result.methodMetricInfo);
                /**显示接口正执行线程次数*/
                result.methodThreadRunningCnt["近一分钟系统负载"]= result.operatingSystemInfo.systemLoadAverage || 0;
                showMethodThreadRunningCntChart(result.methodThreadRunningCnt);
                /**显示线程*/
                showThreadGroupChart(result.containerThreadInfo);
                /**显示内存信息*/
                showMemoryChart(result.containerMemoryInfo);
                /**显示系统信息*/
                showSysInfo(result);
                /**切换显示ip*/
                selectPrependServerIp(result.operatingSystemInfo.ip);
            }
            globalInfo.autoGetSysStatus = true;
        },fail: function(req,data){
            globalInfo.autoGetSysStatus = true;
            console.error("请求出错:"+data);
            return;
        }
    })
}
getCurrentMetricsInfo();
window.setInterval(getCurrentMetricsInfo,3000);

