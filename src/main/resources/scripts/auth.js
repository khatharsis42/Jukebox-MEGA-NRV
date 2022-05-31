
document.addEventListener('keypress', function (e) {
    if (e.key === 'Enter') {
        sendLogins("/login")
    }
});

function sendLogins(action) {
    username = document.getElementById("inputUser").cloneNode();
    password = document.getElementById("inputPassword").cloneNode();
    var form = document.createElement("form");
    form.setAttribute('method', "post");
    form.appendChild(username);
    form.appendChild(password);
    form.setAttribute('action', action);
    form.style.display = "none";
    document.body.appendChild(form);
    form.submit();
}

