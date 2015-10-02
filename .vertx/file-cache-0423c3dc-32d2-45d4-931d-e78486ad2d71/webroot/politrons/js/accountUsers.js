// DOM Ready =============================================================
$(document).ready(function () {
    accountUsers.initListener();
});

var accountUsers = (function () {

    var eb;
    var userListData = [];

    /**
     * Client event bus handlers to be called from the Server
     */
    function registerHandlers(eb) {
        eb.registerHandler("find.users.login.client", function (data) {
            loadTable(jQuery.parseJSON(data));
        });
        getAuthUser(eb);
    }


    function getAuthUser(eb) {
        eb.send("find.users.login.server", null, function (res) {
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
            eb.send("delete.user.server", $(this).attr('rel'));
        });
    }

// Functions =============================================================


    function loadTable(data) {
        userListData = data;
        var tableContent = '';
        $.each(data, function () {
            tableContent += '<tr>';
            tableContent += '<td>' + this.username + '</td>';
            tableContent += '<td>' + this.password + '</td>';
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
