// Userlist data array for filling in info box
var userListData = [];

// DOM Ready =============================================================
$(document).ready(function () {
    populateTable();
    initListetener();
});

function initListetener() {
    $(".linkShowUser").on('click', function () {
        showUserInfo($(this));
    });
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

}

// Functions =============================================================

function searchBy(attributeName, value) {
    $.getJSON('/user/' + attributeName + "/" + value, function (data) {
        $('#inputUserName').text(data.fullname);
        $('#inputUserEmail').text(data.age);
        $('#inputUserFullname').text(data.gender);
        $('#inputUserAge').text(data.location);
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
    var thisUserName = element.attr('rel');
    // Get Index of object based on id value
    var arrayPosition = userListData.map(function (arrayItem) {
        return arrayItem.username;
    }).indexOf(thisUserName);
    // Get our User Object
    var thisUserObject = userListData[arrayPosition];
    //Populate Info Box
    $('#inputUserName').text(thisUserObject.fullname);
    $('#inputUserEmail').text(thisUserObject.age);
    $('#inputUserFullname').text(thisUserObject.gender);
    $('#inputUserAge').text(thisUserObject.location);

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
            'username': $('#inputUserName').val(),
            'email': $('#inputUserEmail').val(),
            'fullname': $('#inputUserFullname').val(),
            'age': $('#inputUserAge').val(),
            'location': $('#inputUserLocation').val(),
            'gender': $('#inputUserGender').val()
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