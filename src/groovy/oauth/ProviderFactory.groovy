package oauth

import org.scribe.model.Token

class ProviderFactory implements ProviderFactoryMethod {

    def static google = "google"
    def static facebook = "facebook"

    @Override
    Provider makeProvider(String provider, Token token) {
        switch (provider) {
            case google:
                return new GoogleProvider(token);
                break
            case facebook:
                return new FacebookProvider(token);
                break
        }
    }
}
