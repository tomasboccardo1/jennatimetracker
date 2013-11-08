<head>
    <meta name='layout' content='main'/>
    <title>Create or Link Account</title>
    <style type="text/css">
    fieldset {
        border: 1px solid green;
        padding: 1em;
        font: 100%/1 sans-serif;
    }

    fieldset legend {
        padding: 0.2em 0.5em;
        border: 1px solid green;
        color: green;
        font-weight: bold;
        font-size: 90%;
        text-align: left;
    }

    fieldset label select{
        float: left;
        width: 25%;
        margin-top: 5px;
        margin-right: 0.5em;
        padding-top: 0.2em;
        text-align: right;
        font-weight: bold;
    }

    label{
        margin-top: 15px;
    }

    fieldset input[type="submit"] {
        float: right;
        background: #F0F0F0;
        cursor: pointer;
    }

    fieldset br {
        margin-top: 50px;
    }
    </style>
</head>

<body>

<div class='body' style="padding: 15px;">
    <g:if test='${flash.error}'>
        <div class="errors">${flash.error}</div>
    </g:if>

    <h4><g:message code="springSecurity.oauth.registration.link.not.exists"
                   default="No user was found with this account."
                   args="[session.springSecurityOAuthToken.providerName]"/></h4>
    <br/>

    <g:hasErrors bean="${createAccountCommand}">
        <div class="errors">
            <g:renderErrors bean="${createAccountCommand}" as="list"/>
        </div>
    </g:hasErrors>

    <g:form action="createAccount" method="post" autocomplete="on">
        <fieldset>
            <legend><g:message code="springSecurity.oauth.registration.create.legend"
                               default="Create a new account"/></legend>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'account', 'error')} ">
                <label for='account'><g:message code="OAuthCreateAccountCommand.account.label"
                                                default="Account"/>:</label>
                <g:textField name='account' value='${createAccountCommand?.account}'/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'password1', 'error')} ">
                <label for='password1'><g:message code="OAuthCreateAccountCommand.password1.label"
                                                  default="Password"/>:</label>
                <g:passwordField name='password1' value='${createAccountCommand?.password1}'/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'password2', 'error')} ">
                <label for='password2'><g:message code="OAuthCreateAccountCommand.password2.label"
                                                  default="Password re-type"/>:</label>
                <g:passwordField name='password2' value='${createAccountCommand?.password2}'/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'name', 'error')} ">
                <label for='name'>Full Name:</label>
                <g:textField name="name" value="${createAccountCommand?.name}"/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'companyName', 'error')} ">
                <label for='name'>Company:</label>
                <g:textField name='name' value='${createAccountCommand?.companyName}'/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'chatTime', 'error')} ">
                <label for="chatTime">Chat time:</label>
                <g:select name="chatTime"
                          from="${TimeZoneUtil.getAvailablePromptTimes()}"
                          value="${'17:00'}"/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'timeZone', 'error')} ">
                <label for="timeZone">Time Zone:</label>
                <g:select id="timeZone"
                          name="timeZone"
                          from="${TimeZoneUtil.getAvailableTimeZones()}"
                          value="${request.locale.language}"/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: createAccountCommand, field: 'locale', 'error')} ">
                <label for="locale">Locale:</label>
                <g:select name="locale"
                          from="${LocaleUtil.getAvailableLocales()}"
                          value="${createAccountCommand?.locale ?: request?.locale?.language}"
                          optionValue="displayName"/>
            </div>


            <tr>
                <td valign="top" class="name" style="color:white">

                </td>
                <td valign="top" class="value ${hasErrors(bean: person, field: 'timeZone', 'errors')}">

                </td>
            </tr>

            <g:submitButton
                    name="${message(code: 'springSecurity.oauth.registration.create.button', default: 'Create')}"/>
        </fieldset>
    </g:form>

    <br/>

    <g:hasErrors bean="${linkAccountCommand}">
        <div class="errors">
            <g:renderErrors bean="${linkAccountCommand}" as="list"/>
        </div>
    </g:hasErrors>

    <g:form action="linkAccount" method="post" autocomplete="on">
        <fieldset>
            <legend><g:message code="springSecurity.oauth.registration.login.legend"
                               default="Link to an existing account"/></legend>

            <div class="fieldcontain ${hasErrors(bean: linkAccountCommand, field: 'account', 'error')} ">
                <label for='account'><g:message code="OAuthLinkAccountCommand.account.label"
                                                default="Account"/>:</label>
                <g:textField name='account' value='${linkAccountCommand?.account}'/>
            </div>

            <div class="fieldcontain ${hasErrors(bean: linkAccountCommand, field: 'password', 'error')} ">
                <label for='password'><g:message code="OAuthLinkAccountCommand.password.label"
                                                 default="Password"/>:</label>
                <g:passwordField name='password' value='${linkAccountCommand?.password}'/>
            </div>
            <g:submitButton
                    name="${message(code: 'springSecurity.oauth.registration.login.button', default: 'Login')}"/>
        </fieldset>
    </g:form>

    <br/>

    <g:link controller="login" action="auth"><g:message code="springSecurity.oauth.registration.back"
                                                        default="Back to login page"/></g:link>
</div>

</body>
