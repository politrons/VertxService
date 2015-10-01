// DOM Ready =============================================================
$(document).ready(function () {
    index.initListener();
});

var chat = (function () {

    var ebChat;

    function registerHandlers(eb) {
        ebChat = eb;
        ebChat.registerHandler("chat.user.client", function (data) {
            addChatMessage(jQuery.parseJSON(data))
        });

    }

    function  addChatMessage(data) {
        $("#chatTextArea").append(data.username + ":" + data.message)
    }

    function initListener() {
        var sendText = $("#sendText");
        sendText.unbind('click');
        sendText.on('click', function () {
            debugger;
            var chatMessage = {
                'username': index.getUserData().username,
                'message': $("#inputChat").val()
            };
            ebChat.send("chat.user.server", chatMessage);
        });

    }

// Functions =============================================================


    return {
        registerHandlers: registerHandlers,
        initListener: initListener
    };
}());