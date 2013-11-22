<head>
    <link href="${resource(dir: 'css', file: 'login.css')}" rel="stylesheet" type="text/css"/>
    <meta name='layout' content='main'/>
</head>

<body>

<div class="main login">
    <img src="${resource(dir: 'images', file: 'AIAlogo.png')}" id="logo"/>

    <div>
        <h1>Login</h1>

        <form method="post" id="loginForm" action="${postUrl}">
            <g:if test='${flash.message}'>
                <p id="validateTips">

                <div class='errors'>${flash.message}</div>
                </p>
            </g:if>
            <input type="text" id="j_username" name="j_username" value="" maxlength="50"/>
            <input type="password" id="j_password" name="j_password" value="" maxlength="50"/>

            <span><input type='checkbox' class='chk' name='_spring_security_remember_me' id='remember_me'
                         <g:if test='${hasCookie}'>checked='checked'</g:if>/>        Remember me</span>

            <input type="submit" value="Enter">
            <h6>Iniciar Sesión usando:</h6>

            <div class="social">
                <div >
                    <oauth:connect provider="facebook"
                                   class="registerbtn fbutton">
                        Facebook Login
                    </oauth:connect>
                    <oauth:connect
                            provider="google"
                            class="registerbtn gbutton">
                        Google Login
                    </oauth:connect>
                </div>
            </div>
            <a href="javascript: register();" id=registrarse>Soy nuevo, quiero registrarme!</a>
        </form>
    </div>
</div>


<script type='text/javascript'>
    function register() {
        window.location = "../register";
    }
</script>
</body>
