function logout() {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    fetch("/logout", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            [csrfHeader]: csrfToken
        }
    }).then(response => {
        if (response.ok) {
            window.location.reload();
        }
    });
}