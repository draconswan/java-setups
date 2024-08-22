package com.example.dswan.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.connection.SslSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.StringReader;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
@Profile({"!local"})
@RequiredArgsConstructor
@Slf4j
public class MongoX509Configuration implements MongoClientSettingsBuilderCustomizer {

    private static final String MONGO_KEY_ENTRY_ALIAS = "mongo-client-key";
    @Value("${mongo.certificate:}")
    private String pemCertificate;
    @Value("${mongo.tls.version:TLSv1.3}")
    private String tlsVersion;
    @Value("${spring.data.mongodb.options.allowInvalidHost:false}")
    private boolean allowInvalidHost;

    /**
     * Customizes the {@link SslSettings} for use with X.509 Certificate Authentication.
     */
    @Override
    public void customize(MongoClientSettings.Builder clientSettingsBuilder) {
        SSLContext sslContext;
        try {
            sslContext = sslContext();
        } catch (Exception e) {
            log.error("Exception occurred during mongo client configuration: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to configure sslContext for mongo connection");
        }

        clientSettingsBuilder
                .applyToSslSettings(builder -> builder
                        .applySettings(SslSettings
                                               .builder()
                                               .invalidHostNameAllowed(allowInvalidHost)
                                               .context(sslContext)
                                               .enabled(true)
                                               .build()))
                .credential(MongoCredential.createMongoX509Credential());
    }

    /**
     * Creates an {@link SSLContext} that can connect to any endpoint exposing a
     * valid well known CA by the JRE. And uses a dynamic array of
     * {@link KeyManager} that contains the {@link X509Certificate} and
     * {@link PrivateKey} configured for use with a MongoDB instance.
     *
     * @return sslContext
     */
    public SSLContext sslContext()
            throws NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance(tlsVersion);
        sslContext.init(keyManagers(x509Certificate(), privateKey()), trustManagers(), null);
        return sslContext;
    }

    /**
     * Creates an array of {@link TrustManager} containing the default set of
     * trusted certificate authorities. This is required to make a TLS connection to
     * the MongoDB instance. If MongoDB is Atlas then the CA is Let's Encrypt and
     * should already be trusted so copy that over to the SSLContext that we are
     * creating.
     *
     * @return an array of {@link TrustManager} initialized with the default trust
     * managers.
     */
    private TrustManager[] trustManagers() throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory defaultTrustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // Using null here to init the trustManagerFactory with the default trust store.
        defaultTrustManagerFactory.init((KeyStore) null);

        // only need the default trust managers if the CA for mongo is already in the
        // default trust store for the JVM
        return defaultTrustManagerFactory.getTrustManagers();
    }

    /**
     * Creates an array of {@link KeyManager} containing the certificate and
     * privateKey provided in a in memory only {@link KeyStore} with alias
     * {@link #MONGO_KEY_ENTRY_ALIAS} used for x509 authentication. This is a dynamic
     * key manager containing the private key and certificate in the store for use
     * by the {@link SSLContext} that this class creates.
     *
     * @param certificate the x509 cert
     * @param privateKey  the related private key
     * @return an array of {@link KeyManager} initialized with the in memory
     * {@link KeyStore}.
     */
    private KeyManager[] keyManagers(X509Certificate certificate, PrivateKey privateKey)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null); // You don't need the KeyStore instance to come from a file.
        keyStore.setKeyEntry(MONGO_KEY_ENTRY_ALIAS, privateKey, "".toCharArray(), new Certificate[]{certificate});

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "".toCharArray());
        return keyManagerFactory.getKeyManagers();
    }

    /**
     * Parses the PEM Encoded <b> mongo.x509.private-key </b> property to a
     * {@link PrivateKey}.
     *
     * @return privateKey
     */
    private PrivateKey privateKey() throws IOException {
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        try (PEMParser parser = new PEMParser(new StringReader(getCertPortion("PRIVATE KEY")))) {
            return converter.getPrivateKey((PrivateKeyInfo) parser.readObject());
        }
    }

    /**
     * Parses the PEM-encoded certificate into an
     * {@link X509Certificate}.
     *
     * @return x509Certificate
     */
    private X509Certificate x509Certificate() throws IOException, CertificateException {
        JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
        try (PEMParser parser = new PEMParser(new StringReader(getCertPortion("CERTIFICATE")))) {
            return converter.getCertificate((X509CertificateHolder) parser.readObject());
        }
    }

    private String getCertPortion(String portion) {
        int beginIndex = pemCertificate.indexOf("-----BEGIN " + portion + "-----");
        int endIndex = pemCertificate.indexOf("-----END " + portion + "-----");
        int trailer = ("-----END " + portion + "-----").length();
        return pemCertificate.substring(beginIndex, endIndex + trailer);
    }
}
