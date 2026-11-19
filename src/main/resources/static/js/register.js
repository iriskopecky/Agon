const form = document.getElementById("register-form");
const password = document.getElementById("register-password");
const confirmPassword = document.getElementById("register-confirm-password");
const username = document.getElementById("register-username");

function validatePasswordsMatch() {
    if (password.value !== confirmPassword.value) {
        confirmPassword.setCustomValidity("Passwords do not match.");
        return false;
    } else {
        confirmPassword.setCustomValidity("");
        return true;
    }
}

password.addEventListener("input", validatePasswordsMatch);
confirmPassword.addEventListener("input", validatePasswordsMatch);

form.addEventListener("submit", function (event) {
    event.preventDefault();

    if (validatePasswordsMatch()) {
        let usernameVal = username.value;
        let passwordVal = password.value;
        const csrfToken = document.querySelector('meta[name="_csrf"]').content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
         fetch("/register", {
             method: "POST",
             headers: {
                 "Content-Type": "application/json",
                 [csrfHeader]: csrfToken
             },
             body: JSON.stringify({
                 username: usernameVal,
                 password: passwordVal
             })
         }).then(response => {
             if (response.ok) {
                 response.json().then(data => {
                     window.location.href = data.redirect;
                 })
             }
         })
    }
})