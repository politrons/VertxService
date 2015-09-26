// DOM Ready =============================================================
$(document).ready(function () {
    index.populateTable();
    index.initEventBus();
    index.initListetener();
});

var index = (function () {


    var eb;
    var userListData = [];

    function getEventBus() {
        return eb;
    }

    function initEventBus() {
        eb = new vertx.EventBus("/eventbus/");
        eb.onopen = function () {
            registerHandlers();
            geolocation.registerHandlers(eb);

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


    function initListetener() {
        $(".linkDeleteUser").on('click', function () {
            deleteUser($(this));
        });
        $("#searchByButton").on('click', function () {
            var selectedOption = $('#searchBy').find(":selected").val();
            searchBy(selectedOption, $("#inputSearchBy").val());
        });

        $("#addUserButtonId").on('click', function () {
            addUser();
        });
        var busAddUserButtonId =$("#busAddUserButtonId");
        busAddUserButtonId.unbind('click');
        busAddUserButtonId.on('click', function () {
            debugger;
            var newUser = getUserData();
            eb.send("add.user.server", newUser);
        });
        var busSearchByButton = $("#busSearchByButton");
        busSearchByButton.unbind('click');
        busSearchByButton.on('click', function () {
            debugger;
            eb.send("find.user.server", findUserData());
        });
        var busLinkDeleteUser = $(".busLinkDeleteUser");
        busLinkDeleteUser.unbind('click');
        busLinkDeleteUser.on('click', function () {
            eb.send("delete.user.server", $(this).attr('rel'));
        });ç
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
                "latitude": "",
                "longitude": ""
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
    }

    function searchBy(attributeName, value) {
        $.getJSON('/user/' + attributeName + "/" + value, function (data) {
            setUserInformation(data);
        });
    }

    function populateTable() {
        $.getJSON('/users', function (data) {
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
        initListetener();
    }

// Show User Info
    function showUserInfo(element) {
        var thisUserName = element.attr('rel');
        // Get Index of object based on id value
        var arrayPosition = userListData.map(function (arrayItem) {
            return arrayItem.username;
        }).indexOf(thisUserName);
        // Get our User Object
        var thisUserObject = userListData[arrayPosition];
        //Populate Info Box
        $('#inputUserName').val(thisUserObject.fullname);
        $('#inputUserEmail').val(thisUserObject.age);
        $('#inputUserFullname').val(thisUserObject.gender);
        $('#inputUserAge').val(thisUserObject.location);

    };

// Add User
    function addUser() {
        // Super basic validation - increase errorCount variable if any fields are blank
        var errorCount = 0;
        $('#addUser input').each(function (index, val) {
            if ($(this).val() === '') {
                errorCount++;
            }
        });
        // Check and make sure errorCount's still at zero
        if (errorCount === 0) {

            // Use AJAX to post the object to our adduser service
            $.ajax({
                type: 'POST',
                data: getUserData(),
                url: '/users'
            }).done(function (response) {
                // Clear the form inputs
                $('#addUser fieldset input').val('');

                // Update the table
                populateTable();
            }).fail(function () {
                // If something goes wrong, alert the error message that our service returned
                alert('Error: Something went wrong.');
            });
        }
        else {
            // If errorCount is more than 0, error out
            alert('Please fill in all fields');
            return false;
        }
    };

    function deleteUser(element) {
        if (confirm('Are you sure you want to delete this user?') === true) {
            $.ajax({
                type: 'DELETE',
                url: '/users/' + element.attr('rel')
            }).done(function (response) {
                // Update the table
                populateTable();
            }).fail(function () {
                alert('Error: Something went wrong.');
                // Update the table
                populateTable();
            });
        }
    };

    return {
        populateTable: populateTable,
        initEventBus: initEventBus,
        initListetener: initListetener,
        getEventBus: getEventBus,
        getUserData: getUserData
    };
}());
