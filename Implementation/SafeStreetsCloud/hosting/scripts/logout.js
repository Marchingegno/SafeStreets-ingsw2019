document.getElementById("logout").addEventListener("click", logout);


function logout() {
    firebase.auth().signOut().then(function () {
        localStorage.clear();
        window.location.href = "index.html"
    }).catch(function (error) {
        console.log("Error logging out");
    })
}