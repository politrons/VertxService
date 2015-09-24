// Userlist data array for filling in info box
var userListData = [];

// DOM Ready =============================================================
$(document).ready(function () {
    populateTable();
    initEventBus();
    initListetener();
});

var eb;

function initEventBus() {
    eb = new vertx.EventBus("/eventbus/");
    eb.onopen = function () {
        registerHandlers();
    };
}

function registerHandlers() {
    eb.registerHandler("find.user.client", function (data) {
        var status = jQuery.parseJSON(data).status;
        if(status === 1) {
            setUserInformation(jQuery.parseJSON(data))
        }else{
            alert("User not found");
        }
    });
    eb.registerHandler("delete.user.client", function (data) {
        var status = jQuery.parseJSON(data).status;
        if(status === 1){
            populateTable();
        }
    });
    eb.registerHandler("add.user.client", function (data) {
        var status = jQuery.parseJSON(data).status;
        if(status === 1){
            populateTable();
        }
    });
    eb.registerHandler("update.user.client", function (data) {
        var status = jQuery.parseJSON(data).status;
        if(status === 1){
            populateTable();
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
    $("#busAddUserButtonId").on('click', function () {
        var newUser = getUserData();
        eb.send("add.user.server", newUser);
    });
    $("#busSearchByButton").on('click', function () {
        eb.send("find.user.server", findUserData());
    });
    $(".busLinkDeleteUser").on('click', function () {
        eb.send("delete.user.server", $(this).attr('rel'));
    });
    $("#busUpdateUserButtonId").on('click', function () {
        var newUser = getUserData();
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
        '_id': $('#inputUserId').val(),
        'username': $('#inputUserName').val(),
        'email': $('#inputUserEmail').val(),
        'fullname': $('#inputUserFullname').val(),
        'age': $('#inputUserAge').val(),
        'location': $('#inputUserLocation').val(),
        'gender': $('#inputUserGender').val()
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
    var tableContent = '';
    $.getJSON('/users', function (data) {
        userListData = data;
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
    });
};

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