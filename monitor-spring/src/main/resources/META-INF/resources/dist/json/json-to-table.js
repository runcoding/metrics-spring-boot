var json2table = function (json, classes) {
    var cols = Object.keys(json[0]);

    var headerRow = '';
    var bodyRows = '';

    classes = classes || '';

    function capitalizeFirstLetter(string) {
        return string.charAt(0).toUpperCase() + string.slice(1);
    }

    cols.map(function(col) {
        headerRow += '<th>' + capitalizeFirstLetter(col) + '</th>';
    });

    json.map(function(row) {
        bodyRows += '<tr>';

        cols.map(function(colName) {
            var rowInfo ;
            var rowVal =  row[colName];
            /**值是数组*/
            if( rowVal instanceof Array){
                rowInfo = "<ul>";
                rowVal.forEach(function(innerVal,index,array){
                    var innerRowInfo;
                    if (innerVal instanceof Object){ 
                       for(innerKey in innerVal){  
                            innerRowInfo = "<label>"+innerKey+"</label>: <span>"+innerVal[innerKey]+"</span>";
                        }      
                    } 
                    rowInfo += "<li>"+(innerRowInfo || innerVal)+"</li>";
                });
                rowInfo += "</ul>";
            } 
            rowInfo = (rowInfo   || row[colName]) || '';
            bodyRows += '<td>' + rowInfo + '</td>';
        })

        bodyRows += '</tr>';
    });

    return '<table class="' + classes + '">' +
                '<thead>' +'<tr>' + marked(headerRow) + '</tr>' + '</thead>' +
                '<tbody>' + marked(bodyRows)+ '</tbody>' +
            '</table>';
}