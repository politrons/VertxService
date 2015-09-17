// Userlist data array for filling in info box
var userListData = [];

// DOM Ready =============================================================
$(document).ready(function () {
    populateTable();
    initListetener();
});

function initListetener(){
    $(".linkShowUser").on('click',function(){
        showUserInfo($(this));
    });
    $(".linkDeleteUser").on('click',function(){
        deleteUser($(this));
    });
    $("#searchByNameButton").on('click',function(){
        searchBy($(this).attr("attributeName"), $("#inputSearchByName").val());
    });
    $("#inputSearchByEmail").on('click',function(){
        searchBy($("#inputSearchByEmail").val());
    });
    $("#inputSearchByAge").on('click',function(){
        searchBy($("#inputSearchByAge").val());
    });
}

// Functions =============================================================

function searchBy(attributeName, value){
    debugger;
    $.getJSON('/user/'+ attributeName + "/"+value, function (data) {
        userListData = data;
        $.each(data, function () {
            $('#userInfoName').text(data.fullname);
            $('#userInfoAge').text(data.age);
            $('#userInfoGender').text(data.gender);
            $('#userInfoLocation').text(data.location);
        });
    });
}


function populateTable() {
    var tableContent = '';
    $.getJSON('/users', function (data) {
        userListData = data;
        $.each(data, function () {
            tableContent += '<tr>';
            tableContent += '<td><a href="#" class="linkShowUser" rel="' + this.username + '" title="Show Details">' + this.username + '</a></td>';
            tableContent += '<td>' + this.email + '</td>';
            tableContent += '<td><a href="#" class="linkDeleteUser" rel="' + this._id + '">delete</a></td>';
            tableContent += '</tr>';
        });
        $('#userList table tbody').html(tableContent);
        initListetener();
    });
};

// Show User Info
function showUserInfo(element) {
    debugger;
    var thisUserName = element.attr('rel');
    // Get Index of object based on id value
    var arrayPosition = userListData.map(function (arrayItem) {
        return arrayItem.username;
    }).indexOf(thisUserName);
    // Get our User Object
    var thisUserObject = userListData[arrayPosition];
    //Populate Info Box
    $('#userInfoName').text(thisUserObject.fullname);
    $('#userInfoAge').text(thisUserObject.age);
    $('#userInfoGender').text(thisUserObject.gender);
    $('#userInfoLocation').text(thisUserObject.location);

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
        // If it is, compile all user info into one object
        var newUser = {
            'username': $('#addUser fieldset input#inputUserName').val(),
            'email': $('#addUser fieldset input#inputUserEmail').val(),
            'fullname': $('#addUser fieldset input#inputUserFullname').val(),
            'age': $('#addUser fieldset input#inputUserAge').val(),
            'location': $('#addUser fieldset input#inputUserLocation').val(),
            'gender': $('#addUser fieldset input#inputUserGender').val()
        };
        // Use AJAX to post the object to our adduser service
        $.ajax({
            type: 'POST',
            data: newUser,
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