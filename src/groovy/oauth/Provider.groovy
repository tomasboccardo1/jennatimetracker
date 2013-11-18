package oauth

import org.scribe.model.Token

abstract class Provider {

    def token

    public Provider(Token aToken) {
        token = aToken
    }

    public abstract String getEmail();
    public abstract String getName();
    public abstract String getLocale();

}
