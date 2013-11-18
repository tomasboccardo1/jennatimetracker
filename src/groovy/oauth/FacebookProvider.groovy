package oauth

import org.scribe.model.Token
import org.springframework.social.facebook.api.Facebook
import org.springframework.social.facebook.api.FacebookProfile
import org.springframework.social.facebook.api.impl.FacebookTemplate

class FacebookProvider extends Provider{

    FacebookProfile fbProfile

    public FacebookProvider(Token token){
        super(token)
        Facebook facebook = new FacebookTemplate(token.getToken())
        fbProfile = facebook.userOperations().userProfile
    }

    @Override
    public String getEmail() {
        return fbProfile.email
    }

    @Override
    String getLocale() {
        return fbProfile.getLocale().getLanguage()
    }


    @Override
    String getName() {
        return fbProfile.getName()
    }

}
