(function () {
    const nameInput = document.getElementById('yhm');
    const passwordInput = document.getElementById('mm');
    const loginButton = document.getElementById('dl');
    if (/\.html$/i.test(location.pathname)&&nameInput&&passwordInput&&loginButton){
        nameInput.value='';
        passwordInput.value='';
        loginButton.click();
    }
})();