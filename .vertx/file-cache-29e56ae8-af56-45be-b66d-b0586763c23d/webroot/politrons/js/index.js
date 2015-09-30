// DOM Ready =============================================================
$(document).ready(function () {
    index.populateTable();
    index.initEventBus();
    index.initListener();
    $("#redEvent").hide();
    $("#greenEvent").hide();
});

var index = (function () {

    var eb;
    var userListData = [];

    function getEventBus() {
        return eb;
    }

    function initEventBus() {
        eb = new vertx.EventBus("/politrons/eventbus/");
        eb.onopen = function () {
            registerHandlers();
            if (typeof geolocation !== "undefined") {
                geolocation.registerHandlers(eb);
            }
            if (typeof chat !== "undefined") {
                chat.registerHandlers(eb);
            }
        };
        eb.onclose = function () {
            eb = null;
            $("#redEvent").show();
            console.log("event bus socket closed")
        };
    }

    function registerHandlers() {
        eb.registerHandler("find.user.client", function (data) {
            var status = jQuery.parseJSON(data).status;
            if (status === 1) {
                setUserInformation(jQuery.parseJSON(data))
            } else {
                alert("User not found");
            }
        });
        eb.registerHandler("find.users.client", function (data) {
            loadTable(jQuery.parseJSON(data));
        });
        eb.registerHandler("delete.user.client", function (data) {
            var status = jQuery.parseJSON(data).status;
            if (status === 1) {
                eb.send("find.users.server", null);
            }
        });
        eb.registerHandler("add.user.client", function (data) {
            var status = jQuery.parseJSON(data).status;
            if (status === 1) {
                eb.send("find.users.server", null);
            }
        });
        eb.registerHandler("update.user.client", function (data) {
            var status = jQuery.parseJSON(data).status;
            if (status === 1) {
                eb.send("find.users.server", null);
            }
        });
    }


    function initListener() {
        var linkDeleteUser = $(".linkDeleteUser");
        linkDeleteUser.unbind('click');
        linkDeleteUser.on('click', function () {
            deleteUser($(this));
        });
        var searchByButton = $("#searchByButton");
        searchByButton.unbind('click');
        searchByButton.on('click', function () {
            var selectedOption = $('#searchBy').find(":selected").val();
            searchBy(selectedOption, $("#inputSearchBy").val());
        });

        var addUserButtonId = $("#addUserButtonId");
        addUserButtonId.unbind('click');
        addUserButtonId.on('click', function () {
            addUser();
        });
        var busAddUserButtonId = $("#busAddUserButtonId");
        busAddUserButtonId.unbind('click');
        busAddUserButtonId.on('click', function () {
            var newUser = getUserData();
            eb.send("add.user.server", newUser);
        });
        var busSearchByButton = $("#busSearchByButton");
        busSearchByButton.unbind('click');
        busSearchByButton.on('click', function () {
            eb.send("find.user.server", findUserData(), function (res) {
                debugger;
                console.log(res);
            });

        });
        var busLinkDeleteUser = $(".busLinkDeleteUser");
        busLinkDeleteUser.unbind('click');
        busLinkDeleteUser.on('click', function () {
            eb.send("delete.user.server", $(this).attr('rel'));
        });
        var busUpdateUserButtonId = $("#busUpdateUserButtonId");
        busUpdateUserButtonId.unbind('click');
        busUpdateUserButtonId.on('click', function () {
            var newUser = getUserData();
            newUser._id = $('#inputUserId').val();
            eb.send("update.user.server", newUser);
        });
    }

// Functions =============================================================

    function findUserData() {
        var findUser = {
            'searchBy': $('#searchBy').find(":selected").val(),
            'inputValue': $("#inputSearchBy").val()
        };
        return findUser;
    }

    function getUserData() {
        var newUser = {
            'username': $('#inputUserName').val(),
            'email': $('#inputUserEmail').val(),
            'fullname': $('#inputUserFullname').val(),
            'age': $('#inputUserAge').val(),
            'location': $('#inputUserLocation').val(),
            'gender': $('#inputUserGender').val(),
            'position': {
                "latitude": $('#inputUserLatitude').val(),
                "longitude": $('#inputUserLongitude').val()
            }
        };
        return newUser;
    }

    function setUserInformation(data) {
        $('#inputUserId').val(data._id);
        $('#inputUserName').val(data.username);
        $('#inputUserEmail').val(data.email);
        $('#inputUserFullname').val(data.fullname);
        $('#inputUserAge').val(data.age);
        $('#inputUserGender').val(data.gender);
        $('#inputUserLocation').val(data.location);
        $('#inputUserLatitude').val(data.position.latitude);
        $('#inputUserLongitude').val(data.position.longitude);
    }

    function searchBy(attributeName, value) {
        $.getJSON('/politrons/user/' + attributeName + "/" + value, function (data) {
            setUserInformation(data);
        });
    }

    function populateTable() {
        $.getJSON('/politrons/users', function (data) {
            loadTable(data);
        });
    };

    function loadTable(data) {
        userListData = data;
        var tableContent = '';
        $.each(data, function () {
            tableContent += '<tr>';
            tableContent += '<td>' + this.username + '</td>';
            tableContent += '<td>' + this.email + '</td>';
            tableContent += '<td><a href="#" class="linkDeleteUser" rel="' + this._id + '">delete</a></td>';
            tableContent += '<td><a href="#" class="busLinkDeleteUser" rel="' + this._id + '">delete by bus</a></td>';
            tableContent += '</tr>';
        });
        $('#userList table tbody').html(tableContent);
        initListener();
    }

// Add User
    function addUser() {
        $.ajax({
            type: 'POST',
            data: getUserData(),
            url: '/politrons/users'
        }).done(function (response) {
            $('#addUser fieldset input').val('');
            // Update the table
            populateTable();
        }).fail(function () {
            alert("You dont have permissions to write");
        });
    };

    function deleteUser(element) {
        if (confirm('Are you sure you want to delete this user?') === true) {
            $.ajax({
                type: 'DELETE',
                url: '/politrons/users/' + element.attr('rel')
            }).done(function (response) {
                populateTable();
            }).fail(function () {
                alert("You dont have permissions to delete");
            });
        }
    };

    return {
        populateTable: populateTable,
        initEventBus: initEventBus,
        initListener: initListener,
        getEventBus: getEventBus,
        getUserData: getUserData
    };
}());
