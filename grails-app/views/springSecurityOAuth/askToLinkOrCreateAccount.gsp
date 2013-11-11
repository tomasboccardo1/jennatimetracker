<head>
    <meta name='layout' content='main'/>
    <title>Create or Link Account</title>
    <style type="text/css">
    fieldset {
        border: 1px solid white;
        padding: 1em;
        font: 100%/1 sans-serif;
    }

    fieldset legend {
        padding: 0.2em 0.5em;
        border: 1px solid white;
        color: white;
        font-weight: bold;
        font-size: 14px;
        text-align: left;
    }

    fieldset label select {
        float: left;
        margin-top: 5px;
        margin-right: 0.5em;
        padding-top: 0.2em;
        text-align: right;
        font-weight: bold;
    }

    select {
        width: 50%;
    }

    label {
        margin-top: 15px;
        color: white;
        font-size: 12px;
        margin-bottom: 5px;
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

    <br/>

    <g:if test='${User.findByAccount(account)}'>
        <g:if test="${flash.message}">
            <h4 style="color: #ffffff"><g:message
                    code="${flash.message}"
                    default="A notification was sent to company owners."/>
            </h4>
        </g:if>
        <g:else>
            <h4 style="color: #ffffff"><g:message
                    code="springSecurity.oauth.registration.link.exists"
                    default="An user was found with this account."
                    args="[session.springSecurityOAuthToken.providerName]"/></h4>
        </g:else>
    </g:if>
    <g:else>

        <g:hasErrors bean="${createAccountCommand}">
            <div class="errors">
                <g:renderErrors bean="${createAccountCommand}" as="list"/>
            </div>
        </g:hasErrors>

        <h4 style="color: #ffffff"><g:message code="springSecurity.oauth.registration.link.not.exists"
                                              default="No user was found with this account."
                                              args="[session.springSecurityOAuthToken.providerName]"/></h4>

        <g:form action="createAccount" method="post" autocomplete="off">
            <fieldset>
                <legend><g:message code="springSecurity.oauth.registration.create.legend"
                                   default="Create a new account"/></legend>

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

                <div class="fieldcontain">
                    <label for='name'>Company:</label>
                    <g:select name="company"
                              from="${Company.findAll()}"/>
                </div>

                <div class="fieldcontain">
                    <label for="chatTime">Chat time:</label>
                    <g:select name="chatTime"
                              from="${TimeZoneUtil.getAvailablePromptTimes()}"
                              value="${'17:00'}"/>
                </div>

                <div class="fieldcontain">
                    <label for="timeZone">Time Zone:</label>
                    <g:select id="timeZone"
                              name="timeZone"
                              from="${TimeZoneUtil.getAvailableTimeZones()}"
                              value="${request.locale.language}"/>
                </div>

                <div class="fieldcontain">
                    <label for="locale">Locale:</label>
                    <g:select name="locale"
                              from="${LocaleUtil.getAvailableLocales()}"
                              value="${locale}"
                              optionValue="displayName"/>
                </div>


                <tr>
                    <td valign="top" class="name" style="color:white">

                    </td>
                    <td valign="top" class="value ${hasErrors(bean: person, field: 'timeZone', 'errors')}">

                    </td>
                </tr>

                <div class="buttons" style="margin-top: 15px">
                    <span class="formButton">
                        <g:submitButton
                                class="save"
                                name="${message(code: 'springSecurity.oauth.registration.create.button', default: 'Create')}"/>
                    </span>
                </div>
            </fieldset>
        </g:form>
        <br/>

        <g:hasErrors bean="${linkAccountCommand}">
            <div class="errors">
                <g:renderErrors bean="${linkAccountCommand}" as="list"/>
            </div>
        </g:hasErrors>

        <g:form action="linkAccount" method="post" autocomplete="off">
            <fieldset>
                <legend><g:message code="springSecurity.oauth.registration.login.legend"
                                   default="Link to an existing account"/></legend>

                <div class="fieldcontain ${hasErrors(bean: linkAccountCommand, field: 'account', 'error')} ">
                    <label for='account'><g:message code="OAuthLinkAccountCommand.account.label"
                                                    default="Account"/>:</label>
                    <g:textField name='account'/>
                </div>

                <div class="fieldcontain ${hasErrors(bean: linkAccountCommand, field: 'password', 'error')} ">
                    <label for='password'><g:message code="OAuthLinkAccountCommand.password.label"
                                                     default="Password"/>:</label>
                    <g:passwordField name='password' value='${linkAccountCommand?.password}'/>
                </div>


                <div class="buttons" style="margin-top: 15px">
                    <span class="formButton">
                        <g:submitButton
                                class="save"
                                name="${message(code: 'springSecurity.oauth.registration.login.button', default: 'Login')}"/>
                    </span>
                </div>

            </fieldset>
        </g:form>
    </g:else>

    <br/>

    <g:link controller="login" action="auth" style="font-size: 14px"><g:message
            code="springSecurity.oauth.registration.back"
            default="Back to login page"/></g:link>
</div>

</body>
