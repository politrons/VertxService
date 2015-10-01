// DOM Ready =============================================================
$(document).ready(function () {
    chat.initListener();
});

var chat = (function () {

    var ebChat;

    function registerHandlers(eb) {
        ebChat = eb;
        ebChat.registerHandler("chat.user.client", function (data) {
            addChatMessage(data);
        });

    }

    function addChatMessage(data) {
        $("#chatTextArea").append("");
        $("#chatTextArea").append("\n" + data.username + ":"+  data.message);
    }

    function initListener() {
        var sendText = $("#sendText");
        sendText.unbind('click');
        sendText.on('click', function () {
            var chatMessage = {
                'username': $("#userLogged").text(),
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
