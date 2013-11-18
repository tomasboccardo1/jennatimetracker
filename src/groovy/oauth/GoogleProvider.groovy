package oauth

import grails.converters.JSON
import org.scribe.model.Token
import uk.co.desirableobjects.oauth.scribe.OauthResourceService
import uk.co.desirableobjects.oauth.scribe.OauthService


class GoogleProvider extends Provider {

    final String providerName = ProviderFactory.google
    def userInfoURL = "https://www.googleapis.com/oauth2/v1/userinfo"
    def oauthService
    def userInfo



    public GoogleProvider(Token token) {
        super(token)
        oauthService = new OauthService();
        oauthService.setOauthResourceService(new OauthResourceService());
        def response = oauthService.accessResource(providerName,
                token,
                "GET",
                userInfoURL);
        userInfo = JSON.parse(response?.getBody())
    }

    @Override
    String getName(){
        return  userInfo.getAt("name")
    }

    @Override
    String getEmail() {
        return userInfo.getAt("email");
    }

    @Override
    String getLocale() {
        return userInfo.getAt("locale")
    }

}
