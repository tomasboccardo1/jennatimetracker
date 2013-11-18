package oauth

import org.scribe.model.Token


public interface ProviderFactoryMethod {
    public Provider makeProvider(String provider, Token token);
}