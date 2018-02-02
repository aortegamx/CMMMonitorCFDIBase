package mx.bigdata.sat.security.factory;

import mx.bigdata.sat.security.KeyLoader;
import mx.bigdata.sat.security.KeyLoaderEnumeration;
import mx.bigdata.sat.security.PrivateKeyLoader;
import mx.bigdata.sat.security.PublicKeyLoader;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created with IntelliJ IDEA.
 * User: Gerardo Aquino
 * Date: 4/06/13
 */
public class KeyLoaderFactory {


    public final static KeyLoader createInstance(KeyLoaderEnumeration keyLoaderEnumeration, String keyLocation, String ... keyPassword) {
        KeyLoader keyLoader = null;

        if(keyLoaderEnumeration == KeyLoaderEnumeration.PRIVATE_KEY_LOADER) {
            keyLoader = new PrivateKeyLoader(keyLocation, keyPassword == null ? null : keyPassword[0]);
        } else if (keyLoaderEnumeration == KeyLoaderEnumeration.PUBLIC_KEY_LOADER){
            keyLoader = new PublicKeyLoader(keyLocation);
        }

        return keyLoader;
    }


    public final static KeyLoader createInstance(KeyLoaderEnumeration keyLoaderEnumeration, InputStream keyInputStream, String ... keyPassword) {
        KeyLoader keyLoader = null;

        if(keyLoaderEnumeration == KeyLoaderEnumeration.PRIVATE_KEY_LOADER) {
            keyLoader = new PrivateKeyLoader(keyInputStream, keyPassword == null ? null : keyPassword[0]);
        } else if (keyLoaderEnumeration == KeyLoaderEnumeration.PUBLIC_KEY_LOADER){
            keyLoader = new PublicKeyLoader(keyInputStream);
        }

        return keyLoader;
    }
    
    public static X509Certificate loadX509Certificate(InputStream in) 
        throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) factory.generateCertificate(in);
      }
}
