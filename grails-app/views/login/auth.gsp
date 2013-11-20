<head>
    <meta name='layout' content='main'/>
    <style>
    .button {
        display: block;
        width: 115px;
        height: 25px;
        background: #289fcf;
        padding: 5px;
        margin: auto;
        position: relative;
        font-size: 12px;
        text-align: center;
        border-radius: 5px;
        font-weight: bold;
    }
    </style>
</head>

<body>
<div class='body'>
    <br><br><br>
    <img src="${resource(dir: 'images', file: 'login.png')}" alt="">

    <div
            style="overflow: hidden; display: block; z-index: 1002; outline-color: -moz-use-text-color; outline-style: none; outline-width: 0px; height: auto; width: 230px; float: right;"
            class="ui-dialog ui-widget ui-widget-content ui-corner-all" tabindex="-1" role="dialog"
            aria-labelledby="ui-dialog-title-dialog">
        <form method="post" id="loginForm" action="${postUrl}">
            <div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix" unselectable="on"
                 style="-moz-user-select: none;">
                <span class="ui-dialog-title" id="ui-dialog-title-dialog" unselectable="on"
                      style="-moz-user-select: none;">Login</span>
            </div>

            <div id="dialog" class="ui-dialog-content ui-widget-content"
                 style="height: auto; min-height: 64px; width: auto;">
                <g:if test='${flash.message}'>
                    <p id="validateTips">

                    <div class='errors'>${flash.message}</div>
                    </p>
                </g:if>
                <fieldset>
                    <label for="j_username">Username:</label>
                    <input type="text" id="j_username" name="j_username" value="" maxlength="50" style="width: 200px;"/>
                    <label for="j_password">Password:</label>
                    <input type="password" id="j_password" name="j_password" value="" maxlength="50"
                           style="display: block; margin-bottom: 20px; width: 200px"/>

                    <div class="rememberme">
                        <input type='checkbox' class='chk' name='_spring_security_remember_me' id='remember_me'
                               <g:if test='${hasCookie}'>checked='checked'</g:if>/>
                        <label for='remember_me' style="display: inline; margin-bottom: 20px">Remember me</label>
                    </div>
                </fieldset>
            </div>

            <div class="ui-dialog-buttonpane ui-widget-content ui-helper-clearfix">
                <input type="submit" class="ui-state-default ui-corner-all" value="Ok">
                <input type="button" class="ui-state-default ui-corner-all" value="Register!" onclick="register();">
            </div>
        </form>

    </div>
    <div style="margin-top: 20px; float: left">
        <oauth:connect
                class="button"
                style="color: white; background-color: red"
                provider="google">Google Login</oauth:connect>
    </br>
        <oauth:connect provider="facebook"
                       class="button"
                       style="color: white"
                       id="facebook-connect-link">Facebook Login</oauth:connect>
    </div>


</div>
<script type='text/javascript'>
    function register() {
        window.location = "../register";
    }
</script>
</body>
