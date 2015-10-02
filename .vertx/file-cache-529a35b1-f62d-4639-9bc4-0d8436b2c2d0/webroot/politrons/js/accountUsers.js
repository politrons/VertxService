// DOM Ready =============================================================
$(document).ready(function () {
    accountUsers.initListener();
});

var accountUsers = (function () {

    var chatEb;
    var userListData = [];

    /**
     * Client event bus handlers to be called from the Server
     */
    function registerHandlers(eb) {
        chatEb=eb;
        chatEb.registerHandler("find.users.login.client", function (data) {
            debugger;
            loadTable(jQuery.parseJSON(data));
        });
        getAuthUser(chatEb);
    }


    function getAuthUser(chatEb) {
        chatEb.send("find.users.login.server", null, function (res) {
            debugger;
            loadTable(res);
        });
    }

    /**
     * Event Listener of our elememnt pages
     */
    function initListener() {
        var busLinkDeleteUser = $(".busLinkDeleteUser");
        busLinkDeleteUser.unbind('click');
        busLinkDeleteUser.on('click', function () {
            chatEb.send("delete.user.login.server", $(this).attr('rel'));
        });
    }

// Functions =============================================================


    function loadTable(data) {
        userListData = data;
        var tableContent = '';
        $.each(data, function () {
            tableContent += '<tr>';
            tableContent += '<td>' + this.username + '</td>';
            tableContent += '<td><a href="#" class="busLinkDeleteUser" rel="' + this._id + '">del-eb</a></td>';
            tableContent += '</tr>';
        });
        $('#userList table tbody').html(tableContent);
        initListener();
    }


    return {
        registerHandlers:registerHandlers,
        initListener:initListener
    };
}());
