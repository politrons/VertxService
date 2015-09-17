// Userlist data array for filling in info box
var userListData = [];

// DOM Ready =============================================================
$(document).ready(function () {
    populateTable();
    initListetener();
});

function initListetener(){
    $(".linkdeleteuser").on('click', deleteUser(event));
    $('#getUser').on('click', showUserInfo);
    $('#deleteUser').on('click', deleteUser);
}

// Functions =============================================================

// Fill table with data
function populateTable() {
    var tableContent = '';
    $.getJSON('/users', function (data) {
        userListData = data;
        $.each(data, function () {
            tableContent += '<tr>';
            tableContent += '<td><a href="#" class="linkshowuser" rel="' + this.username + '" title="Show Details">' + this.username + '</a></td>';
            tableContent += '<td>' + this.email + '</td>';
            tableContent += '<td><a href="#" class="linkdeleteuser" rel="' + this._id + '">delete</a></td>';
            tableContent += '</tr>';
        });
        $('#userList table tbody').html(tableContent);
        debugger;
        initListetener();
    });
};

// Show User Info
function showUserInfo(event) {
    // Prevent Link from Firing
    event.preventDefault();
    // Retrieve username from link rel attribute
    var thisUserName = $(this).attr('rel');
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
function addUser(event) {
    event.preventDefault();
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

// Delete User
function deleteUser(event) {
    debugger;
    event.preventDefault();

    // Pop up a confirmation dialog
    var confirmation = confirm('Are you sure you want to delete this user?');

    // Check and make sure the user confirmed
    if (confirmation === true) {

        // If they did, do our delete
        $.ajax({
            type: 'DELETE',
            url: '/users/' + $(this).attr('rel')
        }).done(function (response) {
            // Update the table
            populateTable();

        }).fail(function () {
            alert('Error: Something went wrong.');
            // Update the table
            populateTable();
        });
    } else {

        // If they said no to the confirm, do nothing
        return false;

    }

};