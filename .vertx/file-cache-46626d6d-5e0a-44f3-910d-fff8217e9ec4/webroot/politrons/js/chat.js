// DOM Ready =============================================================
$(document).ready(function () {
    index.initEventBus();
    index.initListetener();
});

var chat = (function () {

    var ebChat;

    function initEventBus() {
        debugger;
        ebChat = index.getEventBus();
        ebChat.onopen = function () {
            debugger;
            registerHandlers();
        };
    }

    function registerHandlers() {
        ebChat.registerHandler("chat.user.client", function (data) {
            addChatMessage(jQuery.parseJSON(data))
        });

    }

    function  addChatMessage(data) {
        $("#chatTextArea").append(data.username + ":" + data.message)
    }


    function initListetener() {
        var sendText = $("#sendText");
        sendText.unbind('click');
        sendText.on('click', function () {
            var chatMessage = {
                'username': index.getUserData().username,
                'message': $("#inputChat").val()
            };
            ebChat.send("find.user.server", chatMessage);
        });

    }

// Functions =============================================================


    return {
        initEventBus: initEventBus,
        initListetener: initListetener,
    };
}());