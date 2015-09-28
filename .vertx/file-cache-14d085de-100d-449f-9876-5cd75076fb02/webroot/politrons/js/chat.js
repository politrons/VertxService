// DOM Ready =============================================================
$(document).ready(function () {
    index.initEventBus();
    index.initListetener();
});

var chat = (function () {

    var eb;

    function initEventBus() {
        debugger;
        eb = index.getEventBus();
        eb.onopen = function () {
            debugger;
            registerHandlers();
        };
    }

    function registerHandlers() {
        eb.registerHandler("chat.user.client", function (data) {
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
            eb.send("find.user.server", chatMessage);
        });

    }

// Functions =============================================================


    return {
        initEventBus: initEventBus,
        initListetener: initListetener,
    };
}());
