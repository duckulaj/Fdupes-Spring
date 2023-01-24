var stompClient = null;

$(document).ready(function()
{
    $('#btnDownload').click(function () {
    	
    	  $.get("/");
    });
    
    $('#btnInterrupt').click(function () {
    	
      var downloadName = $('#name').val();
      $.get("/interrupt/"+downloadName);
    });
    
    $('#btnPause').click(function () {
    	
        var downloadName = $('#name').val();
        $.get("/pause/"+downloadName);
      });
    
    

    connect();
    //setTimeout(updateProgress, 5);
});

function connect(){
    var socket = new SockJS('/mystatus');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame){
    	stompClient.debug = function(str) {};
        stompClient.subscribe('/app/initial', function (messageOutput){
            console.log("INITIAL: "+messageOutput);
            var progressList = $.parseJSON(messageOutput.body);
            $.each(progressList,function(index, element){
                update(element);
            });
        });

        stompClient.subscribe('/topic/status', function(messageOutput) {
            console.log("New Message in connect function: "+messageOutput);
            var messageObject = $.parseJSON(messageOutput.body);
            update(messageObject);
        });


    });
}


function update(newMessage){

	console.log("Message in update function: "+newMessage);

    var rows = $('#tableBody').find('#'+newMessage.jobName);
    
    console.log(rows.length);
    
    if(rows.length === 0)
    {
        $('#tableBody').append('<tr id="'+newMessage.jobName+'">' +
        	'<td>Job Name</td>' +
        	'<td>' + newMessage.jobName + '</td>' +
        	'</tr>' +
        	'<tr>' +
        	'<td>Search Directory</td>' +
            '<td>'+newMessage.directoryToSearch+'</td>' +
            '</tr>' +
            '<tr>' +
            '<td>Directories</td>' +
            '<td>'+newMessage.directoryCount+'</td>' +
            '</tr>' +
            '<tr>' +
            '<td>Files</td>' +
            '<td>'+newMessage.fileCount+'</td>' +
            '</tr>' +
            '<tr>' +
            '<td>Duplicates By Size</td>' +
            '<td>'+newMessage.duplicatesBySizeCount+'</td>' +
            '</tr>'
             +
            '<tr>' +
            '<td>Duplicates By MD5</td>' +
            '<td>'+newMessage.duplicatesByMD5Count+'</td>' +
            '</tr>' +
            '<tr>' +
            '<td>Duplicates By Byte</td>' +
            '<td>'+newMessage.duplicatesByByteCount+'</td>' +
            '</tr>' +
            '<tr>' +
            '<td>Duplicates Total Byte</td>' +
            '<td>'+newMessage.duplicatesTotalSize+'</td>' +
            '</tr>');
    }
    
}