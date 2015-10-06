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
        updateUserPageStatus(ebChat);
    }

    function updateUserPageStatus(ebChat) {
        var pageMessage = {
            'username': $("#userLogged").text(),
            'page': "chat"
        };
        ebChat.send("update.user.page.server", pageMessage, function (data) {
            debugger;
            findUsersPerPage(ebChat);
        });
    }

    function findUsersPerPage(ebChat) {
        ebChat.send("find.user.page.server", 'chat', function (data) {
            debugger;
        });
    }

    function addChatMessage(data) {
        $("#chatTextArea").append("");
        $("#chatTextArea").append("\n" + data.username + ":" + data.message);
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
        initListener: initListener,
        updateUserPageStatus: updateUserPageStatus
    };
}());